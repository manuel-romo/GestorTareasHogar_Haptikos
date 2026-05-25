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
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberStatus
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.network.HomeApi
import haptikos.gestortareashogar_haptikos.network.MemberApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.network.RoomApi
import haptikos.gestortareashogar_haptikos.network.TaskApi
import haptikos.gestortareashogar_haptikos.network.TaskInstanceApi
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceMemberJoin
import haptikos.gestortareashogar_haptikos.data.entity.TaskMemberJoin
import haptikos.gestortareashogar_haptikos.network.ChallengeProgressApi
import haptikos.gestortareashogar_haptikos.network.ChallengeProgressApi.ChallengeProgressDto
import haptikos.gestortareashogar_haptikos.network.EarnedPointsApi
import haptikos.gestortareashogar_haptikos.utils.WeekUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class SyncRepository(
    private val dataStore: DataStoreManager,
    private val appDatabase: TaskDatabase,
    private val homeDao: HomeDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val challengeProgressDao: ChallengeProgressDao,
    private val earnedPointsDao: EarnedPointsDao,
    private val roomDao: RoomDao
) {

    private val allHomes: Flow<List<HomeEntityNew>> = homeDao.getAllHomes()
    private val allMembersNew: Flow<List<MemberEntityNew>> = memberDao.getAllNew()
    private val allTasksNew: Flow<List<TaskEntityNew>> = taskDao.getAllNew()
    private val allInstancesNew: Flow<List<TaskInstanceEntityNew>> = taskInstanceDao.getAllNew()

    val hasPendingSyncs: Flow<Boolean> = combine(
        allHomes,
        allMembersNew,
        allTasksNew,
        allInstancesNew
    ) { homes, members, tasks, instances ->
        homes.any { !it.isSynced } ||
                members.any { !it.isSynced } ||
                tasks.any { !it.isSynced } ||
                instances.any { !it.isSynced }
    }


    suspend fun syncAll(userId: String) {
        Log.d("SYNC_ALL", "Iniciando syncAll para userId=$userId")
        syncPendingItems()
        Log.d("SYNC_ALL", "syncPendingItems completado")
        syncHomes(userId)
        Log.d("SYNC_ALL", "syncHomes completado")

        val homes = homeDao.getAllHomes().first()
        Log.d("SYNC_ALL", "Hogares encontrados: ${homes.size}")
        homes.forEach { home ->
            Log.d("SYNC_ALL", "Sincronizando hogar: ${home.id}")
            syncRoomsForHome(home.id)
            syncTasksForHome(home.id)
        }
        Log.d("SYNC_ALL", "syncAll finalizado")
    }

    suspend fun syncPendingItems() {
        syncPendingHomes()
        syncPendingMembers()
        syncPendingRooms()
        syncPendingTasks()
        syncPendingTaskInstances()
        val userId = dataStore.userIdFlow.first()
        val homes = homeDao.getAllHomes().first()
        homes.forEach { home ->
            syncChallengeProgress(userId, home.id)
        }
    }

    private suspend fun syncPendingHomes() {
        val pendingHomes = homeDao.getAllHomes().first().filter { !it.isSynced }

        pendingHomes.forEach { home ->
            try {
                val creatorMember = memberDao.getCreatorByHomeId(home.id)
                val currentUserId = dataStore.userIdFlow.first()

                // See intenta actualizar primero
                val updateResponse = RetrofitClient.getHomeApi(dataStore).updateHome(
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

                if (updateResponse.isSuccessful) {
                    homeDao.updateInviteCodeAndSync(home.id, home.inviteCode, isSynced = true)
                    Log.d("SYNC", "Hogar actualizado: ${home.name}")
                    return@forEach
                }

                // Si no existe, se crea
                if (updateResponse.code() == 404) {
                    val currentUserName = dataStore.usernameFlow.first()
                    val createResponse = RetrofitClient.getHomeApi(dataStore).createHome(
                        HomeApi.CreateHomeRequest(
                            id = home.id,
                            name = home.name,
                            description = home.description,
                            isPrivate = home.isPrivate,
                            creatorId = creatorMember?.userId?.takeIf { it.isNotEmpty() } ?: currentUserId,
                            creatorName = creatorMember?.name ?: currentUserName,
                            creatorLastName = creatorMember?.lastName ?: "",
                            creatorColorHex = creatorMember?.colorHex ?: "#9E9E9E",
                            creatorMemberId = creatorMember?.id ?: "",
                            invitedUsers = emptyList()
                        )
                    )
                    if (createResponse.isSuccessful) {
                        val inviteCode = createResponse.body()?.inviteCode
                        homeDao.updateInviteCodeAndSync(home.id, inviteCode, isSynced = true)
                        memberDao.markMembersAsSynced(home.id)
                        Log.d("SYNC", "Hogar creado: ${home.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando hogar: ${e.message}")
            }
        }
    }

    private suspend fun syncPendingMembers() {
        val pendingMembers = memberDao.getAllNew().first().filter { !it.isSynced }
        val syncedHomes = homeDao.getAllHomes().first().filter { it.isSynced }.map { it.id }

        pendingMembers.forEach { member ->
            if (!syncedHomes.contains(member.homeId)) {
                Log.d("SYNC", "Pospuesto miembro ${member.id}: hogar no sincronizado")
                return@forEach
            }
            try {
                // Primero se intenta actualizar el rol
                val updateResponse = RetrofitClient.getMemberApi(dataStore)
                    .updateMemberRole(member.id, MemberApi.UpdateRoleRequest(
                        role = member.role.name,
                        actorUserId = dataStore.userIdFlow.first() ?: ""
                    ))

                if (updateResponse.isSuccessful) {
                    memberDao.updateSyncStatus(member.id, isSynced = true)
                    return@forEach
                }

                // Si no existe se crea
                if (updateResponse.code() == 404) {
                    val createResponse = RetrofitClient.getMemberApi(dataStore).createMember(
                        MemberApi.CreateMemberRequest(
                            id = member.id,
                            userId = member.userId.ifEmpty { null },
                            homeId = member.homeId,
                            name = member.name,
                            lastName = member.lastName,
                            colorHex = member.colorHex,
                            role = member.role.name,
                            status = member.status.name
                        )
                    )
                    if (createResponse.isSuccessful) {
                        memberDao.updateSyncStatus(member.id, isSynced = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando miembro ${member.id}: ${e.message}")
            }
        }
    }

    private suspend fun syncPendingRooms() {
        val pendingRooms = roomDao.getAllNew().first().filter { !it.isSynced }
        val currentUserId = dataStore.userIdFlow.first()
        Log.d("SYNC", "Rooms pendientes: ${pendingRooms.size}")
        pendingRooms.forEach { room ->
            try {
                // Primero se intenta actualizar
                val updateResponse = RetrofitClient.getRoomApi(dataStore).updateRoom(
                    room.id,
                    RoomApi.UpdateRoomRequest(room.name, room.icon, room.colorHex)
                )

                if (updateResponse.isSuccessful) {
                    roomDao.updateNew(room.copy(isSynced = true))
                    Log.d("SYNC", "Room actualizado: ${room.name}")
                    return@forEach
                }

                // Si no existe en el servidor, se crea
                if (updateResponse.code() == 404) {
                    val createResponse = RetrofitClient.getRoomApi(dataStore).createRoom(
                        RoomApi.CreateRoomRequest(room.id, room.name, room.icon, room.colorHex, room.homeId, currentUserId)
                    )
                    Log.d("SYNC", "Room creado: ${room.name} HTTP ${createResponse.code()}")
                    if (createResponse.isSuccessful) {
                        roomDao.updateNew(room.copy(isSynced = true))
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Excepción sincronizando room: ${e.message}")
            }
        }
    }

    private suspend fun syncPendingTasks() {
        val pendingTasks = taskDao.getAllNew().first().filter { !it.isSynced }
        Log.d("SYNC", "Tareas pendientes: ${pendingTasks.size}")
        val currentUserId = dataStore.userIdFlow.first()

        pendingTasks.forEach { task ->
            try {
                val memberIds = taskDao.getMemberIdsForTask(task.id)

                // Primero se intenta actualizar
                val updateRequest = TaskApi.UpdateTaskRequest(
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
                val updateResponse = RetrofitClient.getTaskApi(dataStore).updateTask(task.id, updateRequest)

                if (updateResponse.isSuccessful) {
                    taskDao.updateSyncStatus(task.id, isSynced = true)
                    Log.d("SYNC", "Tarea actualizada: ${task.title}")
                    return@forEach
                }

                // Si no existe en el servidor, se crea
                if (updateResponse.code() == 404) {
                    val createRequest = TaskApi.CreateTaskRequest(
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
                    val createResponse = RetrofitClient.getTaskApi(dataStore).createTask(createRequest)
                    Log.d("SYNC", "Tarea creada: ${task.title} HTTP ${createResponse.code()}")
                    if (createResponse.isSuccessful) {
                        taskDao.updateSyncStatus(task.id, isSynced = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Excepción sincronizando tarea: ${e.message}")
            }
        }
    }


    private suspend fun syncHomes(userId: String) {
        try {
            val response = RetrofitClient.getSyncApi(dataStore).getHomesByUser(userId)
            if (response.isSuccessful) {
                val homes = response.body() ?: return
                appDatabase.withTransaction {
                    homes.forEach { homeDto ->
                        val existing = homeDao.getHomeById(homeDto.id)
                        if (existing != null) {
                            homeDao.updateHome(
                                existing.copy(
                                    name = homeDto.name,
                                    inviteCode = homeDto.inviteCode,
                                    editPermission = HomePermission.valueOf(homeDto.editPermission ?: HomePermission.CREATOR_ONLY.name),
                                    // Si el hogar local tiene cambios pendientes, mantiene sus valores.
                                    notifyTaskReminders = if (!existing.isSynced) existing.notifyTaskReminders else homeDto.notifyTaskReminders,
                                    notifyTaskCompleted = if (!existing.isSynced) existing.notifyTaskCompleted else homeDto.notifyTaskCompleted,
                                    notifyNewMembers = if (!existing.isSynced) existing.notifyNewMembers else homeDto.notifyNewMembers,
                                    notifyAllMembers = if (!existing.isSynced) existing.notifyAllMembers else homeDto.notifyAllMembers,
                                    forceSettings = if (!existing.isSynced) existing.forceSettings else homeDto.forceSettings,
                                    isSynced = existing.isSynced
                                )
                            )
                        } else {
                            homeDao.insertHome(
                                HomeEntityNew(
                                    id = homeDto.id,
                                    name = homeDto.name,
                                    description = homeDto.description,
                                    isPrivate = homeDto.isPrivate,
                                    inviteCode = homeDto.inviteCode,
                                    editPermission = HomePermission.valueOf(homeDto.editPermission ?: HomePermission.CREATOR_ONLY.name),
                                    notifyTaskReminders = homeDto.notifyTaskReminders,
                                    notifyTaskCompleted = homeDto.notifyTaskCompleted,
                                    notifyNewMembers = homeDto.notifyNewMembers,
                                    notifyAllMembers = homeDto.notifyAllMembers,
                                    forceSettings = homeDto.forceSettings,
                                    isSynced = true
                                )
                            )
                        }
                        homeDto.members.forEach { memberDto ->
                            upsertMember(
                                MemberEntityNew(
                                    id = memberDto.id,
                                    userId = memberDto.userId,
                                    homeId = homeDto.id,
                                    name = memberDto.name,
                                    lastName = memberDto.lastName ?: "",
                                    colorHex = memberDto.colorHex ?: "#9E9E9E",
                                    role = MemberRole.valueOf(memberDto.role),
                                    status = MemberStatus.valueOf(memberDto.status),
                                    profilePicUrl = memberDto.profilePicUrl,
                                    isSynced = true
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncPendingTaskInstances() {
        val pendingInstances = taskInstanceDao.getAllNew().first().filter { !it.isSynced }
        Log.d("SYNC_VM", "Instancias pendientes: ${pendingInstances.size}")

        val currentUserId = dataStore.userIdFlow.first()

        pendingInstances.forEach { instance ->
            Log.d("SYNC_VM", "Instancia: ${instance.id} state=${instance.state} isSynced=${instance.isSynced}")
            try {
                if (instance.state == TaskState.COMPLETED) {
                    val response = RetrofitClient.getTaskInstanceApi(dataStore).completeInstance(
                        instance.id,
                        currentUserId,
                        instance.completedAt ?: System.currentTimeMillis())
                    if (response.isSuccessful) {
                        taskInstanceDao.updateSyncStatus(instance.id, isSynced = true)
                    }
                    return@forEach
                }

                val memberIds = taskInstanceDao.getMemberIdsForInstance(instance.id)
                val request = TaskInstanceApi.CreateTaskInstanceRequest(
                    id = instance.id,
                    taskId = instance.taskId,
                    dueDate = instance.dueDate,
                    state = instance.state.name,
                    completedAt = instance.completedAt,
                    memberIds = memberIds,
                    userId = currentUserId
                )
                val response = RetrofitClient.getTaskInstanceApi(dataStore).createTaskInstance(request)
                Log.d("SYNC", "completeInstance response: ${response.code()} body: ${response.errorBody()?.string()}")
                if (response.isSuccessful) {
                    taskInstanceDao.updateSyncStatus(instance.id, isSynced = true)
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando instancia ${instance.id}: ${e.message}")
            }
        }
    }


    suspend fun syncMembersForHome(homeId: String) {
        try {
            val response = RetrofitClient.getMemberApi(dataStore).getMembersByHome(homeId)
            if (response.isSuccessful) {
                val remoteMembers = response.body() ?: return

                val localMembers = remoteMembers.map { dto ->
                    MemberEntityNew(
                        id = dto.id,
                        userId = dto.userId ?: "",
                        homeId = dto.homeId,
                        name = dto.name,
                        lastName = dto.lastName ?: "",
                        colorHex = dto.colorHex ?: "#9E9E9E",
                        role = MemberRole.valueOf(dto.role ?: "MEMBER"),
                        status = MemberStatus.valueOf(dto.status ?: "ACTIVE"),
                        profilePicUrl = dto.profilePicUrl,
                        isSynced = true
                    )
                }

                appDatabase.withTransaction {
                    localMembers.forEach { member ->
                        upsertMember(member)
                    }
                    // Elimina solo los que el servidor ya no devuelve
                    val remoteIds = localMembers.map { it.id }.toSet()
                    val localIds = memberDao.getAllIdsByHomeId(homeId)
                    val toDelete = localIds.filter { it !in remoteIds }
                    if (toDelete.isNotEmpty()) {
                        memberDao.deleteByIds(toDelete)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Error al sincronizar miembros del hogar $homeId", e)
        }
    }

    suspend fun syncTasksForHome(homeId: String) {
        try {
            syncMembersForHome(homeId)
            syncRoomsForHome(homeId)

            val response = RetrofitClient.getTaskApi(dataStore).getTasksByHome(homeId)

            if (response.isSuccessful) {
                val remoteTasks = response.body() ?: return

                // Guardar instancias pendientes de sincronizar
                val pendingInstances = taskInstanceDao.getAllNew().first()
                    .filter { !it.isSynced }

                // Guardar isHidden ANTES de borrar
                val hiddenInstanceIds = taskInstanceDao.getAllNew().first()
                    .filter { it.isHidden }
                    .map { it.id }
                    .toSet()

                appDatabase.withTransaction {
                    taskInstanceDao.deleteAllByHomeId(homeId)
                    taskDao.deleteAllByHomeId(homeId)

                    remoteTasks.forEach { dto ->
                        taskDao.addTaskNew(
                            TaskEntityNew(
                                id = dto.id,
                                title = dto.title,
                                description = dto.description ?: "",
                                points = dto.points,
                                priority = PriorityLevel.valueOf(dto.priority),
                                suggestedDay = SuggestedDay.valueOf(dto.suggestedDay),
                                recurrence = RecurrenceType.valueOf(dto.recurrence),
                                workMode = WorkMode.valueOf(dto.workMode),
                                lastMemberIndex = dto.lastMemberIndex,
                                roomId = dto.roomId,
                                homeId = dto.homeId,
                                isSynced = true,
                                isPredetermined = dto.predetermined,
                                pausedUntil = dto.pausedUntil
                            )
                        )

                        if (dto.memberIds.isNotEmpty()) {
                            taskDao.addTaskMemberJoin(
                                dto.memberIds.map { TaskMemberJoin(taskId = dto.id, memberId = it) }
                            )
                        }

                        dto.instances.forEach { inst ->
                            val pendingLocal = pendingInstances.find { it.id == inst.id }

                            taskInstanceDao.insertInstanceWithAssignedMembers(
                                TaskInstanceEntityNew(
                                    id = inst.id,
                                    taskId = inst.taskId,
                                    dueDate = inst.dueDate,
                                    state = pendingLocal?.state ?: TaskState.valueOf(inst.state),
                                    completedAt = pendingLocal?.completedAt ?: inst.completedAt,
                                    isSynced = pendingLocal == null,
                                    isHidden = hiddenInstanceIds.contains(inst.id)
                                ),
                                inst.memberIds
                            )
                        }
                    }
                }

                Log.d("SYNC", "Tareas e instancias del hogar $homeId sincronizadas")
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Error al sincronizar tareas mediante FCM", e)
        }
    }



    suspend fun syncRoomsForHome(homeId: String) {
        try {
            val response = RetrofitClient.getRoomApi(dataStore).getRoomsByHome(homeId)
            if (response.isSuccessful) {
                val remoteRooms = response.body() ?: return

                appDatabase.withTransaction {

                    roomDao.deleteAllByHomeId(homeId)

                    remoteRooms.forEach { dto ->

                        roomDao.addNew(
                            RoomEntityNew(
                                id = dto.id,
                                name = dto.name,
                                icon = dto.icon,
                                colorHex = dto.colorHex,
                                homeId = dto.homeId,
                                isSynced = true
                            )
                        )
                    }
                }
                Log.d("SYNC", "Habitaciones del hogar $homeId sincronizadas exitosamente")
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Error al sincronizar habitaciones por FCM", e)
        }
    }


    suspend fun syncHomeDetails(homeId: String) {
        try {
            val currentUserId = dataStore.userIdFlow.first()
            if (!currentUserId.isNullOrEmpty()) {
                syncHomes(currentUserId)
                Log.d("SYNC", "Hogar actualizado exitosamente")
            }
        } catch (e: Exception) {
            Log.e("SYNC", "Error al sincronizar detalles del hogar por FCM", e)
        }
    }

    suspend fun syncChallengeProgress(userId: String, homeId: String) {
        val weekId = WeekUtils.getCurrentWeekId()
        val pending = challengeProgressDao.getProgressForWeekSuspend(userId, homeId, weekId)
            .filter { !it.isSynced }

        pending.forEach { progress ->
            try {
                val response = RetrofitClient.getChallengeApi(dataStore).upsert(
                    ChallengeProgressDto(
                        id = progress.id,
                        userId = progress.userId,
                        homeId = progress.homeId,
                        challengeType = progress.challengeType.name,
                        weekId = progress.weekId,
                        currentProgress = progress.currentProgress,
                        isCompleted = progress.isCompleted,
                        pointsAwarded = progress.pointsAwarded,
                        completedAt = progress.completedAt
                    )
                )
                if (response.isSuccessful) {
                    challengeProgressDao.markSynced(progress.id)
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando reto: ${e.message}")
            }
        }
    }

    suspend fun syncPendingEarnedPoints() {
        val pending = earnedPointsDao.getPendingSynced()
        pending.forEach { entry ->
            try {
                val response = RetrofitClient.getEarnedPointsApi(dataStore).save(
                    EarnedPointsApi.EarnedPointsDto(
                        entry.instanceId,
                        entry.userId,
                        entry.points,
                        entry.earnedAt
                    )
                )
                if (response.isSuccessful) {
                    earnedPointsDao.markSynced(entry.instanceId)
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error sincronizando earned points: ${e.message}")
            }
        }
    }


    suspend fun deleteHomeLocally(homeId: String) {
        appDatabase.withTransaction {
            homeDao.deleteHomeById(homeId)
        }
    }

    private suspend fun upsertMember(member: MemberEntityNew) {
        val updated = memberDao.updateMember(
            id = member.id,
            name = member.name,
            lastName = member.lastName,
            colorHex = member.colorHex,
            role = member.role.name,
            status = member.status.name,
            profilePicUrl = member.profilePicUrl,
            isSynced = member.isSynced
        )
        // Si no actualizó ninguna fila, el miembro no existía, se inserta sin replace
        if (updated == 0) {
            memberDao.addNew(member)
        }
    }

}