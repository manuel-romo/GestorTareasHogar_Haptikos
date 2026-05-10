package haptikos.gestortareashogar_haptikos.viewModel

import androidx.compose.remote.creation.first
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.helpers.UserSuggestion
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.network.HomeApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.InvitedUser
import haptikos.gestortareashogar_haptikos.utils.generateUniqueId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    // Modelo para la vista previa del hogar
    data class HomePreviewInfo(
        val id: String,
        val name: String,
        val creatorName: String,
        val memberCount: Int,
        val taskCount: Int,
        val pendingCount: Int
    )

    // Estados de pantalla de unión a un hogar
    sealed class JoinHomeState {
        object Input : JoinHomeState()
        object Searching : JoinHomeState()
        data class Error(val message: String) : JoinHomeState()
        data class Found(val home: HomePreviewInfo) : JoinHomeState()
        object Joining : JoinHomeState()
        data class Success(val homeName: String, val totalMembers: Int) : JoinHomeState()
    }

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

    fun createNewHome(
        name: String,
        description: String,
        isPrivate: Boolean,
        userName: String,
        userLastName: String,
        userColor: String,
        invitedUsers: List<InvitedUser> = emptyList(),
        onComplete: (String?) -> Unit
    ) {
        val finalDescription = description.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            try {
                // Generación de ID local
                val generatedHomeId = generateUniqueId()

                val userId = dataStore.userIdFlow.first()

                if (userId == null) {
                    onComplete(null)
                }

                val invitedUsersWithIds = invitedUsers.map { user ->
                    HomeApi.InvitedUserDto(
                        id = generateUniqueId(),
                        title = user.title,
                        subtitle = user.subtitle
                    )
                }

                val inviteCode = repository.createHomeWithSync(
                    homeId = generatedHomeId,
                    creatorId = userId,
                    homeName = name,
                    homeDescription = finalDescription,
                    isPrivate = isPrivate,
                    creatorName = userName,
                    creatorLastName = userLastName,
                    creatorColorHex = userColor,
                    invitedUsers = invitedUsersWithIds,
                    defaultInviteColor = "#9E9E9E"
                )

                // Se envía el código obtenido o null si no se generó
                onComplete(inviteCode)

            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
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

    // Flujo para unirse a un hogar -------------------------------------------------------------- -

    private val _joinState = MutableStateFlow<JoinHomeState>(JoinHomeState.Input)
    val joinState = _joinState.asStateFlow()

    private val _joinCode = MutableStateFlow("")
    val joinCode = _joinCode.asStateFlow()

    fun updateJoinCode(code: String) {
        // Limitar a 9 caracteres alfanuméricos
        val cleanCode = code.filter { it.isLetterOrDigit() }.take(9).uppercase()
        _joinCode.value = cleanCode

        // Si el usuario empieza a escribir se quita el error
        if (_joinState.value is JoinHomeState.Error) {
            _joinState.value = JoinHomeState.Input
        }
    }

    fun searchHomeByCode() {
        val currentCode = _joinCode.value
        if (currentCode.length < 8) return

        viewModelScope.launch {
            _joinState.value = JoinHomeState.Searching

            // TODO: Cambiar por llamada real a Repositorio
            delay(1500)

            // Simulación de lógica de búsqueda
            if (currentCode == "APART5W6" || currentCode.startsWith("APART")) {
                _joinState.value = JoinHomeState.Found(
                    HomePreviewInfo(
                        id = "123",
                        name = "Apartamento Playa",
                        creatorName = "Carlos Ruiz",
                        memberCount = 3,
                        taskCount = 18,
                        pendingCount = 5
                    )
                )
            } else {
                _joinState.value = JoinHomeState.Error("No encontramos ningún hogar con ese código. Verifica con el creador del hogar.")
            }
        }
    }

    fun joinFoundHome() {
        val currentState = _joinState.value
        if (currentState !is JoinHomeState.Found) return

        viewModelScope.launch {
            _joinState.value = JoinHomeState.Joining

            // TODO: Cambiar por llamada real a tu API/Repositorio para unirse
            delay(2000) // Simulación de red

            _joinState.value = JoinHomeState.Success(
                homeName = currentState.home.name,
                totalMembers = currentState.home.memberCount + 1
            )
        }
    }

    fun resetJoinFlow() {
        _joinCode.value = ""
        _joinState.value = JoinHomeState.Input
    }
}