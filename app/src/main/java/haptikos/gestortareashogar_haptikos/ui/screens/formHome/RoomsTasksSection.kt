package haptikos.gestortareashogar_haptikos.ui.screens.formHome

import android.util.Log
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskWithDetails
import haptikos.gestortareashogar_haptikos.ui.components.BiometricAuthBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.FeedbackBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.MemberAvatar
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.DarkAmber
import haptikos.gestortareashogar_haptikos.ui.theme.DarkBlue
import haptikos.gestortareashogar_haptikos.ui.theme.LightPurple
import haptikos.gestortareashogar_haptikos.ui.theme.LightRed
import haptikos.gestortareashogar_haptikos.ui.theme.LightYellow
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.PaleBlue
import haptikos.gestortareashogar_haptikos.ui.theme.Purple
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray
import haptikos.gestortareashogar_haptikos.utils.authenticateWithBiometric
import haptikos.gestortareashogar_haptikos.utils.findFragmentActivity
import haptikos.gestortareashogar_haptikos.utils.parseHexColor
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsTasksSection(
    homeId: String,
    roomViewModel: RoomViewModel,
    taskViewModel: TaskViewModel,
    onNavigateToEditPredeterminedTask: (taskId: String) -> Unit,
    onNavigateToNewPredeterminedTask: (roomId: String) -> Unit,
    onNavigateToEditTask: (taskId: String) -> Unit,
    onNavigateToNewTask: (roomId: String?) -> Unit,
    isCreator: Boolean,
    canEditTasks: Boolean
) {

    LaunchedEffect(homeId) {
        roomViewModel.setHomeId(homeId)
    }

    LaunchedEffect(canEditTasks, isCreator) {
        Log.d("PERMISOS", "canEditTasks=$canEditTasks | isCreator=$isCreator")
    }

    val context          = LocalContext.current
    val fragmentActivity = context.findFragmentActivity()

    // Salas y tareas
    val rooms by roomViewModel.rooms.collectAsState()
    val allTasksWithDetails by taskViewModel.tasksWithDetails.collectAsState()

    // Estados de RoomViewModel
    val roomToDelete by roomViewModel.roomToDelete.collectAsState()
    val showRoomSuccess by roomViewModel.showSuccessFeedback.collectAsState()
    val biometricError by roomViewModel.biometricError.collectAsState()
    val roomToEdit by roomViewModel.roomToEdit.collectAsState()

    // Estados de TaskViewModel
    val taskToDelete by taskViewModel.taskToDelete.collectAsState()
    val taskFeedback by taskViewModel.taskFeedback.collectAsState()
    val taskToPause by taskViewModel.taskToPause.collectAsState()

    var showAddRoomSheet by remember { mutableStateOf(false) }

    Column {
        SectionTitleHeader(
            icon  = R.drawable.ic_sparkles,
            title = "HABITACIONES Y TAREAS"
        )

        // Banner informativo
        Surface(
            color = PaleBlue,
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
                    painter = painterResource(R.drawable.ic_circle_information),
                    contentDescription = "Info",
                    tint = DarkBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isCreator){
                        "Gestiona las habitaciones y sus tareas predeterminadas. También puedes crear tareas no predeterminadas desde cada habitación."
                    }
                    else if (canEditTasks) {
                        "Puedes editar y pausar las tareas asignadas a cada habitación."
                    }
                    else {
                        "Estas son las tareas del hogar organizadas por habitación."
                    },
                    color = DarkBlue,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Listado de habitaciones
        rooms.forEach { room ->

            // Separación de categorías
            val roomTasksAll = allTasksWithDetails.filter {
                it.task.roomId == room.id && it.task.homeId == homeId
            }
            val predetermined = roomTasksAll.filter { it.task.isPredetermined }
            val nonPredetermined = roomTasksAll.filter { !it.task.isPredetermined }

            RoomExpandableCard(
                room = room,
                predeterminedTasks = predetermined,
                nonPredeterminedTasks = nonPredetermined,
                isCreator = isCreator,
                canEditTasks = canEditTasks,
                onDeleteRoomClick = { roomViewModel.initiateDeletion(room) },
                onEditRoomClick = { roomViewModel.initiateEdit(room) },
                onEditPredeterminedTask = { task -> onNavigateToEditPredeterminedTask(task.id) },
                onDeletePredeterminedTask = { task -> taskViewModel.initiateTaskDeletion(task) },
                onAddPredeterminedTask = { onNavigateToNewPredeterminedTask(room.id) },
                onEditTask = { task -> onNavigateToEditTask(task.id) },
                onDeleteTask = { task -> taskViewModel.initiateTaskDeletion(task) },
                onPauseTask = { task -> taskViewModel.initiatePause(task) },
                onResumeTask = { task -> taskViewModel.resumeTask(task) },
                onAddTask = { onNavigateToNewTask(room.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        val tasksWithoutRoom = allTasksWithDetails.filter {
            it.task.homeId == homeId && it.task.roomId == null
        }

        if (tasksWithoutRoom.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitleHeader(
                icon = R.drawable.ic_sparkles,
                title = "TAREAS GENERALES"
            )
            val predetermined = tasksWithoutRoom.filter { it.task.isPredetermined }
            val nonPredetermined = tasksWithoutRoom.filter { !it.task.isPredetermined }

            RoomExpandableCard(
                room = RoomEntityNew(
                    id = "general",
                    name = "General",
                    icon = "🏠",
                    colorHex = "#9E9E9E",
                    homeId = homeId
                ),
                predeterminedTasks = predetermined,
                nonPredeterminedTasks = nonPredetermined,
                isCreator = isCreator,
                canEditTasks = canEditTasks,
                onDeleteRoomClick = {},
                onEditRoomClick = {},
                onEditPredeterminedTask = { task -> onNavigateToEditPredeterminedTask(task.id) },
                onDeletePredeterminedTask = { task -> taskViewModel.initiateTaskDeletion(task) },
                onAddPredeterminedTask = {},
                onEditTask = { task -> onNavigateToEditTask(task.id) },
                onDeleteTask = { task -> taskViewModel.initiateTaskDeletion(task) },
                onPauseTask = { task -> taskViewModel.initiatePause(task) },
                onResumeTask = { task -> taskViewModel.resumeTask(task) },
                onAddTask = { onNavigateToNewTask(null) }
            )
        }

        // Botón agregar habitación
        if (isCreator) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showAddRoomSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar habitación", fontWeight = FontWeight.Bold)
            }
        }
    }

    //  Bottom Sheets y diálogos
    // Crear habitación
    if (showAddRoomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest  = { showAddRoomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0.dp) }
        ) {
            EditRoomBottomSheet(
                initialName = "",
                initialIcon = "🛋️",
                initialColorHex = "#FF8A00",
                onDismiss = { showAddRoomSheet = false },
                onSave = { newName, newIcon, newColorHex ->
                    roomViewModel.addRoom(
                        RoomEntityNew(
                            name = newName,
                            icon = newIcon,
                            colorHex = newColorHex,
                            homeId  = homeId
                        )
                    )
                    showAddRoomSheet = false
                }
            )
        }
    }

    // Editar habitación
    roomToEdit?.let { room ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { roomViewModel.cancelEdit() },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0.dp) }
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

    // Autenticación biométrica para eliminar habitación
    roomToDelete?.let { room ->
        BiometricAuthBottomSheet(
            title = "Eliminar \"${room.name}\"",
            description = "Para eliminar esta habitación necesitas autenticarte con huella digital.",
            warningText = "La habitación \"${room.name}\" será eliminada permanentemente.",
            confirmButtonText = "Autenticar",
            confirmButtonColor = MaterialTheme.colorScheme.error,
            onDismissRequest  = { roomViewModel.cancelDeletion() },
            onAuthenticateClick = {
                if (fragmentActivity != null) {
                    val roomName = room.name
                    roomViewModel.cancelDeletion()
                    authenticateWithBiometric(
                        context  = fragmentActivity,
                        title  = "Eliminar $roomName",
                        subtitle = "Confirma tu identidad para eliminar",
                        onSuccess = { roomViewModel.confirmDeletionAfterBiometrics() },
                        onFailed = { roomViewModel.showBiometricError("Huella no reconocida. Intenta de nuevo.") },
                        onError = { msg -> msg?.let { roomViewModel.showBiometricError(it) } }
                    )
                }
            }
        )
    }

    // Éxito eliminación habitación
    if (showRoomSuccess) {
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

    // Error biométrico
    biometricError?.let { errorMsg ->
        FeedbackBottomSheet(
            title = "Autenticación fallida",
            subtitle = errorMsg,
            isSuccess = false,
            onDismissRequest = { roomViewModel.dismissBiometricError() }
        )
    }

    // Confirmación eliminar tarea
    taskToDelete?.let { task ->
        ConfirmDeleteBottomSheet(
            title = "¿Eliminar ${task.title}?",
            description = "Esta tarea será eliminada permanentemente.",
            iconRes = R.drawable.ic_trash,
            cancelButtonText = "Mantener",
            confirmButtonText = "Sí, eliminar",
            onDismissRequest = { taskViewModel.cancelTaskDeletion() },
            onConfirmDelete = { taskViewModel.confirmTaskDeletion() }
        )
    }

    // Pausa de tarea
    taskToPause?.let { task ->
        PauseTaskBottomSheet(
            taskTitle = task.title,
            onDismiss = { taskViewModel.cancelPause() },
            onConfirm = { pausedUntil -> taskViewModel.confirmPause(pausedUntil) }
        )
    }

    // Feedback de tarea, eliminación o pausa o reanudación
    taskFeedback?.let { feedback ->
        FeedbackBottomSheet(
            title            = feedback.title,
            subtitle         = feedback.subtitle,
            isSuccess        = true,
            onDismissRequest = { taskViewModel.dismissFeedback() }
        )
        LaunchedEffect(feedback) {
            kotlinx.coroutines.delay(2000)
            taskViewModel.dismissFeedback()
        }
    }
}


