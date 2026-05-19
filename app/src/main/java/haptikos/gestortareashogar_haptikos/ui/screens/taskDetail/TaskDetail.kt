package haptikos.gestortareashogar_haptikos.ui.screens.taskDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.ui.components.BiometricAuthBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.FeedbackBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.GenericMultiSelectionBottomSheet
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.DarkBlue
import haptikos.gestortareashogar_haptikos.ui.theme.DarkText
import haptikos.gestortareashogar_haptikos.ui.theme.Green
import haptikos.gestortareashogar_haptikos.ui.theme.LightGreen
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.Orange
import haptikos.gestortareashogar_haptikos.ui.theme.Purple
import haptikos.gestortareashogar_haptikos.ui.theme.Red
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.utils.authenticateWithBiometric
import haptikos.gestortareashogar_haptikos.utils.findFragmentActivity
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel

@Composable
fun TaskDetailScreen(
    instanceId: String,
    viewModel: TaskInstanceViewModel,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var instanceDetails by remember { mutableStateOf<TaskInstanceWithDetails?>(null) }
    val context = LocalContext.current
    val fragmentActivity = context.findFragmentActivity()

    val allHomes by homeViewModel.allHomes.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val canEdit by viewModel.canEditCurrentInstance.collectAsState()
    val isDeletingInstance by viewModel.isDeletingInstance.collectAsState()
    val instanceDeleteError by viewModel.instanceDeleteError.collectAsState()

    var showMemberSheet by remember { mutableStateOf(false) }
    var selectedMembers by remember { mutableStateOf<Set<MemberEntityNew>>(emptySet()) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Estado de scroll para animación de contracción
    val scrollState = rememberScrollState()
    val isCollapsed by remember {
        derivedStateOf { scrollState.value > 80 }
    }

    LaunchedEffect(instanceId) {
        instanceDetails = viewModel.getInstanceWithDetailsById(instanceId)
        instanceDetails?.let {
            selectedMembers = it.assignedMembers.toSet()
            viewModel.checkEditPermission(it, allHomes, userId)
        }
    }

    val details = instanceDetails ?: return

    if (showMemberSheet) {
        GenericMultiSelectionBottomSheet(
            showSheet = true,
            onDismissRequest = { showMemberSheet = false },
            title = "Asignar miembros",
            description = "Selecciona quién realiza esta tarea esta semana",
            items = details.taskDetails.members,
            selectedItems = selectedMembers,
            onItemToggled = { member ->
                selectedMembers = if (selectedMembers.contains(member))
                    selectedMembers - member else selectedMembers + member
            },
            onConfirm = {
                viewModel.updateInstanceMembers(details.taskInstance.id, selectedMembers.map { it.id })
                showMemberSheet = false
            },
            itemBgColorHex = { it.colorHex },
            itemText = { it.name }
        )
    }

    if (isDeletingInstance) {
        BiometricAuthBottomSheet(
            title = "Eliminar tarea",
            description = "Debes autenticarte para continuar.",
            warningText = "Se eliminará \"${details.taskDetails.task.title}\" de esta semana.",
            onDismissRequest = { viewModel.cancelInstanceDeletion() },
            onAuthenticateClick = {
                if (fragmentActivity != null) {
                    authenticateWithBiometric(
                        context = fragmentActivity,
                        title = "Eliminar tarea",
                        subtitle = "Confirma tu identidad",
                        onSuccess = { viewModel.confirmInstanceDeletion(details.taskInstance, onBack) },
                        onFailed = { viewModel.showInstanceDeleteError("Huella no reconocida") },
                        onError = { viewModel.showInstanceDeleteError(it ?: "Error desconocido") }
                    )
                }
            }
        )
    }

    instanceDeleteError?.let { msg ->
        FeedbackBottomSheet(
            title = "Error",
            subtitle = msg,
            isSuccess = false,
            onDismissRequest = { viewModel.dismissInstanceDeleteError() }
        )
    }

    Scaffold(
        bottomBar = {
            BottomActionArea {
                if (details.taskInstance.state != TaskState.COMPLETED) {
                    Button(
                        onClick = {
                            viewModel.markTaskAsCompleted(details.taskInstance)
                            instanceDetails = details.copy(
                                taskInstance = details.taskInstance.copy(state = TaskState.COMPLETED)
                            )
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_check_circle), null, tint = White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Marcar como completada", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            viewModel.markTaskAsPending(details.taskInstance)
                            instanceDetails = details.copy(
                                taskInstance = details.taskInstance.copy(state = TaskState.PENDING)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MediumDarkGray),
                        border = BorderStroke(1.dp, SilverGray)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_refresh), null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reactivar tarea", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
        ) {
            // Header con animación de contracción
            TaskDetailHeader(
                task = details.taskDetails.task,
                state = details.taskInstance.state,
                isCollapsed = isCollapsed,
                canEdit = canEdit,
                menuExpanded = menuExpanded,
                onMenuExpand = { menuExpanded = it },
                onBack = onBack,
                onEditAssignment = { showMemberSheet = true },
                onDelete = { viewModel.initiateInstanceDeletion() }
            )

            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_calendar,
                        label = "Día sugerido",
                        value = details.taskDetails.task.suggestedDay.name.lowercase().replaceFirstChar { it.uppercase() },
                        iconColor = BrightOrange
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_location,
                        label = "Habitación",
                        value = details.taskDetails.room?.name ?: "General",
                        iconColor = DarkBlue
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_refresh,
                        label = "Recurrencia",
                        value = "${details.taskDetails.task.recurrence.icon} ${details.taskDetails.task.recurrence.displayName}",
                        iconColor = Purple
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_clock,
                        label = "Estado",
                        value = when (details.taskInstance.state) {
                            TaskState.PENDING -> "Pendiente"
                            TaskState.COMPLETED -> "Completada"
                        },
                        iconColor = DarkBlue
                    )
                }
                Spacer(Modifier.height(24.dp))
                RewardSection(
                    basePoints = details.taskDetails.task.points,
                    priorityBonus = details.taskDetails.task.priority.points,
                    priorityName = details.taskDetails.task.priority.title.lowercase()
                )
                Spacer(Modifier.height(24.dp))
                MembersSection(details.assignedMembers)
                Spacer(Modifier.height(24.dp))
                DescriptionSection(details.taskDetails.task.description)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TaskDetailHeader(
    task: TaskEntityNew,
    state: TaskState,
    isCollapsed: Boolean,
    canEdit: Boolean,
    menuExpanded: Boolean,
    onMenuExpand: (Boolean) -> Unit,
    onBack: () -> Unit,
    onEditAssignment: () -> Unit,
    onDelete: () -> Unit
) {
    val headerHeight by animateDpAsState(
        targetValue = if (isCollapsed) 80.dp else 200.dp,
        animationSpec = tween(300),
        label = "header_height"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isCollapsed) 0.dp else 32.dp,
        animationSpec = tween(300),
        label = "bottom_radius"
    )

    val gradientColors = if (state == TaskState.COMPLETED) {
        listOf(Green, LightGreen)
    } else {
        listOf(BrightOrange, Orange)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(
                brush = Brush.verticalGradient(colors = gradientColors),
                shape = RoundedCornerShape(bottomStart = bottomRadius, bottomEnd = bottomRadius)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Fila superior siempre visible
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(painterResource(id = R.drawable.ic_back), "Volver", tint = White, modifier = Modifier.size(22.dp))
            }
            Text(
                text = if (isCollapsed) task.title else "Detalle de tarea",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = if (isCollapsed) 16.sp else 14.sp,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Box {
                IconButton(onClick = { onMenuExpand(true) }) {
                    Icon(painterResource(id = R.drawable.ic_more_vertical), "Opciones", tint = White, modifier = Modifier.size(22.dp))
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpand(false) },
                    modifier = Modifier.background(White, RoundedCornerShape(12.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar asignación", fontWeight = FontWeight.Bold, color = if (canEdit) DarkText else MediumDarkGray) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_pencil), null, tint = if (canEdit) DarkBlue else MediumDarkGray, modifier = Modifier.size(20.dp)) },
                        enabled = canEdit,
                        onClick = { onMenuExpand(false); onEditAssignment() }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar tarea", fontWeight = FontWeight.Bold, color = if (canEdit) Red else MediumDarkGray) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_trash), null, tint = if (canEdit) Red else MediumDarkGray, modifier = Modifier.size(20.dp)) },
                        enabled = canEdit,
                        onClick = { onMenuExpand(false); onDelete() }
                    )
                }
            }
        }

        // Contenido expandido
        AnimatedVisibility(
            visible = !isCollapsed,
            modifier = Modifier.align(Alignment.BottomStart),
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                StatusBadge(state)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        color = White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier, iconId: Int, label: String, value: String, iconColor: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier.size(36.dp).background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(id = iconId), null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(label, color = MediumDarkGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Bold, color = DarkText, fontSize = 14.sp)
        }
    }
}

