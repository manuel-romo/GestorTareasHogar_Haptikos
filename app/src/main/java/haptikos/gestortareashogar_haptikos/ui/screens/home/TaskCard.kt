package haptikos.gestortareashogar_haptikos.ui.screens.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.ui.components.MemberAvatar
import haptikos.gestortareashogar_haptikos.utils.getDayName
import haptikos.gestortareashogar_haptikos.utils.parseHexColor
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel

@Composable
fun TaskCard(
    taskInstance: TaskInstanceWithDetails,
    onClick: () -> Unit,
    onStatusClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    val isCompleted = taskInstance.taskInstance.state == TaskState.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(if (isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {

            // Círculo estado
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onStatusClick() }
                    .border(
                        width = 2.dp,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        painterResource(id = R.drawable.ic_check),
                        contentDescription = "Completada",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Textos y pastillas
            Column(Modifier.weight(1f)) {
                // Título
                Text(
                    text = taskInstance.taskDetails.task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(Modifier.height(4.dp))

                // Fecha y Habitación
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text =" ${getDayName(taskInstance.taskInstance.dueDate)} ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Solo se pinta la pastilla si hay una habitación asignada
                    taskInstance.taskDetails.room?.let { room ->
                        Spacer(Modifier.width(8.dp))

                        val roomBaseColor = parseHexColor(room.colorHex)
                        val roomBgColor = roomBaseColor.copy(alpha = 0.15f)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if(isCompleted) MaterialTheme.colorScheme.surfaceVariant else roomBgColor
                        ) {
                            Text(
                                text = " ${room.name} ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if(isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else roomBaseColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } ?: run {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = " Casa ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Fila de Miembros
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    taskInstance.assignedMembers.forEach { member ->
                        MemberAvatar(member = member, size = 28.dp)
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Puntos, flecha y basura
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (!isCompleted) {
                    // Badge de puntos
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = " ⭐ +${taskInstance.taskDetails.task.points} ",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    IconButton(onClick = { onDeleteClick() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trash),
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}