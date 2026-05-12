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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncViewModel(
    application: Application,
    private val syncRepository: SyncRepository
) : AndroidViewModel(application) {

    private val connectivityObserver = NetworkConnectivityObserver(application.applicationContext)

    val isOffline: StateFlow<Boolean> = connectivityObserver.isConnected
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Indica si hay trabajo pendiente en la cola
    val hasPendingWork: StateFlow<Boolean> = syncRepository.hasPendingSyncs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    init {
        observeSyncTriggers()
    }

    private fun observeSyncTriggers() {
        viewModelScope.launch {
            combine(
                connectivityObserver.isConnected,
                syncRepository.hasPendingSyncs
            ) { connected, pending ->
                connected && pending
            }
                .distinctUntilChanged()
                .collect { shouldSync ->
                    if (shouldSync) {
                        delay(700)
                        syncRepository.syncPendingItems()
                    }
                }
        }
    }
}