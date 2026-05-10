package haptikos.gestortareashogar_haptikos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.AuthRepository
import haptikos.gestortareashogar_haptikos.navigation.AppNavigation
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
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

        val dataStoreManager = DataStoreManager(this)

        val repository by lazy{
            AppRepository(
                database.taskDao(),
                database.taskInstanceDao(),
                database.memberDao(),
                database.roomDao(),
                database.homeDao(),
                database,
                dataStore = dataStoreManager

            )
        }

        val authRepository by lazy { AuthRepository() }

        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(authRepository, dataStoreManager) }
        val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(repository) }
        val taskInstanceViewModel: TaskInstanceViewModel by viewModels { TaskInstanceViewModelFactory(repository) }
        val roomViewModel: RoomViewModel by viewModels { RoomViewModelFactory(repository) }
        val memberViewModel: MemberViewModel by viewModels { MemberViewModelFactory(repository) }
        val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(repository, dataStoreManager) }

        setContent {
            GestorTareasHogar_HaptikosTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    taskViewModel = taskViewModel,
                    taskInstanceViewModel = taskInstanceViewModel,
                    memberViewModel = memberViewModel,
                    roomViewModel = roomViewModel,
                    homeViewModel = homeViewModel
                )
            }
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val dataStore: DataStoreManager
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository, dataStore) as T
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

class HomeViewModelFactory(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return HomeViewModel(repository, dataStore) as T
    }
}