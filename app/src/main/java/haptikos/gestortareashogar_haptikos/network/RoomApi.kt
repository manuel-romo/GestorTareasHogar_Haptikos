package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomApi {

    data class CreateRoomRequest(
        val id: String,
        val name: String,
        val icon: String,
        val colorHex: String,
        val homeId: String
    )

    data class CreateRoomResponse(
        val message: String,
        val roomId: String
    )

    data class RoomNetworkDto(
        val id: String,
        val name: String,
        val icon: String,
        val colorHex: String,
        val homeId: String
    )

    data class UpdateRoomRequest(
        val name: String,
        val icon: String,
        val colorHex: String
    )

    @POST("/api/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<CreateRoomResponse>

    @PATCH("/api/rooms/{roomId}")
    suspend fun updateRoom(@Path("roomId") roomId: String, @Body request: UpdateRoomRequest): Response<Void>

    @GET("/api/rooms/home/{homeId}")
    suspend fun getRoomsByHome(@Path("homeId") homeId: String): Response<List<RoomNetworkDto>>
}