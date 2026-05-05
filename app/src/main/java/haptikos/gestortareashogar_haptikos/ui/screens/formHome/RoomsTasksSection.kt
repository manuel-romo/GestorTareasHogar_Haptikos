package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskWithDetails
import haptikos.gestortareashogar_haptikos.ui.components.BiometricAuthBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.FeedbackBottomSheet
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.utils.authenticateWithBiometric
import haptikos.gestortareashogar_haptikos.utils.findFragmentActivity
import haptikos.gestortareashogar_haptikos.utils.parseHexColor
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsTasksSection(
    roomViewModel: RoomViewModel,
    taskViewModel: TaskViewModel
) {

    val context = LocalContext.current
    val fragmentActivity = context.findFragmentActivity()

    val rooms by roomViewModel.rooms.collectAsState()
    val roomToDelete by roomViewModel.roomToDelete.collectAsState()
    val showSuccessFeedback by roomViewModel.showSuccessFeedback.collectAsState()
    val biometricError by roomViewModel.biometricError.collectAsState()
    val roomToEdit by roomViewModel.roomToEdit.collectAsState()
    val taskToDelete by taskViewModel.taskToDelete.collectAsState()
    val taskFeedback by taskViewModel.taskFeedback.collectAsState()

    val allTasksWithDetails by taskViewModel.tasksWithDetails.collectAsState()

    Column {
        SectionTitleHeader(icon = R.drawable.ic_sparkles, title = "HABITACIONES Y TAREAS PREDETERMINADAS")

        // Banner informativo
        Surface(
            color = Color(0xFFF0F4FF),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_circle_information),
                    contentDescription = "Info",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Define las tareas predeterminadas de cada habitación. Estas se proponen automáticamente al asignar tareas en el hogar.",
                    color = Color(0xFF1976D2),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        rooms.forEach { room ->

            val roomTasks = allTasksWithDetails.filter { it.task.roomId == room.id }

            RoomExpandableCard(
                roomName = room.name,
                roomIcon = room.icon,
                iconBgColor = parseHexColor(room.colorHex),
                tasks = roomTasks,
                isInitiallyExpanded = false,
                onDeleteClick = { roomViewModel.initiateDeletion(room) },
                onEditClick = { roomViewModel.initiateEdit(room) },
                onDeleteTaskClick = { task ->
                    taskViewModel.initiateTaskDeletion(task)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Autenticación
    roomToDelete?.let { room ->
        BiometricAuthBottomSheet(
            title = "Eliminar \"${room.name}\"",
            description = "Para eliminar esta habitación necesitas autenticarte con huella digital.",
            warningText = "La habitación \"${room.name}\" será eliminada permanentemente.",
            confirmButtonText = "Autenticar",
            confirmButtonColor = Color(0xFFFF4B4B),
            onDismissRequest = { roomViewModel.cancelDeletion() },
            onAuthenticateClick = {
                if (fragmentActivity != null) {
                    val roomName = room.name
                    roomViewModel.cancelDeletion()

                    authenticateWithBiometric(
                        context = fragmentActivity,
                        title = "Eliminar $roomName",
                        subtitle = "Confirma tu identidad para eliminar",
                        onSuccess = {
                            roomViewModel.confirmDeletionAfterBiometrics()
                        },
                        onFailed = {
                            roomViewModel.showBiometricError("Huella no reconocida. Intenta de nuevo.")
                        },
                        onError = { errorMessage ->
                            errorMessage?.let { roomViewModel.showBiometricError(it) }
                        }
                    )
                }
            }
        )
    }

    // Éxito
    if (showSuccessFeedback) {
        FeedbackBottomSheet(
            title = "¡Autenticación exitosa!",
            subtitle = "Habitación eliminada correctamente.",
            isSuccess = true,
            onDismissRequest = { roomViewModel.dismissSuccessFeedback() }
        )

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            roomViewModel.dismissSuccessFeedback()
        }
    }

    // Error
    biometricError?.let { errorMsg ->
        FeedbackBottomSheet(
            title = "Autenticación fallida",
            subtitle = errorMsg,
            isSuccess = false,
            onDismissRequest = { roomViewModel.dismissBiometricError() }
        )
    }

    roomToEdit?.let { room ->
        // Rechazo de ocultamiento
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { sheetValue ->
                sheetValue != SheetValue.Hidden
            }
        )

        ModalBottomSheet(
            onDismissRequest = { roomViewModel.cancelEdit() },
            containerColor = Color.White,
            sheetState = sheetState
        ) {
            EditRoomBottomSheet(
                initialName = room.name,
                initialIcon = room.icon,
                initialColorHex = room.colorHex,
                onDismiss = { roomViewModel.cancelEdit() },
                onSave = { newName, newIcon, newColorHex ->
                    roomViewModel.updateRoom(newName, newIcon, newColorHex)
                }
            )
        }
    }

    taskToDelete?.let { task ->
        ConfirmDeleteBottomSheet(
            title = "¿Eliminar ${task.title}?",
            description = "Esta actividad predeterminada será eliminada...",
            iconRes = R.drawable.ic_trash,
            cancelButtonText = "Mantener",
            confirmButtonText = "Sí, eliminar",
            onDismissRequest = { taskViewModel.cancelTaskDeletion() },
            onConfirmDelete = { taskViewModel.confirmTaskDeletion() }
        )
    }

    taskFeedback?.let { feedback ->
        FeedbackBottomSheet(
            title = feedback.title,
            subtitle = feedback.subtitle,
            isSuccess = true,
            onDismissRequest = { taskViewModel.dismissFeedback() }
        )

        // Se cierra a los 2 segundos
        LaunchedEffect(feedback) {
            kotlinx.coroutines.delay(2000)
            taskViewModel.dismissFeedback()
        }
    }


}

@Composable
fun RoomExpandableCard(
    roomName: String,
    roomIcon: String,
    iconBgColor: Color,
    tasks: List<TaskWithDetails>,
    isInitiallyExpanded: Boolean = false,
    onDeleteClick:() -> Unit,
    onEditClick: () -> Unit,
    onDeleteTaskClick: (TaskEntityNew) -> Unit
) {
    var isExpanded by remember { mutableStateOf(isInitiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado de habitación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = roomIcon, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = roomName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "${tasks.size} tareas predeterminadas", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), CircleShape).clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_pencil), null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                    }
                    Box(
                        modifier = Modifier.size(32.dp).background(Color(0xFFFFEBEE), CircleShape).clickable { onDeleteClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_trash), null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                    Box(
                        modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), CircleShape).clickable { isExpanded = !isExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(if (isExpanded) R.drawable.ic_up_arrow else R.drawable.ic_dropdown),
                            null, tint = Color.Gray, modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Contenido expandido de Tareas
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                    // Se recorren las tareas
                    tasks.forEach { taskDetail ->
                        val task = taskDetail.task
                        val members = taskDetail.members

                        val isTeamTask = members.size > 1

                        val uiMembers = members.map { member ->
                            Pair(
                                member.name.take(1).uppercase(),
                                parseHexColor(member.colorHex)
                            )
                        }

                        val membersNamesText = members.joinToString(", ") { it.name }

                        DefaultTaskItem(
                            taskName = task.title,
                            day = task.suggestedDay,
                            recurrence = task.recurrence,
                            isTeam = isTeamTask,
                            members = uiMembers,
                            membersNamesText = membersNamesText,
                            onDeleteClick = { onDeleteTaskClick(task) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { /* Agregar tarea */ },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF8A00)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A00))
                    ) {
                        Icon(painterResource(R.drawable.ic_plus), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar tarea predeterminada", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultTaskItem(
    taskName: String,
    day: SuggestedDay,
    recurrence: RecurrenceType,
    isTeam: Boolean,
    members: List<Pair<String, Color>>,
    onDeleteClick: () -> Unit,
    membersNamesText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.padding(top = 6.dp).size(8.dp).background(Color(0xFFFF8A00), CircleShape))

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = taskName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

            Row(modifier = Modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                BadgeItem(text = day.displayName, icon = R.drawable.ic_calendar, bg = Color(0xFFF5F5F5), color = Color.Gray)

                if(recurrence == RecurrenceType.DIARIO) {
                    BadgeItem(text = recurrence.displayName, bg = Color(0xFFF3E5F5), color = Color(0xFF9C27B0))
                } else {
                    BadgeItem(text = recurrence.displayName, bg = Color(0xFFF5F5F5), color = Color.Gray)
                }

                if (isTeam) {
                    BadgeItem(text = "Equipo", icon = R.drawable.ic_handshake, bg = Color(0xFFFFF8E1), color = Color(0xFFFFA000))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_users), null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    members.forEach { member ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(member.second, CircleShape)
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(member.first, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (members.isEmpty()) "Sin asignar" else membersNamesText,
                    color = Color.Gray, fontSize = 12.sp
                )
            }
        }

        // Tareas
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), CircleShape).clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(id= R.drawable.ic_pencil), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
            Box(
                modifier = Modifier.size(32.dp).background(Color(0xFFFFEBEE), CircleShape).clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(id= R.drawable.ic_user_cross), contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun BadgeItem(text: String, icon: Int? = null, bg: Color, color: Color) {
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(painterResource(icon), null, tint = color, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}