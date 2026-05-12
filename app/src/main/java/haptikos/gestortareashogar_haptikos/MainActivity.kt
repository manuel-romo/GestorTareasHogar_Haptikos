package haptikos.gestortareashogar_haptikos

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.AuthRepository
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.navigation.AppNavigation
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.ProfileViewModel
import haptikos.gestortareashogar_haptikos.viewModel.RoomViewModel
import haptikos.gestortareashogar_haptikos.viewModel.SyncViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskInstanceViewModel
import haptikos.gestortareashogar_haptikos.viewModel.TaskViewModel
import haptikos.gestortareashogar_haptikos.workers.RepositoryHolder
import haptikos.gestortareashogar_haptikos.workers.TaskGeneratorWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
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

        RepositoryHolder.repository = repository

        // Se generan instancias al abrir la app
        lifecycleScope.launch {
            repository.generatePendingInstances()
        }

        // Se programa el WorkManager
        val workRequest = PeriodicWorkRequestBuilder<TaskGeneratorWorker>(
            1, TimeUnit.DAYS
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "task_generator",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        // KEEP para que si ya existe un WorkManager con ese nombre no se reemplace

        val syncRepository by lazy {
            SyncRepository(
                dataStore = dataStoreManager,
                appDatabase = database,
                homeDao = database.homeDao(),
                taskDao = database.taskDao(),
                roomDao = database.roomDao(),
                memberDao = database.memberDao(),
                taskInstanceDao = database.taskInstanceDao()
            )
        }

        val authRepository by lazy { AuthRepository() }

        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(authRepository, syncRepository, dataStoreManager) }
        val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(repository) }
        val taskInstanceViewModel: TaskInstanceViewModel by viewModels { TaskInstanceViewModelFactory(repository, dataStoreManager) }
        val roomViewModel: RoomViewModel by viewModels { RoomViewModelFactory(repository) }
        val memberViewModel: MemberViewModel by viewModels { MemberViewModelFactory(repository) }
        val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(repository, dataStoreManager) }
        val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory(repository, dataStoreManager) }
        val syncViewModel: SyncViewModel by viewModels { SyncViewModelFactory(application, syncRepository) }

        setContent {
            GestorTareasHogar_HaptikosTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    taskViewModel = taskViewModel,
                    taskInstanceViewModel = taskInstanceViewModel,
                    memberViewModel = memberViewModel,
                    roomViewModel = roomViewModel,
                    homeViewModel = homeViewModel,
                    profileViewModel = profileViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val dataStore: DataStoreManager
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository, syncRepository, dataStore) as T
    }
}

class TaskViewModelFactory(private val repository: AppRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return TaskViewModel(repository) as T
    }
}

class TaskInstanceViewModelFactory(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
    ): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return TaskInstanceViewModel(repository, dataStore) as T
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

class ProfileViewModelFactory(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return ProfileViewModel(repository, dataStore) as T
    }
}

class SyncViewModelFactory(
    private val application: Application,
    private val syncRepository: SyncRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyncViewModel(application, syncRepository) as T
    }
}
