package haptikos.gestortareashogar_haptikos.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.navigation.Screen

@Composable
fun CustomBottomNavigation(
    navController: NavController,
    currentRoute: String?,
    hasCenterFab: Boolean
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // INICIO
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Inicio",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Inicio") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        // HOGAR (Aún no tienes esta pantalla en Screen, la dejamos pendiente)
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stats_1),
                    contentDescription = "Hogar",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Hogar") },
            selected = false, // Cambiar cuando tengas la ruta
            onClick = { /* TODO: Navegar a la pantalla del Hogar */ }
        )

        if (hasCenterFab) {
            Spacer(Modifier.weight(1f))
        }

        // STATS
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stats_2),
                    contentDescription = "Stats",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Stats") },
            selected = currentRoute == Screen.Stats.route,
            onClick = {
                navController.navigate(Screen.Stats.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        // PERFIL
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Perfil",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Perfil") },
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    // Esta configuración evita que se abran muchas pantallas iguales al dar varios clics
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}