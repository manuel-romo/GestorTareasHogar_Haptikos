package haptikos.gestortareashogar_haptikos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole

@Entity(tableName = "member_table")

data class MemberEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lastName: String,
    val colorHex: String,
    val role: MemberRole,
)