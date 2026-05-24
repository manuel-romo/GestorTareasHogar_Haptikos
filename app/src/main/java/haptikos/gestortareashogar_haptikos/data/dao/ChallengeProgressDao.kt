package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.ChallengeProgressEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.ChallengeType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeProgressDao {

    @Query("SELECT * FROM challenge_progress WHERE userId = :userId AND homeId = :homeId AND weekId = :weekId")
    fun getProgressForWeek(userId: String, homeId: String, weekId: String): Flow<List<ChallengeProgressEntity>>

    @Query("SELECT * FROM challenge_progress WHERE userId = :userId AND homeId = :homeId AND weekId = :weekId")
    suspend fun getProgressForWeekSuspend(userId: String, homeId: String, weekId: String): List<ChallengeProgressEntity>

    @Query("SELECT * FROM challenge_progress WHERE id = :id")
    suspend fun getById(id: String): ChallengeProgressEntity?

    @Query("""
        SELECT * FROM challenge_progress 
        WHERE userId = :userId AND homeId = :homeId 
        AND challengeType = :challengeType AND weekId = :weekId 
        LIMIT 1
    """)
    suspend fun getByTypeAndWeek(
        userId: String,
        homeId: String,
        challengeType: ChallengeType,
        weekId: String
    ): ChallengeProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: ChallengeProgressEntity)

    @Update
    suspend fun update(progress: ChallengeProgressEntity)

    @Query("UPDATE challenge_progress SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM challenge_progress WHERE userId = :userId AND weekId != :currentWeekId")
    suspend fun deleteOldProgress(userId: String, currentWeekId: String)
}