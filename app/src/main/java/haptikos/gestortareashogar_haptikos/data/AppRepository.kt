package haptikos.gestortareashogar_haptikos.data

import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val taskDao: TaskDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val memberDao: MemberDao,
    private val roomDao: RoomDao
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


}