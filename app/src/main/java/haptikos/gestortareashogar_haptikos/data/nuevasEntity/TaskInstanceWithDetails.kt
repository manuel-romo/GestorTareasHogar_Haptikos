package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity

data class TaskInstanceWithDetails(
    @Embedded val taskInstance: TaskInstanceEntityNew,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "id"
    )
    val task: TaskEntityNew,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TaskInstanceMemberJoin::class,
            parentColumn = "taskInstanceId",
            entityColumn = "memberId"
        )
    )
    val assignedMembers: List<MemberEntityNew>,

    @Relation(
        entity = RoomEntityNew::class,
        parentColumn = "taskId",
        entityColumn = "id")
    val room: RoomEntityNew? = null
)
