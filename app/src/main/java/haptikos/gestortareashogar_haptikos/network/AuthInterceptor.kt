package haptikos.gestortareashogar_haptikos.network

import android.util.Log
import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val dataStore: DataStoreManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        // Se obtiene el token de forma síncrona para el interceptor
        val token = runBlocking {
            dataStore.tokenFlow.first()
        }

        val requestBuilder = chain.request().newBuilder()

        // Si el token existe se inyecta en la cabecera Authorization
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}