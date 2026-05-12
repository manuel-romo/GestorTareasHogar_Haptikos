package haptikos.gestortareashogar_haptikos.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.ui.components.OfflineSyncBanner
import haptikos.gestortareashogar_haptikos.ui.theme.PausedYellow
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.NotificationViewModel
import haptikos.gestortareashogar_haptikos.viewModel.SyncViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel.TaskFilter
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel.DashboardStats

@Composable
fun HomeScreen(
    taskInstanceViewModel: TaskInstanceViewModel,
    homeViewModel: HomeViewModel,
    syncViewModel: SyncViewModel,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    onSettingsClick:() -> Unit,
    onTaskClick: (String) -> Unit,
    onStatusClick: (TaskInstanceEntityNew) -> Unit,
    onDeleteClick: (TaskInstanceEntityNew) -> Unit,
    onNavigateToCreateHome:() -> Unit,
    onNavigateToJoinHome: () -> Unit,
    onNotificationsClick: () -> Unit
){

    val isOffline by syncViewModel.isOffline.collectAsState()

    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val hasNotifications = unreadCount > 0

    // Estados de tareas
    val tasksInstanceList by taskInstanceViewModel.tasks.collectAsState()
    val stats by taskInstanceViewModel.stats.collectAsState()
    val currentFilter by taskInstanceViewModel.currentFilter.collectAsState()
    val searchQuery by taskInstanceViewModel.searchQuery.collectAsState()


    // Estados de hogar
    val homesList by homeViewModel.allHomes.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()

    val userName by authViewModel.userName.collectAsState("")
    val hasAdminPermissions by homeViewModel.isCurrentUserCreatorOrAdmin.collectAsState()

    LaunchedEffect(selectedHome?.id) {
        taskInstanceViewModel.setSelectedHome(selectedHome?.id)
    }

    HomeContent(
        tasks = tasksInstanceList,
        stats = stats,
        currentFilter = currentFilter,
        userName = userName,
        hasAdminPermissions = hasAdminPermissions,
        onFilterChange = { nuevoFiltro -> taskInstanceViewModel.updateFilter(nuevoFiltro) },
        searchQuery = searchQuery,
        homesList = homesList,
        selectedHome = selectedHome,
        onHomeSelected = { home -> homeViewModel.selectHome(home) },
        onSearchQueryChange = { nuevaBusqueda -> taskInstanceViewModel.updateSearchQuery(nuevaBusqueda) },
        onSettingsClick = onSettingsClick,
        onTaskClick = onTaskClick,
        onStatusClick = onStatusClick,
        onDeleteClick = onDeleteClick,
        onNavigateToCreateHome = onNavigateToCreateHome,
        onNavigateToJoinHome = onNavigateToJoinHome,
        onNotificationsClick = onNotificationsClick,
        isOffline = isOffline,
        hasNotifications = hasNotifications
    )
}

