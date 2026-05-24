package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earned_points")
data class EarnedPointsEntity(
    @PrimaryKey val instanceId: String,
    val userId: String,
    val points: Int,
    val earnedAt: Long,
    val isSynced: Boolean = false
)