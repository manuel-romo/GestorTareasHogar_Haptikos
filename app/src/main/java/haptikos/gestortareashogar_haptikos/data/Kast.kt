package haptikos.gestortareashogar_haptikos.data

data class Task(
    val title: String,
    val date: String,
    val category: String,
    val categoryColor: androidx.compose.ui.graphics.Color,
    val points: Int,
    val members: List<androidx.compose.ui.graphics.Color>
)