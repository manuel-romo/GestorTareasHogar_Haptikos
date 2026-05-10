package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation



data class TaskInstanceWithDetails(
    @Embedded val taskInstance: TaskInstanceEntityNew,

    @Relation(
        entity = TaskEntityNew::class,
        parentColumn = "taskId",
        entityColumn = "id"
    )
    val taskDetails: TaskWithDetails,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TaskInstanceMemberJoin::class,
            parentColumn = "taskInstanceId",
            entityColumn = "memberId"
        )
    )
    val assignedMembers: List<MemberEntityNew>
)