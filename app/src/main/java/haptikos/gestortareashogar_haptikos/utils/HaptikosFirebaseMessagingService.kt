package haptikos.gestortareashogar_haptikos.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import haptikos.gestortareashogar_haptikos.MainActivity
import haptikos.gestortareashogar_haptikos.R
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.data.database.TaskDatabase
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HaptikosFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Guardar token localmente para mandarlo al servidor después
        CoroutineScope(Dispatchers.IO).launch {
            val dataStore = DataStoreManager(applicationContext)
            dataStore.saveFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: return
        val body = remoteMessage.notification?.body ?: return
        val type = remoteMessage.data["type"] ?: ""
        val homeId = remoteMessage.data["homeId"] ?: ""

        // Guardar en Room local
        CoroutineScope(Dispatchers.IO).launch {
            val db = TaskDatabase.getDatabase(applicationContext,
                CoroutineScope(SupervisorJob() + Dispatchers.IO))
            db.notificationDao().insert(
                NotificationEntity(
                    title = title,
                    body = body,
                    type = type,
                    homeId = homeId
                )
            )
        }

        // Mostrar notificación del sistema
        showNotification(title, body)
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