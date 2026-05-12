package haptikos.gestortareashogar_haptikos.utils

import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import java.util.Calendar

fun getNextDueDate(suggestedDay: SuggestedDay, recurrence: RecurrenceType): Long {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_WEEK)
    val targetDay = suggestedDay.toCalendarDay()

    return when (recurrence) {
        RecurrenceType.DIARIO -> {
            // Hoy
            calendar.startOfDay().timeInMillis
        }
        RecurrenceType.SEMANAL -> {
            // El día sugerido de esta semana o el próximo si ya pasó
            val daysUntilTarget = (targetDay - today + 7) % 7
            calendar.add(Calendar.DAY_OF_YEAR, daysUntilTarget)
            calendar.startOfDay().timeInMillis
        }
        RecurrenceType.QUINCENAL -> {
            val daysUntilTarget = (targetDay - today + 7) % 7
            calendar.add(Calendar.DAY_OF_YEAR, daysUntilTarget)
            calendar.startOfDay().timeInMillis
        }
        RecurrenceType.MENSUAL -> {
            calendar.set(Calendar.DAY_OF_WEEK, targetDay)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.MONTH, 1)
            }
            calendar.startOfDay().timeInMillis
        }
    }
}

fun getNextDueDateAfter(lastDueDate: Long, recurrence: RecurrenceType, suggestedDay: SuggestedDay): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = lastDueDate

    return when (recurrence) {
        RecurrenceType.DIARIO -> {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.startOfDay().timeInMillis
        }
        RecurrenceType.SEMANAL -> {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            calendar.startOfDay().timeInMillis
        }
        RecurrenceType.QUINCENAL -> {
            calendar.add(Calendar.DAY_OF_YEAR, 15)
            calendar.startOfDay().timeInMillis
        }
        RecurrenceType.MENSUAL -> {
            calendar.add(Calendar.MONTH, 1)
            calendar.startOfDay().timeInMillis
        }
    }
}

private fun SuggestedDay.toCalendarDay(): Int = when (this) {
    SuggestedDay.LUNES -> Calendar.MONDAY
    SuggestedDay.MARTES -> Calendar.TUESDAY
    SuggestedDay.MIERCOLES -> Calendar.WEDNESDAY
    SuggestedDay.JUEVES -> Calendar.THURSDAY
    SuggestedDay.VIERNES -> Calendar.FRIDAY
    SuggestedDay.SABADO -> Calendar.SATURDAY
    SuggestedDay.DOMINGO -> Calendar.SUNDAY
}

private fun Calendar.startOfDay(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}