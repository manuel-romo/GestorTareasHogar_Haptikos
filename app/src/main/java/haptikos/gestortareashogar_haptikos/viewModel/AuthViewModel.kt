package haptikos.gestortareashogar_haptikos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val dataStore: DataStoreManager) : ViewModel() {

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

    fun login(user: String, pass: String) {

        isLoading = true
        errorMessage = null

        if (user == "admin" && pass == "1234") {
            isLoading = false
            isSuccess = true
        } else {
            isLoading = false
            errorMessage = "Credenciales inválidas"
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