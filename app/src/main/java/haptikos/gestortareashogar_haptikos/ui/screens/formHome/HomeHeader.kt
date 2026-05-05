package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun HomeConfigurationHeader(
    homeName: String,
    inviteCode: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFF8A00),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Atrás
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(
                    id = R.drawable.ic_back),
                    contentDescription = "Atrás",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Configuración", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Text(text = homeName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Botón Casa Transparente
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Inicio", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Código de Invitación
        Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Código de invitación", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    Text(inviteCode, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(painterResource(id = R.drawable.ic_copy), contentDescription = "Copiar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Box(modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(painterResource(id = R.drawable.ic_refresh), contentDescription = "Generar otro", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}