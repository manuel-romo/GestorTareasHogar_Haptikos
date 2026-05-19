package haptikos.gestortareashogar_haptikos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskInstanceApi {
    data class CreateTaskInstanceRequest(
        val id: String,
        val taskId: String,
        val dueDate: Long,
        val state: String,
        val memberIds: List<String>,
        val userId: String = "",
        val completedAt: Long? = null
    )

    data class TaskInstanceNetworkDto(
        val id: String,
        val taskId: String,
        val dueDate: Long,
        val state: String,
        val memberIds: List<String>,
        val completedAt: Long? = null
    )

    @POST("/api/tasks/instances")
    suspend fun createTaskInstance(@Body request: CreateTaskInstanceRequest): Response<Void>

    @PATCH("/api/tasks/instances/{instanceId}/complete")
    suspend fun completeInstance(
        @Path("instanceId") instanceId: String,
        @Query("userId") userId: String,
        @Query("completedAt") completedAt: Long
    ): Response<Void>


}