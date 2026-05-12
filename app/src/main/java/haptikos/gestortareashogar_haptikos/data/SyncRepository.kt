package haptikos.gestortareashogar_haptikos.data

import android.util.Log
import androidx.room.withTransaction
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.enumerators.HomePermission
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberStatus
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.network.HomeApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.network.RoomApi
import haptikos.gestortareashogar_haptikos.network.TaskApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class SyncRepository(
    private val dataStore: DataStoreManager,
    private val appDatabase: TaskDatabase,
    private val homeDao: HomeDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao,
    private val roomDao: RoomDao
) {

    private val allHomes: Flow<List<HomeEntityNew>> = homeDao.getAllHomes()
    private val allMembersNew: Flow<List<MemberEntityNew>> = memberDao.getAllNew()
    private val allTasksNew: Flow<List<TaskEntityNew>> = taskDao.getAllNew()

    val hasPendingSyncs: Flow<Boolean> = combine(
        allHomes,
        allMembersNew,
        allTasksNew
    ) { homes, members, tasks ->
        homes.any { !it.isSynced } ||
                members.any { !it.isSynced } ||
                tasks.any { !it.isSynced }
    }


    suspend fun syncAll(userId: String) {
        syncPendingItems()
        syncHomes(userId)
    }

    suspend fun syncPendingItems() {
        syncPendingHomes()
        syncPendingRooms()
        syncPendingTasks()
    }

    private suspend fun syncPendingHomes() {
        val pendingHomes = homeDao.getAllHomes().first().filter { !it.isSynced }
        Log.d("SYNC", "Hogares pendientes: ${pendingHomes.size}")
        pendingHomes.forEach { home ->
            try {
                val creatorMember = memberDao.getCreatorByHomeId(home.id)
                Log.d("SYNC", "Subiendo hogar: ${home.name}, creatorMember: ${creatorMember?.id}")
                val request = HomeApi.CreateHomeRequest(
                    id = home.id,
                    name = home.name,
                    description = home.description,
                    isPrivate = home.isPrivate,
                    creatorId = dataStore.userIdFlow.first(),
                    creatorName = creatorMember?.name ?: dataStore.usernameFlow.first(),
                    creatorLastName = creatorMember?.lastName ?: "",
                    creatorColorHex = "#9E9E9E",
                    creatorMemberId = "",
                    invitedUsers = emptyList()
                )
                val response = RetrofitClient.getHomeApi(dataStore).createHome(request)
                if (response.isSuccessful) {
                    val inviteCode = response.body()?.inviteCode
                    homeDao.updateInviteCodeAndSync(home.id, inviteCode, isSynced = true)
                    memberDao.markMembersAsSynced(home.id)
                }
                Log.d("SYNC", "Respuesta: ${response.code()} - ${response.errorBody()?.string()}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SYNC", "Error: ${e.message}")
            }
        }
    }

    private suspend fun syncPendingRooms() {
        val pendingRooms = roomDao.getAllNew().first().filter { !it.isSynced }
        pendingRooms.forEach { room ->
            try {
                val request = RoomApi.CreateRoomRequest(
                    id = room.id,
                    name = room.name,
                    icon = room.icon,
                    colorHex = room.colorHex,
                    homeId = room.homeId
                )
                val response = RetrofitClient.getRoomApi(dataStore).createRoom(request)
                if (response.isSuccessful) {
                    roomDao.updateNew(room.copy(isSynced = true))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun syncPendingTasks() {
        val pendingTasks = taskDao.getAllNew().first().filter { !it.isSynced }
        pendingTasks.forEach { task ->
            try {
                val memberIds = taskDao.getMemberIdsForTask(task.id)
                val request = TaskApi.CreateTaskRequest(
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
                    memberIds = memberIds
                )
                val response = RetrofitClient.getTaskApi(dataStore).createTask(request)
                if (response.isSuccessful) {
                    taskDao.updateSyncStatus(task.id, isSynced = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                        homeDao.insertHome(
                            HomeEntityNew(
                                id = homeDto.id,
                                name = homeDto.name,
                                description = homeDto.description,
                                isPrivate = homeDto.isPrivate,
                                inviteCode = homeDto.inviteCode,
                                editPermission = HomePermission.valueOf(
                                    homeDto.editPermission ?: "CREATOR_ONLY"
                                ),
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}