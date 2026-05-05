package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDao {
    @Query("SELECT * FROM home_table_new")
    fun getAllHomes(): Flow<List<HomeEntityNew>>

    @Query("SELECT * FROM home_table_new WHERE id = :homeId")
    suspend fun getHomeById(homeId: Int): HomeEntityNew?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHome(home: HomeEntityNew): Long

    @Update
    suspend fun updateHome(home: HomeEntityNew)

    @Delete
    suspend fun deleteHome(home: HomeEntityNew)
}