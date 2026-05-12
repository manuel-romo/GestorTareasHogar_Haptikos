package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path


interface TaskApi {

    data class CreateTaskRequest(
        val id: String,
        val title: String,
        val description: String?,
        val points: Int,
        val priority: String,
        val suggestedDay: String,
        val recurrence: String,
        val workMode: String,
        val lastMemberIndex: Int,
        val roomId: String?,
        val homeId: String,
        val memberIds: List<String>
    )

    data class TaskResponse(
        val id: String,
        val title: String
    )
    @POST("/api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<TaskResponse>

    @PATCH("/api/tasks/instances/{instanceId}/complete")
    suspend fun completeInstance(@Path("instanceId") instanceId: String): Response<Void>

}