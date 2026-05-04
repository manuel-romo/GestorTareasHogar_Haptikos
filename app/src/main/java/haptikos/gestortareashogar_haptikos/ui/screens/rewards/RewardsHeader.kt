package haptikos.gestortareashogar_haptikos.ui.screens.rewards

import haptikos.gestortareashogar_haptikos.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RewardsHeader(points: Int, progress: Float, onBackClick: () -> Unit) {
    val orangeGradient = Brush.verticalGradient(listOf(Color(0xFFFF8A00), Color(0xFFFF6D00)))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(orangeGradient)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(0.2f), CircleShape)
                    .clickable{ onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_back), null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text("Recompensas", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Tu progreso y logros", color = Color.White.copy(0.8f), fontSize = 14.sp)
            }

            Surface(color = Color(0xFFFFD54F), shape = RoundedCornerShape(16.dp)) {
                Text("$points pts", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))

        //Tarjeta de nivel del usuario
        Surface(
            color = Color.White.copy(0.15f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠", fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Nivel 3", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Text("Organizado", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(0.3f)
                )
                Text("${(progress * 100).toInt()}% del objetivo diario", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}