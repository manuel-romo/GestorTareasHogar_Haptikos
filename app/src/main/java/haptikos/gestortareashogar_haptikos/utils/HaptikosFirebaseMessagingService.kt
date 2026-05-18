package haptikos.gestortareashogar_haptikos.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import haptikos.gestortareashogar_haptikos.MainActivity
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.SyncRepository
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.entity.NotificationEntity
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import haptikos.gestortareashogar_haptikos.workers.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HaptikosFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            val dataStore = DataStoreManager(applicationContext)
            // Se guarda el token localmente
            dataStore.saveFcmToken(token)

            // Actualización repentina
            try {
                // Se obtiene el ID del usuario actual
                val userId = dataStore.userIdFlow.first()
                if (!userId.isNullOrEmpty()) {
                    RetrofitClient.getUserApi(dataStore).updateFcmToken(
                        userId,
                        mapOf("fcmToken" to token)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Mensaje recibido: type=${remoteMessage.data["type"]} homeId=${remoteMessage.data["homeId"]}")
        val type = remoteMessage.data["type"] ?: ""
        val homeId = remoteMessage.data["homeId"] ?: ""
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val syncTypes = setOf(
            "SYNC_MEMBERS", "NEW_MEMBER", "MEMBER_JOINED",
            "SYNC_TASKS", "SYNC_ROOMS", "SYNC_HOME", "HOME_DELETED"
        )

        if (type in syncTypes) {
            runSyncSilently(homeId, type)
        }

        if (!title.isNullOrEmpty() && !body.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = TaskDatabase.getDatabase(
                    applicationContext,
                    CoroutineScope(SupervisorJob() + Dispatchers.IO)
                )
                db.notificationDao().insert(
                    NotificationEntity(
                        title = title,
                        body = body,
                        type = type,
                        homeId = homeId
                    )
                )
            }
            showNotification(title, body)
        }
    }


    private fun runSyncSilently(homeId: String, type: String) {
        if (homeId.isEmpty()) return
        val data = workDataOf("homeId" to homeId, "type" to type)
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "$type-$homeId",
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "haptikos_channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Haptikos", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}