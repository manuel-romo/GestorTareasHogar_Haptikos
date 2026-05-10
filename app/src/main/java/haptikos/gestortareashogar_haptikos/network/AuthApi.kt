package haptikos.gestortareashogar_haptikos.network

import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    // Modelos de Request
    data class RegisterRequest(
        val name: String,
        val email: String,
        val password: String,
        val gender: UserGender,
        val dob: String
    )
    data class LoginRequest(val email: String, val password: String)

    // Modelos de Response
    data class LoginResponse(val token: String, val name: String)
    data class MessageResponse(val message: String)

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}