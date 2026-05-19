package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


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
        val memberIds: List<String>,
        val predetermined: Boolean,
        val userId: String = ""
    )

    data class TaskResponse(
        val id: String,
        val title: String
    )

    data class TaskNetworkDto(
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
        val predetermined: Boolean,
        val memberIds: List<String>,
        val pausedUntil: Long? = null,
        val instances: List<TaskInstanceApi.TaskInstanceNetworkDto> = emptyList()
    )

    data class UpdateTaskRequest(
        val title: String,
        val description: String?,
        val points: Int,
        val priority: String,
        val suggestedDay: String,
        val recurrence: String,
        val workMode: String,
        val roomId: String?,
        val memberIds: List<String>,
        val pausedUntil: Long?
    )

    @POST("/api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<TaskResponse>

    @PUT("/api/tasks/{taskId}")
    suspend fun updateTask(@Path("taskId") taskId: String, @Body request: UpdateTaskRequest): Response<Void>

    @PATCH("/api/tasks/instances/{instanceId}/complete")
    suspend fun completeInstance(
        @Path("instanceId") instanceId: String,
        @Query("userId") userId: String
    ): Response<Void>

    @GET("/api/tasks/home/{homeId}")
    suspend fun getTasksByHome(@Path("homeId") homeId: String): Response<List<TaskNetworkDto>>

    @DELETE("/api/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: String,
        @Query("userId") userId: String
    ): Response<Void>

}