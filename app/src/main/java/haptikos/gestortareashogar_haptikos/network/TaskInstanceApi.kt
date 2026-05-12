package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TaskInstanceApi {
    data class CreateInstanceRequest(
        val id: String,
        val taskId: String,
        val dueDate: Long,
        val state: String
    )

    @POST("/api/tasks/instances")
    suspend fun createInstance(@Body request: CreateInstanceRequest): Response<Void>
}