package haptikos.gestortareashogar_haptikos.ui.screens.userStats
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .background(Color.White.copy(0.2f), CircleShape)
            ) {
                Icon(
                    painterResource(R.drawable.ic_back),
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                "Mis Estadísticas",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
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