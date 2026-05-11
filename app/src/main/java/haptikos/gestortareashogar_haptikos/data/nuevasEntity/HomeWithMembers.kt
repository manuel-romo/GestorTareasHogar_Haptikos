package haptikos.gestortareashogar_haptikos.data.nuevasEntity

import androidx.room.Embedded
import androidx.room.Relation

data class HomeWithMembers(
    @Embedded val home: HomeEntityNew,

    @Relation(
        parentColumn = "id",
        entityColumn = "homeId"
    )
    val members: List<MemberEntityNew>
)