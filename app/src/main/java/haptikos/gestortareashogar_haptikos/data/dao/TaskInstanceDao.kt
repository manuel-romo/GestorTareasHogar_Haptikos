package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    @Query("SELECT * FROM task_instance_table ORDER BY dueDate ASC")
    fun getAll(): Flow<List<TaskInstanceEntity>>

    @Query("SELECT * FROM task_instance_table WHERE id = :taskInstanceId")
    suspend fun getById(taskInstanceId: Int): TaskInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(taskInstance: TaskInstanceEntity)

    @Update
    suspend fun update(task: TaskInstanceEntity)

    @Delete
    suspend fun delete(task: TaskInstanceEntity)

}