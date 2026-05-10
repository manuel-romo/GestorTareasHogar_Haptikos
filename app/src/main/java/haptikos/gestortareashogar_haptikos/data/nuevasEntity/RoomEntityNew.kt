package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "room_table_new",
    foreignKeys = [
        ForeignKey(
            entity = HomeEntityNew::class,
            parentColumns = ["id"],
            childColumns = ["homeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoomEntityNew (
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String,
    val colorHex: String,
    val homeId: String,

    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)