@Composable
fun RoomExpandableCard(
    room: RoomEntityNew,
    predeterminedTasks: List<TaskWithDetails>,
    nonPredeterminedTasks: List<TaskWithDetails>,
    isCreator: Boolean,
    canEditTasks: Boolean,
    onDeleteRoomClick: () -> Unit,
    onEditRoomClick: () -> Unit,
    onEditPredeterminedTask: (TaskEntityNew) -> Unit,
    onDeletePredeterminedTask: (TaskEntityNew) -> Unit,
    onAddPredeterminedTask: () -> Unit,
    onEditTask: (TaskEntityNew) -> Unit,
    onDeleteTask: (TaskEntityNew) -> Unit,
    onResumeTask: (TaskEntityNew) -> Unit,
    onPauseTask: (TaskEntityNew) -> Unit,
    onAddTask: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val totalTasks = predeterminedTasks.size + nonPredeterminedTasks.size
    val iconBgColor = parseHexColor(room.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment  = Alignment.Center
                ) {
                    Text(text = room.icon, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = buildString {
                            if (predeterminedTasks.isNotEmpty())
                                append("${predeterminedTasks.size} predeterminada${if (predeterminedTasks.size > 1) "s" else ""}")
                            if (predeterminedTasks.isNotEmpty() && nonPredeterminedTasks.isNotEmpty())
                                append(" · ")
                            if (nonPredeterminedTasks.isNotEmpty())
                                append("${nonPredeterminedTasks.size} adicional${if (nonPredeterminedTasks.size > 1) "es" else ""}")
                            if (totalTasks == 0) append("Sin tareas")
                        },
                        color = MediumDarkGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Editar/eliminar habitación
                    if (isCreator) {
                        SmallIconButton(
                            iconRes = R.drawable.ic_pencil,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            tint = DarkBlue,
                            onClick = onEditRoomClick
                        )
                        SmallIconButton(
                            iconRes = R.drawable.ic_trash,
                            bgColor = MaterialTheme.colorScheme.errorContainer,
                            tint = MaterialTheme.colorScheme.error,
                            onClick = onDeleteRoomClick
                        )
                    }
                    SmallIconButton(
                        iconRes = if (isExpanded) R.drawable.ic_up_arrow else R.drawable.ic_dropdown,
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        tint = MediumDarkGray,
                        onClick = { isExpanded = !isExpanded }
                    )
                }
            }

            // Contenido expandido
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )

                    // Tareas predeterminadas
                    if (isCreator || predeterminedTasks.isNotEmpty()) {
                        TaskSubsectionHeader(
                            title = "PREDETERMINADAS",
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (predeterminedTasks.isEmpty()) {
                            if (isCreator) EmptyTasksHint("Sin tareas predeterminadas.")
                        } else {
                            predeterminedTasks.forEach { detail ->
                                TaskItem(
                                    taskDetail = detail,
                                    isPredetermined = true,
                                    canEdit = isCreator,
                                    canPause = false,
                                    showPauseChip = false,
                                    onEditClick = { onEditPredeterminedTask(detail.task) },
                                    onDeleteClick = { onDeletePredeterminedTask(detail.task) },
                                    onPauseClick = {}
                                )
                            }
                        }
                        if (isCreator) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AddTaskButton(
                                label = "Agregar tarea predeterminada",
                                onClick = onAddPredeterminedTask
                            )
                        }
                    }

                    // Tareas no predeterminadas
                    Spacer(modifier = Modifier.height(if (isCreator) 20.dp else 0.dp))

                    TaskSubsectionHeader(
                        title = if (isCreator) "TAREAS ADICIONALES" else "TAREAS",
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (nonPredeterminedTasks.isEmpty()) {
                        EmptyTasksHint(
                            if (canEditTasks || isCreator)
                                "Aún no hay tareas. Crea la primera."
                            else
                                "No hay tareas para esta habitación."
                        )
                    } else {
                        nonPredeterminedTasks.forEach { detail ->
                            val isPaused = detail.task.pausedUntil != null &&
                                    detail.task.pausedUntil!! > System.currentTimeMillis()

                            TaskItem(
                                taskDetail = detail,
                                isPredetermined = false,
                                canEdit = canEditTasks || isCreator,
                                canPause = canEditTasks || isCreator,
                                showPauseChip = isPaused,
                                pausedUntil = detail.task.pausedUntil,
                                onEditClick = { onEditTask(detail.task) },
                                onDeleteClick = { onDeleteTask(detail.task) },
                                onPauseClick = {
                                    if (isPaused) {
                                        onResumeTask(detail.task)
                                    } else {
                                        onPauseTask(detail.task)
                                    }
                                }
                            )
                        }
                    }

                    // Botón agregar tarea adicional
                    if (canEditTasks || isCreator) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AddTaskButton(
                            label = "Agregar tarea",
                            onClick = onAddTask
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun TaskItem(
    taskDetail: TaskWithDetails,
    isPredetermined: Boolean,
    canEdit: Boolean,
    canPause: Boolean,
    showPauseChip: Boolean,
    pausedUntil: Long? = null,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    val task = taskDetail.task
    val members = taskDetail.members
    val isTeam = members.size > 1


    val membersText = members.joinToString(", ") { it.name }.ifEmpty { "Sin asignar" }

    // Fecha de pausa formateada
    val pausedUntilText = pausedUntil?.let {
        val sdf = SimpleDateFormat("d MMM", Locale("es", "ES"))
        "Pausada hasta ${sdf.format(Date(it))}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.Top
    ) {

        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(
                    color = if (showPauseChip) MediumDarkGray else MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Nombre
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (showPauseChip)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (isPredetermined) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Base",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Badges de programación
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgeItem(
                    text = task.suggestedDay.displayName,
                    icon = R.drawable.ic_calendar,
                    bg = MaterialTheme.colorScheme.surfaceVariant,
                    color = MediumDarkGray
                )
                if (task.recurrence == RecurrenceType.DIARIO) {
                    BadgeItem(
                        text = task.recurrence.displayName,
                        bg = LightPurple,
                        color = Purple
                    )
                } else {
                    BadgeItem(
                        text = task.recurrence.displayName,
                        bg = MaterialTheme.colorScheme.surfaceVariant,
                        color = MediumDarkGray
                    )
                }
                if (isTeam) {
                    BadgeItem(
                        text = "Equipo",
                        icon = R.drawable.ic_handshake,
                        bg = LightYellow,
                        color = DarkAmber
                    )
                }
                // Chip de pausa activa
                if (showPauseChip) {
                    BadgeItem(
                        text = pausedUntilText ?: "Pausada",
                        icon = R.drawable.ic_pause,
                        bg = MaterialTheme.colorScheme.surfaceVariant,
                        color = MediumDarkGray
                    )
                }
            }

            // Miembros asignados
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                members.forEach { member ->
                    Box(
                        modifier = Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                    ) {
                        MemberAvatar(member = member, size = 20.dp)
                    }
                }
            }
        }

        // Botones de acción
        if (canEdit || canPause) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (canEdit) {
                    SmallIconButton(
                        iconRes = R.drawable.ic_pencil,
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        tint = MediumDarkGray,
                        onClick = onEditClick
                    )
                }
                // Pausa solo para tareas no predeterminadas
                if (canPause && !isPredetermined) {
                    SmallIconButton(
                        iconRes = if (showPauseChip) R.drawable.ic_play else R.drawable.ic_pause,
                        bgColor = if (showPauseChip)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        tint = if (showPauseChip)
                            MaterialTheme.colorScheme.primary
                        else
                            MediumDarkGray,
                        onClick = onPauseClick
                    )
                }
                if (canEdit) {
                    SmallIconButton(
                        iconRes = R.drawable.ic_trash,
                        bgColor = MaterialTheme.colorScheme.errorContainer,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}




@Composable
private fun SmallIconButton(
    iconRes: Int,
    bgColor: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun TaskSubsectionHeader(title: String, color: Color) {
    Text(
        text     = title,
        color    = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
    )
}

@Composable
private fun EmptyTasksHint(text: String) {
    Text(
        text = text,
        color = MediumDarkGray,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AddTaskButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}


@Composable
fun BadgeItem(text: String, icon: Int? = null, bg: Color, color: Color) {
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
