package haptikos.gestortareashogar_haptikos.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.window.PopupProperties
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
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
    userHasAdminPermissions: Boolean,
    onFilterChange: (TaskFilter) -> Unit,
    onSettingsClick: () -> Unit,
    currentHomeName: String,
    isHomeSelected: Boolean,
    homesList: List<HomeEntityNew>,
    onHomeSelected: (HomeEntityNew) -> Unit,
    onNavigateToCreateHome: () -> Unit,
    onNavigateToJoinHome: () -> Unit,
    onNotificationsClick: () -> Unit,
    onRewardsClick: () -> Unit,
    isCollapsed: Boolean = false,
    userIsCreator: Boolean
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    val bottomPadding by animateDpAsState(targetValue = if (isCollapsed) 16.dp else 24.dp, label = "bottom_padding")
    // Animamos también el espaciador del medio para evitar saltos
    val middleSpacerHeight by animateDpAsState(targetValue = if (isCollapsed) 16.dp else 24.dp, label = "middle_spacer")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = gradientBrush,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = bottomPadding)
    ) {
        // Selector de hogares
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                var expanded by remember { mutableStateOf(false) }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { expanded = true }
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Casa", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Text(" $currentHomeName ", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(painterResource(id = R.drawable.ic_dropdown), contentDescription = "Expandir", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    properties = PopupProperties(focusable = true)
                ) {
                    Text("MIS HOGARES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))

                    homesList.forEach { home ->
                        val isSelected = home.name == currentHomeName
                        val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

                        DropdownMenuItem(
                            modifier = Modifier.background(bgColor),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Icon(painterResource(id = R.drawable.ic_home), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp)) }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = home.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = "Miembros... • Rol...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            trailingIcon = { if (isSelected) Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape)) },
                            onClick = { onHomeSelected(home); expanded = false }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(painterResource(id = R.drawable.ic_plus), contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Crear hogar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Comienza un nuevo hogar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        onClick = { expanded = false; onNavigateToCreateHome() }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(painterResource(id = R.drawable.ic_arrow_right), contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Unirse a un hogar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Usa un código de invitación", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        onClick = { expanded = false; onNavigateToJoinHome() }
                    )
                }
            }

            // Configuración / Visualización de hogar
            val canAccessHomeDetails = homesList.isNotEmpty() && isHomeSelected

            if (canAccessHomeDetails) {
                Surface(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSettingsClick() }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (userIsCreator) R.drawable.ic_configuration else R.drawable.ic_circle_information
                        ),
                        contentDescription = if (userIsCreator) "Configuración del hogar" else "Detalles del hogar",
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Saludo y notificaciones
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            Column {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¡Hola, $userName! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        val taskMessage = when (pendingTasksCount) {
                            0 -> "¡Semana libre! No tienes tareas pendientes"
                            1 -> "Tienes 1 tarea pendiente esta semana"
                            else -> "Tienes $pendingTasksCount tareas pendientes esta semana"
                        }
                        Text(taskMessage, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), fontSize = 14.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box {
                            Surface(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { onNotificationsClick() }
                            ) {
                                Box(contentAlignment = Alignment.Center) { Icon(painterResource(id = R.drawable.ic_bell), contentDescription = "Notificaciones", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp)) }
                            }
                            if (hasNotifications) {
                                Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.error, CircleShape).align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
                            }
                        }

                        Box {
                            Surface(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { onRewardsClick() }
                            ) {
                                Box(contentAlignment = Alignment.Center) { Icon(painterResource(id = R.drawable.ic_trophy), contentDescription = "Logros", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp)) }
                            }
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape, modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-4).dp).defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(text = userPoints.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSurface) }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(middleSpacerHeight))

        // Búsqueda y filtros
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(painterResource(id = R.drawable.ic_search), contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) Text("Buscar tareas...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                            innerTextField()
                        }
                    }
                )

                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    var showFilterMenu by remember { mutableStateOf(false) }

                    Icon(
                        painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filtrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { showFilterMenu = true }
                            .padding(4.dp)
                    )

                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text("Asignación", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Text("Estado", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            text = { Text("Completadas") },
                            onClick = { onFilterChange(currentFilter.copy(status = TaskState.COMPLETED)); showFilterMenu = false },
                            trailingIcon = { if (currentFilter.status == TaskState.COMPLETED) Icon(painterResource(id = R.drawable.ic_check), "Activo", modifier = Modifier.size(20.dp)) else null }
                        )
                    }
                }
            }
        }

        // Barra de progreso
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            Column {
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Progreso del día", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { dailyProgress },
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                    )
                }
            }
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