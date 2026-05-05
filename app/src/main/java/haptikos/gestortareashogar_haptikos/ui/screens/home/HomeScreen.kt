package haptikos.gestortareashogar_haptikos.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.ui.theme.PausedYellow
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel.TaskFilter
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel.DashboardStats

@Composable
fun HomeScreen(
    taskInstanceViewModel: TaskInstanceViewModel,
    homeViewModel: HomeViewModel,
    onNewTaskClick:() -> Unit,
    onSettingsClick: () -> Unit
){
    // Estados de tareas
    val tasksInstanceList by taskInstanceViewModel.tasks.collectAsState()
    val stats by taskInstanceViewModel.stats.collectAsState()
    val currentFilter by taskInstanceViewModel.currentFilter.collectAsState()
    val searchQuery by taskInstanceViewModel.searchQuery.collectAsState()

    // Estados de hogar
    val homesList by homeViewModel.allHomes.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()

    HomeContent(
        tasks = tasksInstanceList,
        stats = stats,
        currentFilter = currentFilter,
        onFilterChange = { nuevoFiltro -> taskInstanceViewModel.updateFilter(nuevoFiltro) },
        searchQuery = searchQuery,
        homesList = homesList,
        selectedHome = selectedHome,
        onHomeSelected = { home -> homeViewModel.selectHome(home) },
        onSearchQueryChange = { nuevaBusqueda -> taskInstanceViewModel.updateSearchQuery(nuevaBusqueda) },
        onNewTaskClick = onNewTaskClick,
        onSettingsClick = onSettingsClick
    )
}

@Composable
fun HomeContent(
    tasks: List<TaskInstanceWithDetails>,
    stats: DashboardStats,
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNewTaskClick:() -> Unit,
    onSettingsClick: () -> Unit,
    homesList: List<HomeEntityNew>,
    selectedHome: HomeEntityNew?,
    onHomeSelected: (HomeEntityNew) -> Unit,
) {
    val tareasPendientes = tasks.filter { it.taskInstance.state == TaskState.PENDING }
    val tareasPausadas = tasks.filter { it.taskInstance.state == TaskState.PAUSED }
    val tareasCompletadas = tasks.filter { it.taskInstance.state == TaskState.COMPLETED }

    val pendingTasksCount = stats.pendingTasksCount
    val totalTasks = stats.totalTasks
    val dailyProgress = stats.dailyProgress
    val userPoints = stats.userPoints

    Scaffold(
        bottomBar = { CustomBottomNavigation() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNewTaskClick()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(64.dp).offset(y = 50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "Agregar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                DashboardHeader(
                    userName = "María",
                    pendingTasksCount = pendingTasksCount,
                    hasNotifications = false,
                    userPoints = userPoints,
                    dailyProgress = dailyProgress,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    currentFilter = currentFilter,
                    onFilterChange = onFilterChange,
                    currentHomeName = selectedHome?.name ?: "Sin Hogar",
                    homesList = homesList,
                    onHomeSelected = onHomeSelected,
                    onSettingsClick = onSettingsClick
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
            item { SectionTitle("PENDIENTES (${tareasPendientes.size})") }

            items(tareasPendientes) { task ->
                TaskCard(taskInstance = task)
            }

            // Tareas pausadas
            if (tareasPausadas.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "PAUSADAS (${tareasPausadas.size})",
                        color = PausedYellow
                    )
                }
                items(tareasPausadas) { task ->
                    TaskCard(taskInstance = task)
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
                    TaskCard(taskInstance = task)
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