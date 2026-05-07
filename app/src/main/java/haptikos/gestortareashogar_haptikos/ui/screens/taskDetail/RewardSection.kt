package haptikos.gestortareashogar_haptikos.ui.screens.taskDetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun RewardSection(basePoints: Int, priorityBonus: Int, priorityName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF9C4).copy(alpha = 0.3f),
        border = BorderStroke(1.dp, Color(0xFFFFF176).copy(alpha = 0.5f))
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_star), null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RECOMPENSA AL COMPLETAR", fontWeight = FontWeight.ExtraBold, color = Color(0xFF827717), fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text("• Puntos base: +$basePoints pts", color = Color.DarkGray, fontSize = 13.sp)

                Text("• Bono prioridad $priorityName: +$priorityBonus pts", color = Color(0xFFE53935), fontSize = 13.sp)
            }
            // Círculo de puntos totales
            Box(Modifier.size(64.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+${basePoints + priorityBonus}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFF8A00))
                    Text("pts", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}