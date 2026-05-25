package haptikos.gestortareashogar_haptikos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val homeId = inputData.getString("homeId") ?: return Result.failure()
        val type = inputData.getString("type") ?: return Result.failure()

        Log.d("SYNC_WORKER", "Ejecutando tipo=$type homeId=$homeId")

        val dataStore = DataStoreManager(applicationContext)
        val userId = dataStore.userIdFlow.first()
        if (userId.isNullOrEmpty()) return Result.failure()

        val db = TaskDatabase.getDatabase(
            applicationContext,
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
        val syncRepository = SyncRepository(
            dataStore = dataStore,
            appDatabase = db,
            homeDao = db.homeDao(),
            memberDao = db.memberDao(),
            taskDao = db.taskDao(),
            taskInstanceDao = db.taskInstanceDao(),
            challengeProgressDao = db.challengeProgressDao(),
            earnedPointsDao = db.earnedPointsDao(),
            roomDao = db.roomDao()
        )

        return try {
            when (type) {
                "SYNC_MEMBERS", "NEW_MEMBER", "MEMBER_JOINED" ->
                    syncRepository.syncMembersForHome(homeId)
                "SYNC_TASKS" -> {
                    delay(1500)
                    syncRepository.syncTasksForHome(homeId)
                }
                "SYNC_ROOMS" ->
                    syncRepository.syncRoomsForHome(homeId)
                "SYNC_HOME" ->
                    syncRepository.syncHomeDetails(homeId)
                "HOME_DELETED" ->
                    syncRepository.deleteHomeLocally(homeId)
                "SYNC_EARNED_POINTS" ->
                    syncRepository.syncEarnedPointsForUser(userId)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error: ${e.message}")
            Result.retry()
        }
    }
}