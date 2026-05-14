package haptikos.gestortareashogar_haptikos.ui.screens.rewards

data class RankingMemberItem(
    val memberId: String,
    val name: String,
    val points: Int,
    val colorHex: String,
    val position: Int,
    val isCurrentUser: Boolean
)

data class BadgeItem(
    val name: String,
    val icon: String,
    val dateUnlocked: String? = null,
    val isUnlocked: Boolean = false
)

data class ChallengeItem(
    val title: String,
    val description: String,
    val currentProgress: Int,
    val totalGoal: Int,
    val rewardPoints: Int,
    val icon: String,
    val isCompleted: Boolean = (currentProgress >= totalGoal)
)

data class RewardsUiState(
    val totalPoints: Int = 0,
    val dailyProgress: Float = 0f,
    val rankingList: List<RankingMemberItem> = emptyList(),
    val badges: List<BadgeItem> = emptyList(),
    val challenges: List<ChallengeItem> = emptyList()
)