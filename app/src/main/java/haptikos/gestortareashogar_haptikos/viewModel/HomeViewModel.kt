package haptikos.gestortareashogar_haptikos.viewModel

import androidx.compose.remote.creation.first
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.helpers.UserSuggestion
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.InvitedUser
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
        description: String,
        isPrivate: Boolean,
        userName: String,
        userLastName: String,
        userColor: String,
        invitedUsers: List<InvitedUser> = emptyList()
    ) {
        val finalDescription = description.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            try {
                // Creación de hogar
                repository.createHomeAndCreator(
                    homeName = name,
                    homeDescription = finalDescription,
                    isPrivate = isPrivate,
                    creatorName = userName,
                    creatorLastName = userLastName,
                    creatorColorHex = userColor
                )

                // TODO envio de invitaciones
                if (invitedUsers.isNotEmpty()) {
                    // repository.sendInvitations(invitedUsers.map { it.id })
                }

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


    // Sugerencia y Búsqueda de usuarios

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _suggestedUsers = MutableStateFlow<List<UserSuggestion>>(emptyList())
    val suggestedUsers = _suggestedUsers.asStateFlow()

    init {
        loadSuggestedUsers()
    }

    private var allSuggestedUsers: List<UserSuggestion> = emptyList()

    private fun loadSuggestedUsers() {
        viewModelScope.launch {
            try {
                // Simulamos carga de datos
                val users = listOf(
                    UserSuggestion("1", "Juan Pérez", "@juan.perez", "#2962FF"),
                    UserSuggestion("2", "Ana Gómez", "@ana.gomez", "#AA00FF"),
                    UserSuggestion("3", "Pedro Ramírez", "@pedro.r", "#00C853"),
                    UserSuggestion("4", "Sofía Torres", "@sofi.torres", "#E91E63")
                )
                allSuggestedUsers = users
                _suggestedUsers.value = users
            } catch (e: Exception) { }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _suggestedUsers.value = allSuggestedUsers
            return
        }

        // Búsqueda lógica
        viewModelScope.launch {
            val filteredList = allSuggestedUsers.filter {
                it.fullName.contains(query, ignoreCase = true) ||
                        it.username.contains(query, ignoreCase = true)
            }
            _suggestedUsers.value = filteredList
        }
    }

}