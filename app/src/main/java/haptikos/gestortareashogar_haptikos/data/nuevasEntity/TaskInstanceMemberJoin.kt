package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity

@Entity(
    tableName = "task_instance_member_join",
    primaryKeys = ["taskInstanceId", "memberId"]
)
data class TaskInstanceMemberJoin(
    val taskInstanceId: Int,
    val memberId: Int
)