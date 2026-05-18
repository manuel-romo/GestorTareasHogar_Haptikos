package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import haptikos.gestortareashogar_haptikos.data.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notification_table ORDER BY createdAt DESC")
    fun getAll(): Flow<List<NotificationEntity>>

    @Query("UPDATE notification_table SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("SELECT COUNT(*) FROM notification_table WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notification_table WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnread(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notification_table WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notification_table SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notification_table WHERE id = :id")
    suspend fun delete(id: String)
}