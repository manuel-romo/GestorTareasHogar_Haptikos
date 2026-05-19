package haptikos.gestortareashogar_haptikos.data

import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import haptikos.gestortareashogar_haptikos.network.AuthApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import retrofit2.HttpException

class AuthRepository {

    suspend fun login(email: String, pass: String): Result<AuthApi.AuthResponse> {
        return try {
            val response = RetrofitClient.authApi.login(AuthApi.LoginRequest(email, pass))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(id: String, name: String, gender: UserGender, dob: String, email: String, pass: String): Result<AuthApi.AuthResponse> {
        return try {
            val response = RetrofitClient.authApi.register(
                AuthApi.RegisterRequest(id, name, email, pass, gender, dob)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}