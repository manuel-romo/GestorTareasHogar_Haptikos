package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notification_table")
data class NotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val type: String,
    val homeId: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)