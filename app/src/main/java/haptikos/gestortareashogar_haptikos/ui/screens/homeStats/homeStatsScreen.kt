package haptikos.gestortareashogar_haptikos.ui.screens.homeStats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.components.CustomBottomNavigation
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.InfoCard
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel

data class BarChartData(
    val label: String,
    val completed: Float,
    val pending: Float
)

data class MemberStatsItem(
    val name: String,
    val completedTasks: Int,
    val totalTasks: Int,
    val color: Color
) {
    val progress: Float = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val percentage: Int = (progress * 100).toInt()
}

data class RoomStatsItem(
    val roomName: String,
    val completedTasks: Int,
    val totalTasks: Int
) {
    val progress: Float = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
}

data class HomeStatsUiState(
    val selectedRange: String = "Semana",
    val effectiveness: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val membersCount: Int = 0,
    val barChartData: List<BarChartData> = emptyList(),
    val members: List<MemberStatsItem> = emptyList(),
    val rooms: List<RoomStatsItem> = emptyList()
)

@Composable
fun HomeStatsScreen(
    viewModel: TaskInstanceViewModel,
    homeViewModel: HomeViewModel,
    navController: NavController,
    onBackClick: () -> Unit
) {
    val state by viewModel.homeStatsState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val canCreateTasks by homeViewModel.isCurrentUserCreatorOrAdmin.collectAsState()

    HomeStatsContent(
        userName = userName,
        state = state,
        navController = navController,
        canCreateTasks = canCreateTasks,
        onRangeSelected = { viewModel.updateHomeStatsRange(it) },
        onBackClick = onBackClick
    )
}
@Composable
fun HomeStatsContent(
    userName: String,
    state: HomeStatsUiState,
    navController: NavController? = null, // Opcional para Previews
    canCreateTasks: Boolean = false,
    onRangeSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val mvpName = state.members.maxByOrNull { it.completedTasks }?.name ?: "N/A"

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        // Acá se integra el bottom navigation en la pantalla
        bottomBar = {
            if (navController != null) {
                CustomBottomNavigation(
                    navController = navController,
                    currentRoute = haptikos.gestortareashogar_haptikos.navigation.Screen.HomeStats.route,
                    hasCenterFab = canCreateTasks
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Header que ahora si recibe el porcentaje real
            HomeStatsHeader(
                homeName = "Estadísticas del Hogar",
                percentage = "${state.effectiveness}",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-15).dp)
            ) {
                // Selector
                TimeRangeSelector(
                    selected = state.selectedRange,
                    onSelected = onRangeSelected
                )

                // Metricas rapidas
                QuickMetricsRow(
                    completed = "${state.completedCount}",
                    failed = "${state.pendingCount}",
                    members = "${state.membersCount}"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Gráfica de barras
                TasksByPeriodCard(data = state.barChartData)

                Spacer(modifier = Modifier.height(24.dp))

                // Distribucion general
                GeneralDistributionCard(
                    completed = state.completedCount,
                    pending = state.pendingCount
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Lista por miembro
                MemberStatsCard(members = state.members, mvpName = mvpName)

                Spacer(modifier = Modifier.height(24.dp))

                //Lista por habitacion
                RoomStatsCard(rooms = state.rooms)

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun TimeRangeSelector(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("Semana", "Mes", "Año")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        options.forEach { text ->
            val isSelected = selected == text
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFFFF6D00) else Color.Transparent)
                    .clickable { onSelected(text) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun QuickMetricsRow(completed: String, failed: String, members: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(Modifier.weight(1f), completed, "Completadas", R.drawable.ic_check_circle, Color(0xFF00C853))
        MetricCard(Modifier.weight(1f), failed, "No realizadas", R.drawable.ic_cancel, Color(0xFFFF5252))
        MetricCard(Modifier.weight(1f), members, "Miembros", R.drawable.ic_users, Color(0xFF2196F3))
    }
}

@Composable
fun MetricCard(modifier: Modifier, value: String, label: String, iconRes: Int, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3142))
            Text(text = label, fontSize = 12.sp, color = Color(0xFF7D848F), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TasksByPeriodCard(data: List<BarChartData>) {
    InfoCard(title = "Tareas por período", iconId = R.drawable.ic_bolt) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            PeriodBarChart(data)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                ChartLegendItem("Completadas", Color(0xFFFF6D00))
                Spacer(modifier = Modifier.width(24.dp))
                ChartLegendItem("No realizadas", Color(0xFFFF6D00).copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun PeriodBarChart(data: List<BarChartData>) {
    val gridColor = Color(0xFFEEEEEE)
    val rawMax = data.maxOfOrNull { maxOf(it.completed, it.pending) } ?: 10f
    val maxValue = if (rawMax <= 10f) 10f else (Math.ceil(rawMax / 10.0) * 10).toFloat()

    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val width = size.width
        val height = size.height
        val spacingX = width / (data.size + 1)
        val barWidth = 12.dp.toPx()

        (0..4).forEach { i ->
            val yPos = height - (height * (i * 0.25f))
            drawLine(color = gridColor, start = Offset(30.dp.toPx(), yPos), end = Offset(width, yPos), strokeWidth = 1.dp.toPx())
        }

        data.forEachIndexed { index, item ->
            val xPos = (index + 1) * spacingX
            val totalHeight = (item.pending / maxValue) * height
            drawRoundRect(color = Color(0xFFFF6D00).copy(alpha = 0.3f), topLeft = Offset(xPos - barWidth / 2, height - totalHeight), size = Size(barWidth, totalHeight), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))

            val completedHeight = (item.completed / maxValue) * height
            drawRoundRect(color = Color(0xFFFF6D00), topLeft = Offset(xPos - barWidth / 2, height - completedHeight), size = Size(barWidth, completedHeight), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))

            drawContext.canvas.nativeCanvas.drawText(item.label, xPos, height + 20.dp.toPx(), android.graphics.Paint().apply { color = Color.Gray.toArgb(); textSize = 11.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER })
        }
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun GeneralDistributionCard(completed: Int, pending: Int) {
    val total = completed + pending
    val completedPercentage = if (total > 0) (completed.toFloat() / total) else 0f

    InfoCard(title = "Distribución general", iconId = R.drawable.ic_stats_2) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 22.dp.toPx()
                    drawArc(color = Color(0xFFE0E0E0), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                    drawArc(color = Color(0xFFFF6D00), startAngle = -90f, sweepAngle = 360f * completedPercentage, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DistributionProgressItem("Completadas", (completedPercentage * 100).toInt(), Color(0xFFFF6D00))
                DistributionProgressItem("No realizadas", 100 - (completedPercentage * 100).toInt(), Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = Color(0xFFFFF5E9), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$total", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF6D00))
                        Text(text = "tareas totales", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionProgressItem(label: String, percentage: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
            Text(text = "$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (percentage > 0 && color != Color(0xFFE0E0E0)) color else Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(progress = { percentage.toFloat() / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = color, trackColor = Color(0xFFF0F0F0))
    }
}

@Composable
fun MemberStatItem(member: MemberStatsItem) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(42.dp).background(member.color, CircleShape), contentAlignment = Alignment.Center) {
            Text(text = member.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D3142))
                Text(text = "${member.percentage}%", color = member.color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { member.progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = member.color, trackColor = Color(0xFFF0F0F0))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "${member.completedTasks}/${member.totalTasks}", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.width(45.dp))
    }
}

@Composable
fun MemberStatsCard(members: List<MemberStatsItem>, mvpName: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_users), contentDescription = null, tint = Color(0xFFFF6D00), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(text = "Por miembro", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2D3142))
                }
                Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.ic_star), null, tint = Color(0xFFFFAB00), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(text = "MVP: $mvpName", color = Color(0xFFFFAB00), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            members.forEach { MemberStatItem(it) }
        }
    }
}

@Composable
fun RoomStatsCard(rooms: List<RoomStatsItem>) {
    InfoCard(title = "Por habitación", iconId = R.drawable.ic_home_orange) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            rooms.forEach { room ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = room.roomName, modifier = Modifier.width(100.dp), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2D3142))
                    LinearProgressIndicator(progress = { room.progress }, modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape), color = Color(0xFFFF9800), trackColor = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "${room.completedTasks}/${room.totalTasks}", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.width(35.dp))
                }
            }
        }
    }
}

