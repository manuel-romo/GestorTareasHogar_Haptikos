package haptikos.gestortareashogar_haptikos.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.utils.NetworkConnectivityObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncViewModel(
    application: Application,
    private val syncRepository: SyncRepository,
    private val dataStore: DataStoreManager
) : AndroidViewModel(application) {

    private val connectivityObserver = NetworkConnectivityObserver(application.applicationContext)

    val isOffline: StateFlow<Boolean> = connectivityObserver.isConnected
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasPendingSyncs: StateFlow<Boolean> = syncRepository.hasPendingSyncs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        observeConnectivityForSync()
    }

    private fun observeConnectivityForSync() {
        viewModelScope.launch {
            connectivityObserver.isConnected
                // Cuando hay conexión solamente se ejecuta esto:
                .filter { it }
                .collect {
                    delay(2000)
                    val userId = dataStore.userIdFlow.first()
                    if (userId.isNotEmpty()) {
                        syncRepository.syncPendingItems()
                    }
                }
        }
    }
}