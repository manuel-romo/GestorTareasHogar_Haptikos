package haptikos.gestortareashogar_haptikos.data

import haptikos.gestortareashogar_haptikos.data.enumerators.UserGender
import haptikos.gestortareashogar_haptikos.network.AuthApi
import haptikos.gestortareashogar_haptikos.network.RetrofitClient
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, pass: String): Response<AuthApi.LoginResponse> {
        return RetrofitClient.authApi.login(AuthApi.LoginRequest(email, pass))
    }

    suspend fun signUp(name: String, gender: UserGender, dob: String, email: String, pass: String): Response<AuthApi.MessageResponse> {
        return RetrofitClient.authApi.register(
            AuthApi.RegisterRequest(name, email, pass, gender, dob)
        )
    }
}