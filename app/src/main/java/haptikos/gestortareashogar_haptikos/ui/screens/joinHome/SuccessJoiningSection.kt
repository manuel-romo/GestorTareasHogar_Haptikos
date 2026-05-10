package haptikos.gestortareashogar_haptikos.ui.screens.joinHome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun SuccessJoiningSection(homeName: String, memberCount: Int, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(50.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🏝️", fontSize = 40.sp)
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp).align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("¡Ya eres parte del hogar!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Te has unido a $homeName", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(16.dp)) {
                Text("🎉 ¡Bienvenido!", color = Color(0xFF4CAF50), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(16.dp)) {
                Text("$memberCount miembros ahora", color = Color(0xFF2196F3), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A68FF)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Ir al hogar", fontWeight = FontWeight.Bold)
        }
    }
}