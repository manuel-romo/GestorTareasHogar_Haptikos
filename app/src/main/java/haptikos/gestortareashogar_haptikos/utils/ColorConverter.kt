package haptikos.gestortareashogar_haptikos.utils

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color

fun parseHexColor(hex: String): Color {
    return try { Color(parseColor(hex)) } catch (e: Exception) { Color.Gray }
}