package haptikos.gestortareashogar_haptikos.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.helpers.UserSuggestion
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberStatus
import haptikos.gestortareashogar_haptikos.network.HomeApi
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.InvitedUser
import haptikos.gestortareashogar_haptikos.utils.generateUniqueId
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
    val selectedHome: StateFlow<HomeEntityNew?> = combine(
        _selectedHome,
        allHomes
    ) { selected, homes ->
        if (selected != null) {
            homes.find { it.id == selected.id } ?: selected
        } else {
            homes.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
                val generatedHomeId = generateUniqueId()
                val userId = dataStore.userIdFlow.first()

                if (userId.isEmpty()) {
                    onComplete(null)
                    return@launch
                }

                val tempHome = HomeEntityNew(
                    id = generatedHomeId,
                    name = name,
                    description = finalDescription,
                    isPrivate = isPrivate,
                    inviteCode = null,
                    isSynced = false
                )

                _selectedHome.value = tempHome

                val invitedUsersWithIds = invitedUsers.map { user ->
                    HomeApi.InvitedUserDto(
                        id = generateUniqueId(),
                        title = user.title,
                        subtitle = user.subtitle,
                        userId = user.userId
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

                if (inviteCode != null) {
                    _selectedHome.value = tempHome.copy(inviteCode = inviteCode, isSynced = true)

                    // Se une automaticamente a los usuarios invitados de la lista
                    invitedUsers
                        .filter { !it.userId.isNullOrEmpty() }
                        .forEach { user ->
                            try {
                                repository.joinHome(
                                    inviteCode = inviteCode,
                                    memberId = generateUniqueId(),
                                    homeId = generatedHomeId,
                                    userId = user.userId!!,
                                    name = user.title,
                                    colorHex = "#9E9E9E"
                                )
                            } catch (e: Exception) {
                                Log.e("HOME", "Error uniendo a ${user.title}: ${e.message}")
                            }
                        }
                }

                onComplete(null)

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

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError = _actionError.asStateFlow()

    // Funciones de control
    fun initiateHomeDeletion() { _isDeletingHome.value = true }
    fun cancelDeletion() { _isDeletingHome.value = false }

    fun confirmDeletion() {
        val homeToDelete = _selectedHome.value ?: return

        viewModelScope.launch {
            _isDeletingHome.value = false

            try {
                val success = repository.deleteHomeWithSync(homeToDelete)

                if (success) {
                    _showSuccessFeedback.value = true
                    _selectedHome.value = null
                } else {
                    _actionError.value = "No se pudo conectar al servidor. Revisa tu conexión a internet e inténtalo de nuevo."
                }

            } catch (e: Exception) {
                _actionError.value = "Ocurrió un error inesperado al intentar eliminar el hogar."
            }
        }
    }

    fun dismissSuccessFeedback() { _showSuccessFeedback.value = false }
    fun dismissBiometricError() { _actionError.value = null }
    fun showBiometricError(msg: String) { _actionError.value = msg }


    // Sugerencia y Búsqueda de usuarios


    private var allSuggestedUsers: List<UserSuggestion> = emptyList()

    val suggestedUsers: StateFlow<List<MemberEntityNew>> = combine(
        repository.allMembersNew,
        dataStore.userIdFlow
    ) { members, currentUserId ->
        members.filter {
            it.userId != currentUserId &&
                    !it.isDeleted &&
                    it.userId.isNotEmpty() &&
                    it.status == MemberStatus.ACCEPTED
        }.distinctBy { it.userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Estado para feedback del envío de correo
    private val _inviteEmailState = MutableStateFlow<InviteEmailState>(InviteEmailState.Idle)
    val inviteEmailState = _inviteEmailState.asStateFlow()

    sealed class InviteEmailState {
        object Idle : InviteEmailState()
        object Loading : InviteEmailState()
        object Success : InviteEmailState()
        data class Error(val message: String) : InviteEmailState()
    }

    fun sendInviteEmail(email: String) {
        val homeId = _selectedHome.value?.id ?: return
        val homeName = _selectedHome.value?.name ?: return
        val inviteCode = _selectedHome.value?.inviteCode ?: return

        viewModelScope.launch {
            _inviteEmailState.value = InviteEmailState.Loading
            val success = repository.sendInviteEmail(homeId, email, homeName, inviteCode)
            _inviteEmailState.value = if (success) InviteEmailState.Success
            else InviteEmailState.Error("No se pudo enviar el correo")
        }
    }

    fun resetInviteEmailState() {
        _inviteEmailState.value = InviteEmailState.Idle
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

            val response = repository.findHomeByCode(formattedCode)

            if (response == null) {
                _joinState.value = JoinHomeState.Error("No encontramos ningún hogar con ese código. Verifica con el creador del hogar.")
                return@launch
            }

            val previewInfo = HomePreviewInfo(
                id = response.id,
                name = response.name,
                creatorName = response.creatorName,
                memberCount = response.memberCount,
                taskCount = response.taskCount,
                pendingCount = response.pendingCount,
                isAlreadyMember = response.isAlreadyMember
            )

            _joinState.value = JoinHomeState.Found(previewInfo, previewInfo.isAlreadyMember)
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


    val currentUserRole: StateFlow<MemberRole?> = combine(
        selectedHome,
        repository.allMembersNew,
        dataStore.userIdFlow
    ) { currentHome, members, userId ->
        if (currentHome == null || userId.isEmpty()) {
            null
        } else {
            members.find { it.homeId == currentHome.id && it.userId == userId }?.role
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isCurrentUserCreator: StateFlow<Boolean> = currentUserRole
        .map { it == MemberRole.CREATOR }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canCurrentUserEditTasks: StateFlow<Boolean> = combine(
        selectedHome,
        currentUserRole
    ) { home, role ->
        if (home == null || role == null) {
            false
        } else {
            when (home.editPermission) {
                HomePermission.CREATOR_ONLY -> role == MemberRole.CREATOR
                HomePermission.ADMINS -> role == MemberRole.CREATOR || role == MemberRole.ADMIN
                HomePermission.ALL_MEMBERS -> true
                else -> false
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun leaveCurrentHome(onSuccess: () -> Unit) {
        val currentHome = _selectedHome.value ?: return

        viewModelScope.launch {
            try {
                val userId = dataStore.userIdFlow.first()
                val success = repository.leaveHomeWithSync(currentHome.id, userId)

                if (success) {
                    _selectedHome.value = null
                    onSuccess()
                } else {
                    _actionError.value = "No se pudo abandonar el hogar. Intenta de nuevo."
                }
            } catch (e: Exception) {
                _actionError.value = "Ocurrió un error inesperado."
            }
        }
    }


}