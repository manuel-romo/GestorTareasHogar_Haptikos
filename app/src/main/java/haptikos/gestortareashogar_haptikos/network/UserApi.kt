package haptikos.gestortareashogar_haptikos.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {

    @Multipart
    @POST("/api/users/{userId}/profile-picture")
    suspend fun uploadProfilePicture(
        @Path("userId") userId: String,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>


    data class UpdateUserRequest(
        val name: String? = null,
        val notifyTaskReminders: Boolean? = null,
        val notifyTaskCompleted: Boolean? = null,
        val notifyNewMembers: Boolean? = null
    )

    // Actualización global
    @PATCH("/api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body request: UpdateUserRequest
    ): Response<Map<String, String>>

    // Envío de token FMC
    @PATCH("/api/users/{userId}/fcm-token")
    suspend fun updateFcmToken(
        @Path("userId") userId: String,
        @Body body: Map<String, String>
    ): Response<Void>

}