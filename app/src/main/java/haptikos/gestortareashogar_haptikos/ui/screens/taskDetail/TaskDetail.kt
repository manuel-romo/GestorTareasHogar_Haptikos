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
import haptikos.gestortareashogar_haptikos.ui.theme.CompletedGreen

@Composable
fun TaskDetailScreen(
    instanceId: Int,
    viewModel: TaskInstanceViewModel,
    onBack: () -> Unit
) {
    var instanceDetails by remember { mutableStateOf<TaskInstanceWithDetails?>(null) }

    LaunchedEffect(instanceId) {
        instanceDetails = viewModel.getInstanceWithDetailsById(instanceId)
    }

    val details = instanceDetails ?: return

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF8A00), Color(0xFFFFAB40))
    )

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomActionArea {
                if (details.taskInstance.state != TaskState.COMPLETED) {
                    // Botón para Completar
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
                            Icon(painterResource(R.drawable.ic_check_circle), null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Marcar como completada", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                } else {
                    // Botón para reactivar
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Encabezado
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
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text("Detalle de tarea", color = Color.White, fontWeight = FontWeight.Bold)
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_more_vertical),
                                    contentDescription = "Opciones",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // El menú desplegable
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                            ) {
                                // Editar
                                DropdownMenuItem(
                                    text = { Text("Editar tarea", fontWeight = FontWeight.Bold, color = Color.Black) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_pencil),
                                            contentDescription = null,
                                            tint = Color(0xFF4285F4),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        // TODO: Editar
                                    }
                                )

                                // Opción de Pausar/Reanudar
                                val isPaused = details.taskInstance.state == TaskState.PAUSED
                                DropdownMenuItem(
                                    text = { Text(if (isPaused) "Reanudar tarea" else "Pausar tarea", fontWeight = FontWeight.Bold, color = Color.Black) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_pause),
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.toggleTaskPause(details.taskInstance)
                                    }
                                )

                                // Opción de Eliminar
                                DropdownMenuItem(
                                    text = { Text("Eliminar tarea", fontWeight = FontWeight.Bold, color = Color(0xFFE53935)) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_trash),
                                            contentDescription = null,
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        // TODO: Eliminar
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    StatusBadge(details.taskInstance.state)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = details.task.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    // Descripción
                    if (details.task.description.isNotEmpty()) {
                        Text(
                            text = details.task.description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Grid de información
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_calendar,
                        label = "Día sugerido",
                        value = details.task.suggestedDay.name.lowercase().replaceFirstChar { it.uppercase() },
                        iconColor = Color(0xFFFF8A00)
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_location,
                        label = "Habitación",
                        value = details.room?.name ?: "General",
                        iconColor = Color(0xFF4285F4)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_refresh,
                        label = "Recurrencia",
                        value = "${details.task.recurrence.icon} ${details.task.recurrence.displayName}",
                        iconColor = Color(0xFF9C27B0)
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        iconId = R.drawable.ic_clock,
                        label = "Estado",
                        value = when(details.taskInstance.state) {
                            TaskState.PENDING -> "Pendiente"
                            TaskState.PAUSED -> "Pausada"
                            TaskState.COMPLETED -> "Completada"
                        },
                        iconColor = Color(0xFF03A9F4)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Puntos
                RewardSection(
                    basePoints = details.task.points,
                    priorityBonus = details.task.priority.points,
                    priorityName = details.task.priority.title.lowercase()
                )

                Spacer(Modifier.height(24.dp))

                // Miembros
                MembersSection(details.assignedMembers)

                Spacer(Modifier.height(24.dp))

                // Descripción
                DescriptionSection(details.task.description)

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

