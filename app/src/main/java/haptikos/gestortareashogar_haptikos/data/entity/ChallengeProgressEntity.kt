package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.ChallengeType
import java.util.UUID

@Entity(tableName = "challenge_progress")
data class ChallengeProgressEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val homeId: String,
    val challengeType: ChallengeType,
    val weekId: String,
    val currentProgress: Int,
    val isCompleted: Boolean = false,
    val pointsAwarded: Boolean = false,
    val completedAt: Long? = null,
    val isSynced: Boolean = false
)