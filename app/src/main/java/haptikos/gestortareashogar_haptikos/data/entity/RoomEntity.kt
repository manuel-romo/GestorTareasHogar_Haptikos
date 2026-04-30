package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_table")

data class RoomEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String,
    val colorHex: String
)