package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.utils.getDayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskInstanceViewModel(private val repository: AppRepository) : ViewModel() {

    data class TaskFilter(
        val showOnlyMine: Boolean = false,
        val status: TaskState? = null,
        val selectedDay: String = "Todos"
    )

    private val currentUser = "María"

    private val _currentFilter = MutableStateFlow(TaskFilter())
    val currentFilter = _currentFilter.asStateFlow()

    // Búsqueda combinada
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(newFilter: TaskFilter) {
        _currentFilter.value = newFilter
    }

    // Clase que agrupa las características
    data class DashboardStats(
        val pendingTasksCount: Int = 0,
        val completedTasksCount: Int = 0,
        val totalTasks: Int = 0,
        val dailyProgress: Float = 0f,
        val userPoints: Int = 0
    )

    fun updateTask(taskInstance: TaskInstanceEntity) {
        viewModelScope.launch {
            repository.updateTaskInstance(taskInstance)
        }
    }

    fun markTaskAsCompleted(taskInstance: TaskInstanceEntity) {
        val completedTask = taskInstance.copy(state = TaskState.COMPLETED)
        updateTask(completedTask)
    }

    val tasks: StateFlow<List<TaskInstanceEntity>> = combine(
        repository.allTasksInstance,
        _currentFilter,
        _searchQuery
    ) { rawTasks, filter, query ->
        rawTasks.filter { taskInstance ->
            // Filtro 1. Estado
            val matchesStatus = filter.status == null || taskInstance.state == filter.status

            // Filtro 2. Asignación
            val matchesOwnership = if (filter.showOnlyMine) taskInstance.task.members.any { it.name == currentUser } else true

            // Filtro 3. Nombre
            val matchesSearch = if (query.isNotBlank()) {
                taskInstance.task.title.contains(query, ignoreCase = true)
            } else true

            // Filtro 4. Día de la semana
            val matchesDay = if (filter.selectedDay != "Todos") {
                getDayName(taskInstance.dueDate) == filter.selectedDay
            } else true

            // La tarea debe cumplir los 4 requisitos
            matchesStatus && matchesOwnership && matchesSearch && matchesDay
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Se escuchan todas las tareas directas de la base de datos para hacer los cálculos
    val stats: StateFlow<DashboardStats> = repository.allTasksInstance.map { allTasks ->
        val total = allTasks.size
        val completed = allTasks.count { it.state == TaskState.COMPLETED }
        val pending = allTasks.count { it.state == TaskState.PENDING }
        val points = allTasks.filter { it.state == TaskState.COMPLETED }.sumOf { it.task.points }
        val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        DashboardStats(
            pendingTasksCount = pending,
            completedTasksCount = completed,
            totalTasks = total,
            dailyProgress = progress,
            userPoints = points
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    suspend fun getById(taskInstanceId: Int): TaskInstanceEntity? {
        return repository.getTaskInstanceById(taskInstanceId)
    }
}