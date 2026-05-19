package haptikos.gestortareashogar_haptikos.ui.screens.homeStats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.Orange
import haptikos.gestortareashogar_haptikos.ui.theme.White

@Composable
fun HomeStatsHeader(homeName: String = "Mi Casa", percentage: String = "85", onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Brush.verticalGradient(listOf(BrightOrange, Orange)))
            .padding(horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Row(
                modifier = Modifier.clickable { onBackClick() }.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(id = R.drawable.ic_home_orange), null, tint = White, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(homeName, color = White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(35.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Estadísticas", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                    Text("Rendimiento del hogar", color = White.copy(alpha = 0.9f), fontSize = 13.sp)
                }
                Surface(color = White.copy(alpha = 0.22f), shape = RoundedCornerShape(24.dp), modifier = Modifier.size(width = 115.dp, height = 80.dp)) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$percentage%", color = White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 38.sp)
                        Text("completado", color = White.copy(alpha = 0.9f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeStatsHeaderPreview() {
    HomeStatsHeader(onBackClick = {})
}