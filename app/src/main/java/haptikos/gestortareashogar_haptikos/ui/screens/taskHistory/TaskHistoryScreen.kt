package haptikos.gestortareashogar_haptikos.ui.screens.taskHistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHistoryScreen(
    viewModel: TaskInstanceViewModel,
    onBack: () -> Unit
) {
    val allTaskInstances by viewModel.allTaskInstances.collectAsState()

    val completedInstances = remember(allTaskInstances) {
        allTaskInstances
            .filter { it.taskInstance.state == TaskState.COMPLETED }
            .sortedByDescending { it.taskInstance.completedAt ?: 0L }
    }

    val grouped = remember(completedInstances) {
        val now = System.currentTimeMillis()
        val oneDayMs = 86_400_000L
        val twoDaysMs = 2 * oneDayMs

        val today = completedInstances.filter { now - (it.taskInstance.completedAt ?: 0L) < oneDayMs }
        val yesterday = completedInstances.filter {
            now - (it.taskInstance.completedAt ?: 0L) in oneDayMs until twoDaysMs
        }
        val older = completedInstances.filter { now - (it.taskInstance.completedAt ?: 0L) >= twoDaysMs }

        listOf("HOY" to today, "AYER" to yesterday, "ANTERIORES" to older)
            .filter { it.second.isNotEmpty() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF8A00), Color(0xFFFF6D00))
                    )
                )
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_back),
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        "Historial",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "${completedInstances.size} tareas completadas",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (completedInstances.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Aún no has completado tareas",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tus tareas completadas aparecerán aquí",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                grouped.forEach { (label, items) ->
                    item {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                    items(items, key = { it.taskInstance.id }) { instance ->
                        TaskHistoryCard(instance)
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun TaskHistoryCard(instance: TaskInstanceWithDetails) {
    val completedAt = instance.taskInstance.completedAt ?: 0L
    val timeText = remember(completedAt) {
        val diff = System.currentTimeMillis() - completedAt
        when {
            diff < 3_600_000 -> "hace ${diff / 60_000} min"
            diff < 86_400_000 -> "hace ${diff / 3_600_000} h"
            else -> "hace ${diff / 86_400_000} días"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF00C853).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = Color(0xFF00C853),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instance.taskDetails.task?.title ?: "Tarea",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = instance.taskDetails.room?.name ?: "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = timeText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}