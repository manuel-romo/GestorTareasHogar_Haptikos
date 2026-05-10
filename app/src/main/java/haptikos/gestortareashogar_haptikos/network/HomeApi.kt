package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface HomeApi {

    // Modelos de Request
    data class CreateHomeRequest(
        val homeId: String,
        val homeName: String,
        val homeDescription: String?,
        val isPrivate: Boolean,
        val creatorId: String,
        val creatorName: String,
        val creatorLastName: String,
        val creatorColorHex: String
    )

    // Modelos de Response
    data class CreateHomeResponse(
        val inviteCode: String,
        val message: String
    )

    // Endpoint
    @POST("/api/homes/create")
    suspend fun createHome(@Body request: CreateHomeRequest): Response<CreateHomeResponse>
}