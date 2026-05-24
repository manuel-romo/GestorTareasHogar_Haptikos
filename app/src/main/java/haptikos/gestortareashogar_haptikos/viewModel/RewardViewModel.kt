package haptikos.gestortareashogar_haptikos.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.entity.ChallengeProgressEntity
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.data.enumerators.ChallengeType
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.BadgeItem
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.ChallengeItem
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.RankingMemberItem
import haptikos.gestortareashogar_haptikos.ui.screens.rewards.RewardsUiState
import haptikos.gestortareashogar_haptikos.utils.RewardsUtils
import haptikos.gestortareashogar_haptikos.utils.WeekUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

class RewardViewModel(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    data class ChallengeProgressEvent(
        val icon: String,
        val title: String,
        val current: Int,
        val total: Int,
        val isCompleted: Boolean
    )

    private val _challengeEvents = MutableSharedFlow<List<ChallengeProgressEvent>>(
        extraBufferCapacity = 1
    )
    val challengeEvents: SharedFlow<List<ChallengeProgressEvent>> = _challengeEvents.asSharedFlow()

    private val _selectedHomeId = MutableStateFlow<String?>(null)

    fun setSelectedHome(homeId: String?) {
        _selectedHomeId.value = homeId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val rewardsState: StateFlow<RewardsUiState> = combine(
        repository.allInstancesWithDetails,
        _selectedHomeId,
        dataStore.userIdFlow
    ) { allInstances, homeId, currentUserId ->
        buildRewardsUiState(allInstances, homeId, currentUserId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RewardsUiState())

    init {
        viewModelScope.launch {
            combine(
                repository.allInstancesWithDetails,
                _selectedHomeId,
                dataStore.userIdFlow
            ) { instances, homeId, userId ->
                Triple(instances, homeId, userId)
            }.collect { (instances, homeId, userId) ->
                if (homeId != null && userId.isNotEmpty()) {
                    updateChallengeProgress(instances, homeId, userId)
                }
            }
        }
    }

    private suspend fun updateChallengeProgress(
        allInstances: List<TaskInstanceWithDetails>,
        homeId: String,
        userId: String
    ) {
        val weekId = WeekUtils.getCurrentWeekId()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
        }
        val weekStart = cal.timeInMillis
        val weekEnd = weekStart + 7 * 86_400_000L

        val challenges = calculateWeeklyChallenges(allInstances, userId, homeId)
        val events = mutableListOf<ChallengeProgressEvent>()

        challenges.forEach { challenge ->
            val challengeType = ChallengeType.entries.find {
                it.title == challenge.title
            } ?: return@forEach

            val existing = repository.getChallengeByTypeAndWeek(userId, homeId, challengeType, weekId)
            val wasCompleted = existing?.isCompleted == true
            val alreadyAwarded = existing?.pointsAwarded == true
            val existingProgress = existing?.currentProgress ?: 0

            val newProgress = when (challengeType) {
                ChallengeType.COMPLETE_5_TASKS ->
                    repository.getEarnedCountForWeek(userId, weekStart, weekEnd).coerceAtMost(challenge.totalGoal)
                ChallengeType.HIGH_PRIORITY ->
                    repository.getHighPriorityCountForWeek(userId, weekStart, weekEnd).coerceAtMost(challenge.totalGoal)
                else -> challenge.currentProgress
            }

            val isNowCompleted = newProgress >= challenge.totalGoal
            val shouldRevokeAward = alreadyAwarded && !isNowCompleted

            if (newProgress > existingProgress && !wasCompleted) {
                events.add(
                    ChallengeProgressEvent(
                        icon = challenge.icon,
                        title = challenge.title,
                        current = newProgress,
                        total = challenge.totalGoal,
                        isCompleted = isNowCompleted
                    )
                )
            }

            repository.upsertChallengeProgress(
                ChallengeProgressEntity(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    userId = userId,
                    homeId = homeId,
                    challengeType = challengeType,
                    weekId = weekId,
                    currentProgress = newProgress,
                    isCompleted = isNowCompleted,
                    pointsAwarded = if (shouldRevokeAward) false
                    else alreadyAwarded || (isNowCompleted && !wasCompleted),
                    completedAt = when {
                        isNowCompleted && !wasCompleted -> System.currentTimeMillis()
                        !isNowCompleted -> null
                        else -> existing?.completedAt
                    }
                )
            )
        }

        if (events.isNotEmpty()) {
            _challengeEvents.tryEmit(events)
        }
    }

    private suspend fun buildRewardsUiState(
        allInstances: List<TaskInstanceWithDetails>,
        homeId: String?,
        currentUserId: String
    ): RewardsUiState {
        val weekId = WeekUtils.getCurrentWeekId()

        val taskPoints = if (currentUserId.isNotEmpty()) {
            repository.getTotalEarnedPoints(currentUserId)
        } else 0

        val challengePoints = if (homeId != null && currentUserId.isNotEmpty()) {
            repository.getChallengeProgressForWeek(currentUserId, homeId, weekId)
                .filter { it.pointsAwarded }
                .sumOf { it.challengeType.rewardPoints }
        } else 0

        val totalPoints = taskPoints + challengePoints

        // Ranking
        val earnedPerMember = repository.getEarnedPointsPerMember(homeId)

        // Se obtiene la información de miembros desde las instancias activas
        val memberInfoMap = allInstances
            .flatMap { it.assignedMembers }
            .distinctBy { it.userId }
            .associateBy { it.userId }

        val ranking = earnedPerMember
            .sortedByDescending { it.second }
            .mapIndexedNotNull { index, (userId, pts) ->
                val member = memberInfoMap[userId] ?: return@mapIndexedNotNull null
                RankingMemberItem(
                    memberId = member.id,
                    name = "${member.name} ${member.lastName}".trim(),
                    points = pts,
                    colorHex = member.colorHex,
                    position = index + 1,
                    isCurrentUser = member.userId == currentUserId
                )
            }

        val completedInstances = allInstances.filter {
            it.taskInstance.state == TaskState.COMPLETED &&
                    (homeId == null || it.taskDetails.task.homeId == homeId)
        }

        val userCompleted = completedInstances.filter { instance ->
            instance.assignedMembers.any { it.userId == currentUserId }
        }
        val streakDays = RewardsUtils.calculateStreak(
            allInstances.filter { it.assignedMembers.any { m -> m.userId == currentUserId } }
        )

        return RewardsUiState(
            totalPoints = totalPoints,
            dailyProgress = calculateDailyProgress(allInstances, currentUserId),
            rankingList = ranking,
            badges = calculateBadges(userCompleted, streakDays),
            challenges = calculateWeeklyChallenges(allInstances, currentUserId, homeId)
        )
    }

    private fun calculateDailyProgress(
        allInstances: List<TaskInstanceWithDetails>,
        currentUserId: String
    ): Float {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 86_400_000L

        val todayTasks = allInstances.filter { instance ->
            instance.assignedMembers.any { it.userId == currentUserId } &&
                    instance.taskInstance.dueDate in todayStart until todayEnd
        }

        if (todayTasks.isEmpty()) return 0f
        val completed = todayTasks.count { it.taskInstance.state == TaskState.COMPLETED }
        return completed.toFloat() / todayTasks.size.toFloat()
    }

    private fun calculateBadges(
        userCompleted: List<TaskInstanceWithDetails>,
        streakDays: Int
    ): List<BadgeItem> {
        val totalCompleted = userCompleted.size
        val highPriorityCompleted = userCompleted.count {
            it.taskDetails.task.priority == PriorityLevel.ALTA
        }

        // Fecha de hoy para el dateUnlocked
        val sdf = SimpleDateFormat("MMM yyyy", java.util.Locale("es", "ES"))
        val today = sdf.format(java.util.Date()).replaceFirstChar { it.uppercase() }

        return listOf(
            BadgeItem(
                name = "Primer logro",
                icon = "⭐",
                isUnlocked = totalCompleted >= 1,
                dateUnlocked = if (totalCompleted >= 1) today else null
            ),
            BadgeItem(
                name = "7 días seguidos",
                icon = "🔥",
                isUnlocked = streakDays >= 7,
                dateUnlocked = if (streakDays >= 7) today else null
            ),
            BadgeItem(
                name = "10 tareas",
                icon = "✅",
                isUnlocked = totalCompleted >= 10,
                dateUnlocked = if (totalCompleted >= 10) today else null
            ),
            BadgeItem(
                name = "Alta prioridad",
                icon = "🎯",
                isUnlocked = highPriorityCompleted >= 5,
                dateUnlocked = if (highPriorityCompleted >= 5) today else null
            ),
            BadgeItem(
                name = "Perfeccionista",
                icon = "💎",
                isUnlocked = totalCompleted >= 50,
                dateUnlocked = if (totalCompleted >= 50) today else null
            ),
            BadgeItem(
                name = "Madrugador",
                icon = "🌅",
                isUnlocked = streakDays >= 30,
                dateUnlocked = if (streakDays >= 30) today else null
            )
        )
    }


    private fun calculateWeeklyChallenges(
        allInstances: List<TaskInstanceWithDetails>,
        currentUserId: String,
        homeId: String?
    ): List<ChallengeItem> {

        Log.d("DEBUG_RETOS", "currentUserId=$currentUserId | homeId=$homeId")

        allInstances.filter {
            it.taskInstance.state == TaskState.COMPLETED
        }.forEach { inst ->
            Log.d("DEBUG_RETOS", "inst=${inst.taskInstance.id.take(8)} | " +
                    "members=${inst.assignedMembers.map { "'${it.name}' id=${it.id.take(8)} userId=${it.userId.take(8)}" }}"
            )
        }
        // Inicio y fin de la semana actual (lunes a domingo)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        // Retroceder al lunes
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = (dayOfWeek + 5) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        val weekStart = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 7)
        val weekEnd = cal.timeInMillis

        // Instancias completadas por el usuario esta semana
        Log.d("RewardVM", "Semana: $weekStart → $weekEnd")
        Log.d("RewardVM", "Total instancias recibidas: ${allInstances.size}")

        val weekCompleted = allInstances.filter { instance ->
            val completedAt = instance.taskInstance.completedAt ?: instance.taskInstance.dueDate
            val inRange = completedAt in weekStart until weekEnd
            val isUser = instance.assignedMembers.any { it.userId == currentUserId }
            val isHome = homeId == null || instance.taskDetails.task.homeId == homeId
            val isDone = instance.taskInstance.state == TaskState.COMPLETED

            Log.d("RewardVM", "inst=${instance.taskInstance.id.take(6)} | " +
                    "completedAt=${instance.taskInstance.completedAt} | " +
                    "dueDate=${instance.taskInstance.dueDate} | " +
                    "usado=$completedAt | weekStart=$weekStart weekEnd=$weekEnd | " +
                    "inRange=$inRange isDone=$isDone isUser=$isUser isHome=$isHome"
            )

            isDone && isUser && isHome && inRange
        }

        Log.d("DEBUG_RETOS", "weekStart=$weekStart weekEnd=$weekEnd | weekCompleted=${weekCompleted.size}")
        weekCompleted.forEach {
            Log.d("DEBUG_RETOS", "✅ Pasó filtro: ${it.taskInstance.id.take(8)} completedAt=${it.taskInstance.completedAt}")
        }

        Log.d("RewardVM", "weekCompleted final: ${weekCompleted.size}")

        // Todas las instancias del usuario esta semana
        val weekAll = allInstances.filter { instance ->
            instance.assignedMembers.any { it.userId == currentUserId } &&
                    (homeId == null || instance.taskDetails.task.homeId == homeId) &&
                    instance.taskInstance.dueDate in weekStart until weekEnd
        }

        // Reto 1. Completar 5 tareas esta semana
        val reto1Progress = weekCompleted.size
        val reto1Goal = 5

        // Reto 2. Racha de 3 días seguidos esta semana
        val userInstances = allInstances.filter { it.assignedMembers.any { m -> m.userId == currentUserId } }
        val streakDays = RewardsUtils.calculateStreak(userInstances)
        val reto2Progress = streakDays.coerceAtMost(3)
        val reto2Goal = 3

        // Reto 3. Completar todas las tareas de al menos un día
        val completedFullDays = (0..6).count { dayOffset ->
            val dayStart = weekStart + dayOffset * 86_400_000L
            val dayEnd   = dayStart  + 86_400_000L
            val dayTasks = weekAll.filter { it.taskInstance.dueDate in dayStart until dayEnd }
            dayTasks.isNotEmpty() && dayTasks.all { it.taskInstance.state == TaskState.COMPLETED }
        }
        val reto3Progress = completedFullDays
        val reto3Goal = 1

        // Reto 4. Tareas de alta prioridad esta semana
        val reto4Progress = weekCompleted.count {
            it.taskDetails.task.priority == PriorityLevel.ALTA
        }
        val reto4Goal = 3

        return listOf(
            ChallengeItem(
                title = "Completa 5 tareas",
                description = "Completa 5 tareas esta semana",
                currentProgress = reto1Progress.coerceAtMost(reto1Goal),
                totalGoal = reto1Goal,
                rewardPoints = 25,
                icon = "🎯",
                isCompleted = reto1Progress >= reto1Goal
            ),
            ChallengeItem(
                title = "Racha de 3 días",
                description = "Completa tareas 3 días seguidos",
                currentProgress = reto2Progress,
                totalGoal = reto2Goal,
                rewardPoints = 15,
                icon = "🔥",
                isCompleted = reto2Progress >= reto2Goal
            ),
            ChallengeItem(
                title = "Todo el día",
                description = "Completa todas las tareas de un día",
                currentProgress = reto3Progress,
                totalGoal = reto3Goal,
                rewardPoints = 25,
                icon = "⚡",
                isCompleted = reto3Progress >= reto3Goal
            ),
            ChallengeItem(
                title = "Alta prioridad",
                description = "Completa 3 tareas de alta prioridad",
                currentProgress = reto4Progress.coerceAtMost(reto4Goal),
                totalGoal = reto4Goal,
                rewardPoints = 20,
                icon = "🚀",
                isCompleted = reto4Progress >= reto4Goal
            )
        )
    }
}