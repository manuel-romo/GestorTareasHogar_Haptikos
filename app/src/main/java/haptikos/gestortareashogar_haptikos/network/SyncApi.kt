package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SyncApi {

    data class MemberSyncDto(
        val id: String,
        val userId: String,
        val name: String,
        val lastName: String,
        val colorHex: String,
        val role: String,
        val status: String
    )

    data class HomeSyncDto(
        val id: String,
        val name: String,
        val description: String?,
        val isPrivate: Boolean,
        val inviteCode: String?,
        val editPermission: String?,
        val notifyTaskReminders: Boolean,
        val notifyTaskCompleted: Boolean,
        val notifyNewMembers: Boolean,
        val notifyAllMembers: Boolean,
        val forceSettings: Boolean,
        val members: List<MemberSyncDto>
    )

    @GET("/api/homes/user/{userId}")
    suspend fun getHomesByUser(@Path("userId") userId: String): Response<List<HomeSyncDto>>
}