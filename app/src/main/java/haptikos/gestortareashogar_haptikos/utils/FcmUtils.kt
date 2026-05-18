package haptikos.gestortareashogar_haptikos.utils

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FcmUtils {

    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            null
        }
    }
}