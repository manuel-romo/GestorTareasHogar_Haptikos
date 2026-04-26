package haptikos.gestortareashogar_haptikos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")

data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val room: String,
    val points: Int,
    val members: String,
    val state: String
)