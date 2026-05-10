package haptikos.gestortareashogar_haptikos.data

import android.util.Log
import androidx.room.withTransaction
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberStatus
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskWithDetails
import haptikos.gestortareashogar_haptikos.network.HomeApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class AppRepository(
    private val taskDao: TaskDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val memberDao: MemberDao,
    private val roomDao: RoomDao,
    private val homeDao: HomeDao,
    private val appDatabase: TaskDatabase,
    private val dataStore: DataStoreManager
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


    suspend fun createHomeWithSync(
        homeId: String,
        creatorId: String,
        homeName: String,
        homeDescription: String?,
        isPrivate: Boolean,
        creatorName: String,
        creatorLastName: String,
        creatorColorHex: String,
        invitedUsers: List<HomeApi.InvitedUserDto> = emptyList(),
        defaultInviteColor: String
    ): String? {
        // Preparación de entidades locales
        val newHome = HomeEntityNew(
            id = homeId,
            name = homeName,
            description = homeDescription,
            isPrivate = isPrivate,
            inviteCode = null,
            isSynced = false
        )

        val creatorMember = MemberEntityNew(
            id = creatorId,
            homeId = homeId,
            name = creatorName,
            lastName = creatorLastName,
            colorHex = creatorColorHex,
            role = MemberRole.CREATOR,
            status = MemberStatus.ACCEPTED,
            isSynced = false
        )

        val invitedMembers = invitedUsers.map { invite ->
            MemberEntityNew(
                id = invite.id,
                homeId = homeId,
                name = invite.title,
                lastName = invite.subtitle,
                colorHex = defaultInviteColor,
                role = MemberRole.MEMBER,
                status = MemberStatus.PENDING,
                isSynced = false
            )
        }

        // Guardado local
        appDatabase.withTransaction {
            homeDao.insertHome(newHome)
            memberDao.addNew(creatorMember)
            invitedMembers.forEach { memberDao.addNew(it) }
        }

        // Intento de sincronización
        return try {
            val request = HomeApi.CreateHomeRequest(
                id = homeId,
                name = homeName,
                description = homeDescription,
                isPrivate = isPrivate,
                creatorId = creatorId,
                creatorName = creatorName,
                creatorLastName = creatorLastName,
                creatorColorHex = creatorColorHex,
                invitedUsers = invitedUsers
            )

            val response = RetrofitClient.getHomeApi(dataStore).createHome(request)

            if (response.isSuccessful && response.body() != null) {
                val inviteCode = response.body()!!.inviteCode

                updateHomeSyncStatus(homeId, inviteCode, isSynced = true)

                inviteCode
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateHomeSyncStatus(homeId: String, inviteCode: String?, isSynced: Boolean) {
        appDatabase.withTransaction {
            homeDao.updateInviteCodeAndSync(homeId, inviteCode, isSynced)
            if (isSynced) {
                memberDao.markMembersAsSynced(homeId)
            }
        }
    }


    // Actualziación de foto de usuario ------------------------------
    suspend fun uploadProfilePicture(userId: String, imageFile: File): String? {
        return try {
            // Se convierte el archivon File a MultipartBody.Part
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = RetrofitClient.getUserApi(dataStore).uploadProfilePicture(userId, body)

            if (response.isSuccessful && response.body() != null) {

                val newUrl = response.body()?.get("profilePicUrl")

                // Guardado local de URL de imagen obtenida
                if (newUrl != null) {
                    dataStore.saveProfilePicUrl(newUrl)
                }
                newUrl
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}