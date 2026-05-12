package haptikos.gestortareashogar_haptikos.ui.screens.homeStats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.InfoCard

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
fun HomeStatsContent(
    userName: String,
    state: HomeStatsUiState,
    onRangeSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // 1. Header
            HomeStatsHeader(
                homeName = "Mi Casa",
                percentage = "${state.effectiveness}",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-15).dp) // Solapamiento ligero
            ) {
                // 2. Selector
                TimeRangeSelector(
                    selected = state.selectedRange,
                    onSelected = onRangeSelected
                )

                // 3. Metricas rapidas
                QuickMetricsRow(
                    completed = "${state.completedCount}",
                    failed = "${state.pendingCount}",
                    members = "${state.membersCount}"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Grfaica de barras
                TasksByPeriodCard(data = state.barChartData)

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Distribucion general
                GeneralDistributionCard(
                    completed = state.completedCount,
                    pending = state.pendingCount
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Lista por miembro
                MemberStatsCard(members = state.members, mvpName = "Ana")

                Spacer(modifier = Modifier.height(24.dp))

                // 7. Lista por habitacion
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
            .padding(20.dp)
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
fun QuickMetricsRow(
    completed: String,
    failed: String,
    members: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(Modifier.weight(1f), completed, "Completadas", R.drawable.ic_check_circle, Color(0xFF00C853))
        MetricCard(Modifier.weight(1f), failed, "No realizadas", R.drawable.ic_cancel, Color(0xFFFF5252))
        MetricCard(Modifier.weight(1f), members, "Miembros", R.drawable.ic_users, Color(0xFF2196F3))
    }
}

@Composable
fun MetricCard(
    modifier: Modifier,
    value: String,
    label: String,
    iconRes: Int,
    color: Color
) {
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
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3142)
            )

            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF7D848F),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TasksByPeriodCard(data: List<BarChartData>) {
    InfoCard(title = "Tareas por período", iconId = R.drawable.ic_bolt) { // Usando tu ic_bolt
        Column(modifier = Modifier.padding(top = 16.dp)) {
            PeriodBarChart(data)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

    val maxValue = when {
        rawMax <= 10f -> 10f
        rawMax <= 20f -> 20f
        rawMax <= 50f -> 50f
        else -> (Math.ceil(rawMax / 10.0) * 10).toFloat()
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacingX = width / (data.size + 1)
        val barWidth = 12.dp.toPx()

        val gridLines = 4
        (0..gridLines).forEach { i ->
            val yValue = (i * (maxValue / gridLines))
            val yPos = height - (height * (yValue / maxValue))

            drawLine(
                color = gridColor,
                start = Offset(30.dp.toPx(), yPos),
                end = Offset(width, yPos),
                strokeWidth = 1.dp.toPx()
            )

            drawContext.canvas.nativeCanvas.drawText(
                yValue.toInt().toString(),
                10.dp.toPx(),
                yPos + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }

        data.forEachIndexed { index, item ->
            val xPos = (index + 1) * spacingX

            val totalHeight = (item.pending / maxValue) * height
            drawRoundRect(
                color = Color(0xFFFF6D00).copy(alpha = 0.3f),
                topLeft = Offset(xPos - barWidth / 2, height - totalHeight),
                size = Size(barWidth, totalHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            val completedHeight = (item.completed / maxValue) * height
            drawRoundRect(
                color = Color(0xFFFF6D00),
                topLeft = Offset(xPos - barWidth / 2, height - completedHeight),
                size = Size(barWidth, completedHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                item.label,
                xPos,
                height + 20.dp.toPx(),
                android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun GeneralDistributionCard(completed: Int, pending: Int) {
    val total = completed + pending
    val completedPercentage = if (total > 0) (completed.toFloat() / total) else 0f

    InfoCard(title = "Distribución general", iconId = R.drawable.ic_stats_2) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Grafico de Dona
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 22.dp.toPx()

                    // Círculo de fondo
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Arco de progreso
                    drawArc(
                        color = Color(0xFFFF6D00),
                        startAngle = -90f,
                        sweepAngle = 360f * completedPercentage,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Porcentajes y Total
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Barra Completadas
                DistributionProgressItem(
                    label = "Completadas",
                    percentage = (completedPercentage * 100).toInt(),
                    color = Color(0xFFFF6D00)
                )

                // Barra No realizadas
                DistributionProgressItem(
                    label = "No realizadas",
                    percentage = 100 - (completedPercentage * 100).toInt(),
                    color = Color(0xFFE0E0E0)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Cuadro de Tareas Totales
                Surface(
                    color = Color(0xFFFFF5E9),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$total",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6D00)
                        )
                        Text(
                            text = "tareas totales",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionProgressItem(label: String, percentage: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
            Text(
                text = "$percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (percentage > 0 && color != Color(0xFFE0E0E0)) color else Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage.toFloat() / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color(0xFFF0F0F0)
        )
    }
}

@Composable
fun MemberStatItem(member: MemberStatsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar con inicial
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(member.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = member.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2D3142)
                )
                Text(
                    text = "${member.percentage}%",
                    color = member.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = { member.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = member.color,
                trackColor = Color(0xFFF0F0F0)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "${member.completedTasks}/${member.totalTasks}",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.width(45.dp)
        )
    }
}

@Composable
fun MemberStatsCard(members: List<MemberStatsItem>, mvpName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Encabezado de la Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_users),
                        contentDescription = null,
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Por miembro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2D3142)
                    )
                }

                // Badge de MVP
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = Color(0xFFFFAB00),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "MVP: $mvpName",
                            color = Color(0xFFFFAB00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de miembros
            members.forEach { member ->
                MemberStatItem(member)
            }
        }
    }
}

@Composable
fun RoomStatItem(room: RoomStatsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre de la habitación
        Text(
            text = room.roomName,
            modifier = Modifier.width(100.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3142)
        )

        // Barra de progreso
        LinearProgressIndicator(
            progress = { room.progress },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape),
            color = Color(0xFFFF9800),
            trackColor = Color(0xFFF0F0F0)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "${room.completedTasks}/${room.totalTasks}",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.width(35.dp)
        )
    }
}

@Composable
fun RoomStatsCard(rooms: List<RoomStatsItem>) {
    InfoCard(title = "Por habitación", iconId = R.drawable.ic_home_orange) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            rooms.forEach { room ->
                RoomStatItem(room)
            }
        }
    }
}

// --- PREVIEWS ---
// --- VISTA SEMANAL ---
@Preview(showBackground = true, showSystemUi = true, name = "Vista Semana")
@Composable
fun HomeStatsWeekPreview() {
    val weekState = HomeStatsUiState(
        selectedRange = "Semana",
        effectiveness = 76,
        completedCount = 25,
        pendingCount = 8,
        membersCount = 4,
        barChartData = listOf(
            BarChartData("Lun", 4f, 2f),
            BarChartData("Mar", 3f, 1f),
            BarChartData("Mié", 5f, 2f),
            BarChartData("Jue", 2f, 4f),
            BarChartData("Vie", 4f, 1f),
            BarChartData("Sáb", 6f, 2f),
            BarChartData("Dom", 1f, 0f)
        ),
        members = listOf(
            MemberStatsItem("María", 10, 12, Color(0xFFE91E63)),
            MemberStatsItem("Juan", 6, 9, Color(0xFF2196F3))
        ),
        rooms = listOf(
            RoomStatsItem("Cocina", 7, 8),
            RoomStatsItem("Sala", 5, 5))
    )
    HomeStatsContent("María", weekState, {}, {})
}

// --- VISTA MENSUAL ---
@Preview(showBackground = true, showSystemUi = true, name = "Vista Mes")
@Composable
fun HomeStatsMonthPreview() {
    val monthState = HomeStatsUiState(
        selectedRange = "Mes",
        effectiveness = 82,
        completedCount = 112,
        pendingCount = 24,
        membersCount = 4,
        barChartData = listOf(
            BarChartData("Sem 1", 25f, 10f),
            BarChartData("Sem 2", 30f, 5f),
            BarChartData("Sem 3", 28f, 4f),
            BarChartData("Sem 4", 29f, 5f)
        ),
        members = listOf(
            MemberStatsItem("Ana", 40, 40, Color(0xFF9C27B0)),
            MemberStatsItem("Pedro", 22, 35, Color(0xFF00C853))
        ),
        rooms = listOf(
            RoomStatsItem("Baños", 15, 20),
            RoomStatsItem("Jardín", 10, 10))
    )
    HomeStatsContent("María", monthState, {}, {})
}

// --- VISTA ANUAL ---
@Preview(showBackground = true, showSystemUi = true, name = "Vista Año")
@Composable
fun HomeStatsYearPreview() {
    val yearState = HomeStatsUiState(
        selectedRange = "Año",
        effectiveness = 84,
        completedCount = 1450,
        pendingCount = 280,
        membersCount = 6,
        barChartData = listOf(
            BarChartData("Ene", 120f, 20f),
            BarChartData("Feb", 110f, 30f),
            BarChartData("Mar", 140f, 10f),
            BarChartData("Abr", 130f, 25f)
        ),
        members = listOf(
            MemberStatsItem("María", 524, 612, Color(0xFFFF6D00))),
        rooms = listOf(
            RoomStatsItem("Toda la casa", 1450, 1730))
    )
    HomeStatsContent("María", yearState, {}, {})
}

