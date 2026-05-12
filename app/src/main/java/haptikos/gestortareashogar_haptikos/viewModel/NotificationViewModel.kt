package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.dao.NotificationDao
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.NotificationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationDao: NotificationDao
) : ViewModel() {

    enum class NotificationFilter {
        ALL, UNREAD, TASK_COMPLETED, NEW_MEMBER, TASK_REMINDER
    }

    private val _filter = MutableStateFlow(NotificationFilter.ALL)
    val filter = _filter.asStateFlow()

    val unreadCount: StateFlow<Int> = notificationDao.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notifications: StateFlow<List<NotificationEntity>> = _filter
        .flatMapLatest { filter ->
            when (filter) {
                NotificationFilter.ALL -> notificationDao.getAll()
                NotificationFilter.UNREAD -> notificationDao.getUnread()
                NotificationFilter.TASK_COMPLETED -> notificationDao.getByType("TASK_COMPLETED")
                NotificationFilter.NEW_MEMBER -> notificationDao.getByType("NEW_MEMBER")
                NotificationFilter.TASK_REMINDER -> notificationDao.getByType("TASK_REMINDER")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: NotificationFilter) {
        _filter.value = filter
    }

    fun markAsRead(id: String) {
        viewModelScope.launch { notificationDao.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { notificationDao.markAllAsRead() }
    }

    fun delete(id: String) {
        viewModelScope.launch { notificationDao.delete(id) }
    }
}