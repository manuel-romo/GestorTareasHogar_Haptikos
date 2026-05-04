package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.RoomEntityNew
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomViewModel(private val repository: AppRepository): ViewModel(){


    val rooms: StateFlow<List<RoomEntityNew>> = repository.allRoomsNew
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRoom(room: RoomEntityNew) {
        viewModelScope.launch {
            repository.insertRoomNew(room)
        }
    }



}