@Composable
fun HomeContent(
    tasks: List<TaskInstanceWithDetails>,
    stats: DashboardStats,
    currentFilter: TaskFilter,
    userName: String,
    hasAdminPermissions: Boolean,
    onFilterChange: (TaskFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    homesList: List<HomeEntityNew>,
    selectedHome: HomeEntityNew?,
    onHomeSelected: (HomeEntityNew) -> Unit,
    onTaskClick: (String) -> Unit,
    onStatusClick: (TaskInstanceEntityNew) -> Unit,
    onDeleteClick: (TaskInstanceEntityNew) -> Unit,
    onNavigateToCreateHome:() -> Unit,
    onNavigateToJoinHome: () -> Unit,
    onNotificationsClick: () -> Unit,
    isOffline: Boolean,
    hasNotifications: Boolean
) {
    val tareasPendientes = tasks.filter { it.taskInstance.state == TaskState.PENDING }
    val tareasCompletadas = tasks.filter { it.taskInstance.state == TaskState.COMPLETED }


    val pendingTasksCount = stats.pendingTasksCount
    val totalTasks = stats.totalTasks
    val dailyProgress = stats.dailyProgress
    val userPoints = stats.userPoints

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        OfflineSyncBanner(
            isOffline = isOffline
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                DashboardHeader(
                    userName = userName,
                    userHasAdminPermissions = hasAdminPermissions,
                    pendingTasksCount = pendingTasksCount,
                    hasNotifications = hasNotifications,
                    userPoints = userPoints,
                    dailyProgress = dailyProgress,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    currentFilter = currentFilter,
                    onFilterChange = onFilterChange,
                    isHomeSelected = selectedHome != null,
                    currentHomeName = selectedHome?.name ?: "Seleccionar hogar",
                    homesList = homesList,
                    onHomeSelected = onHomeSelected,
                    onSettingsClick = onSettingsClick,
                    onNavigateToCreateHome = onNavigateToCreateHome,
                    onNavigateToJoinHome = onNavigateToJoinHome,
                    onNotificationsClick = onNotificationsClick,
                )
            }
            item {
                DaySelector(
                    selectedDay = currentFilter.selectedDay,
                    onDaySelected = { dia ->
                        onFilterChange(currentFilter.copy(selectedDay = dia))
                    }
                )
            }

            // Tareas pendientes
            if (tareasPendientes.isNotEmpty()) {
                item { SectionTitle("PENDIENTES (${tareasPendientes.size})") }
            }

            if (tareasPendientes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFFFF3E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sparkles),
                                contentDescription = null,
                                tint = Color(0xFFFF8A00),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Sin tareas por mostrar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("No tienes tareas en esta categoría.\n¡Disfruta tu tiempo libre!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp))
                    }
                }
            } else {
                items(tareasPendientes) { task ->
                    TaskCard(
                        taskInstance = task,
                        onClick = { onTaskClick(task.taskInstance.id) },
                        onStatusClick = { onStatusClick(task.taskInstance) },
                        onDeleteClick = { onDeleteClick(task.taskInstance) }
                    )
                }
            }

            // Tareas completadas
            if (tareasCompletadas.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(
                            title = "COMPLETADAS (${tareasCompletadas.size})"
                        )
                        Text(
                            text = "Ver historial >",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(tareasCompletadas) { task ->
                    TaskCard(
                        taskInstance = task,
                        onClick = { onTaskClick(task.taskInstance.id) },
                        onStatusClick = { onStatusClick(task.taskInstance) },
                        onDeleteClick = {onDeleteClick(task.taskInstance) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

}

/*
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Pausada_Pendiente() {
    val m1 = MemberEntityNew(
        id = 1,
        name = "María",
        lastName = "Gómez",
        colorHex = "#F014A8",
        role = MemberRole.CREATOR
    )
    val m2 = MemberEntityNew(
        id = 2,
        name = "Juan",
        lastName = "Pérez",
        colorHex = "#2979FF",
        role = MemberRole.MEMBER
    )

    GestorTareasHogar_HaptikosTheme {
        HomeContent(
            tasks = listOf(
                // Tarea Pendiente envuelta en WithDetails
                TaskInstanceWithDetails(
                    taskInstance = TaskInstanceEntityNew(
                        id = 1,
                        taskId = 101,
                        state = TaskState.PENDING,
                        dueDate = 1711929600000L
                    ),
                    task = TaskEntityNew(
                        id = 101,
                        title = "Limpiar la cocina",
                        points = 15,
                        roomId = 1
                    ),
                    assignedMembers = listOf(m1, m2),
                    room = RoomEntityNew(id = 1, name = "Cocina", icon = "🍳", colorHex = "#FF5252")
                ),
                // Tarea Pausada envuelta en WithDetails
                TaskInstanceWithDetails(
                    taskInstance = TaskInstanceEntityNew(
                        id = 2,
                        taskId = 102,
                        state = TaskState.PAUSED,
                        dueDate = 1712016000000L,
                        pausedUntil = 1711929600000L),
                    task = TaskEntityNew(
                        id = 102,
                        title = "Lavar la ropa",
                        points = 10,
                        roomId = 1),
                    assignedMembers = listOf(m1),
                    room = null
                )
            ),
            stats = DashboardStats(
                pendingTasksCount = 1,
                totalTasks = 2,
                dailyProgress = 0.5f,
                userPoints = 150),
            currentFilter = TaskFilter(),
            onFilterChange = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onNewTaskClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Completada() {
    val m1 = MemberEntityNew(
        id = 1,
        name = "María",
        lastName = "Gómez",
        colorHex = "#F014A8",
        role = MemberRole.CREATOR)

    GestorTareasHogar_HaptikosTheme {
        HomeContent(
            tasks = listOf(
                TaskInstanceWithDetails(
                    taskInstance = TaskInstanceEntityNew(
                        id = 3,
                        taskId = 103,
                        state = TaskState.COMPLETED,
                        dueDate = 1711843200000L),
                    task = TaskEntityNew(
                        id = 103,
                        title = "Comprar víveres",
                        points = 5,
                        roomId = 1),
                    assignedMembers = listOf(m1),
                    room = null
                )
            ),
            stats = DashboardStats(
                pendingTasksCount = 0,
                totalTasks = 1,
                dailyProgress = 1f,
                userPoints = 200),
            currentFilter = TaskFilter(),
            onFilterChange = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onNewTaskClick = {},
            onSettingsClick = {}
        )
    }
}

 */