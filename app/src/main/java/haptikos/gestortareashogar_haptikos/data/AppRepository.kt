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
import haptikos.gestortareashogar_haptikos.network.HomeApi
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AppRepository(
    private val taskDao: TaskDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val memberDao: MemberDao,
    private val roomDao: RoomDao,
    private val homeDao: HomeDao,
    private val appDatabase: TaskDatabase,
    private val homeApi: HomeApi
) {

    // Lecturas reactivas
    val allTasksNew: Flow<List<TaskEntityNew>> = taskDao.getAllNew()
    val allTasksInstanceNew: Flow<List<TaskInstanceEntityNew>> = taskInstanceDao.getAllNew()
    val allMembersNew: Flow<List<MemberEntityNew>> = memberDao.getAllNew()
    val allRoomsNew: Flow<List<RoomEntityNew>> = roomDao.getAllNew()
    val allInstancesWithDetails: Flow<List<TaskInstanceWithDetails>> = taskInstanceDao.getAllInstancesWithDetails()
    val allTasksWithDetails: Flow<List<TaskWithDetails>> = taskDao.getAllTasksWithDetails()
    val allHomes: Flow<List<HomeEntityNew>> = homeDao.getAllHomes()

    // Operaciones de tareas
    suspend fun getTaskById(taskId: String): TaskEntityNew? = taskDao.getById(taskId)

    suspend fun insertTaskNew(task: TaskEntityNew, memberIds: List<String>) = taskDao.insertTaskWithMembers(task, memberIds)

    suspend fun updateTaskNewWithMembers(task: TaskEntityNew, memberIds: List<String>) = taskDao.updateTaskWithMembers(task, memberIds)

    suspend fun deleteTaskNew(task: TaskEntityNew) = taskDao.deleteTaskBaseNew(task)

    suspend fun getTaskWithDetailsById(taskId: String) = taskDao.getTaskWithDetailsById(taskId)

    // Operaciones de instancias
    suspend fun updateTaskInstance(taskInstance: TaskInstanceEntityNew) = taskInstanceDao.update(taskInstance)

    suspend fun deleteTaskInstance(taskInstance: TaskInstanceEntityNew) = taskInstanceDao.delete(taskInstance)

    suspend fun getTaskInstanceById(instanceId: String): TaskInstanceEntityNew? = taskInstanceDao.getById(instanceId)

    suspend fun getTaskInstanceWithDetailsById(instanceId: String): TaskInstanceWithDetails? {
        return taskInstanceDao.getInstanceWithDetailsById(instanceId)
    }

    suspend fun getFilteredInstances(
        status: TaskState?,
        searchQuery: String,
        memberName: String?
    ): Flow<List<TaskInstanceWithDetails>> {
        return taskInstanceDao.getFilteredInstances(status, searchQuery, memberName)
    }

    // Operaciones básicas
    suspend fun insertMemberNew(member: MemberEntityNew) = memberDao.addNew(member)
    suspend fun updateMemberNew(member: MemberEntityNew) = memberDao.updateNew(member)
    suspend fun deleteMemberNew(member: MemberEntityNew) = memberDao.deleteNew(member)

    suspend fun insertRoomNew(room: RoomEntityNew) = roomDao.addNew(room)
    suspend fun updateRoomNew(room: RoomEntityNew) = roomDao.updateNew(room)
    suspend fun deleteRoomNew(room: RoomEntityNew) = roomDao.deleteNew(room)

    suspend fun updateHome(home: HomeEntityNew) = homeDao.updateHome(home)
    suspend fun deleteHome(home: HomeEntityNew) = homeDao.deleteHome(home)


    suspend fun createHomeAndCreator(
        homeName: String,
        homeDescription: String?,
        isPrivate: Boolean,
        creatorName: String,
        creatorLastName: String,
        creatorColorHex: String
    ) {

        // Generación de UUIDs
        val newHomeId = UUID.randomUUID().toString()
        val newCreatorId = UUID.randomUUID().toString()

        val newHome = HomeEntityNew(
            id = newHomeId,
            name = homeName,
            description = homeDescription,
            isPrivate = isPrivate,
            inviteCode = null,
            isSynced = false
        )

        val creatorMember = MemberEntityNew(
            id = newCreatorId,
            homeId = newHomeId,
            name = creatorName,
            lastName = creatorLastName,
            colorHex = creatorColorHex,
            role = MemberRole.CREATOR,
            isSynced = false
        )

        // Guardado local
        appDatabase.withTransaction {
            homeDao.insertHome(newHome)
            memberDao.addNew(creatorMember)
        }

        // Intento de sincronización
        try {
            // Creación de modelo para Request
            val requestBody = HomeApi.CreateHomeRequest(
                homeId = newHomeId,
                homeName = homeName,
                homeDescription = homeDescription,
                isPrivate = isPrivate,
                creatorId = newCreatorId,
                creatorName = creatorName,
                creatorLastName = creatorLastName,
                creatorColorHex = creatorColorHex
            )

            val response = homeApi.createHome(requestBody)

            if (response.isSuccessful) {
                val serverData = response.body()

                // Actualización local
                val syncedHome = newHome.copy(
                    inviteCode = serverData?.inviteCode,
                    isSynced = true
                )
                val syncedMember = creatorMember.copy(isSynced = true)

                appDatabase.withTransaction {
                    homeDao.updateHome(syncedHome)
                    memberDao.updateNew(syncedMember)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}