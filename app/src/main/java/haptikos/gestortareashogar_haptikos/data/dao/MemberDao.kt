package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM member_table ORDER BY name ASC")
    fun getAll(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM member_table WHERE id = :memberId")
    suspend fun getById(memberId: Int): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(member: MemberEntity)

    @Update
    suspend fun update(member: MemberEntity)

    @Delete
    suspend fun delete(member: MemberEntity)

    // Funcioones nuevas
    @Query("SELECT * FROM member_table_new ORDER BY name ASC")
    fun getAllNew(): Flow<List<MemberEntityNew>>

    @Query("SELECT * FROM member_table_new WHERE id = :memberId")
    suspend fun getByIdNew(memberId: Int): MemberEntityNew?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNew(member: MemberEntityNew): Long

    @Update
    suspend fun updateNew(member: MemberEntityNew)

    @Delete
    suspend fun deleteNew(member: MemberEntityNew)

}