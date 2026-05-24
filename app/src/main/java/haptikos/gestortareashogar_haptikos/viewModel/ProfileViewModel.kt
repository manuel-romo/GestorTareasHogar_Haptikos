package haptikos.gestortareashogar_haptikos.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _isUpdatingName = MutableStateFlow(false)
    val isUpdatingName = _isUpdatingName.asStateFlow()

    val profilePicUrl: StateFlow<String?> = dataStore.profilePicUrlFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val notifyReminders: StateFlow<Boolean> = dataStore.notifyRemindersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notifyCompleted: StateFlow<Boolean> = dataStore.notifyCompletedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notifyNewMembers: StateFlow<Boolean> = dataStore.notifyNewMembersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    fun uploadPhoto(uri: Uri, context: Context) {
        viewModelScope.launch {

            _isUploadingPhoto.value = true
            val currentUserId = dataStore.userIdFlow.first()

            // Se convierte la URI a un archivo File temporal
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output -> inputStream?.copyTo(output) }

            val resultUrl = repository.uploadProfilePicture(currentUserId, tempFile)

            _isUploadingPhoto.value = false

            if (resultUrl != null) {
                // Éxito. La UI se actualizará sola si está leyendo profilePicUrlFlow del DataStore.
            } else {
                // Manejar error (mostrar un Toast o SnackBar)
            }
        }
    }

    fun updateUserName(newName: String) {
        if (newName.isBlank()) return

        _isUpdatingName.value = true

        viewModelScope.launch {
            try {
                // Obtención del ID del usuario
                val currentUserId = dataStore.userIdFlow.first()

                if (currentUserId != null) {
                    val success = repository.updateUserName(currentUserId, newName)

                    if (success) {
                        dataStore.saveUserName(newName)
                    } else {
                        // TODO: Manejar error
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUpdatingName.value = false
            }
        }
    }

    // Actualizar notificaciones de usuario
    fun updateNotificationPreference(type: String, isEnabled: Boolean, homeId: String?) {
        viewModelScope.launch {
            // Guardado local
            dataStore.saveNotificationPreference(type, isEnabled)
            // Intento de sincronización
            try {
                val currentUserId = dataStore.userIdFlow.first()

                val success = repository.updateUserNotificationSettings(
                    userId = currentUserId,
                    type = type,
                    isEnabled = isEnabled,
                    homeId = homeId
                )

                if (!success) {
                    // Si el servidor falla, se revierte la acción
                    dataStore.saveNotificationPreference(type, !isEnabled)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Si hay o otro error también se revierte la acción
                dataStore.saveNotificationPreference(type, !isEnabled)
            }
        }
    }


}