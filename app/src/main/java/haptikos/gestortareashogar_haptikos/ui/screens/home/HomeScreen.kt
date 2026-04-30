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
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.ui.theme.PausedYellow
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel.TaskFilter
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel.DashboardStats

@Composable
fun HomeScreen(
    taskInstanceViewModel: TaskInstanceViewModel,
    onNewTaskClick:() -> Unit
){
    val tasksInstanceList by taskInstanceViewModel.tasks.collectAsState()
    val stats by taskInstanceViewModel.stats.collectAsState()
    val currentFilter by taskInstanceViewModel.currentFilter.collectAsState()

    val searchQuery by taskInstanceViewModel.searchQuery.collectAsState()

    HomeContent(
        tasks = tasksInstanceList,
        stats = stats,
        currentFilter = currentFilter,
        onFilterChange = { nuevoFiltro -> taskInstanceViewModel.updateFilter(nuevoFiltro) },
        searchQuery = searchQuery,
        onSearchQueryChange = { nuevaBusqueda -> taskInstanceViewModel.updateSearchQuery(nuevaBusqueda) },
        onNewTaskClick = onNewTaskClick
    )
}

@Composable
fun HomeContent(
    tasks: List<TaskInstanceEntity>,
    stats: DashboardStats,
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNewTaskClick:() -> Unit
) {
    val tareasPendientes = tasks.filter { it.state == TaskState.PENDING }
    val tareasPausadas = tasks.filter { it.state == TaskState.PAUSED }
    val tareasCompletadas = tasks.filter { it.state == TaskState.COMPLETED }

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
                    onFilterChange = onFilterChange
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

@Preview
@Composable
fun HomeScreenPreview_Pausada_Pendiente(){
    GestorTareasHogar_HaptikosTheme {
        HomeContent(
            tasks = listOf(
                // Tarea Pendiente
                TaskInstanceEntity(
                    id = 1,
                    task = TaskEntity(
                        id = 101,
                        title = "Limpiar la cocina",
                        points = 15,
                        members = emptyList(),
                        room = null
                    ),
                    dueDate = 1711929600000L,
                    assignedMembers = listOf(
                        MemberEntity(id = 1, name = "María", lastName = "Gómez", colorHex = "#F014A8"),
                        MemberEntity(id = 2, name = "Juan", lastName = "Pérez", colorHex = "#2979FF")
                    ),
                    state = TaskState.PENDING,
                    pausedUntil = 0L
                ),
                // Tarea Pausada
                TaskInstanceEntity(
                    id = 2,
                    task = TaskEntity(
                        id = 102,
                        title = "Lavar la ropa",
                        points = 10,
                        members = emptyList(),
                        room = null
                    ),
                    dueDate = 1712016000000L,
                    assignedMembers = listOf(
                        MemberEntity(id = 3, name = "Ana", lastName = "López", colorHex = "#9C27B0")
                    ),
                    state = TaskState.PAUSED,
                    pausedUntil = 1711929600000L
                )
            ),
            stats = TaskInstanceViewModel.DashboardStats(
                pendingTasksCount = 1,
                completedTasksCount = 0,
                totalTasks = 2,
                dailyProgress = 0f,
                userPoints = 0
            ),
            currentFilter = TaskInstanceViewModel.TaskFilter(),
            onFilterChange = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onNewTaskClick = {}
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview_Completada(){
    GestorTareasHogar_HaptikosTheme {
        HomeContent(
            tasks = listOf(
                TaskInstanceEntity(
                    id = 3,
                    task = TaskEntity(
                        id = 103,
                        title = "Comprar víveres",
                        points = 5,
                        members = emptyList(),
                        room = null
                    ),
                    dueDate = 1711843200000L,
                    assignedMembers = listOf(
                        MemberEntity(id = 1, name = "María", lastName = "Gómez", colorHex = "#F014A8")
                    ),
                    state = TaskState.COMPLETED,
                    pausedUntil = 0L
                )
            ),
            stats = TaskInstanceViewModel.DashboardStats(
                pendingTasksCount = 0,
                completedTasksCount = 1,
                totalTasks = 1,
                dailyProgress = 1f,
                userPoints = 5
            ),
            currentFilter = TaskInstanceViewModel.TaskFilter(),
            onFilterChange = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onNewTaskClick = {}
        )
    }
}