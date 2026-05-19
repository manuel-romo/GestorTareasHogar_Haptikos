package haptikos.gestortareashogar_haptikos.ui.screens.userEdition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import haptikos.gestortareashogar_haptikos.R

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    profilePicUrl: String?,
    homeCount: Int,
    tasksDoneCount: Int,
    isUpdatingName: Boolean = false,
    isUploadingPhoto: Boolean = false,
    onNameChangeConfirmed: (String) -> Unit,
    onCameraClick: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF8A00), Color(0xFFFF6A00))
    )

    // Estados para la edición de nombre
    var isEditingName by remember { mutableStateOf(false) }
    var editableName by remember(userName) { mutableStateOf(userName) }

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
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f))
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onCameraClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!profilePicUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = profilePicUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_user),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
                if (isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (isEditingName) {
                androidx.compose.foundation.text.BasicTextField(
                    value = editableName,
                    onValueChange = {
                        if (it.length <= 25) {
                            editableName = it
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .widthIn(max = 220.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else {
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 220.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Guardar/Editar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(enabled = !isUpdatingName) {
                        if (isEditingName) {
                            onNameChangeConfirmed(editableName)
                        } else {
                            editableName = userName
                        }
                        if (!isEditingName) {
                            isEditingName = true
                        } else {
                            isEditingName = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isUpdatingName) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        painter = if (isEditingName) painterResource(R.drawable.ic_save) else painterResource(R.drawable.ic_pencil),
                        contentDescription = if (isEditingName) "Guardar" else "Editar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Correo
        Text(userEmail, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjetas de Estadísticas
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(homeCount.toString(), "Hogares")
            StatCard(tasksDoneCount.toString(), "Tareas hechas")
        }
    }
}