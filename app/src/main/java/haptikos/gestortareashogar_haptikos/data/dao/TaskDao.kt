package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskMemberJoin
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table ORDER BY suggestedDay ASC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task_table WHERE id = :taskId")
    suspend fun getById(taskId: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)


    // Nuevas funciones
    @Query("SELECT * FROM task_table ORDER BY suggestedDay ASC")
    fun getAllNew(): Flow<List<TaskEntityNew>>

    @Transaction
    @Query("SELECT * FROM task_table_new ORDER BY suggestedDay ASC")
    fun getAllTasksWithDetails(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM task_table_new WHERE id = :taskId")
    suspend fun getTaskWithDetailsById(taskId: Int): TaskWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTaskNew(task: TaskEntityNew): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTaskMemberJoin(joins: List<TaskMemberJoin>)

    // Guarda la tarea con sus miembros
    @Transaction
    suspend fun insertTaskWithMembers(task: TaskEntityNew, memberIds: List<Int>) {
        val newTaskId = addTaskNew(task)
        val joins = memberIds.map { memberId ->
            TaskMemberJoin(taskId = newTaskId.toInt(), memberId = memberId)
        }
        addTaskMemberJoin(joins)
    }

    // Actualiza los datos base de la nueva tarea
    @Update
    suspend fun updateTaskBaseNew(task: TaskEntityNew)

    @Query("DELETE FROM task_member_join WHERE taskId = :taskId")
    suspend fun deleteMembersForTask(taskId: Int)

    @Delete
    suspend fun deleteTaskBaseNew(task: TaskEntityNew)

    @Transaction
    suspend fun updateTaskWithMembers(task: TaskEntityNew, memberIds: List<Int>) {
        updateTaskBaseNew(task)
        deleteMembersForTask(task.id)
        val newJoins = memberIds.map { memberId ->
            TaskMemberJoin(taskId = task.id, memberId = memberId)
        }
        addTaskMemberJoin(newJoins)
    }

}