package haptikos.gestortareashogar_haptikos.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map



private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val TOKEN = stringPreferencesKey("token")
        val PROFILE_PIC_URL = stringPreferencesKey("profile_pic_url")
        val USER_EMAIL = stringPreferencesKey("user_email")

        val NOTIFY_REMINDERS = booleanPreferencesKey("notify_reminders")
        val NOTIFY_COMPLETED = booleanPreferencesKey("notify_completed")
        val NOTIFY_NEW_MEMBERS = booleanPreferencesKey("notify_new_members")

    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { it[IS_LOGGED_IN] ?: false }

    val usernameFlow: Flow<String> = context.dataStore.data
        .map { it[USERNAME] ?: "" }

    val userIdFlow: Flow<String> = context.dataStore.data
        .map { it[USER_ID] ?: "" }

    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { it[TOKEN] }

    val profilePicUrlFlow: Flow<String?> = context.dataStore.data
        .map { it[PROFILE_PIC_URL] }

    val userEmailFlow: Flow<String> = context.dataStore.data
        .map { it[USER_EMAIL] ?: "" }

    val notifyRemindersFlow: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFY_REMINDERS] ?: true }

    val notifyCompletedFlow: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFY_COMPLETED] ?: true }

    val notifyNewMembersFlow: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFY_NEW_MEMBERS] ?: true }

    suspend fun saveUserName(newName: String) {
        context.dataStore.edit { it[USERNAME] = newName }
    }

    suspend fun saveProfilePicUrl(url: String) {
        context.dataStore.edit { it[PROFILE_PIC_URL] = url }
    }

    suspend fun saveSession(userId: String, username: String, email: String, token: String) {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = true
            it[USER_ID] = userId
            it[USERNAME] = username
            it[USER_EMAIL] = email
            it[TOKEN] = token
        }
    }

    suspend fun saveNotificationPreference(keyType: String, isEnabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (keyType) {
                "reminders" -> prefs[NOTIFY_REMINDERS] = isEnabled
                "completed" -> prefs[NOTIFY_COMPLETED] = isEnabled
                "newMembers" -> prefs[NOTIFY_NEW_MEMBERS] = isEnabled
            }
        }
    }


    suspend fun logout() {
        context.dataStore.edit {
            it.clear()
        }
    }
}