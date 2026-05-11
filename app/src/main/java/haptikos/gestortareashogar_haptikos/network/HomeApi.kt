package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface HomeApi {

    // Creación
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

    data class InvitedUserDto(
        val id: String,
        val title: String,
        val subtitle: String
    )

    data class CreateHomeResponse(
        val message: String,
        val inviteCode: String,
        val homeId: String
    )

    @POST("/api/homes")
    suspend fun createHome(@Body request: CreateHomeRequest): Response<CreateHomeResponse>

    // Actualización
    data class UpdateHomeRequest(
        val name: String? = null,
        val description: String? = null,
        val isPrivate: Boolean? = null,
        val notifyTaskReminders: Boolean? = null,
        val notifyTaskCompleted: Boolean? = null,
        val notifyNewMembers: Boolean? = null,
        val notifyAllMembers: Boolean? = null,
        val forceSettings: Boolean? = null
    )

    data class UpdateHomeResponse(
        val message: String,
        val inviteCode: String?,
        val homeId: String
    )

    @PATCH("/api/homes/{homeId}")
    suspend fun updateHome(
        @Path("homeId") homeId: String,
        @Body request: UpdateHomeRequest
    ): Response<UpdateHomeResponse>

    @DELETE("/api/homes/{homeId}")
    suspend fun deleteHome(@Path("homeId") homeId: String): Response<Void>

}