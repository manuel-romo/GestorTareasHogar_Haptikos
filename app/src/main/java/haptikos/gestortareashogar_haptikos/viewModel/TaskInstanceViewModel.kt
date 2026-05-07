package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.utils.getDayName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskInstanceViewModel(private val repository: AppRepository) : ViewModel() {

    data class TaskFilter(
        val showOnlyMine: Boolean = false,
        val status: TaskState? = null,
        val selectedDay: String = "Todos"
    )

    // TODO obtener de Auth
    private val currentUser = "María"

    private val _currentFilter = MutableStateFlow(TaskFilter())
    val currentFilter = _currentFilter.asStateFlow()

    private fun updateTask(taskInstance: TaskInstanceEntityNew) {
        viewModelScope.launch {
            repository.updateTaskInstance(taskInstance)
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    data class DashboardStats(
        val pendingTasksCount: Int = 0,
        val completedTasksCount: Int = 0,
        val totalTasks: Int = 0,
        val dailyProgress: Float = 0f,
        val userPoints: Int = 0
    )

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateFilter(newFilter: TaskFilter) { _currentFilter.value = newFilter }

    // Acciones
    fun markTaskAsCompleted(taskInstance: TaskInstanceEntityNew) {
        viewModelScope.launch {
            val completedTask = taskInstance.copy(state = TaskState.COMPLETED)
            repository.updateTaskInstance(completedTask)
        }
    }

    // Datos reactivos
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskInstanceWithDetails>> = combine(
        _currentFilter,
        _searchQuery
    ) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        val ownerName = if (filter.showOnlyMine) currentUser else null

        repository.getFilteredInstances(
            status = filter.status,
            searchQuery = query,
            memberName = ownerName
        ).map { dbResults ->
            dbResults.filter { instance ->
                if (filter.selectedDay != "Todos") {
                    getDayName(instance.taskInstance.dueDate) == filter.selectedDay
                } else true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val stats: StateFlow<DashboardStats> = repository.allInstancesWithDetails.map { allInstances ->
        val total = allInstances.size
        val completed = allInstances.count { it.taskInstance.state == TaskState.COMPLETED }
        val pending = allInstances.count { it.taskInstance.state == TaskState.PENDING }

        val points = allInstances
            .filter { it.taskInstance.state == TaskState.COMPLETED }
            .sumOf { it.task.points + it.task.priority.points }

        val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        DashboardStats(
            pendingTasksCount = pending,
            completedTasksCount = completed,
            totalTasks = total,
            dailyProgress = progress,
            userPoints = points
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Consultas únicas
    suspend fun getInstanceWithDetailsById(instanceId: Int): TaskInstanceWithDetails? {
        return repository.getTaskInstanceWithDetailsById(instanceId)
    }

    // Alternar entre Pausado y Pendiente
    fun toggleTaskPause(taskInstance: TaskInstanceEntityNew) {
        val newState = if (taskInstance.state == TaskState.PAUSED) TaskState.PENDING else TaskState.PAUSED
        val updatedTask = taskInstance.copy(state = newState)
        updateTask(updatedTask)
    }

    // Reactivar una tarea completada
    fun markTaskAsPending(taskInstance: TaskInstanceEntityNew) {
        val updatedTask = taskInstance.copy(state = TaskState.PENDING)
        updateTask(updatedTask)
    }

    // Eliminar la instancia de la tarea
    fun deleteTaskInstance(taskInstance: TaskInstanceEntityNew) {
        viewModelScope.launch {
            // Asegúrate de que se llame así tu método en el AppRepository
            repository.deleteTaskInstance(taskInstance)
        }
    }

}