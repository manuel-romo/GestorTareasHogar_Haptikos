package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode

@Entity(tableName = "task_table")

data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val room: RoomEntity?,
    val points: Int,
    val members: List<MemberEntity>,
    val priority: PriorityLevel = PriorityLevel.MEDIA,
    val suggestedDay: SuggestedDay = SuggestedDay.LUNES,
    val recurrence: RecurrenceType = RecurrenceType.DIARIO,
    val workMode: WorkMode = WorkMode.TEAM,
    val lastMemberIndex: Int = 0
)