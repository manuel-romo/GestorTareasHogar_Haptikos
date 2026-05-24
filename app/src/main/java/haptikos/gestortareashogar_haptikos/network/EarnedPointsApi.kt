package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EarnedPointsApi {

    data class EarnedPointsDto(
        val instanceId: String,
        val userId: String,
        val points: Int,
        val earnedAt: Long
    )

    @POST("api/earned-points")
    suspend fun save(@Body dto: EarnedPointsDto): Response<String>

    @GET("api/earned-points/user/{userId}")
    suspend fun getByUser(@Path("userId") userId: String): Response<List<EarnedPointsDto>>
}