// Preview para sección "Año"
@Preview(showBackground = true, showSystemUi = true, name = "Análisis Anual - Datos Mock")
@Composable
fun HomeStatsYearOnlyPreview() {
    val yearData = listOf(
        BarChartData("Ene", 110f, 15f),
        BarChartData("Feb", 95f, 20f),
        BarChartData("Mar", 130f, 10f),
        BarChartData("Abr", 125f, 30f),
        BarChartData("May", 140f, 5f),
        BarChartData("Jun", 115f, 25f),
        BarChartData("Jul", 100f, 40f),
        BarChartData("Ago", 110f, 20f),
        BarChartData("Sep", 120f, 15f),
        BarChartData("Oct", 135f, 10f),
        BarChartData("Nov", 145f, 5f),
        BarChartData("Dic", 90f, 50f)
    )

    val yearState = HomeStatsUiState(
        selectedRange = "Año",
        effectiveness = 82,
        completedCount = 1515,
        pendingCount = 245,
        membersCount = 5,
        barChartData = yearData,
        members = listOf(
            MemberStatsItem("Yuri", 450, 500, Color(0xFFFF6D00)),
            MemberStatsItem("María", 420, 480, Color(0xFFE91E63)),
            MemberStatsItem("Pedro", 300, 400, Color(0xFF2196F3)),
            MemberStatsItem("Ana", 200, 210, Color(0xFF9C27B0)),
            MemberStatsItem("Juan", 145, 170, Color(0xFF00C853))
        ),
        rooms = listOf(
            RoomStatsItem("Cocina", 400, 450),
            RoomStatsItem("Sala", 350, 350),
            RoomStatsItem("Baños", 300, 420),
            RoomStatsItem("Jardín", 250, 300),
            RoomStatsItem("Recámaras", 215, 240)
        )
    )

    val mockUserName = "SeS"

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        HomeStatsContent(
            userName = mockUserName,
            state = yearState,
            onRangeSelected = {},
            onBackClick = {}
        )
    }
}

