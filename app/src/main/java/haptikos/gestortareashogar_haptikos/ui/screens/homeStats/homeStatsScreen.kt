package haptikos.gestortareashogar_haptikos.ui.screens.homeStats

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.InfoCard
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel

data class BarChartData(
    val label: String,
    val completed: Float,
    val pending: Float
)

@Composable
fun HomeStatsScreen(
    viewModel: TaskInstanceViewModel,
    userName: String,
    onBackClick: () -> Unit
) {
    val backgroundColor = Color(0xFFF8F9FA)
    val uiState by viewModel.userStats.collectAsState()
    val selectedRange by viewModel.selectedTimeRange.collectAsState()

    Scaffold(
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            HomeStatsHeader(
                homeName = "Mi Casa",
                percentage = "${uiState.effectiveness}",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-10).dp)
            ) {
                val selectedRange = remember { mutableStateOf("Semana") }

                Column(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)) {

                    TimeRangeSelector(
                        selectedRange.value,
                        { selectedRange.value = it }
                    )

                    QuickMetricsRow(
                        completed = "${uiState.completedCount}",
                        failed = "${uiState.effectiveness}",
                        members = "${uiState.homeStats.size}"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TasksByPeriodCard(
                        data = listOf(
                            BarChartData("Lun", 4f, 2f),
                            BarChartData("Mar", 3f, 1f),
                            BarChartData("Mié", 5f, 2f),
                            BarChartData("Jue", 2f, 4f),
                            BarChartData("Vie", 4f, 1f),
                            BarChartData("Sáb", 6f, 2f),
                            BarChartData("Dom", 1f, 0f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                }
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

// --- PREVIEWS ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeStatsScreenPreview() {

}

