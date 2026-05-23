package haptikos.gestortareashogar_haptikos.ui.screens.userEdition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.ui.components.NotificationList
import haptikos.gestortareashogar_haptikos.ui.screens.formTask.SectionTitle
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.ProfileViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    homeViewModel: HomeViewModel,
    memberViewModel: MemberViewModel,
    taskInstanceViewModel: TaskInstanceViewModel,
    context: Context = LocalContext.current,
    onNavigateToLogin: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRewards: () -> Unit
) {
    // Estados de autenticación
    val userName by authViewModel.userName.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val userEmail = "correo@ejemplo.com" // TODO: Conecta el correo real
    val profilePicUrl by profileViewModel.profilePicUrl.collectAsState()
    val userIsCreator by homeViewModel.isCurrentUserCreator.collectAsState()

    // Estados dinámicos
    val allHomes by homeViewModel.allHomes.collectAsState()
    val allMembers by memberViewModel.members.collectAsState()
    val userStats by taskInstanceViewModel.stats.collectAsState()
    val allTaskInstances by taskInstanceViewModel.allTaskInstances.collectAsState()
    val isUpdatingName by profileViewModel.isUpdatingName.collectAsState()

    val selectedHome by homeViewModel.selectedHome.collectAsState()

    val userRemindersPref by profileViewModel.notifyReminders.collectAsState(initial = true)
    val userCompletedPref by profileViewModel.notifyCompleted.collectAsState(initial = true)
    val userNewMembersPref by profileViewModel.notifyNewMembers.collectAsState(initial = true)

    val isUploadingPhoto by profileViewModel.isUploadingPhoto.collectAsState()


    // Manejo de la subida de fotos
    val onPhotoSelected: (Uri) -> Unit = { uri ->
        profileViewModel.uploadPhoto(uri, context)
    }

    // Mapeo dinámico y exacto de los hogares y roles
    val userHomes = allHomes.map { home ->
        // Filtrado de miembros que pertenecen al hogar
        val homeMembers = allMembers.filter { it.homeId == home.id }

        // Búsqueda de usuario actual
        val currentMember = homeMembers.find { it.id == userId }

        // Se determina el rol del usuario en cada hogar
        val roleString = when (currentMember?.role) {
            MemberRole.CREATOR -> "Creador"
            MemberRole.ADMIN -> "Administrador"
            else -> "Miembro"
        }

        // Tareas pendientes por hogar
        val pendingTasksCount = allTaskInstances.count { instance ->
            val belongsToHome = instance.taskDetails.room?.homeId == home.id
            val isPending = instance.taskInstance.state == TaskState.PENDING

            belongsToHome && isPending
        }


        ProfileHomeItem(
            id = home.id,
            name = home.name,
            memberCount = homeMembers.size,
            taskCount = pendingTasksCount,
            role = roleString,
            iconRes = R.drawable.ic_home
        )
    }

    ProfileContent(
        userName = userName,
        userEmail = userEmail,
        userIsCreator = userIsCreator,
        profilePicUrl = profilePicUrl,
        userHomes = userHomes,
        totalHomesCount = allHomes.size,
        tasksDoneCount = userStats.completedTasksCount,
        context = context,
        onLogoutClick = {
            authViewModel.logout()
            onNavigateToLogin()
        },
        onNameChanged = { newName ->
            profileViewModel.updateUserName(newName)
        },
        onPhotoSelected = onPhotoSelected,
        isUpdatingName = isUpdatingName,
        selectedHome = selectedHome,
        userRemindersPref = userRemindersPref,
        userCompletedPref = userCompletedPref,
        userNewMembersPref = userNewMembersPref,
        isUploadingPhoto = isUploadingPhoto,
        onRemindersChange = { newValue ->
            profileViewModel.updateNotificationPreference("reminders", newValue)
        },
        onCompletedChange = { newValue ->
            profileViewModel.updateNotificationPreference("completed", newValue)
        },
        onNewMembersChange = { newValue ->
            profileViewModel.updateNotificationPreference("newMembers", newValue)
        },
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToRewards = onNavigateToRewards
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    userName: String,
    userEmail: String,
    userIsCreator: Boolean,
    profilePicUrl: String?,
    userHomes: List<ProfileHomeItem>,
    totalHomesCount: Int,
    tasksDoneCount: Int,
    context: Context,
    onLogoutClick: () -> Unit,
    onNameChanged: (String) -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    isUpdatingName: Boolean,
    selectedHome: HomeEntityNew?,
    userRemindersPref: Boolean,
    userCompletedPref: Boolean,
    userNewMembersPref: Boolean,
    isUploadingPhoto: Boolean,
    onRemindersChange: (Boolean) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onNewMembersChange: (Boolean) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRewards: () -> Unit

    ) {
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val tempCameraUri = remember {
        val tempFile = File.createTempFile("camera_pic_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) onPhotoSelected(tempCameraUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) cameraLauncher.launch(tempCameraUri)
        else Toast.makeText(context, "Se necesita permiso para usar la cámara", Toast.LENGTH_SHORT).show()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPhotoSelected(uri)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {

        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                ProfileHeader(
                    userName = userName,
                    userEmail = userEmail,
                    profilePicUrl = profilePicUrl,
                    homeCount = totalHomesCount,
                    tasksDoneCount = tasksDoneCount,
                    isUpdatingName = isUpdatingName,
                    isUploadingPhoto = isUploadingPhoto,
                    onNameChangeConfirmed = onNameChanged,
                    onCameraClick = { showImageSourceDialog = true }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color.White)
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Sección de Mis Hogares
                SectionTitle("MIS HOGARES")
                Spacer(modifier = Modifier.height(16.dp))

                if (userHomes.isEmpty()) {
                    Text("Aún no perteneces a ningún hogar.", color = Color.Gray)
                } else {
                    var homesExpanded by remember { mutableStateOf(false) }
                    val visibleHomes = if (homesExpanded) userHomes else userHomes.take(2)

                    visibleHomes.forEach { home ->
                        HomeProfileItem(home)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (userHomes.size > 2) {
                        TextButton(
                            onClick = { homesExpanded = !homesExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (homesExpanded) "Ver menos" else "Ver ${userHomes.size - 2} más",
                                color = Color(0xFFFF8A00),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Text(
                    text = "💡 Para editar miembros y roles, accede a ⚙️ Configuración del Hogar.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Sección de Notificaciones
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("NOTIFICACIONES PERSONALES")
                Spacer(modifier = Modifier.height(16.dp))


                val isConfigForced = selectedHome?.forceSettings == true
                val isHomeNotificationsOff = selectedHome?.notifyAllMembers == false

                if (selectedHome != null) {
                    if (isConfigForced) {
                        val mensaje = when {
                            userIsCreator && isHomeNotificationsOff ->
                                "🔒 Has desactivado todas las notificaciones para este hogar."
                            userIsCreator ->
                                "🔒 Has impuesto tu configuración a los miembros."
                            isHomeNotificationsOff ->
                                "🔒 El creador ha desactivado todas las notificaciones para este hogar."
                            else ->
                                "🔒 El creador ha impuesto una configuración específica y no puedes modificarla."
                        }
                        Text(
                            text = mensaje,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                    ) {
                        NotificationList(
                            notifyTaskReminders = if (isConfigForced) {
                                selectedHome.notifyAllMembers && selectedHome.notifyTaskReminders
                            } else userRemindersPref,

                            notifyTaskCompleted = if (isConfigForced) {
                                selectedHome.notifyAllMembers && selectedHome.notifyTaskCompleted
                            } else userCompletedPref,

                            notifyNewMembers = if (isConfigForced) {
                                selectedHome.notifyAllMembers && selectedHome.notifyNewMembers
                            } else userNewMembersPref,

                            onRemindersChange = onRemindersChange,
                            onCompletedChange = onCompletedChange,
                            onNewMembersChange = onNewMembersChange,
                            isEnabled = !isConfigForced
                        )
                    }
                }

                // Sección de Cuenta
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("CUENTA")
                Spacer(modifier = Modifier.height(16.dp))
                AccountActionItem(
                    "Recompensas", "Puntos, niveles e insignias", R.drawable.ic_trophy,
                    onClick = onNavigateToRewards
                )
                AccountActionItem(
                    "Historial", "Tareas completadas", R.drawable.ic_history,
                    onClick = onNavigateToHistory
                )
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