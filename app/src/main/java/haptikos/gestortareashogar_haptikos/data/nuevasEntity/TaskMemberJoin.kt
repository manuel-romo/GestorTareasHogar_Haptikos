package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity

@Entity(
    tableName = "task_member_join",
    primaryKeys = ["taskId", "memberId"]
)
data class TaskMemberJoin(
    val taskId: Int,
    val memberId: Int
)

