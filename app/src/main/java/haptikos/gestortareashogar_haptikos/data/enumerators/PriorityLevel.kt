package haptikos.gestortareashogar_haptikos.data.enumerators

import androidx.compose.ui.graphics.Color
import haptikos.gestortareashogar_haptikos.ui.theme.CompletedGreen
import haptikos.gestortareashogar_haptikos.ui.theme.Red
import haptikos.gestortareashogar_haptikos.ui.theme.YellowGreen

enum class PriorityLevel(val title: String, val points: Int, val mainColor: Color) {
    ALTA("Alta", 15, Red),
    MEDIA("Media", 10, YellowGreen),
    BAJA("Baja", 7, CompletedGreen)
}