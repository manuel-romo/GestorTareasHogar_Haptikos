package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntity
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.RoomEntityNew
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Query("SELECT * FROM room_table ORDER BY name ASC")
    fun getAll(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM room_table WHERE id = :roomId")
    suspend fun getById(roomId: Int): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(room: RoomEntity)

    @Update
    suspend fun update(room: RoomEntity)

    @Delete
    suspend fun delete(room: RoomEntity)

    // Funciones nuevas
    @Query("SELECT * FROM room_table_new ORDER BY name ASC")
    fun getAllNew(): Flow<List<RoomEntityNew>>

    @Query("SELECT * FROM room_table_new WHERE id = :roomId")
    suspend fun getByIdNew(roomId: Int): RoomEntityNew?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNew(room: RoomEntityNew): Long

    @Update
    suspend fun updateNew(room: RoomEntityNew)

    @Delete
    suspend fun deleteNew(room: RoomEntityNew)

}