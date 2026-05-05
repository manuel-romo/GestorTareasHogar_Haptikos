package haptikos.gestortareashogar_haptikos.utils

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color

fun parseHexColor(hex: String): Color {
    return try {
        // Se normaliza el texto
        val normalizedHex = if (hex.startsWith("0x", ignoreCase = true)) {
            "#" + hex.substring(2)
        } else if (!hex.startsWith("#")) {
            "#$hex"
        } else {
            hex
        }
        Color(parseColor(normalizedHex))

    } catch (e: Exception) {
        Color.Gray
    }
}