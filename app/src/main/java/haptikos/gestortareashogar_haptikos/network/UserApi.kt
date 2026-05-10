package haptikos.gestortareashogar_haptikos.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
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
}