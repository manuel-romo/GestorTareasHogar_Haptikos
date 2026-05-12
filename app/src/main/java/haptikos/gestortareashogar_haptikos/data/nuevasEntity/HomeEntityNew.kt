package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import java.util.UUID

@Entity(tableName = "home_table_new")
data class HomeEntityNew(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,
    val description: String? = null,
    val isPrivate: Boolean = false,
    val inviteCode: String? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val notifyTaskReminders: Boolean = true,
    val notifyTaskCompleted: Boolean = true,
    val notifyNewMembers: Boolean = true,
    val notifyAllMembers: Boolean = true,
    val forceSettings: Boolean = false,
    val editPermission: HomePermission = HomePermission.CREATOR_ONLY,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)