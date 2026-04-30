package haptikos.gestortareashogar_haptikos.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.ui.theme.CompletedGreen
import haptikos.gestortareashogar_haptikos.ui.theme.LightYellow
import haptikos.gestortareashogar_haptikos.ui.theme.PausedYellow
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.ui.theme.Yellow
import haptikos.gestortareashogar_haptikos.utils.getDateString
import haptikos.gestortareashogar_haptikos.utils.getDayName
import haptikos.gestortareashogar_haptikos.utils.parseHexColor

@Composable
fun TaskCard(taskInstance: TaskInstanceEntity) {

    val isPaused = taskInstance.state == TaskState.PAUSED
    val isCompleted = taskInstance.state == TaskState.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(if (isCompleted) 0.dp else 2.dp),
        border = if (isPaused) BorderStroke(1.5.dp, PausedYellow) else null
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
                    .border(
                        width = 2.dp,
                        color = when {
                            isCompleted -> CompletedGreen
                            isPaused -> PausedYellow
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPaused) {
                    Icon(
                        painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pausada",
                        tint = PausedYellow,
                        modifier = Modifier.size(12.dp)
                    )
                } else if (isCompleted) {
                    Icon(
                        painterResource(id = R.drawable.ic_check),
                        contentDescription = "Completada",
                        tint = CompletedGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Textos y pastillas
            Column(Modifier.weight(1f)) {
                // Título
                Text(
                    text = taskInstance.task.title,
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
                        text =" ${getDayName(taskInstance.dueDate)} ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Solo pintamos la pastilla si hay una habitación asignada
                    taskInstance.task.room?.let { room ->
                        Spacer(Modifier.width(8.dp)) // Espacio entre fecha y pastilla

                        val roomBaseColor = parseHexColor(room.colorHex)
                        val roomBgColor = roomBaseColor.copy(alpha = 0.15f)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if(isCompleted) LightGray else roomBgColor
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
                            color = LightGray
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

                // Fecha fin de pausa para tarea pausada
                if (isPaused && taskInstance.pausedUntil != null) {
                    Text(
                        text = "Pausada hasta ${getDateString(taskInstance.pausedUntil)}",
                        color = PausedYellow,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Fila de Miembros
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    taskInstance.task.members.forEach { member ->
                        val memberColor = parseHexColor(member.colorHex)

                        Surface(shape = RoundedCornerShape(12.dp), color = memberColor) {
                            Text(
                                text = member.name,
                                color = White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
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
                    Surface(color = LightYellow, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = " ⭐ +${taskInstance.task.points} ",
                            color = Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Detalles",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}