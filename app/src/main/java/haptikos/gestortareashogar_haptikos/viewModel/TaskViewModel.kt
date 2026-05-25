package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: AppRepository) : ViewModel() {

    // Escritura

    fun deleteTask(task: TaskEntityNew) {
        viewModelScope.launch {
            repository.deleteTaskNew(task)
        }
    }

    suspend fun getById(taskId: String): TaskEntityNew?{
        return repository.getTaskById(taskId)
    }

    // Tarea con datos completos
    val tasksWithDetails: StateFlow<List<TaskWithDetails>> = repository.allTasksWithDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Agregar tarea con miembros
    fun addTaskNew(task: TaskEntityNew, selectedMemberIds: List<String>) {
        viewModelScope.launch {
            repository.insertTaskNew(task, selectedMemberIds)
            repository.syncPendingTasksNow()
        }
    }

    // Edición de tarea
    fun updateTaskNew(task: TaskEntityNew, selectedMemberIds: List<String>) {
        viewModelScope.launch {
            repository.updateTaskNewWithMembers(task, selectedMemberIds)
            repository.syncPendingTasksNow()
        }
    }

    fun deleteTaskNew(task: TaskEntityNew) {
        viewModelScope.launch {
            repository.deleteTaskNew(task)
        }
    }

    suspend fun getByIdNew(taskId: String): TaskWithDetails? {
        return repository.getTaskWithDetailsById(taskId)
    }


    // Estados para retroalimentación de tareas
    data class TaskFeedback(val title: String, val subtitle: String)

    private val _taskFeedback = MutableStateFlow<TaskFeedback?>(null)
    val taskFeedback = _taskFeedback.asStateFlow()

    private fun showSuccessFeedback(title: String, subtitle: String) {
        _taskFeedback.value = TaskFeedback(title, subtitle)
    }

    // Crear aviso
    fun dismissFeedback() {
        _taskFeedback.value = null
    }

    // Eliminación de tarea
    private val _taskToDelete = MutableStateFlow<TaskEntityNew?>(null)
    val taskToDelete = _taskToDelete.asStateFlow()

    fun initiateTaskDeletion(task: TaskEntityNew) {
        _taskToDelete.value = task
    }

    fun cancelTaskDeletion() {
        _taskToDelete.value = null
    }

    fun confirmTaskDeletion() {
        val task = _taskToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteTaskNew(task)
            _taskToDelete.value = null
            showSuccessFeedback("Actividad eliminada", "La tarea se eliminó correctamente.")
        }
    }

    // Estados para pausa
    private val _taskToPause = MutableStateFlow<TaskEntityNew?>(null)
    val taskToPause = _taskToPause.asStateFlow()

    // Acciones

    fun initiatePause(task: TaskEntityNew) {
        _taskToPause.value = task
    }

    fun cancelPause() {
        _taskToPause.value = null
    }

    fun confirmPause(pausedUntil: Long) {
        val task = _taskToPause.value ?: return
        viewModelScope.launch {
            repository.updateTaskOnly(task.copy(pausedUntil = pausedUntil, isSynced = false))
            repository.syncPendingTasksNow()
            _taskToPause.value = null
            showSuccessFeedback(
                title = "Tarea pausada",
                subtitle = "La tarea no aparecerá como pendiente durante el periodo indicado."
            )
        }
    }

    fun resumeTask(task: TaskEntityNew) {
        viewModelScope.launch {
            repository.updateTaskOnly(task.copy(pausedUntil = null, isSynced = false))
            repository.syncPendingTasksNow()
            showSuccessFeedback(
                title = "Tarea reanudada",
                subtitle = "La tarea vuelve a aparecer como pendiente."
            )
        }
    }

    suspend fun getMemberIdsOrdered(taskId: String): List<String> {
        return repository.getMemberIdsForTask(taskId)
    }


}