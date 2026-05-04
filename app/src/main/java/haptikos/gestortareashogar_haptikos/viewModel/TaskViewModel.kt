package haptikos.gestortareashogar_haptikos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskWithDetails
import haptikos.gestortareashogar_haptikos.utils.getDayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: AppRepository) : ViewModel() {

    // Escritura
    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    suspend fun getById(taskId: Int): TaskEntity?{
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
    fun addTaskNew(task: TaskEntityNew, selectedMemberIds: List<Int>) {
        viewModelScope.launch {
            repository.insertTaskNew(task, selectedMemberIds)
        }
    }

    // Edición de tarea
    fun updateTaskNew(task: TaskEntityNew, selectedMemberIds: List<Int>) {
        viewModelScope.launch {
            repository.updateTaskNewWithMembers(task, selectedMemberIds)
        }
    }

    fun deleteTaskNew(task: TaskEntityNew) {
        viewModelScope.launch {
            repository.deleteTaskNew(task)
        }
    }

    suspend fun getByIdNew(taskId: Int): TaskWithDetails? {
        return repository.getTaskWithDetailsById(taskId)
    }




}