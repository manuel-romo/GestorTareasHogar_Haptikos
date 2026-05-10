package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "task_instance_member_join",
    primaryKeys = ["taskInstanceId", "memberId"],
    foreignKeys = [
        ForeignKey(
            entity = TaskInstanceEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["taskInstanceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(entity = MemberEntityNew::class, parentColumns = ["id"], childColumns = ["memberId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TaskInstanceMemberJoin(
    val taskInstanceId: String,
    val memberId: String
)