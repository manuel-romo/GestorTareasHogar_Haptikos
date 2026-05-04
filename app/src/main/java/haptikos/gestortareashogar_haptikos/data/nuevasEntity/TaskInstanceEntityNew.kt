package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState

@Entity(
    tableName = "task_instance_table_new",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class TaskInstanceEntityNew(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val dueDate: Long,
    val state: TaskState = TaskState.PENDING,
    val pausedUntil: Long? = null
)