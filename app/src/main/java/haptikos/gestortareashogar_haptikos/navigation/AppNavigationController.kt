package haptikos.gestortareashogar_haptikos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import haptikos.gestortareashogar_haptikos.ui.screens.HomeScreen
import haptikos.gestortareashogar_haptikos.ui.screens.login.LogInScreen
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel

sealed class Screen(val route: String){
    object Login: Screen("login")
    object Home: Screen("home")
    object AddTask: Screen("addTask")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel
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

            )
        }


    }

}