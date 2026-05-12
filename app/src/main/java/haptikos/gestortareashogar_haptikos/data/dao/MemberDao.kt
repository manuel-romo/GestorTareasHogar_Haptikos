package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {


    @Query("SELECT * FROM member_table_new ORDER BY name ASC")
    fun getAllNew(): Flow<List<MemberEntityNew>>

    @Query("SELECT * FROM member_table_new WHERE id = :memberId")
    suspend fun getByIdNew(memberId: Int): MemberEntityNew?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNew(member: MemberEntityNew): Long

    @Update
    suspend fun updateNew(member: MemberEntityNew)

    @Query("UPDATE member_table_new SET isSynced = 1 WHERE homeId = :homeId")
    suspend fun markMembersAsSynced(homeId: String)

    @Delete
    suspend fun deleteNew(member: MemberEntityNew)

    @Query("SELECT * FROM member_table_new WHERE homeId = :homeId AND isDeleted = 0 ORDER BY name ASC")
    fun getMembersByHome(homeId: String): Flow<List<MemberEntityNew>>

    @Query("SELECT * FROM member_table_new WHERE homeId = :homeId AND role = 'CREATOR' LIMIT 1")
    suspend fun getCreatorByHomeId(homeId: String): MemberEntityNew?

}