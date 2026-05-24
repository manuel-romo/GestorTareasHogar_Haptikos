package haptikos.gestortareashogar_haptikos.utils

import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import java.util.Calendar

object RewardsUtils {
    fun calculateStreak(instances: List<TaskInstanceWithDetails>): Int {
        val completedDays = instances
            .filter { it.taskInstance.state == TaskState.COMPLETED }
            .map { instance ->
                Calendar.getInstance().apply {
                    timeInMillis = instance.taskInstance.dueDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            .toSortedSet(reverseOrder())

        if (completedDays.isEmpty()) return 0

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val oneDayMs = 24 * 60 * 60 * 1000L

        var checkDay = if (completedDays.contains(today)) today else today - oneDayMs
        var streak = 0
        while (completedDays.contains(checkDay)) {
            streak++
            checkDay -= oneDayMs
        }
        return streak
    }
}