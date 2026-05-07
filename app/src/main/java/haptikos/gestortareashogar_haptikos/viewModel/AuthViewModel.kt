package haptikos.gestortareashogar_haptikos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AuthRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

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

    // Consulta a Firebase
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Llena todos los campos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // Llamada a Firebase
            val success = authRepository.login(email, pass)

            if (success) {
                // Si la autenticación es válida, se guarda en datastore.
                val username = email.substringBefore("@")
                dataStore.saveSession(username)

                isSuccess = true
            } else {
                errorMessage = "Correo o contraseña incorrectos"
            }
            isLoading = false
        }
    }

    fun resetError() {
        errorMessage = null
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.logout()
        }
    }

    fun loginWithBiometrics() {
        errorMessage = null
        isSuccess = true
    }

    fun showBiometricError(error: String) {
        errorMessage = error
    }
}