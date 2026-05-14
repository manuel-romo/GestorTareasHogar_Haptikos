package haptikos.gestortareashogar_haptikos.ui.screens.rewards

data class RankingMemberItem(
    val memberId: String,
    val name: String,
    val points: Int,
    val colorHex: String,
    val position: Int,
    val isCurrentUser: Boolean
)

data class RewardsUiState(
    val totalPoints: Int = 0,
    val dailyProgress: Float = 0f,
    val rankingList: List<RankingMemberItem> = emptyList(),
    val isLoading: Boolean = false
)