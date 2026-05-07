package haptikos.gestortareashogar_haptikos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import haptikos.gestortareashogar_haptikos.ui.screens.formHome.FormHomeConfigurationScreen
import haptikos.gestortareashogar_haptikos.ui.screens.formTask.FormTaskScreen
import haptikos.gestortareashogar_haptikos.ui.screens.home.HomeScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.LogInScreen
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel

sealed class Screen(val route: String){
    object Login: Screen("login")
    object Home: Screen("home")
    object NewTask: Screen("newTask")
    object NewPredeterminedTask: Screen("newPredeterminedTask")
    object EditTask: Screen("editTask")
    object EditPredeterminedTask: Screen("editPredeterminedTask")
    object HomeConfiguration: Screen("homeConfiguration")
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
                authViewModel = authViewModel
            )
        }

        composable(Screen.Home.route){
            HomeScreen(
                taskInstanceViewModel = taskInstanceViewModel,
                homeViewModel = homeViewModel,
                onNewTaskClick = {
                    navController.navigate(Screen.NewTask.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.HomeConfiguration.route)
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
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ){ backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable

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
            arguments = listOf(navArgument("roomId") { type = NavType.IntType })
        ){ backStackEntry ->
            val roomId = backStackEntry.arguments?.getInt("roomId") ?: return@composable

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
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ){ backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable

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
    }
}