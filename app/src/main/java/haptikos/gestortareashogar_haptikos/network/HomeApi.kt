package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface HomeApi {

    // Dto de envío
    data class CreateHomeRequest(
        val id: String,
        val name: String,
        val description: String?,
        val isPrivate: Boolean,
        val creatorId: String,
        val creatorName: String,
        val creatorLastName: String,
        val creatorColorHex: String,
        val invitedUsers: List<InvitedUserDto>
    )

    // Dto de usuario invitado
    data class InvitedUserDto(
        val id: String,
        val title: String,
        val subtitle: String
    )

    // Dto de respuesta
    data class CreateHomeResponse(
        val message: String,
        val inviteCode: String,
        val homeId: String
    )

    // Endpoint
    @POST("/api/homes")
    suspend fun createHome(@Body request: CreateHomeRequest): Response<CreateHomeResponse>
}