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
import haptikos.gestortareashogar_haptikos.ui.theme.Amber
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.DarkAmber
import haptikos.gestortareashogar_haptikos.ui.theme.DarkText
import haptikos.gestortareashogar_haptikos.ui.theme.LightAmber
import haptikos.gestortareashogar_haptikos.ui.theme.LightYellow
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.Orange
import haptikos.gestortareashogar_haptikos.ui.theme.White

@Composable
fun RewardSection(basePoints: Int, priorityBonus: Int, priorityName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LightYellow,
        border = BorderStroke(1.dp, LightAmber)
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_star), null, tint = Amber, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RECOMPENSA AL COMPLETAR", fontWeight = FontWeight.ExtraBold, color = DarkAmber, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text("• Puntos por completar: +$basePoints pts", color = DarkText, fontSize = 13.sp)
                Text("• Bono prioridad $priorityName: +$priorityBonus pts", color = Orange, fontSize = 13.sp)
            }
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier.size(64.dp).background(White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+${basePoints + priorityBonus}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BrightOrange)
                    Text("pts", fontSize = 10.sp, color = MediumDarkGray)
                }
            }
        }
    }
}