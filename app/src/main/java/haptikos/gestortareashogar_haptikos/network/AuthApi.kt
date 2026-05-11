package haptikos.gestortareashogar_haptikos.network

import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    // Modelos de Request
    data class RegisterRequest(
        val id: String,
        val name: String,
        val email: String,
        val password: String,
        val gender: UserGender,
        val dob: String
    )
    data class LoginRequest(val email: String, val password: String)

    data class AuthResponse(
        val message: String?,
        val token: String?,
        val name: String?,
        val id: String?,
        val email: String?
    )

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}