package haptikos.gestortareashogar_haptikos.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.ui.components.CustomBottomNavigation
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep1Screen
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep2Screen
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep3Screen
import haptikos.gestortareashogar_haptikos.ui.screens.formHome.FormHomeConfigurationScreen
import haptikos.gestortareashogar_haptikos.ui.screens.formTask.FormTaskScreen
import haptikos.gestortareashogar_haptikos.ui.screens.home.HomeScreen
import haptikos.gestortareashogar_haptikos.ui.screens.joinHome.JoinHomeScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.LogInScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.SignUpScreen
import haptikos.gestortareashogar_haptikos.ui.screens.pruebaUserEdition.ProfileScreen
import haptikos.gestortareashogar_haptikos.ui.screens.taskDetail.TaskDetailScreen
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel

sealed class Screen(val route: String){
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
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    taskInstanceViewModel: TaskInstanceViewModel,
    roomViewModel: RoomViewModel,
    memberViewModel: MemberViewModel,
    homeViewModel: HomeViewModel
) {
    val navController = rememberNavController()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userName by authViewModel.userName.collectAsState()

    val userId by authViewModel.userId.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()
    val allMembers by memberViewModel.members.collectAsState()

    // Se busca el usuario como miembro que coincida en la lista seleccionada y en el hogar actual
    val currentMember = allMembers.find {
        it.homeId == selectedHome?.id && it.id == userId
    }

    val canCreateTasks = currentMember?.role == MemberRole.CREATOR || currentMember?.role == MemberRole.ADMIN

    // Para saber en que pantalla se está acutalmente
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Pantallas en las que se debe mostrar el menú de navegación
    val screensWithBottomNav = listOf(
        Screen.Home.route,
        Screen.Profile.route,
        Screen.Stats.route
    )
    val showBottomNav = currentRoute in screensWithBottomNav

    LaunchedEffect(
        isLoggedIn
    ) {
        if (!isLoggedIn) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }

        } else {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        // Botón flotante
        floatingActionButton = {

            if (canCreateTasks && showBottomNav) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.NewTask.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp).offset(y = (65).dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Agregar",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
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

        // Se ajusta el padding para no mostrar un espacio en las pantallas que no muestran el menú
        // de navegación ingerior.
        val adjustedPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp
        )

        NavHost(
            navController = navController,
            startDestination = if(isLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(adjustedPadding)
        ) {

            composable(Screen.Login.route) {
                LogInScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    taskInstanceViewModel = taskInstanceViewModel,
                    homeViewModel = homeViewModel,
                    onSettingsClick = { navController.navigate(Screen.HomeConfiguration.route) },
                    onTaskClick = { instanceId -> navController.navigate("${Screen.TaskDetail.route}/$instanceId") },
                    onStatusClick = { taskInstance ->
                        taskInstanceViewModel.markTaskAsCompleted(
                            taskInstance
                        )
                    },
                    onDeleteClick = { taskInstance ->
                        taskInstanceViewModel.deleteTaskInstance(
                            taskInstance
                        )
                    },
                    onNavigateToCreateHome = { navController.navigate(Screen.CreateHomeStep1.route) },
                    onNavigateToJoinHome = { navController.navigate(Screen.JoinHome.route) }
                )
            }

            composable(Screen.NewTask.route) {
                FormTaskScreen(
                    roomViewModel = roomViewModel,
                    taskViewModel = taskViewModel,
                    memberViewModel = memberViewModel,
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
                    onReturn = {
                        navController.popBackStack()
                    }
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
                    onReturn = {
                        navController.popBackStack()
                    }
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
                    onReturn = {
                        navController.popBackStack()
                    }
                )
            }


            composable(Screen.HomeConfiguration.route) {
                FormHomeConfigurationScreen(
                    homeViewModel = homeViewModel,
                    memberViewModel = memberViewModel,
                    roomViewModel = roomViewModel,
                    taskViewModel = taskViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEditPredeterminedTask = { taskId ->
                        navController.navigate("${Screen.EditPredeterminedTask.route}/$taskId")
                    },
                    onNavigateToNewPredeterminedTask = { roomId ->
                        navController.navigate("${Screen.NewPredeterminedTask.route}/$roomId")
                    }
                )
            }

            composable(
                route = "${Screen.TaskDetail.route}/{instanceId}",
                arguments = listOf(navArgument("instanceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val instanceId = backStackEntry.arguments?.getString("instanceId") ?: return@composable

                TaskDetailScreen(
                    instanceId = instanceId,
                    viewModel = taskInstanceViewModel,
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
                    navArgument("isPrivate") { type = NavType.BoolType; defaultValue = false }
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
                route = "${Screen.CreateHomeStep3.route}/{homeName}?inviteCode={inviteCode}",
                arguments = listOf(
                    navArgument("homeName") { type = NavType.StringType },
                    navArgument("inviteCode") {
                        type = NavType.StringType; nullable = true; defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val homeName = backStackEntry.arguments?.getString("homeName") ?: "Mi Hogar"
                val inviteCode = backStackEntry.arguments?.getString("inviteCode")

                CreateHomeStep3Screen(
                    homeName = homeName,
                    inviteCode = inviteCode,
                    invitedUsers = emptyList(),
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
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    userName = userName,
                    onLogoutClick = { /* Lógica para cerrar sesión */ },
                    onEditProfileClick = { },
                    onPhotoSelected = { uri ->
                        /* Aquí mandaremos la foto al servidor más adelante */
                    }
                )
            }

        }
    }

}