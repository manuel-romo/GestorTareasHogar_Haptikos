package haptikos.gestortareashogar_haptikos.ui.screens.userEdition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
fun HomeProfileItem(home: ProfileHomeItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de hogar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(home.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${home.memberCount} miembros • ${home.taskCount} tareas", color = Color.Gray, fontSize = 12.sp)
        }

        // Badge de Rol
        val isCreator = home.role == "Creador"
        Surface(
            color = if (isCreator) Color(0xFFFFF0E0) else Color(0xFFF0F0F0),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isCreator) {
                    Text("👑 ", fontSize = 10.sp)
                } else {
                    Icon(painterResource(id = R.drawable.ic_user), contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = home.role,
                    color = if (isCreator) Color(0xFFFF8A00) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}