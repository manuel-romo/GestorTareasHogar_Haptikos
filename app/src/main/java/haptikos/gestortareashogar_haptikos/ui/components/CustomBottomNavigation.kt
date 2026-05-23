package haptikos.gestortareashogar_haptikos.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray
        )

        // Inicio
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_home),
                    contentDescription = "Inicio",
                    modifier = Modifier.size(27.dp)
                )
            },
            label = { Text("Inicio", fontWeight = FontWeight.Medium) },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = navColors
        )

        // Hogar
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_stats_1),
                    contentDescription = "Hogar",
                    modifier = Modifier.size(27.dp)
                )
            },
            label = { Text("Hogar", fontWeight = FontWeight.Medium) },
            selected = currentRoute == Screen.HomeStats.route,
            onClick = {
                navController.navigate(Screen.HomeStats.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = navColors
        )

        // Centro
        if (hasCenterFab) {
            NavigationBarItem(
                icon = { Spacer(modifier = Modifier.size(24.dp)) },
                label = {
                    Text(
                        "Agregar",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = false,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Transparent,
                    unselectedIconColor = Color.Transparent,
                    indicatorColor = Color.Transparent,
                    selectedTextColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent
                ),
                interactionSource = remember { MutableInteractionSource() }
            )
        }


        // Mis Stats
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_stats_2),
                    contentDescription = "Mis Stats",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Mis Stats", fontWeight = FontWeight.Medium) },
            selected = currentRoute == Screen.Stats.route,
            onClick = {
                navController.navigate(Screen.Stats.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = navColors
        )

        // Perfil
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_user),
                    contentDescription = "Perfil",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Perfil", fontWeight = FontWeight.Medium) },
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = navColors
        )
    }
}