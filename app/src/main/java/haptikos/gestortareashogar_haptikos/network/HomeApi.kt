package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApi {

    // Creación
    data class CreateHomeRequest(
        val id: String,
        val name: String,
        val description: String?,
        val isPrivate: Boolean,
        val creatorMemberId: String,
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
        val editPermission: String? = null,
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

    data class RegenerateCodeResponse(val inviteCode: String)

    @POST("/api/homes/{homeId}/regenerate-code")
    suspend fun regenerateInviteCode(@Path("homeId") homeId: String): Response<RegenerateCodeResponse>


    data class HomePreviewResponse(
        val id: String,
        val name: String,
        val creatorName: String,
        val memberCount: Int,
        val taskCount: Long,
        val pendingCount: Long,
        val isAlreadyMember: Boolean
    )

    data class JoinHomeRequest(
        val inviteCode: String,
        val userId: String,
        val name: String,
        val lastName: String,
        val colorHex: String
    )

    data class JoinHomeResponse(
        val message: String,
        val memberId: String
    )


    @GET("/api/homes/by-code/{inviteCode}")
    suspend fun findHomeByCode(
        @Path("inviteCode") inviteCode: String,
        @Query("userId") userId: String
    ): Response<HomePreviewResponse>

    @POST("/api/homes/join")
    suspend fun joinHome(@Body request: JoinHomeRequest): Response<JoinHomeResponse>

    @DELETE("/api/homes/{homeId}/members/{userId}")
    suspend fun leaveHome(
        @Path("homeId") homeId: String,
        @Path("userId") userId: String
    ): Response<Void>

    @POST("api/homes/{homeId}/invite-email")
    suspend fun inviteByEmail(
        @Path("homeId") homeId: String,
        @Body request: InviteEmailRequest
    ): Response<Map<String, String>>

    data class InviteEmailRequest(
        val email: String,
        val homeName: String,
        val inviteCode: String
    )

}