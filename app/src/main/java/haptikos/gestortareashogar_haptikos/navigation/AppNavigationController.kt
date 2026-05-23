package haptikos.gestortareashogar_haptikos.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.ui.components.CustomBottomNavigation
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep1Screen
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep2Screen
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep3Screen
import haptikos.gestortareashogar_haptikos.ui.screens.formHome.FormHomeConfigurationScreen
import haptikos.gestortareashogar_haptikos.ui.screens.formTask.FormTaskScreen
import haptikos.gestortareashogar_haptikos.ui.screens.home.HomeScreen
import haptikos.gestortareashogar_haptikos.ui.screens.homeStats.HomeStatsScreen
import haptikos.gestortareashogar_haptikos.ui.screens.joinHome.JoinHomeScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.LogInScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.SignUpScreen
import haptikos.gestortareashogar_haptikos.ui.screens.userEdition.ProfileScreen
import haptikos.gestortareashogar_haptikos.ui.screens.taskDetail.TaskDetailScreen
import haptikos.gestortareashogar_haptikos.ui.screens.notifications.NotificationsScreen
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.RewardsScreen
import haptikos.gestortareashogar_haptikos.ui.screens.taskHistory.TaskHistoryScreen
import haptikos.gestortareashogar_haptikos.ui.screens.userStats.UserStatsScreen
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.LightOrange
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.NotificationViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel
import haptikos.gestortareashogar_haptikos.viewModel.ProfileViewModel
import haptikos.gestortareashogar_haptikos.viewModel.SyncViewModel

