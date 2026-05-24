package haptikos.gestortareashogar_haptikos.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


class BiometricCredentialManager(context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "biometric_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    fun getCredentials(): Pair<String, String>? {
        val email = prefs.getString("email", null)
        val password = prefs.getString("password", null)
        return if (email != null && password != null) Pair(email, password) else null
    }

    fun hasCredentials(): Boolean = getCredentials() != null

    fun clearCredentials() {
        prefs.edit().clear().apply()
    }
}