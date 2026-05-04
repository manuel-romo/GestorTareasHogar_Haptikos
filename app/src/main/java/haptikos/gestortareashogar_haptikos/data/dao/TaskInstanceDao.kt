package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceMemberJoin
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
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

    // Funciones nuevas

    @Query("SELECT * FROM task_instance_table_new ORDER BY dueDate ASC")
    fun getAllNew(): Flow<List<TaskInstanceEntityNew>>

    @Transaction
    @Query("SELECT * FROM task_instance_table_new ORDER BY dueDate ASC")
    fun getAllInstancesWithDetails(): Flow<List<TaskInstanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM task_instance_table_new WHERE taskId = :taskId ORDER BY dueDate ASC")
    fun getInstancesForTask(taskId: Int): Flow<List<TaskInstanceWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTaskInstanceNew(taskInstance: TaskInstanceEntityNew): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTaskInstanceMemberJoin(joins: List<TaskInstanceMemberJoin>)

    // Función para guardar una lista de miembros en la instancia de la tarea.
    @Transaction
    suspend fun insertInstanceWithAssignedMembers(instance: TaskInstanceEntityNew, memberIds: List<Int>) {
        val newInstanceId = addTaskInstanceNew(instance)
        val joins = memberIds.map { memberId ->
            TaskInstanceMemberJoin(taskInstanceId = newInstanceId.toInt(), memberId = memberId)
        }
        addTaskInstanceMemberJoin(joins)
    }

    @Transaction
    @Query("""
        SELECT DISTINCT task_instance_table_new.* FROM task_instance_table_new
        INNER JOIN task_table_new ON task_instance_table_new.taskId = task_table_new.id
        LEFT JOIN task_instance_member_join ON task_instance_table_new.id = task_instance_member_join.taskInstanceId
        LEFT JOIN member_table ON task_instance_member_join.memberId = member_table.id
        WHERE (:status IS NULL OR task_instance_table_new.state = :status)
        AND task_table_new.title LIKE '%' || :searchQuery || '%'
        AND (:memberName IS NULL OR member_table.name = :memberName)
        ORDER BY task_instance_table_new.dueDate ASC
    """)
    fun getFilteredInstances(
        status: TaskState?,
        searchQuery: String,
        memberName: String?
    ): Flow<List<TaskInstanceWithDetails>>


}