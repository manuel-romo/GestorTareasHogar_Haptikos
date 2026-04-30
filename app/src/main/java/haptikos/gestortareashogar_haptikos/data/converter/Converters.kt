package haptikos.gestortareashogar_haptikos.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.ui.enums.RecurrenceType
import haptikos.gestortareashogar_haptikos.ui.enums.SuggestedDay
import haptikos.gestortareashogar_haptikos.ui.enums.WorkMode

class Converters {

    // Traductores Enum Estado - String
    @TypeConverter
    fun fromTaskState(state: TaskState): String {
        return state.name
    }
    @TypeConverter
    fun toTaskState(name: String): TaskState {
        return TaskState.valueOf(name)
    }

    // Traductores Lista de Miembros - String
    @TypeConverter
    fun fromMemberList(members: List<MemberEntity>): String {
        return Gson().toJson(members)
    }
    @TypeConverter
    fun toMemberList(membersString: String): List<MemberEntity> {
        val type = object: TypeToken<List<MemberEntity>>() {}.type
        return Gson().fromJson(membersString, type)
    }

    // Traductores de Room - String
    @TypeConverter
    fun fromRoom(room: RoomEntity?): String? {
        return if (room == null) null else Gson().toJson(room)
    }
    @TypeConverter
    fun toRoom(roomString: String?): RoomEntity? {
        return if (roomString == null) null else Gson().fromJson(roomString, RoomEntity::class.java)
    }

    // Traductores de TaskEntity (Plantilla) - String
    @TypeConverter
    fun fromTaskEntity(task: TaskEntity): String {
        return Gson().toJson(task)
    }

    @TypeConverter
    fun toTaskEntity(taskString: String): TaskEntity {
        return Gson().fromJson(taskString, TaskEntity::class.java)
    }

    // Traductores de Enumeradores
    @TypeConverter
    fun fromPriorityLevel(priority: PriorityLevel): String = priority.name
    @TypeConverter
    fun toPriorityLevel(name: String): PriorityLevel = PriorityLevel.valueOf(name)

    @TypeConverter
    fun fromSuggestedDay(day: SuggestedDay): String = day.name
    @TypeConverter
    fun toSuggestedDay(name: String): SuggestedDay = SuggestedDay.valueOf(name)

    @TypeConverter
    fun fromRecurrenceType(recurrence: RecurrenceType): String = recurrence.name
    @TypeConverter
    fun toRecurrenceType(name: String): RecurrenceType = RecurrenceType.valueOf(name)

    @TypeConverter
    fun fromWorkMode(mode: WorkMode): String = mode.name
    @TypeConverter
    fun toWorkMode(name: String): WorkMode = WorkMode.valueOf(name)
}