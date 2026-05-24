package haptikos.gestortareashogar_haptikos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceMemberJoin
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    @Query("SELECT * FROM task_instance_table_new WHERE id = :taskInstanceId")
    suspend fun getById(taskInstanceId: String): TaskInstanceEntityNew?

    @Query("SELECT * FROM task_instance_table_new ORDER BY dueDate ASC")
    fun getAllNew(): Flow<List<TaskInstanceEntityNew>>

    @Update
    suspend fun update(task: TaskInstanceEntityNew)

    @Delete
    suspend fun delete(task: TaskInstanceEntityNew)

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
    suspend fun insertInstanceWithAssignedMembers(instance: TaskInstanceEntityNew, memberIds: List<String>) {
        addTaskInstanceNew(instance)
        val joins = memberIds.map { memberId ->
            TaskInstanceMemberJoin(taskInstanceId = instance.id, memberId = memberId)
        }
        addTaskInstanceMemberJoin(joins)
    }

    @Transaction
    @Query("""
    SELECT DISTINCT ti.* FROM task_instance_table_new ti
    INNER JOIN task_table_new t ON ti.taskId = t.id
    LEFT JOIN room_table_new r ON t.roomId = r.id
    LEFT JOIN task_instance_member_join jim ON ti.id = jim.taskInstanceId
    LEFT JOIN member_table_new m ON jim.memberId = m.id
    WHERE (:homeId IS NULL OR t.homeId = :homeId)
    AND (:status IS NULL OR ti.state = :status)
    AND t.title LIKE '%' || :searchQuery || '%'
    AND (:memberName IS NULL OR m.name = :memberName)
    AND ti.isHidden = 0
    ORDER BY ti.dueDate ASC
""")
    fun getFilteredInstances(
        homeId: String?,
        status: TaskState?,
        searchQuery: String,
        memberName: String?
    ): Flow<List<TaskInstanceWithDetails>>

    @Transaction
    @Query("SELECT * FROM task_instance_table_new WHERE id = :instanceId")
    suspend fun getInstanceWithDetailsById(instanceId: String): TaskInstanceWithDetails?

    @Query("DELETE FROM task_instance_member_join WHERE taskInstanceId = :instanceId")
    suspend fun deleteInstanceMembers(instanceId: String)

    @Transaction
    suspend fun updateInstanceMembers(instanceId: String, memberIds: List<String>) {
        deleteInstanceMembers(instanceId)
        val joins = memberIds.map { TaskInstanceMemberJoin(taskInstanceId = instanceId, memberId = it) }
        addTaskInstanceMemberJoin(joins)
    }

    @Query("SELECT * FROM task_instance_table_new WHERE taskId = :taskId ORDER BY dueDate DESC LIMIT 1")
    suspend fun getLastInstanceForTask(taskId: String): TaskInstanceEntityNew?

    @Query("UPDATE task_instance_table_new SET isSynced = :isSynced WHERE id = :instanceId")
    suspend fun updateSyncStatus(instanceId: String, isSynced: Boolean)

    @Query("SELECT memberId FROM task_instance_member_join WHERE taskInstanceId = :instanceId")
    suspend fun getMemberIdsForInstance(instanceId: String): List<String>

    @Query("""
        SELECT COUNT(ti.id) FROM task_instance_table_new ti
        INNER JOIN task_instance_member_join tim ON ti.id = tim.taskInstanceId
        WHERE tim.memberId = :memberId AND ti.state = 'COMPLETED'
    """)
    fun getCompletedTaskCountForMember(memberId: String): Flow<Int>

    @Query("""
        DELETE FROM task_instance_table_new 
        WHERE taskId IN (SELECT id FROM task_table_new WHERE homeId = :homeId)
    """)
    suspend fun deleteAllByHomeId(homeId: String)

    @Transaction
    @Query("SELECT * FROM task_instance_table_new WHERE id = :instanceId")
    fun getInstanceWithDetailsByIdFlow(instanceId: String): Flow<TaskInstanceWithDetails?>

    @Query("DELETE FROM task_instance_table_new")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) > 0 FROM task_instance_table_new WHERE taskId = :taskId AND state = 'PENDING' AND dueDate > :afterDate")
    suspend fun hasNewerPendingInstance(taskId: String, afterDate: Long): Boolean

    @Query("SELECT * FROM task_instance_table_new WHERE taskId = :taskId AND dueDate = :dueDate AND state = 'PENDING' LIMIT 1")
    suspend fun getPendingInstanceForTaskAndDate(taskId: String, dueDate: Long): TaskInstanceEntityNew?

    @Query("SELECT * FROM task_instance_table_new WHERE id = :id")
    suspend fun getInstanceById(id: String): TaskInstanceEntityNew?

}