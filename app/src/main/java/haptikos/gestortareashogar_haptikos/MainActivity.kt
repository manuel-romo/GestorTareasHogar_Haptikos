package haptikos.gestortareashogar_haptikos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.navigation.AppNavigation
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
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

        val repository by lazy{
            AppRepository(
                database.taskDao(),
                database.taskInstanceDao(),
                database.memberDao(),
                database.roomDao()
            )
        }

        val authViewModel = AuthViewModel(DataStoreManager(this))
        val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(repository) }
        val taskInstanceViewModel: TaskInstanceViewModel by viewModels { TaskInstanceViewModelFactory(repository) }
        val roomViewModel: RoomViewModel by viewModels { RoomViewModelFactory(repository) }
        val memberViewModel: MemberViewModel by viewModels { MemberViewModelFactory(repository) }

        setContent {
            GestorTareasHogar_HaptikosTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    taskViewModel = taskViewModel,
                    taskInstanceViewModel = taskInstanceViewModel,
                    memberViewModel = memberViewModel,
                    roomViewModel = roomViewModel
                )
            }
        }
    }
}

class TaskViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return TaskViewModel(repository) as T
    }
}

class TaskInstanceViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return TaskInstanceViewModel(repository) as T
    }
}

class RoomViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return RoomViewModel(repository) as T
    }
}

class MemberViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return MemberViewModel(repository) as T
    }
}