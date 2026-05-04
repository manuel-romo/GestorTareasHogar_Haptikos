package haptikos.gestortareashogar_haptikos.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel.TaskFilter


@Composable
fun DashboardHeader(
    userName: String,
    pendingTasksCount: Int,
    hasNotifications: Boolean,
    userPoints: Int,
    dailyProgress: Float,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    onRewardsClick: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = gradientBrush,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(24.dp)
    ) {
        // Botones superiores
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Casa", modifier = Modifier.size(16.dp), tint = Color.White)
                    Text(" Mi Casa ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Icon(painterResource(id = R.drawable.ic_dropdown), contentDescription = "Expandir", modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
                Icon(painterResource(id = R.drawable.ic_configuration), contentDescription = "Ajustes", modifier = Modifier.padding(8.dp).size(20.dp), tint = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Saludo e íconos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Saludo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡Hola, $userName! 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                val taskMessage = when (pendingTasksCount) {
                    0 -> "¡Semana libre! No tienes tareas pendientes"
                    1 -> "Tienes 1 tarea pendiente esta semana"
                    else -> "Tienes $pendingTasksCount tareas pendientes esta semana"
                }
                Text(
                    text = taskMessage,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }

            // Campana y trofeo
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Campana
                Box {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painterResource(id = R.drawable.ic_bell),
                                contentDescription = "Notificaciones",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    if (hasNotifications) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                        )
                    }
                }

                // Trofeo con puntos
                Box {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable{ onRewardsClick() }) {
                            Icon(
                                painterResource(id = R.drawable.ic_trophy),
                                contentDescription = "Logros",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFFCDDC39),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userPoints.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Buscador y filtro
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(painterResource(id = R.drawable.ic_search), contentDescription = "Buscar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))

                // Campo de texto
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text("Buscar tareas...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    }
                )

                // Botón de filtro y menú
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    var showFilterMenu by remember { mutableStateOf(false) }

                    Icon(
                        painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filtrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showFilterMenu = true }
                    )

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        // Asignación
                        Text("Asignación", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Gray)
                        DropdownMenuItem(
                            text = { Text("Todas las tareas") },
                            onClick = { onFilterChange(currentFilter.copy(showOnlyMine = false)); showFilterMenu = false },
                            trailingIcon = { if (!currentFilter.showOnlyMine) Icon(painterResource(id = R.drawable.ic_check), "Activo", modifier = Modifier.size(20.dp)) else null }
                        )
                        DropdownMenuItem(
                            text = { Text("Solo mis tareas") },
                            onClick = { onFilterChange(currentFilter.copy(showOnlyMine = true)); showFilterMenu = false },
                            trailingIcon = { if (currentFilter.showOnlyMine) Icon(painterResource(id = R.drawable.ic_check), "Activo", modifier = Modifier.size(20.dp)) else null }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Estado
                        Text("Estado", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Gray)
                        DropdownMenuItem(
                            text = { Text("Todos los estados") },
                            onClick = { onFilterChange(currentFilter.copy(status = null)); showFilterMenu = false },
                            trailingIcon = { if (currentFilter.status == null) Icon(painterResource(id = R.drawable.ic_check), "Activo", modifier = Modifier.size(20.dp)) else null }
                        )
                        DropdownMenuItem(
                            text = { Text("Pendientes") },
                            onClick = { onFilterChange(currentFilter.copy(status = TaskState.PENDING)); showFilterMenu = false },
                            trailingIcon = { if (currentFilter.status == TaskState.PENDING) Icon(painterResource(id = R.drawable.ic_check), "Activo", modifier = Modifier.size(20.dp)) else null }
                        )
                        DropdownMenuItem(
                            text = { Text("Pausadas") },
                            onClick = { onFilterChange(currentFilter.copy(status = TaskState.PAUSED)); showFilterMenu = false },
                            trailingIcon = { if (currentFilter.status == TaskState.PAUSED) Icon(painterResource(id = R.drawable.ic_check), "Activo", modifier = Modifier.size(20.dp)) else null }
                        )
                        DropdownMenuItem(
                            text = { Text("Completadas") },
                            onClick = { onFilterChange(currentFilter.copy(status = TaskState.COMPLETED)); showFilterMenu = false },
                            trailingIcon = { if (currentFilter.status == TaskState.COMPLETED) Icon(painterResource(id = R.drawable.ic_check), "Activo", modifier = Modifier.size(20.dp)) else null }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Barra de progreso
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Progreso del día",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { dailyProgress },
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun DaySelector(
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    val dias = listOf("Todos", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    LazyRow(
        Modifier.padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dias) { dia ->
            val isSelected = dia == selectedDay

            val chipShape = RoundedCornerShape(16.dp)

            Surface(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(chipShape)
                    .clickable { onDaySelected(dia) },
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = chipShape,
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Text(
                    text = dia,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        color = color,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    )
}