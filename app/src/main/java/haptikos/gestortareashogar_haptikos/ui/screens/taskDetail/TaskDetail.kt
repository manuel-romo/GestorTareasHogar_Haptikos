package haptikos.gestortareashogar_haptikos.ui.screens.taskDetail

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
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.ui.components.BiometricAuthBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.FeedbackBottomSheet
import haptikos.gestortareashogar_haptikos.ui.components.GenericMultiSelectionBottomSheet
import haptikos.gestortareashogar_haptikos.ui.theme.CompletedGreen
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
    var menuExpanded by remember { mutableStateOf(false) } // ← declarado antes de usarse

    LaunchedEffect(instanceId) {
        instanceDetails = viewModel.getInstanceWithDetailsById(instanceId)
        instanceDetails?.let {
            selectedMembers = it.assignedMembers.toSet()
            viewModel.checkEditPermission(it, allHomes, userId)
        }
    }

    val details = instanceDetails ?: return

    // Selección de miembros
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
                viewModel.updateInstanceMembers(
                    details.taskInstance.id,
                    selectedMembers.map { it.id }
                )
                showMemberSheet = false
            },
            itemBgColorHex = { it.colorHex },
            itemText = { it.name }
        )
    }

    // Diálogo de eliminación
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
                        onSuccess = {
                            viewModel.confirmInstanceDeletion(details.taskInstance, onBack)
                        },
                        onFailed = {
                            viewModel.showInstanceDeleteError("Huella no reconocida") // ← método público
                        },
                        onError = {
                            viewModel.showInstanceDeleteError(it ?: "Error desconocido")
                        }
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

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF8A00), Color(0xFFFFAB40))
    )

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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_check_circle), null,
                                tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Marcar como completada",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_refresh), null,
                                modifier = Modifier.size(18.dp))
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(painterResource(id = R.drawable.ic_back),
                                contentDescription = "Volver",
                                tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Text("Detalle de tarea", color = Color.White, fontWeight = FontWeight.Bold)
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(painterResource(id = R.drawable.ic_more_vertical),
                                    contentDescription = "Opciones",
                                    tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar asignación",
                                        fontWeight = FontWeight.Bold,
                                        color = if (canEdit) Color.Black else Color.Gray) },
                                    leadingIcon = {
                                        Icon(painterResource(R.drawable.ic_pencil), null,
                                            tint = if (canEdit) Color(0xFF4285F4) else Color.Gray,
                                            modifier = Modifier.size(20.dp))
                                    },
                                    enabled = canEdit,
                                    onClick = {
                                        menuExpanded = false
                                        showMemberSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar tarea",
                                        fontWeight = FontWeight.Bold,
                                        color = if (canEdit) Color(0xFFE53935) else Color.Gray) },
                                    leadingIcon = {
                                        Icon(painterResource(R.drawable.ic_trash), null,
                                            tint = if (canEdit) Color(0xFFE53935) else Color.Gray,
                                            modifier = Modifier.size(20.dp))
                                    },
                                    enabled = canEdit,
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.initiateInstanceDeletion()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    StatusBadge(details.taskInstance.state)
                    Spacer(Modifier.height(12.dp))
                    Text(text = details.taskDetails.task.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    if (details.taskDetails.task.description.isNotEmpty()) {
                        Text(text = details.taskDetails.task.description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(modifier = Modifier.weight(1f), iconId = R.drawable.ic_calendar,
                        label = "Día sugerido",
                        value = details.taskDetails.task.suggestedDay.name
                            .lowercase().replaceFirstChar { it.uppercase() },
                        iconColor = Color(0xFFFF8A00))
                    InfoCard(modifier = Modifier.weight(1f), iconId = R.drawable.ic_location,
                        label = "Habitación",
                        value = details.taskDetails.room?.name ?: "General",
                        iconColor = Color(0xFF4285F4))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(modifier = Modifier.weight(1f), iconId = R.drawable.ic_refresh,
                        label = "Recurrencia",
                        value = "${details.taskDetails.task.recurrence.icon} ${details.taskDetails.task.recurrence.displayName}",
                        iconColor = Color(0xFF9C27B0))
                    InfoCard(modifier = Modifier.weight(1f), iconId = R.drawable.ic_clock,
                        label = "Estado",
                        value = when (details.taskInstance.state) {
                            TaskState.PENDING -> "Pendiente"
                            TaskState.COMPLETED -> "Completada"
                        },
                        iconColor = Color(0xFF03A9F4))
                }
                Spacer(Modifier.height(24.dp))
                RewardSection(basePoints = details.taskDetails.task.points,
                    priorityBonus = details.taskDetails.task.priority.points,
                    priorityName = details.taskDetails.task.priority.title.lowercase())
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
fun InfoCard(modifier: Modifier, iconId: Int, label: String, value: String, iconColor: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F9FA)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier.size(36.dp).background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
        }
    }
}

