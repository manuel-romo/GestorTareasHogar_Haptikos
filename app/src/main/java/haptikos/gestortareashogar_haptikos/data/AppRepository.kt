package haptikos.gestortareashogar_haptikos.data

import androidx.room.withTransaction
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
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
import haptikos.gestortareashogar_haptikos.network.RoomApi
import haptikos.gestortareashogar_haptikos.network.TaskApi
import haptikos.gestortareashogar_haptikos.network.UserApi
import haptikos.gestortareashogar_haptikos.utils.getNextDueDate
import haptikos.gestortareashogar_haptikos.utils.getNextDueDateAfter
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.UUID

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

    suspend fun insertTaskNew(task: TaskEntityNew, memberIds: List<String>) {

        // Guardado local
        taskDao.insertTaskWithMembers(task, memberIds)

        // Se genera la instancia de la semana actual
        generateInstanceForTask(task, memberIds)

    }

    suspend fun generateInstanceForTask(task: TaskEntityNew, memberIds: List<String>) {
        // No se genera si la tarea está pausada
        if (task.pausedUntil != null && task.pausedUntil > System.currentTimeMillis()) return

        val dueDate = getNextDueDate(task.suggestedDay, task.recurrence)

        val instance = TaskInstanceEntityNew(
            taskId = task.id,
            dueDate = dueDate,
            state = TaskState.PENDING
        )

        taskInstanceDao.insertInstanceWithAssignedMembers(instance, memberIds)
    }

    suspend fun generatePendingInstances() {
        val allTasks = taskDao.getAllNew().first()
        val now = System.currentTimeMillis()

        allTasks.forEach { task ->
            // Se pasa si está pausada
            if (task.pausedUntil != null && task.pausedUntil > now) return@forEach

            // Se obtiene la última instancia de esta tarea
            val lastInstance = taskInstanceDao.getLastInstanceForTask(task.id)

            // Si nunca ha tenido instancia
            val shouldGenerate = if (lastInstance == null) {
                true
            } else {
                val nextDue = getNextDueDateAfter(
                    lastInstance.dueDate, task.recurrence, task.suggestedDay
                )
                nextDue <= now
            }

            if (shouldGenerate) {
                val dueDate = if (lastInstance == null) {
                    getNextDueDate(task.suggestedDay, task.recurrence)
                } else {
                    getNextDueDateAfter(
                        lastInstance.dueDate, task.recurrence, task.suggestedDay
                    )
                }

                // Obtener miembros de la tarea base
                val memberIds = taskDao.getMemberIdsForTask(task.id)

                val instance = TaskInstanceEntityNew(
                    taskId = task.id,
                    dueDate = dueDate,
                    state = TaskState.PENDING
                )
                taskInstanceDao.insertInstanceWithAssignedMembers(instance, memberIds)
            }
        }
    }


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

    fun getFilteredInstances(
        homeId: String?,
        status: TaskState?,
        searchQuery: String,
        memberName: String?
    ): Flow<List<TaskInstanceWithDetails>> {
        return taskInstanceDao.getFilteredInstances(homeId, status, searchQuery, memberName)
    }

    // Operaciones básicas
    suspend fun insertMemberNew(member: MemberEntityNew) = memberDao.addNew(member)
    suspend fun updateMemberNew(member: MemberEntityNew) = memberDao.updateNew(member)
    suspend fun deleteMemberNew(member: MemberEntityNew) = memberDao.deleteNew(member)

    suspend fun insertRoomNew(room: RoomEntityNew) = roomDao.addNew(room)
    suspend fun updateRoomNew(room: RoomEntityNew) = roomDao.updateNew(room)
    suspend fun deleteRoomNew(room: RoomEntityNew) = roomDao.deleteNew(room)

    suspend fun updateHome(home: HomeEntityNew) {
        // Guardado
        val homeToSave = home.copy(isSynced = false)
        homeDao.updateHome(homeToSave)
    }

    suspend fun deleteHome(home: HomeEntityNew) = homeDao.deleteHome(home)


    suspend fun regenerateInviteCode(homeId: String): String? {
        return try {
            val response = RetrofitClient.getHomeApi(dataStore).regenerateInviteCode(homeId)
            if (response.isSuccessful) {
                val newCode = response.body()!!.inviteCode
                homeDao.updateInviteCodeAndSync(homeId, newCode, isSynced = true)
                newCode
            } else null
        } catch (e: Exception) { null }
    }

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

        val creatorMemberId = UUID.randomUUID().toString()

        val creatorMember = MemberEntityNew(
            id = creatorMemberId,
            userId = creatorId,
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
                userId = "",
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

        // El código será recibido por Sync
        return null

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

    // Actualización de nombre de usuario --------------------------------------
    suspend fun updateUserName(userId: String, newName: String): Boolean {
        return try {
            // Solamente se envía el nombre
            val request = UserApi.UpdateUserRequest(name = newName)
            val response = RetrofitClient.getUserApi(dataStore).updateUser(userId, request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // Actualización de notificaciones de usuario --------------------------------------
    suspend fun updateUserNotificationSettings(userId: String, type: String, isEnabled: Boolean): Boolean {
        return try {
            // Solo se envía la configuración de modificación modificada
            val request = when (type) {
                "reminders" -> UserApi.UpdateUserRequest(notifyTaskReminders = isEnabled)
                "completed" -> UserApi.UpdateUserRequest(notifyTaskCompleted = isEnabled)
                "newMembers" -> UserApi.UpdateUserRequest(notifyNewMembers = isEnabled)
                else -> return false
            }

            val response = RetrofitClient.getUserApi(dataStore).updateUser(userId, request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }


    // Creación de habitación -----------------------------------------------
    suspend fun createRoomWithSync(room: RoomEntityNew) {
        // Guardado local
        roomDao.addNew(room.copy(isSynced = false))
    }


    suspend fun deleteHomeWithSync(home: HomeEntityNew) {
        // Eliminación local
        appDatabase.withTransaction {
            homeDao.deleteHome(home)
        }
    }

    fun getMembersByHome(homeId: String): Flow<List<MemberEntityNew>> =
        memberDao.getMembersByHome(homeId)


    // Unirse a hogar -------------------------------------------------------------
    suspend fun findHomeByCode(inviteCode: String): HomeViewModel.HomePreviewInfo? {
        val userId = dataStore.userIdFlow.first()
        return try {
            val response = RetrofitClient.getHomeApi(dataStore).findHomeByCode(inviteCode, userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    HomeViewModel.HomePreviewInfo(
                        id = it.id, name = it.name, creatorName = it.creatorName,
                        memberCount = it.memberCount, taskCount = it.taskCount.toInt(),
                        pendingCount = it.pendingCount.toInt(),
                        isAlreadyMember = it.isAlreadyMember
                    )
                }
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun joinHome(inviteCode: String, memberId: String, homeId: String,
                         userId: String, name: String, colorHex: String): Boolean {
        return try {
            // Verificar con servidor
            val request = HomeApi.JoinHomeRequest(inviteCode, userId, name, "", colorHex)
            val response = RetrofitClient.getHomeApi(dataStore).joinHome(request)

            if (response.isSuccessful) {
                val newMember = MemberEntityNew(
                    id = memberId,
                    userId = userId,
                    homeId = homeId,
                    name = name,
                    lastName = "",
                    colorHex = colorHex,
                    role = MemberRole.MEMBER,
                    status = MemberStatus.ACCEPTED,
                    isSynced = true
                )
                memberDao.addNew(newMember)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // Editar miembros de instancia de tarea ----------------------------------------
    suspend fun updateInstanceMembers(instanceId: String, memberIds: List<String>) {
        taskInstanceDao.updateInstanceMembers(instanceId, memberIds)
    }

    // Completar instancia de tarea -----------------------------------------------------
    suspend fun completeTaskInstance(taskInstance: TaskInstanceEntityNew) {
        taskInstanceDao.update(taskInstance.copy(state = TaskState.COMPLETED, isSynced = false))
    }
}