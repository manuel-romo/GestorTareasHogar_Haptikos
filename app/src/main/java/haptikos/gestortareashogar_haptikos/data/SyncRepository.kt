package haptikos.gestortareashogar_haptikos.data

import android.util.Log
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
        syncPendingItems()
        syncHomes(userId)
        
        val homes = homeDao.getAllHomes().first()
        homes.forEach { home ->
            syncRoomsForHome(home.id)
            syncTasksForHome(home.id)
        }
    }

    suspend fun syncPendingItems() {
        syncPendingHomes()
        syncPendingMembers()
        syncPendingRooms()
        syncPendingTasks()
        syncPendingTaskInstances()
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

            if (syncedHomes.contains(member.homeId)) {
                try {
                    Log.d("SYNC", "Subiendo miembro: ${member.name} al hogar ${member.homeId}")
                    val request = MemberApi.CreateMemberRequest(
                        id = member.id,
                        userId = member.userId.ifEmpty { null },
                        homeId = member.homeId,
                        name = member.name,
                        lastName = member.lastName,
                        colorHex = member.colorHex,
                        role = member.role.name,
                        status = member.status.name
                    )

                    val response = RetrofitClient.getMemberApi(dataStore).createMember(request)

                    if (response.isSuccessful) {
                        memberDao.updateSyncStatus(member.id, isSynced = true)
                    } else {
                        Log.d("SYNC", "Error al subir miembro: ${response.code()}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("SYNC", "Excepción subiendo miembro: ${e.message}")
                }
            } else {
                Log.d("SYNC", "Se pospuso el miembro ${member.id} porque su hogar aún no está sincronizado.")
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
                        } else {
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
                        }
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncPendingTaskInstances() {
        val pendingInstances = taskInstanceDao.getAllNew().first().filter { !it.isSynced }

        val currentUserId = dataStore.userIdFlow.first()

        pendingInstances.forEach { instance ->
            try {
                if (instance.state == TaskState.COMPLETED) {
                    val response = RetrofitClient.getTaskInstanceApi(dataStore).completeInstance(instance.id, currentUserId)
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
                    memberIds = memberIds,
                    userId = currentUserId
                )
                val response = RetrofitClient.getTaskInstanceApi(dataStore).createTaskInstance(request)
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
                val remoteMembers = response.body()

                if (remoteMembers != null) {
                    // Mapeo de datos recibidos
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
                            isSynced = true
                        )
                    }

                    appDatabase.withTransaction {

                        memberDao.deleteAllByHomeId(homeId)
                        localMembers.forEach { member ->
                            memberDao.addNew(member)
                        }
                    }
                    Log.d("SYNC", "Miembros del hogar $homeId sincronizados exitosamente por FCM")
                }
            } else {
                Log.e("SYNC", "Error HTTP al traer miembros: ${response.code()}")
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

                appDatabase.withTransaction {
                    // 1. Borrar en orden correcto respetando FKs
                    taskInstanceDao.deleteAllByHomeId(homeId)
                    taskDao.deleteAllMemberJoinsByHomeId(homeId)  // <-- agregar esto
                    taskDao.deleteAllByHomeId(homeId)

                    // 2. Insertar tareas
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
                            taskInstanceDao.insertInstanceWithAssignedMembers(
                                TaskInstanceEntityNew(
                                    id = inst.id,
                                    taskId = inst.taskId,
                                    dueDate = inst.dueDate,
                                    state = TaskState.valueOf(inst.state),
                                    isSynced = true
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

    suspend fun deleteHomeLocally(homeId: String) {
        appDatabase.withTransaction {
            homeDao.deleteHomeById(homeId)
        }
    }

}