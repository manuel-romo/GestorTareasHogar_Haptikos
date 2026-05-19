package haptikos.gestortareashogar_haptikos.viewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.ui.screens.homeStats.BarChartData
import haptikos.gestortareashogar_haptikos.ui.screens.homeStats.HomeStatsUiState
import haptikos.gestortareashogar_haptikos.ui.screens.homeStats.MemberStatsItem
import haptikos.gestortareashogar_haptikos.ui.screens.homeStats.RoomStatsItem
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.BadgeItem
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.ChallengeItem
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.RankingMemberItem
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.RewardsUiState
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

class TaskInstanceViewModel(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    data class TaskFilter(
        val showOnlyMine: Boolean = false,
        val status: TaskState? = null,
        val selectedDay: String = "Todos"
    )

    // Todas las tareas pendientes
    val allTaskInstances: StateFlow<List<TaskInstanceWithDetails>> = repository.allInstancesWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentFilter = MutableStateFlow(TaskFilter())
    val currentFilter = _currentFilter.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow("Año")
    val selectedTimeRange = _selectedTimeRange.asStateFlow()

    // Estado de permisos de edición para la instancia actual
    private val _canEditCurrentInstance = MutableStateFlow(false)
    val canEditCurrentInstance = _canEditCurrentInstance.asStateFlow()

    // Estado para eliminación de instancia
    private val _isDeletingInstance = MutableStateFlow(false)
    val isDeletingInstance = _isDeletingInstance.asStateFlow()

    private val _instanceDeleteError = MutableStateFlow<String?>(null)
    val instanceDeleteError = _instanceDeleteError.asStateFlow()

    private val _selectedHomeId = MutableStateFlow<String?>(null)

    fun setSelectedHome(homeId: String?) {
        _selectedHomeId.value = homeId
    }


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
            repository.updateTaskInstance(
                taskInstance.copy(state = TaskState.COMPLETED, isSynced = false)
            )
            repository.syncPendingInstances()
        }
    }

    // Datos reactivos
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskInstanceWithDetails>> = combine(
        _currentFilter,
        _searchQuery,
        _selectedHomeId,
        dataStore.usernameFlow
    ) { filter, query, homeId, userName ->
        Triple(filter, query, Pair(homeId, userName))
    }.flatMapLatest { (filter, query, homeAndUser) ->
        val (homeId, userName) = homeAndUser
        val ownerName = if (filter.showOnlyMine) userName else null

        repository.getFilteredInstances(
            homeId = homeId,
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
            .sumOf { it.taskDetails.task.points + it.taskDetails.task.priority.points }

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
    suspend fun getInstanceWithDetailsById(instanceId: String): TaskInstanceWithDetails? {
        return repository.getTaskInstanceWithDetailsById(instanceId)
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
        instances: List<haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails>,
        range: String
    ): List<haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails> {
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
        instances: List<haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails>,
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
        instances: List<haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails>
    ): Float {
        if (instances.isEmpty()) return 0f
        val completed = instances.count { it.taskInstance.state == TaskState.COMPLETED }
        return (completed.toFloat() / instances.size.toFloat() * 100)
    }




    // Calcular si el usuario puede editar según el hogar de la tarea
    fun checkEditPermission(instance: TaskInstanceWithDetails, allHomes: List<HomeEntityNew>, userId: String) {
        val homeId = instance.taskDetails.room?.homeId
        val home = allHomes.find { it.id == homeId }

        _canEditCurrentInstance.value = when (home?.editPermission) {
            HomePermission.ALL_MEMBERS -> true
            HomePermission.ADMINS -> {
                instance.assignedMembers.any { it.userId == userId } ||
                        instance.taskDetails.members.any {
                            it.userId == userId && (it.role == MemberRole.ADMIN || it.role == MemberRole.CREATOR)
                        }
            }
            HomePermission.CREATOR_ONLY -> {
                instance.taskDetails.members.any {
                    it.userId == userId && it.role == MemberRole.CREATOR
                }
            }
            null -> false
        }
    }

    // Actualizar miembros asignados a la instancia
    fun updateInstanceMembers(instanceId: String, memberIds: List<String>) {
        viewModelScope.launch {
            repository.updateInstanceMembers(instanceId, memberIds)
        }
    }

    // Control de eliminación
    fun initiateInstanceDeletion() { _isDeletingInstance.value = true }
    fun cancelInstanceDeletion() { _isDeletingInstance.value = false }
    fun dismissInstanceDeleteError() { _instanceDeleteError.value = null }

    fun confirmInstanceDeletion(instance: TaskInstanceEntityNew, onBack: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteTaskInstance(instance)
                _isDeletingInstance.value = false
                onBack()
            } catch (e: Exception) {
                _instanceDeleteError.value = "Error al eliminar la tarea"
            }
        }
    }

    fun showInstanceDeleteError(message: String) {
        _instanceDeleteError.value = message
    }

    // Lógica para las rewards (específicamente el ranking)
    @OptIn(ExperimentalCoroutinesApi::class)
    val rewardsState: StateFlow<RewardsUiState> = combine(
        repository.allInstancesWithDetails,
        _selectedHomeId,
        dataStore.userIdFlow
    ) { allInstances, homeId, currentUserId ->

        // Se filtran las instancias completadas del hogar actual
        val completedInstances = allInstances.filter {
            it.taskInstance.state == TaskState.COMPLETED &&
                    (homeId == null || it.taskDetails.task.homeId == homeId)
        }

        // Se calculan los puntos totales del usuario logueado para que salgan en el Header
        val userPoints = completedInstances
            .filter { instance -> instance.assignedMembers.any { it.userId == currentUserId } }
            .sumOf { it.taskDetails.task.points + it.taskDetails.task.priority.points }

        // Generar ranking en local
        // Primero se agrupa los puntos por cada miembro asignado en las tareas completadas
        val memberPointsMap = mutableMapOf<String, Int>()
        val memberInfoMap = mutableMapOf<String, MemberEntityNew>()

        completedInstances.forEach { instance ->
            instance.assignedMembers.forEach { member ->
                val pts = instance.taskDetails.task.points + instance.taskDetails.task.priority.points
                memberPointsMap[member.id] = memberPointsMap.getOrDefault(member.id, 0) + pts
                memberInfoMap[member.id] = member
            }
        }

        // Acá se convierte a una lista luego se ordena de mayor a menor y ya por último se asignan posiciones
        val ranking = memberPointsMap.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                val member = memberInfoMap[entry.key]!!
                RankingMemberItem(
                    memberId = member.id,
                    name = "${member.name} ${member.lastName}".trim(),
                    points = entry.value,
                    colorHex = member.colorHex,
                    position = index + 1,
                    isCurrentUser = member.userId == currentUserId
                )
            }

        RewardsUiState(
            totalPoints = userPoints,
            dailyProgress = calculateDailyProgress(completedInstances),
            rankingList = ranking,
            badges = listOf(
                BadgeItem("Primer logro", "⭐", isUnlocked = true, dateUnlocked = "May 2026"),
                BadgeItem("7 días seguidos", "🔥", isUnlocked = true, dateUnlocked = "May 2026"),
                BadgeItem("10 tareas", "✅", isUnlocked = false),
                BadgeItem("Invitador", "👥", isUnlocked = false),
                BadgeItem("Perfeccionista", "💎", isUnlocked = false),
                BadgeItem("Madrugador", "🌅", isUnlocked = false)
            ),
            challenges = listOf(
                ChallengeItem("Completa 5 tareas", "Completa 5 tareas esta semana", 3, 5, 25, "🎯"),
                ChallengeItem("Racha de 3 días", "Completa tareas 3 días seguidos", 2, 3, 15, "🔥"),
                ChallengeItem("Todo el día", "Completa todas las tareas de un día", 0, 1, 25, "⚡")
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RewardsUiState())

    private fun calculateDailyProgress(instances: List<TaskInstanceWithDetails>): Float {
        // Lógica actual
        return if (instances.isNotEmpty()) 0.84f else 0f
    }

    private val _homeStatsRange = MutableStateFlow("Semana")
    val homeStatsRange = _homeStatsRange.asStateFlow()

    fun updateHomeStatsRange(range: String) {
        _homeStatsRange.value = range
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeStatsState: StateFlow<HomeStatsUiState> = combine(
        repository.allInstancesWithDetails,
        _selectedHomeId,
        _homeStatsRange
    ) { allInstances, homeId, range ->

        val homeInstances = allInstances.filter { inst ->
            homeId == null || inst.taskDetails.task.homeId == homeId
        }

        val filtered = filterInstancesByRange(homeInstances, range)

        val completed = filtered.filter { it.taskInstance.state == TaskState.COMPLETED }
        val pending = filtered.filter { it.taskInstance.state == TaskState.PENDING }

        val barData = calculateBarChartData(filtered, range)

        val memberStats = filtered
            .flatMap { it.assignedMembers }
            .distinctBy { it.id }
            .map { member ->
                val mInstances = filtered.filter { inst -> inst.assignedMembers.any { it.id == member.id } }
                MemberStatsItem(
                    name = member.name,
                    completedTasks = mInstances.count { it.taskInstance.state == TaskState.COMPLETED },
                    totalTasks = mInstances.size,
                    color = Color(android.graphics.Color.parseColor(member.colorHex))
                )
            }.sortedByDescending { it.completedTasks }

        val roomStats = filtered
            .groupBy { it.taskDetails.room?.id }
            .map { (roomId, insts) ->
                RoomStatsItem(
                    roomName = insts.first().taskDetails.room?.name ?: "General",
                    completedTasks = insts.count { it.taskInstance.state == TaskState.COMPLETED },
                    totalTasks = insts.size
                )
            }

        HomeStatsUiState(
            selectedRange = range,
            effectiveness = if (filtered.isNotEmpty()) (completed.size.toFloat() / filtered.size * 100).toInt() else 0,
            completedCount = completed.size,
            pendingCount = pending.size,
            membersCount = memberStats.size,
            barChartData = barData,
            members = memberStats,
            rooms = roomStats
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeStatsUiState())

    private fun calculateBarChartData(
        instances: List<haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails>,
        range: String
    ): List<BarChartData> {
        return when (range) {
            "Semana" -> {
                val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                days.mapIndexed { index, label ->
                    val dayInstances = instances.filter {
                        val instCal = Calendar.getInstance().apply { timeInMillis = it.taskInstance.dueDate }
                        val dayOfWeek = (instCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                        dayOfWeek == index
                    }
                    BarChartData(
                        label = label,
                        completed = dayInstances.count { it.taskInstance.state == TaskState.COMPLETED }.toFloat(),
                        pending = dayInstances.count { it.taskInstance.state == TaskState.PENDING }.toFloat()
                    )
                }
            }
            "Mes" -> {
                listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4").mapIndexed { index, label ->
                    val mInst = instances.filter {
                        val instCal = Calendar.getInstance().apply { timeInMillis = it.taskInstance.dueDate }
                        val dayOfMonth = instCal.get(Calendar.DAY_OF_MONTH)
                        val weekOfInstance = when {
                            dayOfMonth <= 7 -> 0
                            dayOfMonth <= 14 -> 1
                            dayOfMonth <= 21 -> 2
                            else -> 3
                        }
                        weekOfInstance == index
                    }
                    BarChartData(label, mInst.count { it.taskInstance.state == TaskState.COMPLETED }.toFloat(), mInst.count { it.taskInstance.state == TaskState.PENDING }.toFloat())
                }
            }
            "Año" -> {
                val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                months.mapIndexed { index, label ->
                    val mInst = instances.filter {
                        val instCal = Calendar.getInstance().apply { timeInMillis = it.taskInstance.dueDate }
                        instCal.get(Calendar.MONTH) == index
                    }
                    BarChartData(label, mInst.count { it.taskInstance.state == TaskState.COMPLETED }.toFloat(), mInst.count { it.taskInstance.state == TaskState.PENDING }.toFloat())
                }
            }
            else -> emptyList()
        }
    }

    val userName: StateFlow<String> = dataStore.usernameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

}