package haptikos.gestortareashogar_haptikos.utils


import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun getDayName(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE", Locale("es", "ES"))
    return sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
}

fun getDateString(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES"))
    return sdf.format(Date(timestamp))
}

fun getRelativeDateString(timestamp: Long): String {
    val calendarTask = Calendar.getInstance().apply { timeInMillis = timestamp }
    val calendarToday = Calendar.getInstance()

    val yearTask = calendarTask.get(Calendar.YEAR)
    val yearToday = calendarToday.get(Calendar.YEAR)
    val dayTask = calendarTask.get(Calendar.DAY_OF_YEAR)
    val dayToday = calendarToday.get(Calendar.DAY_OF_YEAR)

    return if (yearTask == yearToday) {
        when (dayTask - dayToday) {
            0 -> "Hoy"
            1 -> "Mañana"
            -1 -> "Ayer"
            else -> getDateString(timestamp)
        }
    } else {
        getDateString(timestamp)
    }
}