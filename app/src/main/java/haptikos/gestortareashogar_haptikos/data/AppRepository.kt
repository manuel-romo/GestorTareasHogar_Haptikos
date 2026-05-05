package haptikos.gestortareashogar_haptikos.data

import androidx.room.withTransaction
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
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
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAll()
    val allTasksInstance: Flow<List<TaskInstanceEntity>> = taskInstanceDao.getAll()
    val allMembers: Flow<List<MemberEntity>> = memberDao.getAll()
    val allRooms: Flow<List<RoomEntity>> = roomDao.getAll()

    // Operaciones con tareas
    suspend fun insertTask(task: TaskEntity) {
        taskDao.add(task)
    }
    suspend fun updateTask(task: TaskEntity) {
        taskDao.update(task)
    }
    suspend fun deleteTask(task: TaskEntity) {
        taskDao.delete(task)
    }

    suspend fun getTaskById(taskId: Int): TaskEntity? {
        return taskDao.getById(taskId)
    }

    // Operaciones con tareas (instancias)
    suspend fun insertTaskInstance(taskInstance: TaskInstanceEntity) {
        taskInstanceDao.add(taskInstance)
    }
    suspend fun updateTaskInstance(taskInstance: TaskInstanceEntity) {
        taskInstanceDao.update(taskInstance)
    }
    suspend fun deleteTaskInstance(taskInstance: TaskInstanceEntity) {
        taskInstanceDao.delete(taskInstance)
    }

    suspend fun getTaskInstanceById(taskInstanceId: Int): TaskInstanceEntity? {
        return taskInstanceDao.getById(taskInstanceId)
    }

    // Operaciones con miembros
    suspend fun insertMember(member: MemberEntity) {
        memberDao.add(member)
    }


    // Operaciones con habitaciones
    suspend fun insertRoom(room: RoomEntity) {
        roomDao.add(room)
    }


    // Funciones nuevas -----------------------------------------------
    // Lecturas reactivas
    val allTasksNew: Flow<List<TaskEntityNew>> = taskDao.getAllNew()
    val allTasksInstanceNew: Flow<List<TaskInstanceEntityNew>> = taskInstanceDao.getAllNew()
    val allMembersNew: Flow<List<MemberEntityNew>> = memberDao.getAllNew()
    val allRoomsNew: Flow<List<RoomEntityNew>> = roomDao.getAllNew()

    val allTasksWithDetails: Flow<List<TaskWithDetails>> = taskDao.getAllTasksWithDetails()
    val allInstancesWithDetails: Flow<List<TaskInstanceWithDetails>> = taskInstanceDao.getAllInstancesWithDetails()

    // Operaciones de Inserción
    suspend fun insertTaskNew(task: TaskEntityNew, memberIds: List<Int>) {
        taskDao.insertTaskWithMembers(task, memberIds)
    }

    suspend fun insertTaskInstanceNew(taskInstance: TaskInstanceEntityNew, memberIds: List<Int>) {
        taskInstanceDao.insertInstanceWithAssignedMembers(taskInstance, memberIds)
    }

    suspend fun getTaskWithDetailsById(taskId: Int): TaskWithDetails? {
        return taskDao.getTaskWithDetailsById(taskId)
    }

    // Operaciones de Actualización y Eliminación
    suspend fun updateTaskNewWithMembers(task: TaskEntityNew, memberIds: List<Int>) {
        taskDao.updateTaskWithMembers(task, memberIds)
    }

    suspend fun deleteTaskNew(task: TaskEntityNew) {
        taskDao.deleteTaskBaseNew(task)
    }

    suspend fun getFilteredInstances (status: TaskState?,
                                      searchQuery: String,
                                      memberName: String?): Flow<List<TaskInstanceWithDetails>>{
        return taskInstanceDao.getFilteredInstances(status, searchQuery, memberName)
    }

    // Operaciones con miembros
    suspend fun insertMemberNew(member: MemberEntityNew) {
        memberDao.addNew(member)
    }

    suspend fun updateMemberNew(member: MemberEntityNew) {
        memberDao.updateNew(member)
    }

    suspend fun deleteMemberNew(member: MemberEntityNew) {
        memberDao.deleteNew(member)
    }

    // Operaciones con habitaciones
    suspend fun insertRoomNew(room: RoomEntityNew) {
        roomDao.addNew(room)
    }

    suspend fun updateRoomNew(room: RoomEntityNew) {
        roomDao.updateNew(room)
    }

    suspend fun deleteRoomNew(room: RoomEntityNew) {
        roomDao.deleteNew(room)
    }

    // Lectura reactiva Hogares
    val allHomes: Flow<List<HomeEntityNew>> = homeDao.getAllHomes()

    suspend fun getHomeById(homeId: Int): HomeEntityNew? {
        return homeDao.getHomeById(homeId)
    }

    suspend fun updateHome(home: HomeEntityNew) {
        homeDao.updateHome(home)
    }

    suspend fun deleteHome(home: HomeEntityNew) {
        homeDao.deleteHome(home)
    }

    // Creación de nuevo hogar con nuevo código de invitación (Temporal)
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