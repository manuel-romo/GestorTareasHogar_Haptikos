package haptikos.gestortareashogar_haptikos.viewModel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AuthRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isLoggedIn = dataStore.isLoggedInFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val userName = dataStore.usernameFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
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
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
            try {
                val response = authRepository.signUp(name, gender, dob, email, pass)

                if (response.isSuccessful) {
                    dataStore.saveSession(name)
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = "Error al crear la cuenta"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Login
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Llena todos los campos"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Por favor, ingresa un correo válido"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Llamada al servidor
                val response = authRepository.login(email, pass)

                if (response.isSuccessful) {
                    // Datos recibidos
                    val loginResponse = response.body()

                    val username = loginResponse?.name ?: email.substringBefore("@")

                    dataStore.saveSession(username)
                    _isSuccess.value = true
                } else {
                    // Si la respuesta es 401, o no autorizado
                    _errorMessage.value = "Correo o contraseña incorrectos"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión al servidor"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
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
        _errorMessage.value = null
        _isSuccess.value = true
    }

    fun showBiometricError(error: String) {
        _errorMessage.value = error
    }
}