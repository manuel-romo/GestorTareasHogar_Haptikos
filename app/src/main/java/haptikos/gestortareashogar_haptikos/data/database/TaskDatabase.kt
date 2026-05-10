package haptikos.gestortareashogar_haptikos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberStatus
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskMemberJoin
import haptikos.gestortareashogar_haptikos.data.nuevasEntity.TaskInstanceMemberJoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        TaskEntityNew::class,
        TaskInstanceEntityNew::class,
        MemberEntityNew::class,
        RoomEntityNew::class,
        TaskMemberJoin::class,
        TaskInstanceMemberJoin::class,
        HomeEntityNew::class
    ],
    version = 2,
    exportSchema = false
)

abstract class TaskDatabase: RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun memberDao(): MemberDao
    abstract fun roomDao(): RoomDao
    abstract fun taskInstanceDao(): TaskInstanceDao
    abstract fun homeDao(): HomeDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(TaskDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class TaskDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                INSTANCE?.let { database ->
                    scope.launch {
                        val taskDao = database.taskDao()
                        val instanceDao = database.taskInstanceDao()
                        val memberDao = database.memberDao()
                        val roomDao = database.roomDao()
                        val homeDao = database.homeDao()

                        val hoy = System.currentTimeMillis()
                        val ayer = hoy - (24 * 60 * 60 * 1000)
                        val mañana = hoy + (24 * 60 * 60 * 1000)
                        val pasadoMañana = hoy + (2 * 24 * 60 * 60 * 1000)

                        // Creación de hogar
                        val inviteCode = UUID.randomUUID().toString().take(6).uppercase()
                        val newHome = HomeEntityNew(
                            name = "Mi Casa Principal",
                            inviteCode = inviteCode
                        )
                        homeDao.insertHome(newHome)
                        val idHome = newHome.id

                        // Cuarto
                        val newRoom = RoomEntityNew(homeId = idHome, name = "Cocina Nueva", colorHex = "#FF5252", icon = "🍳")
                        roomDao.addNew(newRoom)
                        val newRoomId = newRoom.id

                        // Miembros
                        val maria = MemberEntityNew(homeId = idHome, name = "María", lastName = "Gómez", colorHex = "#F014A8", role = MemberRole.CREATOR, status = MemberStatus.ACCEPTED)
                        val juan = MemberEntityNew(homeId = idHome, name = "Juan", lastName = "Pérez", colorHex = "#2979FF", role = MemberRole.MEMBER, status = MemberStatus.ACCEPTED)

                        memberDao.addNew(maria)
                        memberDao.addNew(juan)

                        val idMaria = maria.id
                        val idJuan = juan.id

                        // Tarea Pendiente. Limpiar estufa
                        val task1 = TaskEntityNew(title = "Limpiar la estufa", roomId = newRoomId, points = 15, priority = PriorityLevel.ALTA)
                        taskDao.addTaskNew(task1)

                        val inst1 = TaskInstanceEntityNew(taskId = task1.id, dueDate = hoy, state = TaskState.PENDING)
                        instanceDao.insertInstanceWithAssignedMembers(inst1, listOf(idMaria))

                        // Tarea Completada. Sacar la basura
                        val task2 = TaskEntityNew(title = "Sacar la basura", roomId = newRoomId, points = 5, priority = PriorityLevel.BAJA)
                        taskDao.addTaskNew(task2)

                        val inst2 = TaskInstanceEntityNew(taskId = task2.id, dueDate = ayer, state = TaskState.COMPLETED)
                        instanceDao.insertInstanceWithAssignedMembers(inst2, listOf(idJuan))

                        // Tarea Pausada. Organizar despensa
                        val task3 = TaskEntityNew(title = "Organizar despensa", roomId = newRoomId, points = 20, priority = PriorityLevel.MEDIA)
                        taskDao.addTaskNew(task3)

                        val inst3 = TaskInstanceEntityNew(
                            taskId = task3.id,
                            dueDate = mañana,
                            state = TaskState.PAUSED,
                            pausedUntil = pasadoMañana
                        )
                        instanceDao.insertInstanceWithAssignedMembers(inst3, listOf(idMaria, idJuan))
                    }
                }
            }
        }
    }
}