package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TaskWithDetails(
    @Embedded val task: TaskEntityNew,

    @Relation(
        parentColumn = "roomId",
        entityColumn = "id"
    )
    val room: RoomEntityNew?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TaskMemberJoin::class,
            parentColumn = "taskId",
            entityColumn = "memberId"
        )
    )
    val members: List<MemberEntityNew>
)