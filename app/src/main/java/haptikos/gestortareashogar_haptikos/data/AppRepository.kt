package haptikos.gestortareashogar_haptikos.data

import android.util.Log
import androidx.room.withTransaction
import haptikos.gestortareashogar_haptikos.data.dao.ChallengeProgressDao
import haptikos.gestortareashogar_haptikos.data.dao.EarnedPointsDao
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.entity.ChallengeProgressEntity
import haptikos.gestortareashogar_haptikos.data.entity.EarnedPointsEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberStatus
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceWithDetails
import haptikos.gestortareashogar_haptikos.data.entity.TaskWithDetails
import haptikos.gestortareashogar_haptikos.data.enumerators.ChallengeType
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import haptikos.gestortareashogar_haptikos.network.EarnedPointsApi
import haptikos.gestortareashogar_haptikos.network.HomeApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.network.RoomApi
import haptikos.gestortareashogar_haptikos.network.TaskApi
import haptikos.gestortareashogar_haptikos.network.UserApi
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode
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
    private val challengeProgressDao: ChallengeProgressDao,
    private val earnedPointsDao: EarnedPointsDao,
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

        Log.d("GEN_INSTANCE", "insertTaskNew tarea='${task.title}' memberIds=$memberIds")
        // Guardado local
        taskDao.insertTaskWithMembers(task, memberIds)

        // Se genera la instancia de la semana actual
        generateInstanceForTask(task, memberIds)

    }

    suspend fun generateInstanceForTask(task: TaskEntityNew, memberIds: List<String>) {
        Log.d("GEN_INSTANCE", "tarea='${task.title}' workMode=${task.workMode} memberIds=$memberIds")

        if (task.pausedUntil != null && task.pausedUntil > System.currentTimeMillis()) return

        val dueDate = getNextDueDate(task.suggestedDay, task.recurrence)

        // Se verifica que no esté duplicada la instancia
        val existing = taskInstanceDao.getPendingInstanceForTaskAndDate(task.id, dueDate)
        if (existing != null) return

        val assignedMemberIds = if (task.workMode == WorkMode.SPLIT && memberIds.isNotEmpty()) {
            val index = task.lastMemberIndex % memberIds.size
            listOf(memberIds[index])
        } else {
            memberIds
        }

        Log.d("GEN_INSTANCE", "assignedMemberIds a insertar=$assignedMemberIds")

        val instance = TaskInstanceEntityNew(
            taskId = task.id,
            dueDate = dueDate,
            state = TaskState.PENDING
        )

        taskInstanceDao.insertInstanceWithAssignedMembers(instance, assignedMemberIds)
        Log.d("GEN_INSTANCE", "instancia insertada id=${instance.id}")

        if (task.workMode == WorkMode.SPLIT && memberIds.isNotEmpty()) {
            val nextIndex = (task.lastMemberIndex + 1) % memberIds.size
            taskDao.updateTaskOnly(task.copy(lastMemberIndex = nextIndex, isSynced = false))
        }
    }

    suspend fun generatePendingInstances() {
        Log.d("GEN_INSTANCES", "=== INICIO generatePendingInstances ===")
        Log.d("GEN_INSTANCES", "Llamado desde: ${Thread.currentThread().stackTrace.getOrNull(3)}")

        val allTasks = taskDao.getAllNew().first()
        val now = System.currentTimeMillis()
        Log.d("GEN_INSTANCES", "Total tareas: ${allTasks.size}")

        allTasks.forEach { task ->
            if (task.pausedUntil != null && task.pausedUntil > now) return@forEach

            val lastInstance = taskInstanceDao.getLastInstanceForTask(task.id)

            val shouldGenerate = if (lastInstance == null) {
                true
            } else if (lastInstance.state == TaskState.PENDING) {
                false
            } else {
                lastInstance.dueDate <= now
            }

            Log.d("GEN_INSTANCES", "Tarea='${task.title}' | lastState=${lastInstance?.state} | lastDueDate=${lastInstance?.dueDate} | now=$now | shouldGenerate=$shouldGenerate")

            if (shouldGenerate) {
                val dueDate = if (lastInstance == null) {
                    getNextDueDate(task.suggestedDay, task.recurrence)
                } else {
                    getNextDueDateAfter(
                        lastInstance.dueDate, task.recurrence, task.suggestedDay
                    )
                }

                Log.d("GEN_INSTANCES", "Tarea='${task.title}' lastState=${lastInstance?.state} lastDueDate=${lastInstance?.dueDate} shouldGenerate=$shouldGenerate")
                val existing = taskInstanceDao.getPendingInstanceForTaskAndDate(task.id, dueDate)
                Log.d("GEN_INSTANCES", ">>> '${task.title}' | dueDate calculado=$dueDate | existing=${existing?.id?.take(6)}")

                if (existing != null) {
                    Log.d("GEN_INSTANCES", ">>> SKIP '${task.title}' — instancia ya existe")
                    return@forEach
                }

                Log.d("GEN_INSTANCES", ">>> GENERANDO instancia para '${task.title}' dueDate=$dueDate")

                val memberIds = taskDao.getMemberIdsForTask(task.id)

                val assignedMemberIds = if (task.workMode == WorkMode.SPLIT && memberIds.isNotEmpty()) {
                    val index = task.lastMemberIndex % memberIds.size
                    listOf(memberIds[index])
                } else {
                    memberIds
                }

                val instance = TaskInstanceEntityNew(
                    taskId = task.id,
                    dueDate = dueDate,
                    state = TaskState.PENDING
                )

                taskInstanceDao.insertInstanceWithAssignedMembers(instance, assignedMemberIds)
                Log.d("GEN_INSTANCES", ">>> INSERTADA instancia para '${task.title}'")

                if (task.workMode == WorkMode.SPLIT && memberIds.isNotEmpty()) {
                    val nextIndex = (task.lastMemberIndex + 1) % memberIds.size
                    taskDao.updateTaskOnly(task.copy(lastMemberIndex = nextIndex, isSynced = false))
                }
            }
        }
        Log.d("GEN_INSTANCES", "=== generatePendingInstances FIN ===")
    }


    suspend fun updateTaskNewWithMembers(task: TaskEntityNew, memberIds: List<String>) =
        taskDao.updateTaskWithMembers(task.copy(isSynced = false), memberIds)

    suspend fun deleteTaskNew(task: TaskEntityNew) {
        // Se borra localmente
        taskDao.deleteTaskBaseNew(task)

        // Se borra en el serivdor
        try {
            val userId = dataStore.userIdFlow.first() ?: ""
            RetrofitClient.getTaskApi(dataStore).deleteTask(task.id, userId)
        } catch (e: Exception) {
            Log.e("SYNC", "Error eliminando tarea en servidor: ${e.message}")
        }
    }

    suspend fun getTaskWithDetailsById(taskId: String) = taskDao.getTaskWithDetailsById(taskId)

    // Operaciones de instancias
    suspend fun updateTaskInstance(taskInstance: TaskInstanceEntityNew) = taskInstanceDao.update(taskInstance)

    suspend fun hideTaskInstance(taskInstance: TaskInstanceEntityNew) {
        taskInstanceDao.update(taskInstance.copy(isHidden = true))
    }

    suspend fun getTaskInstanceById(instanceId: String): TaskInstanceEntityNew? = taskInstanceDao.getById(instanceId)

    suspend fun getTaskInstanceWithDetailsById(instanceId: String): TaskInstanceWithDetails? {
        return taskInstanceDao.getInstanceWithDetailsById(instanceId)
    }

    fun getInstanceWithDetailsByIdFlow(instanceId: String): Flow<TaskInstanceWithDetails?> {
        return taskInstanceDao.getInstanceWithDetailsByIdFlow(instanceId)
    }

    fun getFilteredInstances(
        homeId: String?,
        status: TaskState?,
        searchQuery: String,
        memberName: String?
    ): Flow<List<TaskInstanceWithDetails>> {
        return taskInstanceDao.getFilteredInstances(homeId, status, searchQuery, memberName)
    }

    // Operaciones básicas de Miembros
    suspend fun insertMemberNew(member: MemberEntityNew) = memberDao.addNew(member)
    suspend fun updateMemberNew(member: MemberEntityNew) = memberDao.updateNew(member)
    suspend fun deleteMemberNew(member: MemberEntityNew) = memberDao.deleteNew(member)

    fun getCompletedTaskCountForMember(memberId: String): Flow<Int> {
        return taskInstanceDao.getCompletedTaskCountForMember(memberId)
    }

    suspend fun updateMemberRole(memberId: String, homeId: String, newRole: MemberRole) {
        val member = memberDao.getMemberById(memberId)
        member?.let {
            val updatedMember = it.copy(role = newRole, isSynced = false)
            memberDao.updateNew(updatedMember)
        }
    }

    suspend fun removeMemberFromHome(memberId: String, homeId: String) {
        val member = memberDao.getMemberById(memberId)
        member?.let {
            memberDao.deleteNew(it)
        }
    }

    suspend fun insertRoomNew(room: RoomEntityNew) = roomDao.addNew(room)
    suspend fun updateRoomNew(room: RoomEntityNew) = roomDao.updateNew(room.copy(isSynced = false))
    suspend fun deleteRoomNew(room: RoomEntityNew) {
        roomDao.deleteNew(room)
        try {
            RetrofitClient.getRoomApi(dataStore).deleteRoom(room.id)
        } catch (e: Exception) {
            Log.e("SYNC", "Error eliminando habitación: ${e.message}")
        }
    }

    suspend fun updateHome(home: HomeEntityNew) {
        val homeToSave = home.copy(isSynced = false)
        homeDao.updateHome(homeToSave)
    }

    suspend fun deleteHomeById(homeId: String) = homeDao.deleteHomeById(homeId)


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

        // Solo se guarda el creador localmente
        appDatabase.withTransaction {
            homeDao.insertHome(newHome)
            memberDao.addNew(creatorMember)
        }

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
                creatorMemberId = creatorMemberId,
                invitedUsers = invitedUsers
            )

            val response = RetrofitClient.getHomeApi(dataStore).createHome(request)

            if (response.isSuccessful) {
                val inviteCode = response.body()?.inviteCode
                homeDao.updateInviteCodeAndSync(homeId, inviteCode, isSynced = true)
                memberDao.markMembersAsSynced(homeId)
                Log.d("SYNC", "Hogar creado en servidor con código: $inviteCode")
                inviteCode
            } else {
                Log.e("SYNC", "Error creando hogar: ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Excepción creando hogar: ${e.message}")
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
    suspend fun updateUserNotificationSettings(userId: String, homeId: String?, type: String, isEnabled: Boolean): Boolean {
        return try {
            val request = when (type) {
                "reminders" -> UserApi.UpdateUserRequest(notifyTaskReminders = isEnabled, homeId = homeId)
                "completed" -> UserApi.UpdateUserRequest(notifyTaskCompleted = isEnabled, homeId = homeId)
                "newMembers" -> UserApi.UpdateUserRequest(notifyNewMembers = isEnabled, homeId = homeId)
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
        roomDao.addNew(room.copy(isSynced = false))
        syncPendingRoomsNow()
    }


    suspend fun deleteHomeWithSync(home: HomeEntityNew): Boolean {
        return try {
            val response = RetrofitClient.getHomeApi(dataStore).deleteHome(home.id)

            if (response.isSuccessful) {
                appDatabase.withTransaction {
                    homeDao.deleteHomeById(home.id)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getMembersByHome(homeId: String, currentUserId: String): Flow<List<MemberEntityNew>> =
        memberDao.getMembersByHome(homeId, currentUserId)


    // Unirse a hogar -------------------------------------------------------------
    suspend fun findHomeByCode(inviteCode: String): HomeViewModel.HomePreviewInfo? {

        val userId = dataStore.userIdFlow.first()
        return try {
            val response = RetrofitClient.getHomeApi(dataStore).findHomeByCode(inviteCode, userId)

            if (response.isSuccessful) {
                val body = response.body()

                body?.let {
                    HomeViewModel.HomePreviewInfo(
                        id = it.id,
                        name = it.name,
                        creatorName = it.creatorName,
                        memberCount = it.memberCount,
                        taskCount = it.taskCount.toInt(),
                        pendingCount = it.pendingCount.toInt(),
                        isAlreadyMember = it.isAlreadyMember
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun joinHome(
        inviteCode: String,
        memberId: String,
        homeId: String,
        userId: String,
        name: String,
        colorHex: String
    ): Boolean {
        return try {
            // Verificar y unirse en el servidor
            val request = HomeApi.JoinHomeRequest(inviteCode, userId, name, "", colorHex)
            val response = RetrofitClient.getHomeApi(dataStore).joinHome(request)

            if (response.isSuccessful) {

                val syncResponse = RetrofitClient.getSyncApi(dataStore).getHomesByUser(userId)

                if (syncResponse.isSuccessful) {
                    val homes = syncResponse.body() ?: emptyList()

                    // Guardado local
                    appDatabase.withTransaction {
                        homes.forEach { homeDto ->
                            homeDao.insertHome(
                                HomeEntityNew(
                                    id = homeDto.id,
                                    name = homeDto.name,
                                    description = homeDto.description,
                                    isPrivate = homeDto.isPrivate,
                                    inviteCode = homeDto.inviteCode,
                                    editPermission = HomePermission.valueOf(homeDto.editPermission ?: "CREATOR_ONLY"),
                                    notifyTaskReminders = homeDto.notifyTaskReminders,
                                    notifyTaskCompleted = homeDto.notifyTaskCompleted,
                                    notifyNewMembers = homeDto.notifyNewMembers,
                                    notifyAllMembers = homeDto.notifyAllMembers,
                                    forceSettings = homeDto.forceSettings,
                                    isSynced = true
                                )
                            )

                            homeDto.members.forEach { memberDto ->
                                memberDao.addNew(
                                    MemberEntityNew(
                                        id = memberDto.id,
                                        userId = memberDto.userId,
                                        homeId = homeDto.id,
                                        name = memberDto.name,
                                        lastName = memberDto.lastName,
                                        colorHex = memberDto.colorHex,
                                        role = MemberRole.valueOf(memberDto.role),
                                        status = MemberStatus.valueOf(memberDto.status),
                                        isSynced = true
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Log.e("JoinHome", "Falló la sincronización del nuevo hogar")
                }

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
    suspend fun markTaskAsCompleted(taskInstance: TaskInstanceEntityNew) {
        val updated = taskInstance.copy(
            state = TaskState.COMPLETED,
            completedAt = System.currentTimeMillis(),
            isSynced = false
        )
        taskInstanceDao.update(updated)
        syncPendingInstances()
        generatePendingInstances()

    }

    suspend fun markTaskAsPending(taskInstance: TaskInstanceEntityNew): Boolean {
        val hasNewerPending = taskInstanceDao.hasNewerPendingInstance(
            taskId = taskInstance.taskId,
            afterDate = taskInstance.dueDate
        )

        if (hasNewerPending) return false

        taskInstanceDao.update(
            taskInstance.copy(
                state = TaskState.PENDING,
                completedAt = null,
                isSynced = false
            )
        )
        return true
    }

    suspend fun hasNewerPendingInstance(taskId: String, afterDate: Long): Boolean {
        return taskInstanceDao.hasNewerPendingInstance(taskId, afterDate)
    }

    // Abandonar hogar ---------------------------------------------------------------------
    suspend fun leaveHomeWithSync(homeId: String, userId: String): Boolean {
        return try {
            Log.d("LEAVE", "Llamando leaveHome homeId=$homeId userId=$userId")
            val response = RetrofitClient.getHomeApi(dataStore).leaveHome(homeId, userId)
            Log.d("LEAVE", "Response: ${response.code()}")

            if (response.isSuccessful) {
                appDatabase.withTransaction {
                    // Se borra el registro de miembro
                    memberDao.deleteMemberByHomeAndUser(homeId, userId)

                    // Se borra el hogar localmente
                    homeDao.deleteHomeById(homeId)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateTaskOnly(task: TaskEntityNew) =
        taskDao.updateTaskOnly(task.copy(isSynced = false))

    suspend fun syncPendingInstances() {
        val currentUserId = dataStore.userIdFlow.first() ?: return
        val pendingInstances = taskInstanceDao.getAllNew().first().filter { !it.isSynced }

        pendingInstances.forEach { instance ->
            try {
                if (instance.state == TaskState.COMPLETED) {
                    val response = RetrofitClient.getTaskInstanceApi(dataStore)
                        .completeInstance(instance.id, currentUserId, instance.completedAt ?: System.currentTimeMillis())
                    if (response.isSuccessful) {
                        taskInstanceDao.updateSyncStatus(instance.id, true)
                    }
                    return@forEach
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando instancia ${instance.id}: ${e.message}")
            }
        }
    }

    suspend fun syncPendingTasksNow() {

        syncPendingRoomsNow()

        val pendingTasks = taskDao.getAllNew().first().filter { !it.isSynced }
        val currentUserId = dataStore.userIdFlow.first() ?: return

        pendingTasks.forEach { task ->
            try {
                val memberIds = taskDao.getMemberIdsForTask(task.id)

                val updateResponse = RetrofitClient.getTaskApi(dataStore).updateTask(
                    task.id,
                    TaskApi.UpdateTaskRequest(
                        title = task.title,
                        description = task.description,
                        points = task.points,
                        priority = task.priority.name,
                        suggestedDay = task.suggestedDay.name,
                        recurrence = task.recurrence.name,
                        workMode = task.workMode.name,
                        roomId = task.roomId,
                        memberIds = memberIds,
                        pausedUntil = task.pausedUntil
                    )
                )

                if (updateResponse.isSuccessful) {
                    taskDao.updateSyncStatus(task.id, isSynced = true)
                    return@forEach
                }

                if (updateResponse.code() == 404) {
                    val createResponse = RetrofitClient.getTaskApi(dataStore).createTask(
                        TaskApi.CreateTaskRequest(
                            id = task.id,
                            title = task.title,
                            description = task.description,
                            points = task.points,
                            priority = task.priority.name,
                            suggestedDay = task.suggestedDay.name,
                            recurrence = task.recurrence.name,
                            workMode = task.workMode.name,
                            lastMemberIndex = task.lastMemberIndex,
                            roomId = task.roomId,
                            homeId = task.homeId,
                            memberIds = memberIds,
                            predetermined = task.isPredetermined,
                            userId = currentUserId
                        )
                    )
                    if (createResponse.isSuccessful) {
                        taskDao.updateSyncStatus(task.id, isSynced = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando tarea ${task.id}: ${e.message}")
            }
        }
    }

    suspend fun syncPendingRoomsNow() {
        val pendingRooms = roomDao.getAllNew().first().filter { !it.isSynced }
        val currentUserId = dataStore.userIdFlow.first() ?: return

        pendingRooms.forEach { room ->
            try {
                val updateResponse = RetrofitClient.getRoomApi(dataStore).updateRoom(
                    room.id,
                    RoomApi.UpdateRoomRequest(room.name, room.icon, room.colorHex)
                )
                if (updateResponse.isSuccessful) {
                    roomDao.updateNew(room.copy(isSynced = true))
                    return@forEach
                }
                if (updateResponse.code() == 404) {
                    val createResponse = RetrofitClient.getRoomApi(dataStore).createRoom(
                        RoomApi.CreateRoomRequest(room.id, room.name, room.icon, room.colorHex, room.homeId, currentUserId)
                    )
                    if (createResponse.isSuccessful) {
                        roomDao.updateNew(room.copy(isSynced = true))
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando habitación ${room.id}: ${e.message}")
            }
        }
    }

    private suspend fun syncHomeNow(home: HomeEntityNew) {
        try {
            val response = RetrofitClient.getHomeApi(dataStore).updateHome(
                home.id,
                HomeApi.UpdateHomeRequest(
                    name = home.name,
                    description = home.description,
                    isPrivate = home.isPrivate,
                    editPermission = home.editPermission.name,
                    notifyTaskReminders = home.notifyTaskReminders,
                    notifyTaskCompleted = home.notifyTaskCompleted,
                    notifyNewMembers = home.notifyNewMembers,
                    notifyAllMembers = home.notifyAllMembers,
                    forceSettings = home.forceSettings
                )
            )
            if (response.isSuccessful) {
                homeDao.updateHome(home.copy(isSynced = true))
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Error sincronizando hogar: ${e.message}")
        }
    }

    suspend fun sendInviteEmail(homeId: String, email: String, homeName: String, inviteCode: String): Boolean {
        return try {
            val response = RetrofitClient.getHomeApi(dataStore).inviteByEmail(
                homeId,
                HomeApi.InviteEmailRequest(email, homeName, inviteCode)
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }



    // Challenges

    suspend fun awardChallengePoints(progressId: String) {
        val progress = challengeProgressDao.getById(progressId) ?: return
        if (!progress.pointsAwarded) {
            challengeProgressDao.update(progress.copy(pointsAwarded = true))
        }
    }

    suspend fun getChallengeByTypeAndWeek(
        userId: String,
        homeId: String,
        challengeType: ChallengeType,
        weekId: String
    ): ChallengeProgressEntity? =
        challengeProgressDao.getByTypeAndWeek(userId, homeId, challengeType, weekId)

    suspend fun getChallengeProgressForWeek(
        userId: String,
        homeId: String,
        weekId: String
    ): List<ChallengeProgressEntity> =
        challengeProgressDao.getProgressForWeekSuspend(userId, homeId, weekId)

    suspend fun upsertChallengeProgress(progress: ChallengeProgressEntity) {
        val existing = challengeProgressDao.getByTypeAndWeek(
            userId = progress.userId,
            homeId = progress.homeId,
            challengeType = progress.challengeType,
            weekId = progress.weekId
        )
        if (existing == null) {
            challengeProgressDao.insert(progress)
        } else {
            challengeProgressDao.update(existing.copy(
                currentProgress = progress.currentProgress,
                isCompleted = progress.isCompleted,
                pointsAwarded = if (existing.pointsAwarded) true else progress.pointsAwarded,
                completedAt = progress.completedAt ?: existing.completedAt
            ))
        }
    }

    suspend fun getTotalEarnedPoints(userId: String): Int =
        earnedPointsDao.getTotalPoints(userId)

    suspend fun getEarnedPointsPerMember(homeId: String?): List<Pair<String, Int>> =
        earnedPointsDao.getPointsPerMember(homeId).map { it.userId to it.total }

    suspend fun insertEarnedPoints(entry: EarnedPointsEntity) {
        try {
            earnedPointsDao.insert(entry)
            Log.d("EARNED_POINTS", "Insertado: instanceId=${entry.instanceId} userId=${entry.userId} points=${entry.points}")
            // Subir al servidor inmediatamente
            RetrofitClient.getEarnedPointsApi(dataStore).save(
                EarnedPointsApi.EarnedPointsDto(
                    entry.instanceId,
                    entry.userId,
                    entry.points,
                    entry.earnedAt
                )
            )
        } catch (e: Exception) {
            Log.e("EARNED_POINTS", "ERROR insertando: ${e.message}")
        }
    }

    suspend fun getAllMembersForHome(homeId: String?): List<MemberEntityNew> =
        if (homeId != null) memberDao.getMembersByHomeId(homeId)
        else memberDao.getAllNew().first()

    suspend fun removeEarnedPoints(instanceId: String) =
        earnedPointsDao.deleteByInstanceId(instanceId)

    suspend fun getEarnedCountForWeek(userId: String, weekStart: Long, weekEnd: Long): Int =
        earnedPointsDao.getCountForWeek(userId, weekStart, weekEnd)

    suspend fun getHighPriorityCountForWeek(userId: String, weekStart: Long, weekEnd: Long): Int =
        earnedPointsDao.getHighPriorityCountForWeek(userId, weekStart, weekEnd)

    suspend fun getMemberIdsForTask(taskId: String): List<String> = taskDao.getMemberIdsForTask(taskId)

    fun getTotalEarnedPointsFlow(userId: String): Flow<Int> =
        earnedPointsDao.getTotalPointsFlow(userId)

}