sealed class Screen(val route: String) {
    object Login: Screen("login")
    object SignUp: Screen("signUp")
    object Home: Screen("home")
    object NewTask: Screen("newTask")
    object NewPredeterminedTask: Screen("newPredeterminedTask")
    object EditTask: Screen("editTask")
    object EditPredeterminedTask: Screen("editPredeterminedTask")
    object HomeConfiguration: Screen("homeConfiguration")
    object TaskDetail: Screen("taskDetail")
    object CreateHomeStep1: Screen("createHomeStep1")
    object CreateHomeStep2: Screen("createHomeStep2")
    object CreateHomeStep3: Screen("createHomeStep3")
    object JoinHome: Screen("joinHome")
    object Profile: Screen("profile")
    object Stats: Screen("stats")
    object Notifications: Screen("notifications")
    object Rewards: Screen("rewards")
    object HomeStats: Screen("home_stats")
    object TaskHistory: Screen("task_history")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    taskInstanceViewModel: TaskInstanceViewModel,
    roomViewModel: RoomViewModel,
    memberViewModel: MemberViewModel,
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel,
    syncViewModel: SyncViewModel,
    notificationViewModel: NotificationViewModel
) {
    val navController = rememberNavController()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()
    val allMembers by memberViewModel.members.collectAsState()

    val currentMember = allMembers.find {
        it.homeId == selectedHome?.id && it.userId == userId
    }
    val canCreateTasks = when (selectedHome?.editPermission) {
        HomePermission.ALL_MEMBERS -> currentMember != null // cualquier miembro
        HomePermission.ADMINS -> currentMember?.role == MemberRole.CREATOR ||
                currentMember?.role == MemberRole.ADMIN
        HomePermission.CREATOR_ONLY -> currentMember?.role == MemberRole.CREATOR
        null -> false
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Pantallas que muestran bottom nav
    val screensWithBottomNav = listOf(
        Screen.Home.route,
        Screen.HomeStats.route,
        Screen.Stats.route,
        Screen.Profile.route
    )
    val showBottomNav = currentRoute in screensWithBottomNav


    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true -> navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            false -> navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            null -> Unit
        }
    }

    Scaffold(
        floatingActionButton = {
            if (canCreateTasks && showBottomNav) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(y = 60.dp)
                        .size(64.dp)
                        .drawBehind {
                            drawIntoCanvas { canvas ->
                                val shadowPaint = Paint()
                                shadowPaint.asFrameworkPaint().apply {
                                    isAntiAlias = true
                                    color = android.graphics.Color.argb(100, 255, 138, 0)
                                    maskFilter = android.graphics.BlurMaskFilter(
                                        20f,
                                        android.graphics.BlurMaskFilter.Blur.NORMAL
                                    )
                                }
                                canvas.drawCircle(
                                    center = center,
                                    radius = size.minDimension / 2,
                                    paint = shadowPaint
                                )
                            }
                        }
                        .background(
                            brush = Brush.verticalGradient(colors = listOf(LightOrange, BrightOrange)),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { navController.navigate(Screen.NewTask.route) }
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val stroke = 2.8.dp.toPx()
                        val mid = size / 2f
                        drawLine(Color.White, Offset(0f, mid.height), Offset(size.width, mid.height), stroke, StrokeCap.Round)
                        drawLine(Color.White, Offset(mid.width, 0f), Offset(mid.width, size.height), stroke, StrokeCap.Round)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                CustomBottomNavigation(
                    navController = navController,
                    currentRoute = currentRoute,
                    hasCenterFab = canCreateTasks
                )
            }
        }
    ) { innerPadding ->
        val adjustedPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp
        )

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(adjustedPadding)
        ) {
            composable(Screen.Login.route) {
                LogInScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    taskInstanceViewModel = taskInstanceViewModel,
                    homeViewModel = homeViewModel,
                    authViewModel = authViewModel,
                    syncViewModel = syncViewModel,
                    notificationViewModel = notificationViewModel,
                    onSettingsClick = { navController.navigate(Screen.HomeConfiguration.route) },
                    onTaskClick = { instanceId -> navController.navigate("${Screen.TaskDetail.route}/$instanceId") },
                    onDeleteClick = { taskInstanceViewModel.deleteTaskInstance(it) },
                    onNavigateToCreateHome = { navController.navigate(Screen.CreateHomeStep1.route) },
                    onNavigateToJoinHome = { navController.navigate(Screen.JoinHome.route) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                    onRewardsClick = { navController.navigate(Screen.Rewards.route) },
                    onNavigateToHistory = { navController.navigate(Screen.TaskHistory.route) }
                )
            }

            composable(
                route = "${Screen.NewTask.route}?roomId={roomId}",
                arguments = listOf(navArgument("roomId") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId")
                FormTaskScreen(
                    roomId = roomId,
                    isPredetermined = false,
                    roomViewModel = roomViewModel,
                    taskViewModel = taskViewModel,
                    memberViewModel = memberViewModel,
                    homeViewModel = homeViewModel,
                    onReturn = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Screen.EditTask.route}/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                FormTaskScreen(
                    taskId = taskId,
                    roomViewModel = roomViewModel,
                    taskViewModel = taskViewModel,
                    memberViewModel = memberViewModel,
                    homeViewModel = homeViewModel,
                    onReturn = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Screen.NewPredeterminedTask.route}/{roomId}",
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
                FormTaskScreen(
                    roomId = roomId,
                    isPredetermined = true,
                    roomViewModel = roomViewModel,
                    taskViewModel = taskViewModel,
                    memberViewModel = memberViewModel,
                    homeViewModel = homeViewModel,
                    onReturn = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Screen.EditPredeterminedTask.route}/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                FormTaskScreen(
                    taskId = taskId,
                    isPredetermined = true,
                    roomViewModel = roomViewModel,
                    taskViewModel = taskViewModel,
                    memberViewModel = memberViewModel,
                    homeViewModel = homeViewModel,
                    onReturn = { navController.popBackStack() }
                )
            }

            composable(Screen.HomeConfiguration.route) {
                FormHomeConfigurationScreen(
                    homeViewModel = homeViewModel,
                    memberViewModel = memberViewModel,
                    roomViewModel = roomViewModel,
                    taskViewModel = taskViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToEditPredeterminedTask = { taskId ->
                        navController.navigate("${Screen.EditPredeterminedTask.route}/$taskId")
                    },
                    onNavigateToNewPredeterminedTask = { roomId ->
                        navController.navigate("${Screen.NewPredeterminedTask.route}/$roomId")
                    },
                    onLeaveHome = { navController.popBackStack() },
                    onNavigateToEditTask = { taskId ->
                        navController.navigate("${Screen.EditTask.route}/$taskId")
                    },
                    onNavigateToNewTask = { roomId ->
                        if (roomId != null) {
                            navController.navigate("${Screen.NewTask.route}?roomId=$roomId")
                        } else {
                            navController.navigate(Screen.NewTask.route)
                        }
                    }
                )
            }

            composable("${Screen.TaskDetail.route}/{instanceId}") { backStackEntry ->
                val instanceId = backStackEntry.arguments?.getString("instanceId") ?: return@composable
                TaskDetailScreen(
                    instanceId = instanceId,
                    viewModel = taskInstanceViewModel,
                    homeViewModel = homeViewModel,
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateHomeStep1.route) {
                CreateHomeStep1Screen(
                    onBack = { navController.popBackStack() },
                    onNext = { name, colorHex, desc, isPrivate ->
                        navController.navigate("${Screen.CreateHomeStep2.route}/$name/$colorHex?desc=$desc&isPrivate=$isPrivate")
                    }
                )
            }

            composable(
                route = "${Screen.CreateHomeStep2.route}/{name}/{colorHex}?desc={desc}&isPrivate={isPrivate}",
                arguments = listOf(
                    navArgument("name") { type = NavType.StringType },
                    navArgument("colorHex") { type = NavType.StringType },
                    navArgument("desc") { type = NavType.StringType; defaultValue = "" },
                    navArgument("isPrivate") { type = NavType.BoolType;   defaultValue = false }
                )
            ) { backStackEntry ->
                val homeName = backStackEntry.arguments?.getString("name") ?: ""
                val colorHex = backStackEntry.arguments?.getString("colorHex") ?: "FFFF8A00"
                val homeDesc = backStackEntry.arguments?.getString("desc") ?: ""
                val isPrivate = backStackEntry.arguments?.getBoolean("isPrivate") ?: false
                CreateHomeStep2Screen(
                    homeName = homeName,
                    homeDesc = homeDesc,
                    colorHex = colorHex,
                    isPrivate = isPrivate,
                    userFullName = userName,
                    homeViewModel = homeViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { finalHomeName, inviteCode ->
                        navController.navigate("${Screen.CreateHomeStep3.route}/$finalHomeName?inviteCode=$inviteCode") {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = "${Screen.CreateHomeStep3.route}/{homeName}",
                arguments = listOf(navArgument("homeName") { type = NavType.StringType })
            ) { backStackEntry ->
                val homeName = backStackEntry.arguments?.getString("homeName") ?: ""
                CreateHomeStep3Screen(
                    homeName = homeName,
                    homeViewModel = homeViewModel,
                    syncViewModel = syncViewModel,
                    onFinishClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.JoinHome.route) {
                JoinHomeScreen(
                    viewModel = homeViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    homeViewModel = homeViewModel,
                    memberViewModel = memberViewModel,
                    taskInstanceViewModel = taskInstanceViewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToHistory = { navController.navigate(Screen.TaskHistory.route) },
                    onNavigateToRewards = { navController.navigate(Screen.Rewards.route) }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = notificationViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Rewards.route) {
                RewardsScreen(
                    taskInstanceViewModel = taskInstanceViewModel,
                    homeViewModel = homeViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.HomeStats.route) {
                HomeStatsScreen(
                    viewModel = taskInstanceViewModel,
                    homeViewModel = homeViewModel,
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Stats.route) {
                UserStatsScreen(
                    viewModel = taskInstanceViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.TaskHistory.route) {
                TaskHistoryScreen(
                    viewModel = taskInstanceViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

        }
    }
}


