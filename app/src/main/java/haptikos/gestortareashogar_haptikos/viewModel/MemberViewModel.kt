package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemberViewModel(private val repository: AppRepository): ViewModel(){


    val members: StateFlow<List<MemberEntityNew>> = repository.allMembersNew
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMember(member: MemberEntityNew) {
        viewModelScope.launch {
            repository.insertMemberNew(member)
        }
    }



}