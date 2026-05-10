package haptikos.gestortareashogar_haptikos.ui.screens.userStats
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun UserStatsHeader(
    userName: String,
    completedCount: Int,
    effectiveness: Int,
    streakDays: Int,
    onBackClick: () -> Unit
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFFFF8A00), Color(0xFFFF6D00)))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(Color.White.copy(0.2f), RoundedCornerShape(12.dp))
            ) {
                Icon(painterResource(R.drawable.ic_back), null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Mis Estadísticas", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Hola, $userName 👋", color = Color.White.copy(0.8f), fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
            StatCard("Completadas", completedCount.toString(), Modifier.weight(1f))
            StatCard("Efectividad", "$effectiveness%", Modifier.weight(1f))
            StatCard("Días racha", "$streakDays 🔥", Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier) {
    Surface(
        color = Color.White.copy(0.2f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(0.8f), fontSize = 10.sp)
        }
    }
}