package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskMemberJoin
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table_new WHERE id = :taskId")
    suspend fun getById(taskId: String): TaskEntityNew?

    @Query("SELECT * FROM task_table_new ORDER BY suggestedDay ASC")
    fun getAllNew(): Flow<List<TaskEntityNew>>

    @Transaction
    @Query("SELECT * FROM task_table_new ORDER BY suggestedDay ASC")
    fun getAllTasksWithDetails(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM task_table_new WHERE id = :taskId")
    suspend fun getTaskWithDetailsById(taskId: String): TaskWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTaskNew(task: TaskEntityNew): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTaskMemberJoin(joins: List<TaskMemberJoin>)

    // Guarda la tarea con sus miembros
    @Transaction
    suspend fun insertTaskWithMembers(task: TaskEntityNew, memberIds: List<String>) {
        addTaskNew(task)
        val joins = memberIds.map { memberId ->
            TaskMemberJoin(
                taskId = task.id,
                memberId = memberId
            )
        }

        // 3. Insertas las relaciones
        addTaskMemberJoin(joins)
    }

    // Actualiza los datos base de la nueva tarea
    @Update
    suspend fun updateTaskBaseNew(task: TaskEntityNew)

    @Query("DELETE FROM task_member_join WHERE taskId = :taskId")
    suspend fun deleteMembersForTask(taskId: String)

    @Delete
    suspend fun deleteTaskBaseNew(task: TaskEntityNew)

    @Transaction
    suspend fun updateTaskWithMembers(task: TaskEntityNew, memberIds: List<String>) {
        updateTaskBaseNew(task)
        deleteMembersForTask(task.id)
        val newJoins = memberIds.map { memberId ->
            TaskMemberJoin(taskId = task.id, memberId = memberId)
        }
        addTaskMemberJoin(newJoins)
    }

    @Query("UPDATE task_table_new SET isSynced = :isSynced WHERE id = :taskId")
    suspend fun updateSyncStatus(taskId: String, isSynced: Boolean)

    @Query("SELECT memberId FROM task_member_join WHERE taskId = :taskId")
    suspend fun getMemberIdsForTask(taskId: String): List<String>

}