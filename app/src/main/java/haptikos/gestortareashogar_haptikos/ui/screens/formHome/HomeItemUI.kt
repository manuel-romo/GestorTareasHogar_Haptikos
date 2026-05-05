package haptikos.gestortareashogar_haptikos.ui.screens.formHome

data class HomeItemUI(
    val id: String,
    val name: String,
    val membersCount: Int,
    val role: String,
    val isSelected: Boolean = false
)