package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode
import java.util.UUID


@Entity(
    tableName = "task_table_new",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = HomeEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["homeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskEntityNew(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val roomId: String?,
    val homeId: String,
    val points: Int,
    val priority: PriorityLevel = PriorityLevel.MEDIA,
    val suggestedDay: SuggestedDay = SuggestedDay.LUNES,
    val recurrence: RecurrenceType = RecurrenceType.DIARIO,
    val workMode: WorkMode = WorkMode.TEAM,
    val lastMemberIndex: Int = 0,
    val pausedUntil: Long? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val isPredetermined: Boolean
)