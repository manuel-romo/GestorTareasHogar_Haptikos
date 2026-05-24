package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {


    @Query("SELECT * FROM member_table_new ORDER BY name ASC")
    fun getAllNew(): Flow<List<MemberEntityNew>>

    @Query("SELECT * FROM member_table_new WHERE id = :memberId")
    suspend fun getMemberById(memberId: String): MemberEntityNew?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNew(member: MemberEntityNew): Long

    @Update
    suspend fun updateNew(member: MemberEntityNew)

    @Query("UPDATE member_table_new SET isSynced = 1 WHERE homeId = :homeId")
    suspend fun markMembersAsSynced(homeId: String)

    @Delete
    suspend fun deleteNew(member: MemberEntityNew)

    @Query("""
        SELECT * FROM member_table_new 
        WHERE homeId = :homeId AND isDeleted = 0 
        ORDER BY CASE WHEN userId = :currentUserId THEN 0 ELSE 1 END ASC, name ASC
    """)
    fun getMembersByHome(homeId: String, currentUserId: String): Flow<List<MemberEntityNew>>

    @Query("SELECT * FROM member_table_new WHERE homeId = :homeId AND role = 'CREATOR' LIMIT 1")
    suspend fun getCreatorByHomeId(homeId: String): MemberEntityNew?

    @Query("UPDATE member_table_new SET isSynced = :isSynced WHERE id = :memberId")
    suspend fun updateSyncStatus(memberId: String, isSynced: Boolean)

    @Query("DELETE FROM member_table_new WHERE homeId = :homeId")
    suspend fun deleteAllByHomeId(homeId: String)

    @Query("DELETE FROM member_table_new WHERE homeId = :homeId AND userId = :userId")
    suspend fun deleteMemberByHomeAndUser(homeId: String, userId: String)

    @Query("SELECT id FROM member_table_new WHERE homeId = :homeId")
    suspend fun getAllIdsByHomeId(homeId: String): List<String>

    @Query("DELETE FROM member_table_new WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>): Int

    @Query("""
        UPDATE member_table_new SET
            name = :name,
            lastName = :lastName,
            colorHex = :colorHex,
            role = :role,
            status = :status,
            profilePicUrl = :profilePicUrl,
            isSynced = :isSynced
        WHERE id = :id
    """)
    suspend fun updateMember(
        id: String, name: String, lastName: String,
        colorHex: String, role: String, status: String,
        profilePicUrl: String?, isSynced: Boolean
    ): Int

    @Query("SELECT * FROM member_table_new WHERE homeId = :homeId")
    suspend fun getMembersByHomeId(homeId: String): List<MemberEntityNew>

}