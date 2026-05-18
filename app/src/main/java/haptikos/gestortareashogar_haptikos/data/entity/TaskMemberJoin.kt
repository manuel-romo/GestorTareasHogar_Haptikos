package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "task_member_join",
    primaryKeys = ["taskId", "memberId"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(entity = MemberEntityNew::class, parentColumns = ["id"], childColumns = ["memberId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TaskMemberJoin(
    val taskId: String,
    val memberId: String
)