package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.RoomEntityNew
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Estados para eliminación
    private val _roomToDelete = MutableStateFlow<RoomEntityNew?>(null)
    val roomToDelete: StateFlow<RoomEntityNew?> = _roomToDelete.asStateFlow()

    private val _showSuccessFeedback = MutableStateFlow(false)
    val showSuccessFeedback: StateFlow<Boolean> = _showSuccessFeedback.asStateFlow()

    private val _biometricError = MutableStateFlow<String?>(null)
    val biometricError: StateFlow<String?> = _biometricError.asStateFlow()

    // Selección de eliminación de habitación
    fun initiateDeletion(room: RoomEntityNew) {
        _roomToDelete.value = room
    }

    // Cancelación de eliminación
    fun cancelDeletion() {
        _roomToDelete.value = null
    }

    // Autenticación exitosa
    fun confirmDeletionAfterBiometrics() {
        val room = _roomToDelete.value ?: return

        viewModelScope.launch {
            repository.deleteRoomNew(room)
            _roomToDelete.value = null
            _showSuccessFeedback.value = true
        }
    }

    // Autenticación fallida
    fun showBiometricError(errorMsg: String) {
        _roomToDelete.value = null
        _biometricError.value = errorMsg
    }

    // Cerrar mensajes de aviso
    fun dismissBiometricError() {
        _biometricError.value = null
    }

    fun dismissSuccessFeedback() {
        _showSuccessFeedback.value = false
    }

    // Edición de Habitación

    private val _roomToEdit = MutableStateFlow<RoomEntityNew?>(null)
    val roomToEdit: StateFlow<RoomEntityNew?> = _roomToEdit.asStateFlow()

    // Abrir Edición
    fun initiateEdit(room: RoomEntityNew) {
        _roomToEdit.value = room
    }

    // Cerrar Edición Sin Guardar
    fun cancelEdit() {
        _roomToEdit.value = null
    }

    // Guardado de datos editados
    fun updateRoom(newName: String, newIcon: String, newColorHex: String) {
        val currentRoom = _roomToEdit.value ?: return

        viewModelScope.launch {
            val updatedRoom = currentRoom.copy(
                name = newName,
                icon = newIcon,
                colorHex = newColorHex
            )

            repository.updateRoomNew(updatedRoom)
            _roomToEdit.value = null
        }
    }



}