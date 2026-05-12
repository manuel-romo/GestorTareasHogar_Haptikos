package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberStatus
import java.util.UUID

@Entity(
    tableName = "member_table_new",
    foreignKeys = [
        ForeignKey(
            entity = HomeEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["homeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MemberEntityNew(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val userId: String,

    val name: String,
    val lastName: String,
    val colorHex: String,
    val role: MemberRole,

    val homeId: String,
    val status: MemberStatus,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)