package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode


@Entity(
    tableName = "task_table_new",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)

data class TaskEntityNew(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val roomId: Int?,
    val points: Int,
    val priority: PriorityLevel = PriorityLevel.MEDIA,
    val suggestedDay: SuggestedDay = SuggestedDay.LUNES,
    val recurrence: RecurrenceType = RecurrenceType.DIARIO,
    val workMode: WorkMode = WorkMode.TEAM,
    val lastMemberIndex: Int = 0
)