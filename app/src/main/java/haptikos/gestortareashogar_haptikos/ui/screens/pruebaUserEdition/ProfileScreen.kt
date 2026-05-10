package haptikos.gestortareashogar_haptikos.ui.screens.pruebaUserEdition

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.screens.formTask.SectionTitle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String = "María García",
    userEmail: String = "maria@ejemplo.com",
    profilePicUrl: String? = null,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPhotoSelected: (Uri) -> Unit
) {
    val context = LocalContext.current

    // Estado para mostrar u ocultar tarjeta de selección
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // URI temporal para cámara
    val tempCameraUri = remember {
        val tempFile = File.createTempFile("camera_pic_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
    }

    // Launcher de Cámara
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            onPhotoSelected(tempCameraUri)
        }
    }

    // Launcher para pedir permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(tempCameraUri)
        } else {
            Toast.makeText(context, "Se necesita permiso para usar la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher de Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    // Datos simulados
    val userHomes = listOf(
        ProfileHomeItem("1", "Mi Casa", 4, 48, "Creador", R.drawable.ic_home),
        ProfileHomeItem("2", "Casa de Mamá", 6, 38, "Miembro", R.drawable.ic_home),
        ProfileHomeItem("3", "Apartamento", 2, 18, "Creador", R.drawable.ic_home)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        ProfileHeader(
            userName = userName,
            userEmail = userEmail,
            profilePicUrl = profilePicUrl,
            homeCount = 3,
            tasksDoneCount = 104,
            onEditClick = onEditProfileClick,
            onCameraClick = {
                // En lugar de lanzar directo, mostramos la tarjeta
                showImageSourceDialog = true
            }
        )

        // Contenido (LazyColumn)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-32).dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 32.dp, bottom = 24.dp)
        ) {

            // Sección de Mis Hogares
            item {
                SectionTitle("MIS HOGARES")
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(userHomes) { home ->
                HomeProfileItem(home)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Text(
                    text = "💡 Para editar miembros y roles, accede a ⚙️ Configuración del Hogar.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Sección de Notificaciones
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("NOTIFICACIONES")
                Spacer(modifier = Modifier.height(16.dp))
                NotificationSwitchItem("Recordatorios de tareas", "Aviso antes de que venza una tarea", true)
                NotificationSwitchItem("Tareas completadas", "Cuando un miembro completa una tarea", true)
                NotificationSwitchItem("Nuevos miembros", "Cuando alguien se une al hogar", false)
            }

            // Sección de Cuenta
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("CUENTA")
                Spacer(modifier = Modifier.height(16.dp))
                AccountActionItem("Recompensas", "Puntos, niveles e insignias", R.drawable.ic_trophy)
                AccountActionItem("Historial", "Tareas completadas", R.drawable.ic_history)
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color(0xFFFFEBEE))
                ) {
                    Icon(painterResource(R.drawable.ic_logout), null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Tarjeta de selección entre Galería o Cámara
    if (showImageSourceDialog) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceDialog = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 16.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = "Actualizar foto de perfil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Opción Cámara
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showImageSourceDialog = false
                            // Permiso de cámara
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(tempCameraUri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_menu_camera), contentDescription = "Cámara", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Tomar foto", fontSize = 16.sp)
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Opción Galería
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showImageSourceDialog = false
                            galleryLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_menu_gallery), contentDescription = "Galería", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Elegir de la galería", fontSize = 16.sp)
                }
            }
        }
    }

}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}