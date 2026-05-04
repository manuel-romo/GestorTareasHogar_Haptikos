package haptikos.gestortareashogar_haptikos.ui.screens.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState

@Composable
fun RewardsScreen(
    taskInstanceViewModel: TaskInstanceViewModel,
    onBackClick: () -> Unit
) {
    val stats by taskInstanceViewModel.stats.collectAsState()
    val tasks by taskInstanceViewModel.tasks.collectAsState()

    //Filtro para las tareas completadas para la sección de "Puntos recientes"
    val completedTasks = tasks.filter { it.taskInstance.state == TaskState.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        //Header con puntos del usuario
        RewardsHeader(
            points = stats.userPoints,
            progress = stats.dailyProgress,
            onBackClick = onBackClick
        )

        //Selector de secciones (estático de mientras)
        RewardsTabSelector()

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            InfoCard(title = "¿Cómo ganar puntos?", iconId = R.drawable.ic_bolt) {
                RewardEarningRow(
                    "Tarea completada",
                    "+10 pts",
                    Color(0xFFE8F5E9),
                    Color(0xFF4CAF50)
                )
                RewardEarningRow(
                    "Tarea de alta prioridad",
                    "+5 pts",
                    Color(0xFFFFF3E0),
                    Color(0xFFFF9800)
                )
                RewardEarningRow(
                    "Completar el día entero",
                    "+25 pts",
                    Color(0xFFFFF9C4),
                    Color(0xFFFBC02D)
                )
                RewardEarningRow(
                    "Racha semanal (7 días)",
                    "+25 pts",
                    Color(0xFFFFEBEE),
                    Color(0xFFE91E63)
                )
                RewardEarningRow(
                    "Invitar un miembro",
                    "+15 pts",
                    Color(0xFFE3F2FD),
                    Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            //Sección del historial
            InfoCard(title = "Puntos recientes", iconId = R.drawable.ic_star) {
                if (completedTasks.isEmpty()) {
                    Text("Aún no has completado tareas hoy", color = Color.Gray, fontSize = 14.sp)
                } else {
                    completedTasks.take(5).forEach { taskWithDetails ->
                        RecentPointRow(
                            title = taskWithDetails.task.title,
                            points = "+${taskWithDetails.task.points}",
                            iconId = R.drawable.ic_check_circle
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RewardEarningRow(
    title: String,
    points: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 14.sp, color = Color.DarkGray)

        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = points,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

//Función composable extra para crear una card con información
@Composable
fun InfoCard(
    title: String,
    iconId: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

//Función composable para el selector de pestañas
@Composable
fun RewardsTabSelector() {
    val tabs = listOf("Resumen", "Insignias", "Ranking", "Retos")
    var selectedTab by remember { mutableStateOf("Resumen") }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(tabs) { tab ->
            val isSelected = tab == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable() { selectedTab = tab }
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) Color(0xFFFF6D00) else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(16.dp)
                            .height(3.dp)
                            .background(Color(0xFFFF6D00), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentPointRow(title: String, points: String, iconId: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(iconId), null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        Text(points, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

//Preview de la pantalla de recompensas (todo esto es de prueba)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RewardsScreenPreview() {
    //Crear un estado de estadísticas de prueba
    val mockStats = TaskInstanceViewModel.DashboardStats(
        pendingTasksCount = 5,
        completedTasksCount = 3,
        totalTasks = 8,
        dailyProgress = 0.36f,
        userPoints = 680
    )

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F9FA)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                //Header con datos de prueba
                RewardsHeader(
                    points = mockStats.userPoints,
                    progress = mockStats.dailyProgress,
                    onBackClick = {}
                )

                RewardsTabSelector()

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    InfoCard(title = "¿Cómo ganar puntos?", iconId = R.drawable.ic_bolt) {
                        RewardEarningRow(
                            "Tarea completada",
                            "+10 pts",
                            Color(0xFFE8F5E9),
                            Color(0xFF4CAF50)
                        )
                        RewardEarningRow(
                            "Tarea de alta prioridad",
                            "+5 pts",
                            Color(0xFFFFF3E0),
                            Color(0xFFFF9800)
                        )
                        RewardEarningRow(
                            "Completar el día entero",
                            "+25 pts",
                            Color(0xFFFFF9C4),
                            Color(0xFFFBC02D)
                        )
                        RewardEarningRow(
                            "Racha semanal (7 días)",
                            "+25 pts",
                            Color(0xFFFFEBEE),
                            Color(0xFFE91E63)
                        )
                        RewardEarningRow(
                            "Invitar un miembro",
                            "+15 pts",
                            Color(0xFFE3F2FD),
                            Color(0xFF2196F3)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    //Sección de puntos recientes
                    InfoCard(title = "Puntos recientes", iconId = R.drawable.ic_star) {
                        RecentPointRow(
                            title = "Limpiar la cocina",
                            points = "+15",
                            iconId = R.drawable.ic_check_circle
                        )
                        RecentPointRow(
                            title = "Comprar víveres",
                            points = "+10",
                            iconId = R.drawable.ic_check_circle
                        )
                        RecentPointRow(
                            title = "Racha de 7 días 🔥",
                            points = "+25",
                            iconId = R.drawable.ic_bolt
                        )
                        RecentPointRow(
                            title = "Lavar la ropa",
                            points = "+10",
                            iconId = R.drawable.ic_check_circle
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}