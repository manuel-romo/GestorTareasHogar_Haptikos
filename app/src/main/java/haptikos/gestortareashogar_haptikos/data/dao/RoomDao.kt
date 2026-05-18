package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntityNew
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

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

    @Query("DELETE FROM room_table_new WHERE homeId = :homeId")
    suspend fun deleteAllByHomeId(homeId: String)

}