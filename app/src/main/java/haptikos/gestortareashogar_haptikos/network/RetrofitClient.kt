package haptikos.gestortareashogar_haptikos.network

import haptikos.gestortareashogar_haptikos.data.DataStoreManager
import haptikos.gestortareashogar_haptikos.network.SyncApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    //private const val BASE_URL = "http://192.168.1.66:8080/"
    private const val BASE_URL = "https://gestortareashaptikosservidor-production.up.railway.app/"

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    // Creación de cliente autenticado
    private fun getAuthenticatedRetrofit(dataStore: DataStoreManager): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(dataStore))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Función para Home
    fun getHomeApi(dataStore: DataStoreManager): HomeApi {
        return getAuthenticatedRetrofit(dataStore).create(HomeApi::class.java)
    }

    // Función para Member
    fun getMemberApi(dataStore: DataStoreManager): MemberApi {
        return getAuthenticatedRetrofit(dataStore).create(MemberApi::class.java)
    }

    // Función para User
    fun getUserApi(dataStore: DataStoreManager): UserApi {
        return getAuthenticatedRetrofit(dataStore).create(UserApi::class.java)
    }

    // Función para Task
    fun getTaskApi(dataStore: DataStoreManager): TaskApi {
        return getAuthenticatedRetrofit(dataStore).create(TaskApi::class.java)
    }

    // Función para Room
    fun getRoomApi(dataStore: DataStoreManager): RoomApi {
        return getAuthenticatedRetrofit(dataStore).create(RoomApi::class.java)
    }

    // Instancias de Task
    fun getTaskInstanceApi(dataStore: DataStoreManager): TaskInstanceApi {
        return getAuthenticatedRetrofit(dataStore).create(TaskInstanceApi::class.java)
    }

    // Sincronización
    fun getSyncApi(dataStore: DataStoreManager): SyncApi {
        return getAuthenticatedRetrofit(dataStore).create(SyncApi::class.java)
    }

}