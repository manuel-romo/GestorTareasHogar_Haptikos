package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Entity
import androidx.room.PrimaryKey
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole

@Entity(tableName = "member_table_new")

data class MemberEntityNew (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lastName: String,
    val colorHex: String,
    val role: MemberRole,
)