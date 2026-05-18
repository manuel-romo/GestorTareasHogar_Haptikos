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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.ui.components.OfflineSyncBanner
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
    onNotificationsClick: () -> Unit,
    onRewardsClick: () -> Unit
){

    val isOffline by syncViewModel.isOffline.collectAsState()

    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val hasNotifications = unreadCount > 0

    val tasksInstanceList by taskInstanceViewModel.tasks.collectAsState()
    val stats by taskInstanceViewModel.stats.collectAsState()
    val currentFilter by taskInstanceViewModel.currentFilter.collectAsState()
    val searchQuery by taskInstanceViewModel.searchQuery.collectAsState()

    val homesList by homeViewModel.allHomes.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()

    val userName by authViewModel.userName.collectAsState("")
    val hasAdminPermissions by homeViewModel.isCurrentUserCreatorOrAdmin.collectAsState()

    val userIsCreator by homeViewModel.isCurrentUserCreator.collectAsState()

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
        onRewardsClick = onRewardsClick,
        isOffline = isOffline,
        hasNotifications = hasNotifications,
        userIsCreator = userIsCreator
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
    onRewardsClick: () -> Unit,
    isOffline: Boolean,
    hasNotifications: Boolean,
    userIsCreator:Boolean
) {
    val tareasPendientes = tasks.filter { it.taskInstance.state == TaskState.PENDING }
    val tareasCompletadas = tasks.filter { it.taskInstance.state == TaskState.COMPLETED }

    val pendingTasksCount = stats.pendingTasksCount
    val totalTasks = stats.totalTasks
    val dailyProgress = stats.dailyProgress
    val userPoints = stats.userPoints

    val listState = rememberLazyListState()

    val totalVisibleTasks = tareasPendientes.size + tareasCompletadas.size

    val isCollapsed by remember {
        derivedStateOf {
            if (totalVisibleTasks <= 3) {
                false
            } else {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        OfflineSyncBanner(
            isOffline = isOffline
        )

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
            onRewardsClick = onRewardsClick,
            isCollapsed = isCollapsed,
            userIsCreator = userIsCreator
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                DaySelector(
                    selectedDay = currentFilter.selectedDay,
                    onDaySelected = { dia ->
                        onFilterChange(currentFilter.copy(selectedDay = dia))
                    }
                )
            }

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
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sparkles),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Sin tareas por mostrar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "No tienes tareas en esta categoría.\n¡Disfruta tu tiempo libre!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
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
                        onDeleteClick = { onDeleteClick(task.taskInstance) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}
