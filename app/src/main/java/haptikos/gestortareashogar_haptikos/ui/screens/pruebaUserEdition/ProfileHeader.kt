package haptikos.gestortareashogar_haptikos.ui.screens.pruebaUserEdition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    profilePicUrl: String?,
    homeCount: Int,
    tasksDoneCount: Int,
    onEditClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF8A00), Color(0xFFFF6A00))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradientBrush)
            .padding(top = 40.dp, bottom = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi Perfil",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            //Círculo de foto
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // TODO usuar coil
                // if (profilePicUrl != null) {
                //     AsyncImage(model = profilePicUrl, contentDescription = null, contentScale = ContentScale.Crop)
                // } else {
                Icon(painterResource(id = R.drawable.ic_user), contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                // }
            }

            // Botón de cámara
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = 4.dp, y = 4.dp)
                    .background(Color.White, CircleShape)
                    .clickable { onCameraClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Cambiar foto",
                    tint = Color(0xFFFF8A00),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre y Edición
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(userName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pencil),
                    contentDescription = "Editar",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(userEmail, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjetas de Estadísticas
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(homeCount.toString(), "Hogares")
            StatCard(tasksDoneCount.toString(), "Tareas hechas")
        }
    }
}