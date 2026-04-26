package haptikos.gestortareashogar_haptikos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.TaskRepository
import haptikos.gestortareashogar_haptikos.navigation.AppNavigation
import haptikos.gestortareashogar_haptikos.data.TaskDatabase
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.getValue

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        val database by lazy{ TaskDatabase.getDatabase(this, applicationScope)}

        val repository by lazy{ TaskRepository(database.taskDao()) }

        val authViewModel = AuthViewModel(DataStoreManager(this))
        val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(repository) }

        setContent {
            GestorTareasHogar_HaptikosTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    taskViewModel = taskViewModel)
            }
        }
    }
}

class TaskViewModelFactory(private val repository: TaskRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return TaskViewModel(repository) as T
    }
}
