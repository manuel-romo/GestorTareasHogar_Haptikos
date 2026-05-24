package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ChallengeProgressApi {

    data class ChallengeProgressDto(
        val id: String,
        val userId: String,
        val homeId: String,
        val challengeType: String,
        val weekId: String,
        val currentProgress: Int,
        val isCompleted: Boolean,
        val pointsAwarded: Boolean,
        val completedAt: Long?
    )

    @POST("/api/challenges/upsert")
    suspend fun upsert(@Body dto: ChallengeProgressDto): Response<Void>

    @GET("/api/challenges/week")
    suspend fun getForWeek(
        @Query("userId") userId: String,
        @Query("homeId") homeId: String,
        @Query("weekId") weekId: String
    ): Response<List<ChallengeProgressDto>>
}

