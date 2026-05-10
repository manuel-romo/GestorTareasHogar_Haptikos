package haptikos.gestortareashogar_haptikos.ui.screens.userStats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.InfoCard
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel

//Algunos modelos
data class UserStatsUiState(
    val completedCount: Int = 0,
    val effectiveness: Int = 0,
    val streakDays: Int = 0,
    val selectedRange: String = "Año",
    val homeStats: List<HomeStatsItem> = emptyList()
)

data class HomeStatsItem(
    val homeName: String,
    val completedTasks: Int,
    val totalTasks: Int,
    val progress: Float,
    val color: Color,
    val icon: String,
    val iconBgColor: Color
)

//Composable para hacer la pantalla de Stats de usuario
@Composable
fun UserStatsScreen(
    viewModel: TaskInstanceViewModel,
    userName: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.userStats.collectAsState()
    var selectedRange by remember { mutableStateOf("Año") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        //Llamada al Header para colocarlo
        UserStatsHeader(
            userName = userName,
            completedCount = uiState.completedCount,
            effectiveness = uiState.effectiveness,
            streakDays = uiState.streakDays,
            onBackClick = onBackClick
        )

        //Selector de tiempo (Año, Mes, Semana)
        TimeRangeSelector(selectedRange) { selectedRange = it }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            //Selector de Hogares
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                color = Color.White
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Todos los hogares", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${uiState.homeStats.size} miembros · Varios", color = Color.Gray, fontSize = 12.sp)
                    }
                    Icon(painterResource(R.drawable.ic_dropdown), null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Sección Mis Hogares calcada al diseño
            InfoCard(title = "Mis hogares", iconId = R.drawable.ic_home_orange) {
                uiState.homeStats.forEach { home ->
                    HomeStatItem(home)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun HomeStatItem(home: HomeStatsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(home.iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = home.icon, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = home.homeName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF2D3142)
                )
                Text(
                    text = "${(home.progress * 100).toInt()}%",
                    color = home.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { home.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = home.color,
                trackColor = Color(0xFFF0F0F0)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${home.completedTasks} de ${home.totalTasks} tareas",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TimeRangeSelector(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("Semana", "Mes", "Año")
    Row(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        options.forEach { text ->
            val isSelected = selected == text
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFFFF6D00) else Color.Transparent)
                    .clickable { onSelected(text) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserStatsScreenPreview() {
    //Mock la lista de hogares con los colores y fondos exactos de la imagen
    val mockHomeStats = listOf(
        HomeStatsItem(
            homeName = "Mi Casa",
            completedTasks = 524,
            totalTasks = 612,
            progress = 0.86f,
            color = Color(0xFFFF6D00),
            icon = "🏡",
            iconBgColor = Color(0xFFFFF3E0)
        ),
        HomeStatsItem(
            homeName = "Casa de Mamá",
            completedTasks = 368,
            totalTasks = 396,
            progress = 0.93f,
            color = Color(0xFFA143F4),
            icon = "👸",
            iconBgColor = Color(0xFFF3E5F5)
        ),
        HomeStatsItem(
            homeName = "Apartamento",
            completedTasks = 116,
            totalTasks = 216,
            progress = 0.54f,
            color = Color(0xFF2196F3),
            icon = "🏢",
            iconBgColor = Color(0xFFE3F2FD)
        )
    )
    //Mock el estado de la UI para el año
    val mockUiState = UserStatsUiState(
        completedCount = 104,
        effectiveness = 84,
        streakDays = 7,
        homeStats = mockHomeStats
    )
    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            UserStatsHeader(
                userName = "María",
                completedCount = mockUiState.completedCount,
                effectiveness = mockUiState.effectiveness,
                streakDays = mockUiState.streakDays,
                onBackClick = {}
            )
            //Selector de tiempo
            TimeRangeSelector(selected = "Año", onSelected = {})
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                //Selector de Hogares
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    color = Color.White
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🏠", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Todos los hogares", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("12 miembros · Varios", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                InfoCard(title = "Mis hogares", iconId = R.drawable.ic_home_orange) {
                    mockHomeStats.forEach { home ->
                        HomeStatItem(home)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}