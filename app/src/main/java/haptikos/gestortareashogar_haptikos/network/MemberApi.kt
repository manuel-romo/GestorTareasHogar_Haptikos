package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MemberApi {

    data class CreateMemberRequest(
        val id: String,
        val userId: String?,
        val homeId: String,
        val name: String,
        val lastName: String,
        val colorHex: String,
        val role: String,
        val status: String
    )

    data class MemberDto(
        val id: String,
        val userId: String?,
        val homeId: String,
        val name: String,
        val lastName: String?,
        val colorHex: String?,
        val role: String?,
        val status: String?
    )

    @POST("api/members")
    suspend fun createMember(@Body request: CreateMemberRequest): Response<Unit>

    @GET("api/members/home/{homeId}")
    suspend fun getMembersByHome(@Path("homeId") homeId: String): Response<List<MemberDto>>
}