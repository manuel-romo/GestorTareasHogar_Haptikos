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

//Algunos modelos
data class HomeStatsItem(
    val homeName: String,
    val completedTasks: Int,
    val totalTasks: Int,
    val progress: Float,
    val color: Color,
    val icon: String,
    val iconBgColor: Color
)

//Estructura para un punto en la gráfica de tendencia
data class ChartPoint(
    val label: String,
    val value: Float
)

//Estructura para la comparativa por hogar
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
    //Datos necesarios para pintar la gráfica
    val trendChartPoints: List<ChartPoint> = emptyList(),
    val homeComparisonPoints: List<HomeComparisonData> = emptyList()
)

//Composable para hacer la pantalla de Stats de usuario
@Composable
fun UserStatsScreen(
    viewModel: TaskInstanceViewModel,
    userName: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.userStats.collectAsState()
    val selectedRange by viewModel.selectedTimeRange.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*Agregar hogar*/ },
                containerColor = Color(0xFFFF7000),
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = 50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "Agregar",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(Color(0xFFF7F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            //Llamada al Header para colocarlo
            UserStatsHeader(
                userName = userName,
                completedCount = uiState.completedCount,
                effectiveness = uiState.effectiveness,
                streakDays = uiState.streakDays,
                onBackClick = onBackClick
            )

            //Selector de tiempo (Año, Mes, Semana)
            TimeRangeSelector(selectedRange) { viewModel.updateTimeRange(it) }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                //Selector de Hogares
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
                            Text("${uiState.homeStats.size} miembros · Varios", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(painterResource(R.drawable.ic_dropdown), null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                //Sección Mis Hogares
                InfoCard(title = "Mis hogares", iconId = R.drawable.ic_home_orange) {
                    uiState.homeStats.forEach { home ->
                        HomeStatItem(home)
                    }
                }

                //Sección de tendencia de tareas completadas
                InfoCard(title = "Tendencia de tareas completadas", iconId = R.drawable.ic_bolt) {
                    TaskTrendLineChart(points = uiState.trendChartPoints)
                }

                Spacer(Modifier.height(24.dp))

                //Sección de Comparativa
                InfoCard(title = "Comparativa por hogar", iconId = R.drawable.ic_home_orange) {
                    HomeTaskBarChart(data = uiState.homeComparisonPoints)
                }

                Spacer(Modifier.height(24.dp))

                //Sección porcentajes generales
                SummaryGrid(uiState)

            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

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

@Composable
fun TaskTrendLineChart(points: List<ChartPoint>) {
    val chartColor = Color(0xFFFF6D00)
    val gridColor = Color(0xFFEEEEEE)

    Canvas(modifier =
        Modifier
            .fillMaxWidth()
            .height(180.dp).
            padding(top = 16.dp)
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

            //Etiquetas con los meses, días del mes o días de la semana
            drawContext.canvas.nativeCanvas.drawText(
                point.label, x, height + 20.dp.toPx(),
                android.graphics.Paint().apply { color = Color.Gray.toArgb(); textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER }
            )
        }

        drawPath(fillPath, Brush.verticalGradient(listOf(chartColor.copy(0.2f), Color.Transparent)))
        drawPath(path, chartColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun HomeTaskBarChart(data: List<HomeComparisonData>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 16.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = 30.dp.toPx()
        val spacingX = width / (data.size + 1)
        val maxVal = 500f

        data.forEachIndexed { index, item ->
            val x = (index + 1) * spacingX
            val totalTasks = (item.completedCount + item.pendingCount).toFloat()
            val totalHeight = (totalTasks / maxVal) * height
            val completedHeight = (item.completedCount / maxVal) * height

            //Barra Pendiente
            drawRoundRect(item.pendingColor,
                Offset(x - barWidth/2,
                    height - totalHeight),
                Size(barWidth, totalHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
            //Barra Completada
            drawRoundRect(item.color,
                Offset(x - barWidth/2,
                    height - completedHeight),
                Size(barWidth, completedHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                item.homeName, x, height + 20.dp.toPx(),
                android.graphics.Paint().apply { color = Color.Gray.toArgb(); textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Vista Año")
@Composable
fun UserStatsScreenPreview() {
    //Mock la lista de hogares con los colores y fondos exactos de la imagen
    val mockHomeStats = listOf(
        HomeStatsItem(
            homeName = "Mi Casa",
            completedTasks = 524,
            totalTasks = 612,
            progress = 0.86f,
            color = Color(0xFFFF6D00),
            icon = "🏡",
            iconBgColor = Color(0xFFFFF3E0)
        ),
        HomeStatsItem(
            homeName = "Casa de Mamá",
            completedTasks = 368,
            totalTasks = 396,
            progress = 0.93f,
            color = Color(0xFFA143F4),
            icon = "👸",
            iconBgColor = Color(0xFFF3E5F5)
        ),
        HomeStatsItem(
            homeName = "Apartamento",
            completedTasks = 116,
            totalTasks = 216,
            progress = 0.54f,
            color = Color(0xFF2196F3),
            icon = "🏢",
            iconBgColor = Color(0xFFE3F2FD)
        )
    )
    // Mock de puntos para la gráfica de tendencia (Imagen 7 superior)
    val mockTrendPoints = listOf(
        ChartPoint("Ene", 80f),
        ChartPoint("Feb", 72f),
        ChartPoint("Mar", 88f),
        ChartPoint("Abr", 90f),
        ChartPoint("May", 85f),
        ChartPoint("Jun", 78f),
        ChartPoint("Jul", 82f),
        ChartPoint("Ago", 91f),
        ChartPoint("Sep", 76f),
        ChartPoint("Oct", 84f),
        ChartPoint("Nov", 88f),
        ChartPoint("Dic", 93f)
    )

    //Mock de datos para la comparativa por hogar (Imagen 7 inferior)
    val mockHomeComparison = listOf(
        HomeComparisonData(
            homeName = "Mi Casa",
            completedCount = 524,
            pendingCount = 88,
            color = Color(0xFFFF6D00),
            pendingColor = Color(0xFFFF6D00).copy(alpha = 0.3f)
        ),
        HomeComparisonData(
            homeName = "Casa de Mamá",
            completedCount = 368,
            pendingCount = 28,
            color = Color(0xFFA143F4),
            pendingColor = Color(0xFFA143F4).copy(alpha = 0.3f)
        ),
        HomeComparisonData(
            homeName = "Apartamento",
            completedCount = 116, pendingCount = 100,
            color = Color(0xFF2196F3),
            pendingColor = Color(0xFF2196F3).copy(alpha = 0.3f)
        )
    )

    //Mock el estado de la UI para el año
    val mockUiState = UserStatsUiState(
        completedCount = 104,
        effectiveness = 84,
        streakDays = 7,
        homeStats = mockHomeStats,
        trendChartPoints = mockTrendPoints,
        homeComparisonPoints = mockHomeComparison
    )
    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                    containerColor = Color(0xFFFF7000),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .offset(y = 50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Agregar",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .background(Color(0xFFF7F9FA))
                    .verticalScroll(rememberScrollState())
            ) {
                UserStatsHeader(
                    userName = "María",
                    completedCount = mockUiState.completedCount,
                    effectiveness = mockUiState.effectiveness,
                    streakDays = mockUiState.streakDays,
                    onBackClick = {}
                )
                //Selector de tiempo
                TimeRangeSelector(selected = "Año", onSelected = {})
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    //Selector de Hogares
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
                                Text("12 miembros · Varios", color = Color.Gray, fontSize = 12.sp)
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    InfoCard(title = "Mis hogares", iconId = R.drawable.ic_home_orange) {
                        mockHomeStats.forEach { home ->
                            HomeStatItem(home)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    //Sección Tendencia
                    InfoCard(title = "Tendencia de tareas completadas", iconId = R.drawable.ic_bolt) {
                        TaskTrendLineChart(points = mockTrendPoints)
                    }

                    Spacer(Modifier.height(24.dp))

                    //Sección Comparativa
                    InfoCard(title = "Comparativa por hogar", iconId = R.drawable.ic_home_orange) {
                        HomeTaskBarChart(data = mockHomeComparison)
                    }

                    Spacer(Modifier.height(24.dp))

                    //Sección porcentajes generales
                    SummaryGrid(mockUiState)
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

//Segundo preview para la vista de Mes
@Preview(showBackground = true, showSystemUi = true, name = "Vista Mes")
@Composable
fun UserStatsScreenMonthPreview() {
    val mockHomeStatsMonth = listOf(
        HomeStatsItem(
            "Mi Casa",
            45,
            50,
            0.90f,
            Color(0xFFFF6D00),
            "🏡",
            Color(0xFFFFF3E0)
        ),
        HomeStatsItem(
            "Casa de Mamá",
            20,
            25,
            0.80f,
            Color(0xFFA143F4),
            "👸",
            Color(0xFFF3E5F5)
        ),
        HomeStatsItem(
            "Apartamento",
            10,
            30,
            0.33f,
            Color(0xFF2196F3),
            "🏢",
            Color(0xFFE3F2FD)
        )
    )

    val mockTrendPointsMonth = listOf(
        ChartPoint("1", 40f),
        ChartPoint("5", 65f),
        ChartPoint("10", 50f),
        ChartPoint("15", 80f),
        ChartPoint("20", 75f),
        ChartPoint("25", 90f),
        ChartPoint("30", 85f)
    )

    val mockHomeComparisonMonth = listOf(
        HomeComparisonData(
            "Mi Casa",
            45,
            5,
            Color(0xFFFF6D00),
            Color(0xFFFF6D00).copy(alpha = 0.3f)
        ),
        HomeComparisonData(
            "Casa Mamá",
            20,
            5,
            Color(0xFFA143F4),
            Color(0xFFA143F4).copy(alpha = 0.3f)
        ),
        HomeComparisonData(
            "Apto",
            10,
            20,
            Color(0xFF2196F3),
            Color(0xFF2196F3).copy(alpha = 0.3f)
        )
    )

    val mockUiStateMonth = UserStatsUiState(
        completedCount = 75,
        effectiveness = 82,
        streakDays = 31,
        homeStats = mockHomeStatsMonth,
        trendChartPoints = mockTrendPointsMonth,
        homeComparisonPoints = mockHomeComparisonMonth
    )

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                    containerColor = Color(0xFFFF7000),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp).offset(y = 50.dp)
                ) {
                    Icon(painterResource(id = R.drawable.ic_plus), "Agregar", Modifier.size(28.dp), tint = Color.White)
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .background(Color(0xFFF7F9FA))
                    .verticalScroll(rememberScrollState())
            ) {
                UserStatsHeader("María", 75, 82, 31, {})
                TimeRangeSelector(selected = "Mes", onSelected = {})
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), color = Color.White) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏠", fontSize = 20.sp); Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Todos los hogares", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("3 hogares activos", color = Color.Gray, fontSize = 12.sp)
                            }
                            Icon(painterResource(id = R.drawable.ic_dropdown), null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    InfoCard(title = "Mis hogares (Mes)", iconId = R.drawable.ic_home_orange) {
                        mockHomeStatsMonth.forEach { HomeStatItem(it) }
                    }
                    Spacer(Modifier.height(24.dp))
                    InfoCard(title = "Tendencia de tareas (Mes)", iconId = R.drawable.ic_bolt) {
                        TaskTrendLineChart(points = mockTrendPointsMonth)
                    }
                    Spacer(Modifier.height(24.dp))
                    InfoCard(title = "Comparativa por hogar (Mes)", iconId = R.drawable.ic_home_orange) {
                        HomeTaskBarChart(data = mockHomeComparisonMonth)
                    }
                    Spacer(Modifier.height(24.dp))
                    SummaryGrid(mockUiStateMonth)
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

//Tercer preview para la vista de Semana
@Preview(showBackground = true, showSystemUi = true, name = "Vista Semana")
@Composable
fun UserStatsScreenWeekPreview() {
    val mockHomeStatsWeek = listOf(
        HomeStatsItem(
            "Mi Casa",
            12,
            15,
            0.80f,
            Color(0xFFFF6D00),
            "🏡",
            Color(0xFFFFF3E0)
        ),
        HomeStatsItem(
            "Casa de Mamá",
            8,
            8,
            1.00f,
            Color(0xFFA143F4),
            "👸",
            Color(0xFFF3E5F5)
        ),
        HomeStatsItem(
            "Apartamento",
            4,
            10,
            0.40f,
            Color(0xFF2196F3),
            "🏢",
            Color(0xFFE3F2FD)
        )
    )

    val mockTrendPointsWeek = listOf(
        ChartPoint("Lun", 60f),
        ChartPoint("Mar", 85f),
        ChartPoint("Mié", 70f),
        ChartPoint("Jue", 90f),
        ChartPoint("Vie", 75f),
        ChartPoint("Sáb", 95f),
        ChartPoint("Dom", 80f)
    )

    val mockHomeComparisonWeek = listOf(
        HomeComparisonData(
            "Mi Casa",
            12,
            3,
            Color(0xFFFF6D00),
            Color(0xFFFF6D00).copy(alpha = 0.3f)
        ),
        HomeComparisonData(
            "Casa Mamá",
            8,
            0,
            Color(0xFFA143F4),
            Color(0xFFA143F4).copy(alpha = 0.3f)
        ),
        HomeComparisonData(
            "Apto",
            4,
            6,
            Color(0xFF2196F3),
            Color(0xFF2196F3).copy(alpha = 0.3f)
        )
    )

    val mockUiStateWeek = UserStatsUiState(
        completedCount = 24,
        effectiveness = 73,
        streakDays = 5,
        homeStats = mockHomeStatsWeek,
        trendChartPoints = mockTrendPointsWeek,
        homeComparisonPoints = mockHomeComparisonWeek
    )

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                    containerColor = Color(0xFFFF7000),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp).offset(y = 50.dp)
                ) {
                    Icon(painterResource(id = R.drawable.ic_plus), "Agregar", Modifier.size(28.dp), tint = Color.White)
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .background(Color(0xFFF7F9FA))
                    .verticalScroll(rememberScrollState())
            ) {
                UserStatsHeader("María", 24, 73, 5, {})
                TimeRangeSelector(selected = "Semana", onSelected = {})
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), color = Color.White) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏠", fontSize = 20.sp); Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Todos los hogares", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("3 hogares activos", color = Color.Gray, fontSize = 12.sp)
                            }
                            Icon(painterResource(id = R.drawable.ic_dropdown), null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    InfoCard(title = "Mis hogares (Semana)", iconId = R.drawable.ic_home_orange) {
                        mockHomeStatsWeek.forEach { HomeStatItem(it) }
                    }
                    Spacer(Modifier.height(24.dp))
                    InfoCard(title = "Tendencia de tareas (Semana)", iconId = R.drawable.ic_bolt) {
                        TaskTrendLineChart(points = mockTrendPointsWeek)
                    }
                    Spacer(Modifier.height(24.dp))
                    InfoCard(title = "Comparativa por hogar (Semana)", iconId = R.drawable.ic_home_orange) {
                        HomeTaskBarChart(data = mockHomeComparisonWeek)
                    }
                    Spacer(Modifier.height(24.dp))
                    SummaryGrid(mockUiStateWeek)
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}