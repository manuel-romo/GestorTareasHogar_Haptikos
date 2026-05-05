package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_table_new")
data class HomeEntityNew(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val inviteCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val notifyTaskReminders: Boolean = true,
    val notifyTaskCompleted: Boolean = true,
    val notifyNewMembers: Boolean = true,
    val notifyAllMembers: Boolean = false
)