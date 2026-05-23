package haptikos.gestortareashogar_haptikos.ui.screens.taskHistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel

@Composable
fun TaskHistoryScreen(
    viewModel: TaskInstanceViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    filter: String,
    onBack: () -> Unit
) {
    val allTaskInstances by viewModel.allTaskInstances.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()

    // El filtro inicial viene del navegador pero el usuario puede cambiarlo
    var activeFilter by remember { mutableStateOf(filter) }

    val completedInstances = remember(allTaskInstances, activeFilter, userId, selectedHome) {
        allTaskInstances
            .filter { it.taskInstance.state == TaskState.COMPLETED }
            .filter { instance ->
                when (activeFilter) {
                    "home" -> instance.taskDetails.task.homeId == selectedHome?.id
                    "mine" -> instance.assignedMembers.any { it.userId == userId }
                    else -> true
                }
            }
            .sortedByDescending { it.taskInstance.completedAt ?: 0L }
    }

    val grouped = remember(completedInstances) {
        val now = System.currentTimeMillis()
        val oneDayMs = 86_400_000L
        val twoDaysMs = 2 * oneDayMs
        val today = completedInstances.filter {
            it.taskInstance.completedAt != null &&
                    now - it.taskInstance.completedAt < oneDayMs
        }
        val yesterday = completedInstances.filter {
            it.taskInstance.completedAt != null &&
                    now - it.taskInstance.completedAt in oneDayMs until twoDaysMs
        }
        val older = completedInstances.filter {
            it.taskInstance.completedAt == null ||
                    now - it.taskInstance.completedAt >= twoDaysMs
        }
        listOf("HOY" to today, "AYER" to yesterday, "ANTERIORES" to older)
            .filter { it.second.isNotEmpty() }
    }

    val filters = listOf(
        "all" to "Todas",
        "home" to "Este hogar",
        "mine" to "Mis tareas"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
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
                        .background(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        painterResource(R.drawable.ic_back),
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        "Historial",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "${completedInstances.size} tareas completadas",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            // Filtros
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (filterKey, filterLabel) ->
                    val isSelected = activeFilter == filterKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                            )
                            .clickable { activeFilter = filterKey }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filterLabel,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
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
                        color = MediumDarkGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tus tareas completadas aparecerán aquí",
                        color = MediumDarkGray.copy(alpha = 0.6f),
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
        if (completedAt == 0L) {
            "Fecha desconocida"
        } else {
            val diff = System.currentTimeMillis() - completedAt
            when {
                diff < 60_000 -> "Hace un momento"
                diff < 3_600_000 -> "Hace ${diff / 60_000} min"
                diff < 86_400_000 -> "Hace ${diff / 3_600_000} h"
                diff < 7 * 86_400_000 -> "Hace ${diff / 86_400_000} días"
                diff < 30 * 86_400_000L -> "Hace ${diff / (7 * 86_400_000)} semanas"
                diff < 365 * 86_400_000L -> "Hace ${diff / (30 * 86_400_000L)} meses"
                else -> "Hace ${diff / (365 * 86_400_000L)} años"
            }
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