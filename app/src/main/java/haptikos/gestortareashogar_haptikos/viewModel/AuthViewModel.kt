package haptikos.gestortareashogar_haptikos.viewModel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.AuthRepository
import haptikos.gestortareashogar_haptikos.data.BiometricCredentialManager
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.utils.FcmUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.onSuccess
import retrofit2.HttpException

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val dataStore: DataStoreManager,
    private val biometricCredentialManager: BiometricCredentialManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // NUEVO: para saber si mostrar el botón de huella en la UI
    val hasBiometricCredentials: StateFlow<Boolean> = MutableStateFlow(
        biometricCredentialManager.hasCredentials()
    ).asStateFlow()

    // NUEVO: estados para recuperación de contraseña
    private val _resetEmailSent = MutableStateFlow(false)
    val resetEmailSent: StateFlow<Boolean> = _resetEmailSent.asStateFlow()

    private val _passwordResetDone = MutableStateFlow(false)
    val passwordResetDone: StateFlow<Boolean> = _passwordResetDone.asStateFlow()

    val isLoggedIn = dataStore.isLoggedInFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userName = dataStore.usernameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val userId = dataStore.userIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val userEmail = dataStore.userEmailFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    // Registro
    fun signUp(name: String, gender: UserGender, dob: String, email: String, pass: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Por favor, llena los campos obligatorios"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Por favor, ingresa un correo válido"
            return
        }

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = sdf.parse(dob)

            if (parsedDate != null) {
                val today = Calendar.getInstance()
                val birthDate = Calendar.getInstance()
                birthDate.time = parsedDate

                var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) age--

                if (age < 13) {
                    _errorMessage.value = "Debes tener al menos 13 años para registrarte"
                    return
                }
                if (age > 120) {
                    _errorMessage.value = "Por favor, ingresa una fecha válida"
                    return
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Formato de fecha inválido"
            return
        }

        if (pass.length < 8) {
            _errorMessage.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }

        if (pass != confirmPass) {
            _errorMessage.value = "Las contraseñas no coinciden"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val userId = java.util.UUID.randomUUID().toString()
            val result = authRepository.signUp(userId, name, gender, dob, email, pass)

            result.onSuccess { response ->
                val token = response.token
                if (!token.isNullOrEmpty()) {
                    dataStore.saveSession(userId = userId, username = name, token = token, email = email)
                    // Al registrarse también guardamos credenciales para huella futura
                    biometricCredentialManager.saveCredentials(email, pass)
                    syncRepository.syncAll(userId)
                    _isSuccess.value = true

                    val fcmToken = FcmUtils.getToken()
                    if (!fcmToken.isNullOrEmpty()) {
                        try {
                            dataStore.saveFcmToken(fcmToken)
                            RetrofitClient.getUserApi(dataStore).updateFcmToken(userId, mapOf("fcmToken" to fcmToken))
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                } else {
                    _errorMessage.value = "Registro exitoso, pero no se recibió token de acceso"
                }
            }.onFailure { error ->
                _errorMessage.value = parseThrowableError(error, isSignUp = true)
                error.printStackTrace()
            }

            _isLoading.value = false
        }
    }

    // Login
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Llena todos los campos"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Por favor, ingresa un correo válido"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.login(email, pass)

            result.onSuccess { loginResponse ->
                val userId = loginResponse.id ?: ""
                val username = loginResponse.name ?: email.substringBefore("@")
                val token = loginResponse.token ?: ""
                val userEmail = loginResponse.email ?: ""

                dataStore.saveSession(userId = userId, username = username, token = token, email = userEmail)
                // Guardar credenciales cifradas para huella después de login exitoso
                biometricCredentialManager.saveCredentials(email, pass)
                syncRepository.syncAll(userId)
                _isSuccess.value = true

                val fcmToken = FcmUtils.getToken()
                if (!fcmToken.isNullOrEmpty()) {
                    try {
                        dataStore.saveFcmToken(fcmToken)
                        RetrofitClient.getUserApi(dataStore).updateFcmToken(userId, mapOf("fcmToken" to fcmToken))
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }.onFailure { error ->
                _errorMessage.value = parseThrowableError(error, isSignUp = false)
                error.printStackTrace()
            }

            _isLoading.value = false
        }
    }

    // Login con huella — ahora hace login real con credenciales guardadas
    fun loginWithBiometrics() {
        val credentials = biometricCredentialManager.getCredentials()
        if (credentials != null) {
            login(credentials.first, credentials.second)
        } else {
            _errorMessage.value = "Inicia sesión con correo y contraseña la primera vez."
        }
    }

    fun showBiometricError(error: String) {
        _errorMessage.value = error
    }

    fun resetError() {
        _errorMessage.value = null
    }

    // Logout — NO toca las credenciales biométricas
    fun logout() {
        viewModelScope.launch {
            dataStore.logout() // solo borra token/sesión, huella sigue guardada
        }
    }

    // ── Recuperación de contraseña ──────────────────────────────────────────

    fun sendPasswordResetCode(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Ingresa un correo válido"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.forgotPassword(email)

            result.onSuccess {
                _resetEmailSent.value = true
            }.onFailure { error ->
                // Aunque falle, mostramos mensaje genérico por seguridad
                _resetEmailSent.value = true
            }

            _isLoading.value = false
        }
    }

    fun confirmPasswordReset(token: String, newPassword: String) {
        if (token.isBlank()) {
            _errorMessage.value = "Ingresa el código recibido"
            return
        }
        if (newPassword.length < 8) {
            _errorMessage.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.resetPassword(token, newPassword)

            result.onSuccess {
                _passwordResetDone.value = true
                // Si tenía credenciales biométricas guardadas, limpiarlas
                // porque la contraseña cambió
                biometricCredentialManager.clearCredentials()
            }.onFailure { error ->
                _errorMessage.value = when {
                    error.message?.contains("inválido") == true -> "Código incorrecto"
                    error.message?.contains("expirado") == true -> "El código expiró, solicita uno nuevo"
                    else -> "Error al restablecer la contraseña"
                }
            }

            _isLoading.value = false
        }
    }

    fun resetPasswordState() {
        _resetEmailSent.value = false
        _passwordResetDone.value = false
        _errorMessage.value = null
    }

    private fun parseThrowableError(error: Throwable, isSignUp: Boolean = false): String {
        return when (error) {
            is IOException -> "No se ha podido conectar al servidor"
            is HttpException -> {
                when (error.code()) {
                    401 -> "Correo o contraseña incorrectos"
                    404 -> if (isSignUp) "Ruta no encontrada" else "Esta cuenta no está registrada"
                    409 -> "Este correo ya está registrado con otra cuenta"
                    400 -> "Los datos enviados son incorrectos"
                    500 -> "El servidor está experimentando problemas. Intenta más tarde"
                    else -> "Error en el servidor (${error.code()})"
                }
            }
            else -> error.localizedMessage ?: "Ocurrió un error inesperado. Inténtalo de nuevo"
        }
    }
}