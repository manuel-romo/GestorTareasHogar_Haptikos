package haptikos.gestortareashogar_haptikos.viewModel

import android.content.Context
import android.net.Uri
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.ui.screens.pruebaUserEdition.ProfileHomeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    fun uploadPhoto(uri: Uri, context: Context) {
        viewModelScope.launch {

            val currentUserId = dataStore.userIdFlow.first()

            // Se convierte la URI a un archivo File temporal
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output -> inputStream?.copyTo(output) }

            val resultUrl = repository.uploadProfilePicture(currentUserId, tempFile)

            if (resultUrl != null) {
                // Éxito. La UI se actualizará sola si está leyendo profilePicUrlFlow del DataStore.
            } else {
                // Manejar error (mostrar un Toast o SnackBar)
            }
        }
    }

}