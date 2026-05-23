package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemberViewModel(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
): ViewModel() {

    data class MemberModel(
        val member: MemberEntityNew,
        val isCurrentUser: Boolean
    )

    val members: StateFlow<List<MemberEntityNew>> = repository.allMembersNew
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMembersForHome(homeId: String): StateFlow<List<MemberEntityNew>> =
        dataStore.userIdFlow.flatMapLatest { currentUserId ->
            repository.getMembersByHome(homeId, currentUserId)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMember(member: MemberEntityNew) {
        viewModelScope.launch {
            repository.insertMemberNew(member)
        }
    }

    fun getCompletedTaskCountForMember(memberId: String): StateFlow<Int> =
        repository.getCompletedTaskCountForMember(memberId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    fun updateMemberRole(memberId: String, homeId: String, newRole: MemberRole) {
        viewModelScope.launch {
            repository.updateMemberRole(memberId, homeId, newRole)
        }
    }

    fun removeMemberFromHome(memberId: String, homeId: String) {
        viewModelScope.launch {
            repository.removeMemberFromHome(memberId, homeId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMembersForHomeWithUserContext(homeId: String): StateFlow<List<MemberModel>> {
        return dataStore.userIdFlow.flatMapLatest { currentUserId ->

            repository.getMembersByHome(homeId, currentUserId).map { membersList ->
                membersList.map { member ->
                    MemberModel(
                        member = member,
                        isCurrentUser = member.userId == currentUserId
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun getCompletedTaskCountsForMembers(memberIds: List<String>): Flow<Map<String, Int>> {
        return combine(
            memberIds.map { id ->
                getCompletedTaskCountForMember(id).map { count -> id to count }
            }
        ) { pairs ->
            pairs.toMap()
        }
    }

}