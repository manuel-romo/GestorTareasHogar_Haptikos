package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_table_new")

data class RoomEntityNew (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String,
    val colorHex: String
)