// Preview para la sección de "Mes"
@Preview(showBackground = true, showSystemUi = true, name = "Análisis Mensual - Datos Mock")
@Composable
fun HomeStatsMonthOnlyPreview() {
    val monthData = listOf(
        BarChartData("Sem 1", 25f, 5f),
        BarChartData("Sem 2", 18f, 12f),
        BarChartData("Sem 3", 30f, 2f),
        BarChartData("Sem 4", 22f, 8f)
    )

    val monthState = HomeStatsUiState(
        selectedRange = "Mes",
        effectiveness = 78,
        completedCount = 95,
        pendingCount = 27,
        membersCount = 3,
        barChartData = monthData,
        members = listOf(
            MemberStatsItem("Yuri", 40, 45, Color(0xFFFF6D00)),
            MemberStatsItem("María", 35, 40, Color(0xFFE91E63)),
            MemberStatsItem("Pedro", 20, 37, Color(0xFF2196F3))
        ),
        rooms = listOf(
            RoomStatsItem("Cocina", 30, 35),
            RoomStatsItem("Baños", 25, 30),
            RoomStatsItem("Sala", 40, 57)
        )
    )

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        HomeStatsContent(
            userName = "Yuri",
            state = monthState,
            onRangeSelected = {},
            onBackClick = {}
        )
    }
}

// Preview para la sección de "Semana"
@Preview(showBackground = true, showSystemUi = true, name = "Análisis Semanal - Datos Mock")
@Composable
fun HomeStatsWeekOnlyPreview() {
    val weekData = listOf(
        BarChartData("Lun", 4f, 1f),
        BarChartData("Mar", 3f, 0f),
        BarChartData("Mié", 5f, 2f),
        BarChartData("Jue", 2f, 1f),
        BarChartData("Vie", 6f, 0f),
        BarChartData("Sáb", 4f, 3f),
        BarChartData("Dom", 1f, 0f)
    )

    val weekState = HomeStatsUiState(
        selectedRange = "Semana",
        effectiveness = 75,
        completedCount = 25,
        pendingCount = 7,
        membersCount = 2,
        barChartData = weekData,
        members = listOf(
            MemberStatsItem("Yuri", 15, 18, Color(0xFFFF6D00)),
            MemberStatsItem("María", 10, 14, Color(0xFFE91E63))
        ),
        rooms = listOf(
            RoomStatsItem("Cocina", 10, 12),
            RoomStatsItem("Sala", 8, 8),
            RoomStatsItem("Baños", 7, 12)
        )
    )

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        HomeStatsContent(
            userName = "Yuri",
            state = weekState,
            onRangeSelected = {},
            onBackClick = {}
        )
    }
}