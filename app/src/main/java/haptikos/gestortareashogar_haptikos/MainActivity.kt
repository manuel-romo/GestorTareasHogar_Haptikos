package haptikos.gestortareashogar_haptikos

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessaging
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.AppRepository
import haptikos.gestortareashogar_haptikos.data.AuthRepository
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.data.dao.NotificationDao
import haptikos.gestortareashogar_haptikos.navigation.AppNavigation
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.ui.theme.GestorTareasHogar_HaptikosTheme
import haptikos.gestortareashogar_haptikos.viewModel.AuthViewModel
import haptikos.gestortareashogar_haptikos.viewModel.HomeViewModel
import haptikos.gestortareashogar_haptikos.viewModel.MemberViewModel
import haptikos.gestortareashogar_haptikos.viewModel.NotificationViewModel
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
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import haptikos.gestortareashogar_haptikos.data.BiometricCredentialManager
import haptikos.gestortareashogar_haptikos.viewModel.RewardViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = true
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        val database by lazy{ TaskDatabase.getDatabase(this, applicationScope)}

        val dataStoreManager = DataStoreManager(this)

        val biometricCredentialManager = BiometricCredentialManager(this)

        val repository by lazy{
            AppRepository(
                database.taskDao(),
                database.taskInstanceDao(),
                database.memberDao(),
                database.roomDao(),
                database.homeDao(),
                database.challengeProgressDao(),
                database.earnedPointsDao(),
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
                challengeProgressDao = database.challengeProgressDao(),
                earnedPointsDao = database.earnedPointsDao(),
                taskInstanceDao = database.taskInstanceDao()
            )
        }

        val authRepository by lazy { AuthRepository() }

        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(authRepository, syncRepository, dataStoreManager, biometricCredentialManager, database) }
        val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(repository) }
        val taskInstanceViewModel: TaskInstanceViewModel by viewModels { TaskInstanceViewModelFactory(repository, dataStoreManager) }
        val roomViewModel: RoomViewModel by viewModels { RoomViewModelFactory(repository) }
        val memberViewModel: MemberViewModel by viewModels { MemberViewModelFactory(repository, dataStoreManager) }
        val homeViewModel: HomeViewModel by viewModels { HomeViewModelFactory(repository, dataStoreManager) }
        val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory(repository, dataStoreManager) }
        val syncViewModel: SyncViewModel by viewModels { SyncViewModelFactory(application, syncRepository) }
        val notificationViewModel: NotificationViewModel by viewModels { NotificationViewModelFactory(database.notificationDao()) }
        val rewardViewModel: RewardViewModel by viewModels { RewardViewModelFactory(repository, dataStoreManager) }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM_TOKEN", "Token: $token")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

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
                    notificationViewModel = notificationViewModel,
                    rewardViewModel = rewardViewModel
                )
            }
        }

    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val dataStore: DataStoreManager,
    private val biometricCredentialManager: BiometricCredentialManager,
    private val database: TaskDatabase
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository, syncRepository, dataStore, biometricCredentialManager, database) as T
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

class MemberViewModelFactory(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        return MemberViewModel(repository, dataStore) as T
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

class NotificationViewModelFactory(
    private val notificationDao: NotificationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationViewModel(notificationDao) as T
    }
}

class RewardViewModelFactory(
    private val repository: AppRepository,
    private val dataStore: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RewardViewModel(repository, dataStore) as T
    }
}