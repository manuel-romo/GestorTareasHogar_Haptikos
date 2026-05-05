package haptikos.gestortareashogar_haptikos.ui.screens.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

@Composable
fun RewardsScreen(
    taskInstanceViewModel: TaskInstanceViewModel,
    onBackClick: () -> Unit
) {
    val stats by taskInstanceViewModel.stats.collectAsState()
    val tasks by taskInstanceViewModel.tasks.collectAsState()

    //Variable para controlar la pestaña seleccionada
    var selectedTab by remember { mutableStateOf("Resumen") }

    //Filtro para las tareas completadas para la sección de "Puntos recientes"
    val completedTasks = tasks.filter { it.taskInstance.state == TaskState.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        //Header con puntos del usuario
        RewardsHeader(
            points = stats.userPoints,
            progress = stats.dailyProgress,
            onBackClick = onBackClick
        )

        //Selector de secciones (Ahora funcional)
        RewardsTabSelector(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            if (selectedTab == "Resumen") {
                InfoCard(title = "¿Cómo ganar puntos?", iconId = R.drawable.ic_bolt) {
                    RewardEarningRow(
                        "Tarea completada",
                        "+10 pts",
                        Color(0xFFE8F5E9),
                        Color(0xFF4CAF50)
                    )
                    RewardEarningRow(
                        "Tarea de alta prioridad",
                        "+5 pts",
                        Color(0xFFFFF3E0),
                        Color(0xFFFF9800)
                    )
                    RewardEarningRow(
                        "Completar el día entero",
                        "+25 pts",
                        Color(0xFFFFF9C4),
                        Color(0xFFFBC02D)
                    )
                    RewardEarningRow(
                        "Racha semanal (7 días)",
                        "+25 pts",
                        Color(0xFFFFEBEE),
                        Color(0xFFE91E63)
                    )
                    RewardEarningRow(
                        "Invitar un miembro",
                        "+15 pts",
                        Color(0xFFE3F2FD),
                        Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                //Sección del historial
                InfoCard(title = "Puntos recientes", iconId = R.drawable.ic_star) {
                    if (completedTasks.isEmpty()) {
                        Text("Aún no has completado tareas hoy", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        completedTasks.take(5).forEach { taskWithDetails ->
                            RecentPointRow(
                                title = taskWithDetails.task.title,
                                points = "+${taskWithDetails.task.points}",
                                iconId = R.drawable.ic_check_circle
                            )
                        }
                    }
                }
            } else if (selectedTab == "Insignias") {
                //Sección de Insignias integrada
                BadgesContent()
            } else if (selectedTab == "Ranking") {
                //Sección de Ranking integrada
                RankingContent()
            } else if (selectedTab == "Retos") {
                //Sección de Retos integrada
                ChallengesContent()
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RewardEarningRow(
    title: String,
    points: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 14.sp, color = Color.DarkGray)

        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = points,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

//Función composable extra para crear una card con información
@Composable
fun InfoCard(
    title: String,
    iconId: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

//Función composable para el selector de pestañas
@Composable
fun RewardsTabSelector(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Resumen", "Insignias", "Ranking", "Retos")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(tabs) { tab ->
            val isSelected = tab == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable() { onTabSelected(tab) }
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) Color(0xFFFF6D00) else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(16.dp)
                            .height(3.dp)
                            .background(Color(0xFFFF6D00), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentPointRow(title: String, points: String, iconId: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(iconId), null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        Text(points, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

//Sección para las insignias
@Composable
fun BadgesContent() {
    Column {
        //Contador de progreso
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏆", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text("4 de 9 desbloqueadas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D3142))
            }
            Text("🔥🏆🌟👑", fontSize = 14.sp)
        }

        SectionBadgeTitle("DESBLOQUEADAS")
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(
                "Racha Ardiente",
                "15 mar",
                "🔥",
                true, Modifier.weight(1f)
            )
            BadgeCard(
                "Primer Hogar",
                "1 ene",
                "🏆",
                true,
                Modifier.weight(1f)
            )
            BadgeCard(
                "Centenario",
                "20 feb",
                "🌟",
                true,
                Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(
                "Líder del Mes",
                "28 mar",
                "👑",
                true,
                Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionBadgeTitle("POR DESBLOQUEAR")
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(
                "Turbo",
                null,
                "",
                false,
                Modifier.weight(1f)
            )
            BadgeCard(
                "Mano Verde",
                null,
                "",
                false,
                Modifier.weight(1f)
            )
            BadgeCard(
                "Puntual",
                null,
                "",
                false,
                Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(
                "Social",
                null,
                "",
                false,
                Modifier.weight(1f)
            )
            BadgeCard(
                "Diamante",
                null,
                "",
                false,
                Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

//Sección para el ranking
@Composable
fun RankingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        //Banner informativo superior
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFF3E0),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFFE0B2))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ranking mensual del hogar · Mi Casa",
                    fontSize = 13.sp,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //Visualización del Podio
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            //Segundo lugar (mock)
            PodiumItem(
                name = "Pedro",
                points = 610,
                color = Color(0xFF00C853),
                initial = "P",
                rankIcon = "🥈",
                height = 70.dp,
                boxColor = Color(0xFFE0E4E8)
            )

            Spacer(modifier = Modifier.width(12.dp))

            //Primer lugar (mock)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👑", fontSize = 24.sp)
                PodiumItem(
                    name = "María",
                    points = 680,
                    color = Color(0xFFF014A8),
                    initial = "M",
                    rankIcon = "🥇",
                    height = 95.dp,
                    boxColor = Color(0xFFFFC107)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            //Tercer lugar (mock)
            PodiumItem(
                name = "Ana",
                points = 540,
                color = Color(0xFF7C4DFF),
                initial = "A",
                rankIcon = "🥉",
                height = 55.dp,
                boxColor = Color(0xFFFFD1A4)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        //Lista del ranking
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 2.dp
        ) {
            Column {
                RankingRow(
                    "🥇",
                    "M",
                    "María",
                    "(tú)",
                    "52 tareas completadas",
                    680, Color(0xFFF014A8), isMe = true
                )
                RankingRow(
                    "🥈",
                    "P",
                    "Pedro",
                    "",
                    "47 tareas completadas",
                    610, Color(0xFF00C853)
                )
                RankingRow(
                    "🥉",
                    "A",
                    "Ana",
                    "",
                    "41 tareas completadas",
                    540, Color(0xFF7C4DFF)
                )
                RankingRow(
                    "4",
                    "J",
                    "Juan",
                    "",
                    "35 tareas completadas",
                    420, Color(0xFF2979FF)
                )
            }
        }
    }
}

//Sección para los retos
@Composable
fun ChallengesContent() {
    Column {
        //Banner superior
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF7E57C2),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bolt),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Retos semanales",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Completa estos retos para ganar puntos extra 🎯",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        //Cards de los retos
        ChallengeCard(
            title = "Semana Perfecta",
            reward = "+50",
            description = "Completa todas las tareas del día durante 5 días seguidos",
            progress = 3f,
            total = 5f,
            timeLeft = "Quedan 4 días",
            icon = "🏅"
        )
        ChallengeCard(
            title = "Limpieza Profunda",
            reward = "+30",
            description = "Completa 3 tareas de baños esta semana",
            progress = 1f,
            total = 3f,
            timeLeft = "Quedan 2 días",
            icon = "🧽"
        )
        ChallengeCard(
            title = "Madrugador",
            reward = "+20",
            description = "Completa una tarea antes de las 9 am por 3 días",
            progress = 2f,
            total = 3f,
            timeLeft = "Quedan 5 días",
            icon = "🌅"
        )
        ChallengeCard(
            title = "Primer Invitado",
            reward = "+15",
            description = "Invita a un nuevo miembro al hogar",
            progress = 1f,
            total = 1f,
            timeLeft = "",
            icon = "✉️",
            isCompleted = true
        )
    }
}

@Composable
fun ChallengeCard(
    title: String,
    reward: String,
    description: String,
    progress: Float,
    total: Float,
    timeLeft: String,
    icon: String,
    isCompleted: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = if (isCompleted) Color(0xFFE8F5E9) else Color.White,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(icon, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
                Text(
                    text = if (isCompleted) "$reward pts ✓" else "$reward ⭐",
                    color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFFB300),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (!isCompleted) {
                Spacer(modifier = Modifier.height(16.dp))

                //Barra de progreso y texto de conteo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress / total },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF7E57C2),
                        trackColor = Color(0xFFF0F0F0)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${progress.toInt()}/${total.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                if (timeLeft.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = timeLeft,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumItem(
    name: String,
    points: Int,
    color: Color,
    initial: String,
    rankIcon: String,
    height: Dp,
    boxColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = color,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.width(65.dp).height(height),
            color = boxColor,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(rankIcon, fontSize = 16.sp)
                Text(points.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if(rankIcon == "🥇") Color.White else Color.Black)
            }
        }
    }
}

@Composable
fun RankingRow(
    rank: String,
    initial: String,
    name: String,
    suffix: String,
    tasks: String,
    points: Int,
    color: Color,
    isMe: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isMe) Color(0xFFFFF8E1) else Color.Transparent)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            rank,
            modifier = Modifier.width(28.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = color
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (suffix.isNotEmpty()) {
                    Text(" $suffix", color = Color.Gray, fontSize = 13.sp)
                }
            }
            Text(tasks, color = Color.Gray, fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(points.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun BadgeCard(name: String, date: String?, icon: String, isUnlocked: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(135.dp),
        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.6f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(Modifier
            .fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(46.dp).background(if (isUnlocked) Color(0xFFFFF3E0) else Color(0xFFF5F5F5), CircleShape), contentAlignment = Alignment.Center) {
                if (isUnlocked) Text(icon, fontSize = 22.sp) else Icon(painterResource(R.drawable.ic_clock), null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 14.sp)
            date?.let { Text(it, fontSize = 10.sp, color = Color.LightGray) }
        }
    }
}

@Composable
fun SectionBadgeTitle(title: String) {
    Text(title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.LightGray, letterSpacing = 1.sp, modifier = Modifier.padding(vertical = 12.dp))
}

//Preview de la pantalla de recompensas (todo esto es de prueba)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RewardsScreenPreview() {
    //Crear un estado de estadísticas de prueba
    val mockStats = TaskInstanceViewModel.DashboardStats(
        pendingTasksCount = 5,
        completedTasksCount = 3,
        totalTasks = 8,
        dailyProgress = 0.36f,
        userPoints = 680
    )

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F9FA)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                //Header con datos de prueba
                RewardsHeader(
                    points = mockStats.userPoints,
                    progress = mockStats.dailyProgress,
                    onBackClick = {}
                )

                //En Preview se simula la selección de "Retos" para ver el diseño final
                RewardsTabSelector(selectedTab = "Retos", onTabSelected = {})

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    ChallengesContent()
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}