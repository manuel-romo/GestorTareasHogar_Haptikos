package haptikos.gestortareashogar_haptikos.data.enumerators

import androidx.compose.ui.graphics.Color
import haptikos.gestortareashogar_haptikos.ui.theme.Green
import haptikos.gestortareashogar_haptikos.ui.theme.Red
import haptikos.gestortareashogar_haptikos.ui.theme.YellowGreen

enum class PriorityLevel(val title: String, val points: Int, val mainColor: Color) {
    BAJA("Baja", 7, Green),
    MEDIA("Media", 10, YellowGreen),
    ALTA("Alta", 15, Red)
}