package haptikos.gestortareashogar_haptikos.ui.screens.userStats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.InfoCard
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.ui.components.CustomBottomNavigation

// Algunos modelos

data class HomeStatsItem(
    val homeName: String,
    val completedTasks: Int,
    val totalTasks: Int,
    val progress: Float,
    val color: Color,
    val icon: String,
    val iconBgColor: Color
)

data class ChartPoint(
    val label: String,
    val value: Float
)

data class HomeComparisonData(
    val homeName: String,
    val completedCount: Int,
    val pendingCount: Int,
    val color: Color,
    val pendingColor: Color
)

data class UserStatsUiState(
    val completedCount: Int = 0,
    val effectiveness: Int = 0,
    val streakDays: Int = 0,
    val selectedRange: String = "Año",
    val homeStats: List<HomeStatsItem> = emptyList(),
    val trendChartPoints: List<ChartPoint> = emptyList(),
    val homeComparisonPoints: List<HomeComparisonData> = emptyList()
)

// Pantalla principal

@Composable
fun UserStatsScreen(
    viewModel: TaskInstanceViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.userStats.collectAsState()
    val selectedRange by viewModel.selectedTimeRange.collectAsState()
    val userName by viewModel.userName.collectAsState()
    var homesExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        UserStatsHeader(
            userName = userName,
            completedCount = uiState.completedCount,
            effectiveness = uiState.effectiveness,
            streakDays = uiState.streakDays,
            onBackClick = onBackClick
        )

        TimeRangeSelector(selectedRange) { viewModel.updateTimeRange(it) }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // Selector de Hogares
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                color = Color.White
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Todos los hogares", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${uiState.homeStats.size} hogares activos", color = Color.Gray, fontSize = 12.sp)
                    }
                    Icon(painterResource(R.drawable.ic_dropdown), null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Mis hogares
            InfoCard(title = "Mis hogares", iconId = R.drawable.ic_home_orange) {
                val visibleHomes = if (homesExpanded) uiState.homeStats else uiState.homeStats.take(2)
                visibleHomes.forEach { home -> HomeStatItem(home) }

                if (uiState.homeStats.size > 2) {
                    TextButton(
                        onClick = { homesExpanded = !homesExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (homesExpanded) "Ver menos" else "Ver ${uiState.homeStats.size - 2} más",
                            color = Color(0xFFFF8A00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))

            // Tendencia
            InfoCard(title = "Tendencia de tareas completadas", iconId = R.drawable.ic_bolt) {
                TaskTrendLineChart(points = uiState.trendChartPoints)
            }

            Spacer(Modifier.height(20.dp))

            // Comparativa
            InfoCard(title = "Comparativa por hogar", iconId = R.drawable.ic_home_orange) {
                HomeTaskBarChart(
                    data = uiState.homeComparisonPoints,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            SummaryGrid(uiState)
        }

        Spacer(Modifier.height(100.dp))
    }
}

// Gráficas
@Composable
fun TaskTrendLineChart(points: List<ChartPoint>) {
    val chartColor = Color(0xFFFF6D00)
    val gridColor = Color(0xFFEEEEEE)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacingX = if (points.size > 1) width / (points.size - 1) else width
        val maxVal = 100f

        (0..4).forEach { i ->
            val y = height - (height * (i * 25f / maxVal))
            drawLine(gridColor, Offset(0f, y), Offset(width, y), 1.dp.toPx())
        }

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, point ->
            val x = index * spacingX
            val y = height - (height * (point.value / maxVal))
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }

            drawContext.canvas.nativeCanvas.drawText(
                point.label, x, height + 16.dp.toPx(),
                android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        drawPath(fillPath, Brush.verticalGradient(listOf(chartColor.copy(0.2f), Color.Transparent)))
        drawPath(path, chartColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun HomeTaskBarChart(
    data: List<HomeComparisonData>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(top = 16.dp, bottom = 28.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = 30.dp.toPx()
        val spacingX = width / (data.size + 1)
        val maxVal = data.maxOfOrNull { (it.completedCount + it.pendingCount).toFloat() }
            ?.coerceAtLeast(1f) ?: 1f

        data.forEachIndexed { index, item ->
            val x = (index + 1) * spacingX
            val totalTasks = (item.completedCount + item.pendingCount).toFloat()
            val totalHeight = (totalTasks / maxVal) * height
            val completedHeight = (item.completedCount / maxVal) * height

            // Barra pendiente
            drawRoundRect(
                item.pendingColor,
                Offset(x - barWidth / 2, height - totalHeight),
                Size(barWidth, totalHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
            // Barra completada
            drawRoundRect(
                item.color,
                Offset(x - barWidth / 2, height - completedHeight),
                Size(barWidth, completedHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                item.homeName, x, height + 18.dp.toPx(),
                android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

// Componentes

@Composable
fun SummaryGrid(uiState: UserStatsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Efectividad",
                value = "${uiState.effectiveness}%",
                subtitle = "tareas completadas",
                iconId = R.drawable.ic_target,
                color = Color(0xFF00C853)
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "No realizadas",
                value = "${100 - uiState.effectiveness}%",
                subtitle = "del total de tareas",
                iconId = R.drawable.ic_cancel,
                color = Color(0xFFFF5252)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Hogares",
                value = "${uiState.homeStats.size}",
                subtitle = "hogares activos",
                iconId = R.drawable.ic_star,
                color = Color(0xFFFFAB00)
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Racha",
                value = "${uiState.streakDays} días",
                subtitle = "completando tareas",
                iconId = R.drawable.ic_fire,
                color = Color(0xFFFF6D00)
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    iconId: Int,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3142))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun HomeStatItem(home: HomeStatsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(home.iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = home.icon, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = home.homeName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF2D3142)
                )
                Text(
                    text = "${(home.progress * 100).toInt()}%",
                    color = home.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { home.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = home.color,
                trackColor = Color(0xFFF0F0F0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${home.completedTasks} de ${home.totalTasks} tareas",
                color = Color.Gray,
                fontSize = 12.sp
            )
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