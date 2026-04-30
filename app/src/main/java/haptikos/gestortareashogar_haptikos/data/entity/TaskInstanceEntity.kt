package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState

@Entity(tableName = "task_instance_table")
data class TaskInstanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val task: TaskEntity,
    val dueDate: Long,
    val assignedMembers: List<MemberEntity>,
    val state: TaskState = TaskState.PENDING,
    val pausedUntil: Long?
)