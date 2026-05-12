package haptikos.gestortareashogar_haptikos.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import haptikos.gestortareashogar_haptikos.data.AppRepository

class TaskGeneratorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            RepositoryHolder.repository?.generatePendingInstances()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

object RepositoryHolder {
    var repository: AppRepository? = null
}