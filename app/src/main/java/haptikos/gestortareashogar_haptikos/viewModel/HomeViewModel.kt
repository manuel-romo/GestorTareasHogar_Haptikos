package haptikos.gestortareashogar_haptikos.viewModel

import androidx.compose.remote.creation.first
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    // Lista de todos los hogares del usuario
    val allHomes: StateFlow<List<HomeEntityNew>> = repository.allHomes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Hogar seleccionado
    private val _selectedHome = MutableStateFlow<HomeEntityNew?>(null)
    val selectedHome = _selectedHome.asStateFlow()

    fun selectHome(home: HomeEntityNew) {
        _selectedHome.value = home
    }

    // Creación de hogar
    fun createNewHome(
        name: String,
        userName: String,
        userLastName: String,
        userColor: String
    ) {
        viewModelScope.launch {
            try {
                repository.createHomeAndCreator(
                    homeName = name,
                    creatorName = userName,
                    creatorLastName = userLastName,
                    creatorColorHex = userColor
                )
                // TODO mensaje de confirmación
            } catch (e: Exception) {
                // TODO manejar error
            }
        }
    }

    // Actualización
    fun updateHome(home: HomeEntityNew) {
        viewModelScope.launch {
            try {
                repository.updateHome(home)
                _selectedHome.value = home
            } catch (e: Exception) {
                // TODO Manejar error
            }
        }
    }

    fun updateNotificationSettings(
        reminders: Boolean? = null,
        completed: Boolean? = null,
        members: Boolean? = null,
        all: Boolean? = null,
        force: Boolean? = null
    ) {
        val currentHome = _selectedHome.value ?: return

        // Copia del hogar actual con nuevos valores
        val updatedHome = currentHome.copy(
            notifyTaskReminders = reminders ?: currentHome.notifyTaskReminders,
            notifyTaskCompleted = completed ?: currentHome.notifyTaskCompleted,
            notifyNewMembers = members ?: currentHome.notifyNewMembers,
            notifyAllMembers = all ?: currentHome.notifyAllMembers,
            forceSettings = force ?: currentHome.forceSettings
        )

        updateHome(updatedHome)
    }

    // Estados para controlar la eliminación del hogar
    private val _isDeletingHome = MutableStateFlow(false)
    val isDeletingHome = _isDeletingHome.asStateFlow()

    private val _showSuccessFeedback = MutableStateFlow(false)
    val showSuccessFeedback = _showSuccessFeedback.asStateFlow()

    private val _biometricError = MutableStateFlow<String?>(null)
    val biometricError = _biometricError.asStateFlow()

    // Funciones de control
    fun initiateHomeDeletion() { _isDeletingHome.value = true }
    fun cancelDeletion() { _isDeletingHome.value = false }

    fun confirmDeletion() {
        val homeToDelete = _selectedHome.value ?: return
        viewModelScope.launch {
            try {
                repository.deleteHome(homeToDelete)
                _selectedHome.value = null

                // Se selecciona el siguiente hogar existente
                val otherHomes = repository.allHomes.first()
                _selectedHome.value = otherHomes.firstOrNull()

                _isDeletingHome.value = false
                _showSuccessFeedback.value = true
            } catch (e: Exception) {
                _biometricError.value = "Error al eliminar"
            }
        }
    }

    fun dismissSuccessFeedback() { _showSuccessFeedback.value = false }
    fun dismissBiometricError() { _biometricError.value = null }
    fun showBiometricError(msg: String) { _biometricError.value = msg }
}