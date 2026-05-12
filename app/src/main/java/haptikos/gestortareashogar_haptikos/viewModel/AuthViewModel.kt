package haptikos.gestortareashogar_haptikos.viewModel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.AuthRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.onSuccess

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isLoggedIn = dataStore.isLoggedInFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
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

        // Validaciones básicas
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Por favor, llena los campos obligatorios"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Por favor, ingresa un correo válido"
            return
        }

        // Fecha de nacimiento
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = sdf.parse(dob)

            if (parsedDate != null) {
                val today = Calendar.getInstance()
                val birthDate = Calendar.getInstance()
                birthDate.time = parsedDate

                var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)

                if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }

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
                val confirmedId = response.id ?: userId

                if (!token.isNullOrEmpty()) {
                    dataStore.saveSession(
                        userId = userId,
                        username = name,
                        token = token,
                        email = email
                    )
                    syncRepository.syncAll(userId)
                    _isSuccess.value = true

                    val fcmToken = dataStore.fcmTokenFlow.first()
                    if (!fcmToken.isNullOrEmpty()) {
                        try {
                            RetrofitClient.getUserApi(dataStore).updateFcmToken(
                                userId,
                                mapOf("fcmToken" to fcmToken)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                } else {
                    _errorMessage.value = "Registro exitoso, pero no se recibió token de acceso"
                }
            }.onFailure { error ->
                _errorMessage.value = "Error al crear la cuenta o de conexión"
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

                dataStore.saveSession(
                    userId = userId,
                    username = username,
                    token = token,
                    email = userEmail
                )

                syncRepository.syncAll(userId)

                _isSuccess.value = true

                val fcmToken = dataStore.fcmTokenFlow.first()
                if (!fcmToken.isNullOrEmpty()) {
                    try {
                        RetrofitClient.getUserApi(dataStore).updateFcmToken(
                            userId,
                            mapOf("fcmToken" to fcmToken)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }.onFailure { error ->
                _errorMessage.value = "Correo o contraseña incorrectos o error de red"
                error.printStackTrace()
            }

            _isLoading.value = false
        }
    }

    fun resetError() {
        _errorMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.logout()
        }
    }

    fun loginWithBiometrics() {
        if (isLoggedIn.value) {
            viewModelScope.launch {
                val userId = dataStore.userIdFlow.first()
                if (userId.isNotEmpty()) {
                    syncRepository.syncAll(userId)
                }
            }
            _errorMessage.value = null
            _isSuccess.value = true
        } else {
            _errorMessage.value = "Inicia sesión con correo y contraseña la primera vez."
            _isSuccess.value = false
        }
    }

    fun showBiometricError(error: String) {
        _errorMessage.value = error
    }

}