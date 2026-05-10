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

    suspend fun saveProfilePicUrl(url: String) {
        context.dataStore.edit { it[PROFILE_PIC_URL] = url }
    }

    suspend fun saveSession(userId: String, username: String, token: String) {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = true
            it[USER_ID] = userId
            it[USERNAME] = username
            it[TOKEN] = token
        }
    }


    suspend fun logout() {
        context.dataStore.edit {
            it.clear()
        }
    }
}