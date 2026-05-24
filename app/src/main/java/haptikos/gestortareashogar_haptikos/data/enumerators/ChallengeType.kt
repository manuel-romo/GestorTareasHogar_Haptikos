package haptikos.gestortareashogar_haptikos.data.enumerators

enum class ChallengeType(
    val challengeId: String,
    val title: String,
    val description: String,
    val goal: Int,
    val rewardPoints: Int,
    val icon: String
) {
    COMPLETE_5_TASKS(
        challengeId = "complete_5_tasks",
        title = "Completa 5 tareas",
        description = "Completa 5 tareas esta semana",
        goal = 5,
        rewardPoints = 25,
        icon = "🎯"
    ),
    STREAK_3_DAYS(
        challengeId = "streak_3_days",
        title = "Racha de 3 días",
        description = "Completa tareas 3 días seguidos",
        goal = 3,
        rewardPoints = 15,
        icon = "🔥"
    ),
    FULL_DAY(
        challengeId = "full_day",
        title = "Todo el día",
        description = "Completa todas las tareas de un día",
        goal = 1,
        rewardPoints = 25,
        icon = "⚡"
    ),
    HIGH_PRIORITY(
        challengeId = "high_priority",
        title = "Alta prioridad",
        description = "Completa 3 tareas de alta prioridad",
        goal = 3,
        rewardPoints = 20,
        icon = "🚀"
    )
}