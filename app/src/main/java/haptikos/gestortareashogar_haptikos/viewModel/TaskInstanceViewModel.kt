package haptikos.gestortareashogar_haptikos.viewModel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.ui.screens.userStats.ChartPoint
import haptikos.gestortareashogar_haptikos.ui.screens.userStats.HomeComparisonData
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
import haptikos.gestortareashogar_haptikos.ui.screens.userStats.UserStatsUiState
import haptikos.gestortareashogar_haptikos.ui.screens.userStats.HomeStatsItem
import java.text.SimpleDateFormat
import java.util.Calendar

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

    private val _selectedTimeRange = MutableStateFlow("Año")
    val selectedTimeRange = _selectedTimeRange.asStateFlow()

    fun updateTimeRange(range: String) {
        _selectedTimeRange.value = range
    }

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

    //Estadisticas del usuario
    @OptIn(ExperimentalCoroutinesApi::class)
    val userStats: StateFlow<UserStatsUiState> = combine(
        repository.allInstancesWithDetails,
        repository.allHomes,
        repository.allTasksWithDetails,
        _selectedTimeRange
    ) { allInstances, allHomes, allTasksWithDetails, range ->

        //Filtrar instancias según el rango de tiempo seleccionado (Semana, Mes, Año)
        val filteredInstances = filterInstancesByRange(allInstances, range)

        val total = filteredInstances.size
        val completed = filteredInstances.count { it.taskInstance.state == TaskState.COMPLETED }
        val effectiveness = if (total > 0) (completed.toFloat() / total.toFloat() * 100).toInt() else 0

        //Lógica para la Gráfica de Tendencia dinámica
        val trendPoints = calculateTrendPoints(filteredInstances, range)

        //Lógica para la Lista de Hogares
        val homeStatsList = allHomes.mapIndexed { index, home ->
            val taskIdsInHome = allTasksWithDetails.filter { it.room?.homeId == home.id }.map { it.task.id }
            val instancesInHome = filteredInstances.filter { it.taskInstance.taskId in taskIdsInHome }

            val hTotal = instancesInHome.size
            val hCompleted = instancesInHome.count { it.taskInstance.state == TaskState.COMPLETED }

            val hColor = when (index % 3) {
                0 -> Color(0xFFFF6D00)
                1 -> Color(0xFFA143F4)
                else -> Color(0xFF2196F3)
            }

            HomeStatsItem(
                homeName = home.name,
                completedTasks = hCompleted,
                totalTasks = hTotal,
                progress = if (hTotal > 0) hCompleted.toFloat() / hTotal.toFloat() else 0f,
                color = hColor,
                icon = "🏡",
                iconBgColor = hColor.copy(alpha = 0.1f)
            )
        }

        //Lógica Comparativa
        val comparisonPoints = allHomes.mapIndexed { index, home ->
            val taskIdsInHome = allTasksWithDetails.filter { it.room?.homeId == home.id }.map { it.task.id }
            val instancesInHome = filteredInstances.filter { it.taskInstance.taskId in taskIdsInHome }

            val cCompleted = instancesInHome.count { it.taskInstance.state == TaskState.COMPLETED }
            val cPending = instancesInHome.count { it.taskInstance.state == TaskState.PENDING }

            val hColor = when (index % 3) {
                0 -> Color(0xFFFF6D00)
                1 -> Color(0xFFA143F4)
                else -> Color(0xFF2196F3)
            }

            HomeComparisonData(
                homeName = home.name,
                completedCount = cCompleted,
                pendingCount = cPending,
                color = hColor,
                pendingColor = hColor.copy(alpha = 0.3f)
            )
        }

        UserStatsUiState(
            completedCount = completed,
            effectiveness = effectiveness,
            streakDays = 31,
            selectedRange = range,
            homeStats = homeStatsList,
            trendChartPoints = trendPoints,
            homeComparisonPoints = comparisonPoints
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStatsUiState())

    //Funciones para el filtrado y para las gráficas
    private fun filterInstancesByRange(
        instances: List<haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails>,
        range: String
    ): List<haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (range) {
            "Semana" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                instances.filter { it.taskInstance.dueDate in cal.timeInMillis..now }
            }
            "Mes" -> {
                cal.add(Calendar.MONTH, -1)
                instances.filter { it.taskInstance.dueDate in cal.timeInMillis..now }
            }
            else -> { // Año
                cal.add(Calendar.YEAR, -1)
                instances.filter { it.taskInstance.dueDate in cal.timeInMillis..now }
            }
        }
    }

    private fun calculateTrendPoints(
        instances: List<haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails>,
        range: String
    ): List<ChartPoint> {
        val cal = Calendar.getInstance()
        val currentLocale = java.util.Locale("es", "ES")

        return when (range) {
            "Año" -> {
                val monthFormatter = SimpleDateFormat("MMM", currentLocale)
                (0..11).map { monthIndex ->
                    cal.set(Calendar.MONTH, monthIndex)
                    val label = monthFormatter.format(cal.time).replaceFirstChar { it.uppercase() }
                    val monthlyInstances = instances.filter {
                        val instCal = Calendar.getInstance().apply { timeInMillis = it.taskInstance.dueDate }
                        instCal.get(Calendar.MONTH) == monthIndex
                    }
                    val value = calculateEffectiveness(monthlyInstances)
                    ChartPoint(label, value)
                }
            }
            "Mes" -> {
                //Se muestra cada 5 días (revisar si está bien)
                val days = listOf(1, 5, 10, 15, 20, 25, 30)
                days.map { day ->
                    val monthlyInstances = instances.filter {
                        val instCal = Calendar.getInstance().apply { timeInMillis = it.taskInstance.dueDate }
                        instCal.get(Calendar.DAY_OF_MONTH) <= day && instCal.get(Calendar.DAY_OF_MONTH) > (day - 5)
                    }
                    ChartPoint(day.toString(), calculateEffectiveness(monthlyInstances))
                }
            }
            else -> {
                val dayFormatter = SimpleDateFormat("EEE", currentLocale)
                (0..6).map { i ->
                    cal.set(Calendar.DAY_OF_WEEK, i + 1)
                    val label = dayFormatter.format(cal.time).replaceFirstChar { it.uppercase() }
                    val dailyInstances = instances.filter {
                        val instCal = Calendar.getInstance().apply { timeInMillis = it.taskInstance.dueDate }
                        instCal.get(Calendar.DAY_OF_WEEK) == (i + 1)
                    }
                    ChartPoint(label, calculateEffectiveness(dailyInstances))
                }
            }
        }
    }

    private fun calculateEffectiveness(
        instances: List<haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails>
    ): Float {
        if (instances.isEmpty()) return 0f
        val completed = instances.count { it.taskInstance.state == TaskState.COMPLETED }
        return (completed.toFloat() / instances.size.toFloat() * 100)
    }
}