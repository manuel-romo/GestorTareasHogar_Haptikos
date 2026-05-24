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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import haptikos.gestortareashogar_haptikos.ui.theme.BrightOrange
import haptikos.gestortareashogar_haptikos.ui.theme.DarkAmber
import haptikos.gestortareashogar_haptikos.ui.theme.DarkBlue
import haptikos.gestortareashogar_haptikos.ui.theme.DarkText
import haptikos.gestortareashogar_haptikos.ui.theme.DeepOrange
import haptikos.gestortareashogar_haptikos.ui.theme.LightAmber
import haptikos.gestortareashogar_haptikos.ui.theme.LightBlue
import haptikos.gestortareashogar_haptikos.ui.theme.LightYellow
import haptikos.gestortareashogar_haptikos.ui.theme.MediumDarkGray
import haptikos.gestortareashogar_haptikos.ui.theme.Purple
import haptikos.gestortareashogar_haptikos.ui.theme.SilverGray
import haptikos.gestortareashogar_haptikos.ui.theme.White
import haptikos.gestortareashogar_haptikos.ui.theme.Yellow
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RewardViewModel
import java.util.Calendar

@Composable
fun RewardsScreen(
    taskInstanceViewModel: TaskInstanceViewModel,
    homeViewModel: HomeViewModel,
    rewardViewModel: RewardViewModel,
    onBackClick: () -> Unit
) {
    val rewardsState by rewardViewModel.rewardsState.collectAsState()
    val tasks by taskInstanceViewModel.tasks.collectAsState()
    val selectedHome by homeViewModel.selectedHome.collectAsState()

    LaunchedEffect(selectedHome?.id) {
        taskInstanceViewModel.setSelectedHome(selectedHome?.id)
        rewardViewModel.setSelectedHome(selectedHome?.id)
    }


    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val completedTasks = remember(tasks) {
        tasks.filter {
            it.taskInstance.state == TaskState.COMPLETED &&
                    it.taskInstance.dueDate >= todayStart
        }
    }

    var selectedTab by remember { mutableStateOf("Resumen") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            RewardsHeader(
                points = rewardsState.totalPoints,
                progress = rewardsState.dailyProgress,
                onBackClick = onBackClick
            )
            RewardsTabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                when (selectedTab) {
                    "Resumen" -> {
                        InfoCard(title = "¿Cómo ganar puntos?", iconId = R.drawable.ic_bolt) {
                            RewardEarningRow("Tarea completada", "+10 pts", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiary)
                            RewardEarningRow("Tarea de alta prioridad", "+5 pts", LightYellow, DarkAmber)
                            RewardEarningRow("Completar el día entero", "+25 pts", LightYellow, Yellow)
                            RewardEarningRow("Racha semanal (7 días)", "+25 pts", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
                            RewardEarningRow("Invitar un miembro", "+15 pts", LightBlue, DarkBlue)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        InfoCard(title = "Puntos recientes", iconId = R.drawable.ic_star) {
                            if (completedTasks.isEmpty()) {
                                Text("Aún no has completado tareas hoy", color = MediumDarkGray, fontSize = 14.sp)
                            } else {
                                completedTasks.take(5).forEach { taskWithDetails ->
                                    RecentPointRow(
                                        title = taskWithDetails.taskDetails.task.title,
                                        points = "+${taskWithDetails.taskDetails.task.points}",
                                        iconId = R.drawable.ic_check_circle
                                    )
                                }
                            }
                        }
                    }
                    "Insignias" -> BadgesContent(badgeList = rewardsState.badges)
                    "Ranking" -> RankingContent(rankingList = rewardsState.rankingList)
                    "Retos" -> ChallengesContent(challengeList = rewardsState.challenges)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
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
fun InfoCard(title: String, iconId: Int, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = White, shape = RoundedCornerShape(24.dp), shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = iconId), null, tint = BrightOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

//Función composable para el selector de pestañas
@Composable
fun RewardsTabSelector(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Resumen", "Insignias", "Ranking", "Retos")
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(tabs) { tab ->
            val isSelected = tab == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onTabSelected(tab) }
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MediumDarkGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (isSelected) {
                    Box(modifier = Modifier.padding(top = 4.dp).width(16.dp).height(3.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
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
            Icon(painterResource(iconId), null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DarkText)
        }
        Text(points, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

//Sección para las insignias
@Composable
fun BadgesContent(badgeList: List<BadgeItem>) {
    val unlocked = badgeList.filter { it.isUnlocked }
    val locked = badgeList.filter { !it.isUnlocked }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏆", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text("${unlocked.size} de ${badgeList.size} desbloqueadas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D3142))
            }
        }

        if (unlocked.isNotEmpty()) {
            SectionBadgeTitle("DESBLOQUEADAS")
            unlocked.chunked(3).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { badge ->
                        BadgeCard(badge.name, badge.dateUnlocked, badge.icon, true, Modifier.weight(1f))
                    }
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        if (locked.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            SectionBadgeTitle("POR DESBLOQUEAR")
            locked.chunked(3).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { badge ->
                        BadgeCard(badge.name, null, badge.icon, false, Modifier.weight(1f))
                    }
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

//Sección para el ranking
@Composable
fun RankingContent(rankingList: List<RankingMemberItem>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        //Banner informativo superior
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LightYellow,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, LightAmber)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = R.drawable.ic_home), null, tint = BrightOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ranking mensual del hogar", fontSize = 13.sp, color = DeepOrange, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        //Ya se supone que se debe ver el podio bien ahora si
        if (rankingList.size >= 2) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // Segundo lugar
                PodiumItem(
                    name = rankingList[1].name.split(" ").first(),
                    points = rankingList[1].points,
                    color = Color(android.graphics.Color.parseColor(rankingList[1].colorHex)),
                    initial = rankingList[1].name.first().toString(),
                    rankIcon = "🥈",
                    height = 70.dp,
                    boxColor = Color(0xFFE0E4E8)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Primer lugar
                if (rankingList.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👑", fontSize = 24.sp)
                        PodiumItem(
                            name = rankingList[0].name.split(" ").first(),
                            points = rankingList[0].points,
                            color = Color(android.graphics.Color.parseColor(rankingList[0].colorHex)),
                            initial = rankingList[0].name.first().toString(),
                            rankIcon = "🥇",
                            height = 95.dp,
                            boxColor = Color(0xFFFFC107)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Tercer lugar
                if (rankingList.size >= 3) {
                    PodiumItem(
                        name = rankingList[2].name.split(" ").first(),
                        points = rankingList[2].points,
                        color = Color(android.graphics.Color.parseColor(rankingList[2].colorHex)),
                        initial = rankingList[2].name.first().toString(),
                        rankIcon = "🥉",
                        height = 55.dp,
                        boxColor = Color(0xFFFFD1A4)
                    )
                }
            }
        } else if (rankingList.isEmpty()) {
            Text("No hay datos de ranking aún", color = Color.Gray, modifier = Modifier.padding(32.dp))
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
                rankingList.forEach { item ->
                    val rankLabel = when(item.position) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> item.position.toString()
                    }

                    RankingRow(
                        rank = rankLabel,
                        initial = item.name.first().toString(),
                        name = item.name,
                        suffix = if (item.isCurrentUser) "(tú)" else "",
                        tasks = "Puntos acumulados",
                        points = item.points,
                        color = Color(android.graphics.Color.parseColor(item.colorHex)),
                        isMe = item.isCurrentUser
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengesContent(challengeList: List<ChallengeItem>) {
    Column {
        Surface(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            color = Color(0xFF7E57C2),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_bolt), null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Retos semanales", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Completa estos retos para ganar puntos extra 🎯", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        challengeList.forEach { challenge ->
            ChallengeCard(
                title = challenge.title,
                reward = "+${challenge.rewardPoints}",
                description = challenge.description,
                progress = challenge.currentProgress.toFloat(),
                total = challenge.totalGoal.toFloat(),
                timeLeft = "Semana actual",
                icon = challenge.icon,
                isCompleted = challenge.isCompleted
            )
        }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SilverGray)
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
                    color = if (isCompleted) MaterialTheme.colorScheme.tertiary else DarkAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = description, color = MediumDarkGray, fontSize = 13.sp, lineHeight = 18.sp)
            if (!isCompleted) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress / total },
                        modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                        color = Purple,
                        trackColor = SilverGray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("${progress.toInt()}/${total.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MediumDarkGray)
                }
                if (timeLeft.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = timeLeft, fontSize = 11.sp, color = MediumDarkGray)
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
            .background(if (isMe) LightYellow else Color.Transparent)
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
    Text(
        text = title,
        color = MediumDarkGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

//Preview de la pantalla de recompensas
@Preview(showBackground = true, showSystemUi = true, name = "Vista Año - Resumen")
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
                RewardsHeader(
                    points = mockStats.userPoints,
                    progress = mockStats.dailyProgress,
                    onBackClick = {}
                )

                RewardsTabSelector(selectedTab = "Resumen", onTabSelected = {})

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // Contenido de resumen con InfoCards
                    InfoCard(title = "¿Cómo ganar puntos?", iconId = R.drawable.ic_bolt) {
                        RewardEarningRow("Tarea completada", "+10 pts", Color(0xFFE8F5E9), Color(0xFF4CAF50))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Vista Ranking Mock")
@Composable
fun RewardsScreenRankingPreview() {
    // Mock de lista de ranking
    val mockRanking = listOf(
        RankingMemberItem("1", "María", 680, "#F014A8", 1, true),
        RankingMemberItem("2", "Pedro", 610, "#00C853", 2, false),
        RankingMemberItem("3", "Ana", 540, "#7C4DFF", 3, false),
        RankingMemberItem("4", "Juan", 420, "#2979FF", 4, false)
    )

    haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            RewardsHeader(points = 680, progress = 0.84f, onBackClick = {})
            RewardsTabSelector(selectedTab = "Ranking", onTabSelected = {})
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                RankingContent(rankingList = mockRanking)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}