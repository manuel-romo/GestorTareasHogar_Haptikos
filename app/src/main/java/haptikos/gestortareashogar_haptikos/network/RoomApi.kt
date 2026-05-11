package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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

    @POST("/api/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<CreateRoomResponse>

}