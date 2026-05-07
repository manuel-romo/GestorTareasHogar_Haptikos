package haptikos.gestortareashogar_haptikos.data

import androidx.room.withTransaction
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskWithDetails
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AppRepository(
    private val taskDao: TaskDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val memberDao: MemberDao,
    private val roomDao: RoomDao,
    private val homeDao: HomeDao,
    private val appDatabase: TaskDatabase
) {

    // Lecturas reactivas
    val allTasksNew: Flow<List<TaskEntityNew>> = taskDao.getAllNew()
    val allTasksInstanceNew: Flow<List<TaskInstanceEntityNew>> = taskInstanceDao.getAllNew()
    val allMembersNew: Flow<List<MemberEntityNew>> = memberDao.getAllNew()
    val allRoomsNew: Flow<List<RoomEntityNew>> = roomDao.getAllNew()

    val allInstancesWithDetails: Flow<List<TaskInstanceWithDetails>> = taskInstanceDao.getAllInstancesWithDetails()
    val allTasksWithDetails: Flow<List<TaskWithDetails>> = taskDao.getAllTasksWithDetails()

    // Operaciones de tareas
    suspend fun getTaskById(taskId: Int): TaskEntityNew? = taskDao.getById(taskId)
    suspend fun insertTaskNew(task: TaskEntityNew, memberIds: List<Int>) = taskDao.insertTaskWithMembers(task, memberIds)
    suspend fun updateTaskNewWithMembers(task: TaskEntityNew, memberIds: List<Int>) = taskDao.updateTaskWithMembers(task, memberIds)
    suspend fun deleteTaskNew(task: TaskEntityNew) = taskDao.deleteTaskBaseNew(task)
    suspend fun getTaskWithDetailsById(taskId: Int) = taskDao.getTaskWithDetailsById(taskId)

    // Operaciones de instancias de tarea
    suspend fun updateTaskInstance(taskInstance: TaskInstanceEntityNew) = taskInstanceDao.update(taskInstance)
    suspend fun deleteTaskInstance(taskInstance: TaskInstanceEntityNew) = taskInstanceDao.delete(taskInstance)
    suspend fun getTaskInstanceById(instanceId: Int): TaskInstanceEntityNew? = taskInstanceDao.getById(instanceId)
    suspend fun getTaskInstanceWithDetailsById(instanceId: Int): TaskInstanceWithDetails? {
        return taskInstanceDao.getInstanceWithDetailsById(instanceId)
    }
    suspend fun getFilteredInstances(
        status: TaskState?,
        searchQuery: String,
        memberName: String?
    ): Flow<List<TaskInstanceWithDetails>> {
        return taskInstanceDao.getFilteredInstances(status, searchQuery, memberName)
    }

    suspend fun insertMemberNew(member: MemberEntityNew) = memberDao.addNew(member)
    suspend fun updateMemberNew(member: MemberEntityNew) = memberDao.updateNew(member)
    suspend fun deleteMemberNew(member: MemberEntityNew) = memberDao.deleteNew(member)

    suspend fun insertRoomNew(room: RoomEntityNew) = roomDao.addNew(room)
    suspend fun updateRoomNew(room: RoomEntityNew) = roomDao.updateNew(room)
    suspend fun deleteRoomNew(room: RoomEntityNew) = roomDao.deleteNew(room)

    val allHomes: Flow<List<HomeEntityNew>> = homeDao.getAllHomes()
    suspend fun updateHome(home: HomeEntityNew) = homeDao.updateHome(home)
    suspend fun deleteHome(home: HomeEntityNew) = homeDao.deleteHome(home)


    // Creación de nuevo hogar con nuevo código de invitación
    // TODO corregir
    suspend fun createHomeAndCreator(
        homeName: String,
        creatorName: String,
        creatorLastName: String,
        creatorColorHex: String
    ) {
        // Generación temporal de código de invitación
        val inviteCode = UUID.randomUUID().toString().take(6).uppercase()

        val newHome = HomeEntityNew(
            name = homeName,
            inviteCode = inviteCode
        )

        // Ejecución como transacción
        appDatabase.withTransaction {

            val generatedHomeId = homeDao.insertHome(newHome).toInt()

            // Se crea el creador del Hogar
            val creatorMember = MemberEntityNew(
                homeId = generatedHomeId,
                name = creatorName,
                lastName = creatorLastName,
                colorHex = creatorColorHex,
                role = MemberRole.CREATOR
            )

            memberDao.addNew(creatorMember)

        }

    }


}