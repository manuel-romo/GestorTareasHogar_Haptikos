package haptikos.gestortareashogar_haptikos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep1Screen
import haptikos.gestortareashogar_haptikos.ui.screens.createHome.CreateHomeStep2Screen
import haptikos.gestortareashogar_haptikos.ui.screens.formHome.FormHomeConfigurationScreen
import haptikos.gestortareashogar_haptikos.ui.screens.formTask.FormTaskScreen
import haptikos.gestortareashogar_haptikos.ui.screens.home.HomeScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.LogInScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.SignUpScreen
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
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    taskInstanceViewModel: TaskInstanceViewModel,
    roomViewModel: RoomViewModel,
    memberViewModel: MemberViewModel,
    homeViewModel: HomeViewModel
){
    val navController = rememberNavController()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userName by authViewModel.userName.collectAsState()

    LaunchedEffect(
        isLoggedIn
    ) {
        if(!isLoggedIn){
            navController.navigate(Screen.Login.route){
                popUpTo(0){inclusive = true}
            }

        } else{
            navController.navigate(Screen.Home.route){
                popUpTo(Screen.Login.route){inclusive = true}
            }
        }
    }

    NavHost(navController = navController, startDestination = if(isLoggedIn) Screen.Home.route else Screen.Login.route){

        composable(Screen.Login.route){
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
                onNewTaskClick = { navController.navigate(Screen.NewTask.route) },
                onSettingsClick = { navController.navigate(Screen.HomeConfiguration.route) },
                onTaskClick = { instanceId ->
                    navController.navigate("${Screen.TaskDetail.route}/$instanceId")
                },
                onStatusClick = { taskInstance -> taskInstanceViewModel.markTaskAsCompleted(taskInstance) },
                onDeleteClick = { taskInstance -> taskInstanceViewModel.deleteTaskInstance(taskInstance) },
                onNavigateToCreateHome = {
                    navController.navigate(Screen.CreateHomeStep1.route)
                }
            )
        }

        composable(Screen.NewTask.route){
            FormTaskScreen(
                roomViewModel = roomViewModel,
                taskViewModel = taskViewModel,
                memberViewModel = memberViewModel,
                onReturn = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Screen.EditTask.route}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ){ backStackEntry ->
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
        ){ backStackEntry ->
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
        ){ backStackEntry ->
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


        composable(Screen.HomeConfiguration.route){
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

        composable(Screen.CreateHomeStep1.route){
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
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }


    }
}