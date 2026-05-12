package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import java.util.UUID

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
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val dueDate: Long,
    val state: TaskState = TaskState.PENDING,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)