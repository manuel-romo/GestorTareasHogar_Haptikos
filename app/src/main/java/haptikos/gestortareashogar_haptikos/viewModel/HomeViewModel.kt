package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.helpers.UserSuggestion
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.network.HomeApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.InvitedUser
import haptikos.gestortareashogar_haptikos.utils.NetworkConnectivityObserver
import haptikos.gestortareashogar_haptikos.utils.generateUniqueId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

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
        val pendingCount: Int,
        val isAlreadyMember: Boolean = false
    )

    sealed class JoinHomeState {
        object Input : JoinHomeState()
        object Searching : JoinHomeState()
        data class Error(val message: String) : JoinHomeState()
        data class Found(
            val home: HomePreviewInfo,
            val isAlreadyMember: Boolean = false
        ) : JoinHomeState()
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


    // Rol del usuario en el hogar actual
    val isCurrentUserCreatorOrAdmin: StateFlow<Boolean> = combine(
        selectedHome,
        repository.allMembersNew,
        dataStore.userIdFlow
    ) { currentHome, members, userId ->
        if (currentHome == null || userId.isEmpty()) return@combine false

        val currentUserMember = members.find {
            it.homeId == currentHome.id && it.userId == userId
        }

        currentUserMember?.role == MemberRole.CREATOR || currentUserMember?.role == MemberRole.ADMIN
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

                if (userId.isEmpty()) {
                    onComplete(null)
                    return@launch
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
        // Se actualiza la IU antes de guardar en servidor
        _selectedHome.value = home

        viewModelScope.launch {
            try {
                repository.updateHome(home)
            } catch (e: Exception) {
                // TODO puede revertirse el cambio de la IU
                e.printStackTrace()
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

    fun regenerateInviteCode(onResult: (String?) -> Unit) {
        val homeId = _selectedHome.value?.id ?: return
        viewModelScope.launch {
            val newCode = repository.regenerateInviteCode(homeId)
            if (newCode != null) {
                _selectedHome.value = _selectedHome.value?.copy(inviteCode = newCode)
            }
            onResult(newCode)
        }
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

                repository.deleteHomeWithSync(homeToDelete)

                _selectedHome.value = null

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
            val formattedCode = "${currentCode.take(4)}-${currentCode.takeLast(4)}"
            val preview = repository.findHomeByCode(formattedCode)

            if (preview == null) {
                _joinState.value = JoinHomeState.Error("No encontramos ningún hogar con ese código. Verifica con el creador del hogar.")
                return@launch
            }

            _joinState.value = JoinHomeState.Found(preview, preview.isAlreadyMember)
        }
    }

    fun joinFoundHome() {
        val currentState = _joinState.value
        if (currentState !is JoinHomeState.Found) return

        viewModelScope.launch {
            val userId = dataStore.userIdFlow.first()
            if (userId.isEmpty()) {
                _joinState.value = JoinHomeState.Error("Sesión no válida")
                return@launch
            }

            val yaEsMiembro = repository.allMembersNew.first().any {
                it.homeId == currentState.home.id && it.userId == userId
            }
            if (yaEsMiembro) {
                _joinState.value = JoinHomeState.Error("Ya eres miembro de este hogar.")
                return@launch
            }

            _joinState.value = JoinHomeState.Joining

            val userName = dataStore.usernameFlow.first()
            val memberId = UUID.randomUUID().toString()
            val formattedCode = "${_joinCode.value.take(4)}-${_joinCode.value.takeLast(4)}"

            val success = repository.joinHome(
                inviteCode = formattedCode,
                memberId = memberId,
                homeId = currentState.home.id,
                userId = userId,
                name = userName,
                colorHex = "#9E9E9E"
            )

            _joinState.value = if (success) {
                JoinHomeState.Success(
                    homeName = currentState.home.name,
                    totalMembers = currentState.home.memberCount + 1
                )
            } else {
                JoinHomeState.Error("No se pudo unir al hogar. Intenta de nuevo.")
            }
        }
    }

    fun resetJoinFlow() {
        _joinCode.value = ""
        _joinState.value = JoinHomeState.Input
    }

    init {
        loadSuggestedUsers()
        viewModelScope.launch {
            allHomes
                .filter { it.isNotEmpty() }
                .first()
                .let { homes ->
                    if (_selectedHome.value == null) {
                        _selectedHome.value = homes.first()
                    }
                }
        }
    }

}