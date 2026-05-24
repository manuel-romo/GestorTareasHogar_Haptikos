package haptikos.gestortareashogar_haptikos.utils

import java.util.Calendar

object WeekUtils {
    fun getCurrentWeekId(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return "$year-W${week.toString().padStart(2, '0')}"
    }
}