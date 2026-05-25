package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import haptikos.gestortareashogar_haptikos.data.entity.EarnedPointsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EarnedPointsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EarnedPointsEntity)

    @Query("SELECT COALESCE(SUM(points), 0) FROM earned_points WHERE userId = :userId")
    suspend fun getTotalPoints(userId: String): Int

    @Query("SELECT * FROM earned_points WHERE userId = :userId ORDER BY earnedAt DESC LIMIT 5")
    suspend fun getRecent(userId: String): List<EarnedPointsEntity>

    @Query("""
        SELECT ep.userId, SUM(ep.points) as total 
        FROM earned_points ep
        INNER JOIN member_table_new m ON m.userId = ep.userId
        WHERE (:homeId IS NULL OR m.homeId = :homeId)
        GROUP BY ep.userId
    """)
    suspend fun getPointsPerMember(homeId: String?): List<UserPoints>

    data class UserPoints(val userId: String, val total: Int)

    @Query("SELECT * FROM earned_points WHERE isSynced = 0")
    suspend fun getPendingSynced(): List<EarnedPointsEntity>

    @Query("UPDATE earned_points SET isSynced = 1 WHERE instanceId = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM earned_points WHERE instanceId = :instanceId")
    suspend fun deleteByInstanceId(instanceId: String)

    @Query("SELECT COUNT(*) FROM earned_points WHERE userId = :userId AND earnedAt >= :weekStart AND earnedAt < :weekEnd")
    suspend fun getCountForWeek(userId: String, weekStart: Long, weekEnd: Long): Int

    @Query("""
        SELECT COUNT(*) FROM earned_points ep
        INNER JOIN task_instance_table_new ti ON ti.id = ep.instanceId
        INNER JOIN task_table_new t ON t.id = ti.taskId
        WHERE ep.userId = :userId AND ep.earnedAt >= :weekStart AND ep.earnedAt < :weekEnd
        AND t.priority = 'ALTA'
    """)
    suspend fun getHighPriorityCountForWeek(userId: String, weekStart: Long, weekEnd: Long): Int

    @Query("DELETE FROM earned_points")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(points), 0) FROM earned_points WHERE userId = :userId")
    fun getTotalPointsFlow(userId: String): Flow<Int>

}