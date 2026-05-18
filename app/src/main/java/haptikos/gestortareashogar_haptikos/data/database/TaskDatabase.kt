package haptikos.gestortareashogar_haptikos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import haptikos.gestortareashogar_haptikos.data.dao.HomeDao
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.NotificationDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.entity.HomeEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntityNew
import haptikos.gestortareashogar_haptikos.data.entity.NotificationEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskMemberJoin
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceMemberJoin
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        TaskEntityNew::class,
        TaskInstanceEntityNew::class,
        MemberEntityNew::class,
        RoomEntityNew::class,
        TaskMemberJoin::class,
        TaskInstanceMemberJoin::class,
        HomeEntityNew::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)

abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun memberDao(): MemberDao
    abstract fun roomDao(): RoomDao
    abstract fun taskInstanceDao(): TaskInstanceDao
    abstract fun homeDao(): HomeDao
    abstract fun notificationDao(): NotificationDao

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

                /*
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

                        // Hogar de prueba
                        val newHome = HomeEntityNew(
                            name = "Mi Casa Principal",
                            inviteCode = UUID.randomUUID().toString().take(6).uppercase()
                        )
                        homeDao.insertHome(newHome)

                        // Habitación de prueba
                        val newRoom = RoomEntityNew(
                            homeId = newHome.id,
                            name = "Cocina",
                            colorHex = "#FF5252",
                            icon = "🍳"
                        )
                        roomDao.addNew(newRoom)

                        // Miembros de prueba
                        val maria = MemberEntityNew(
                            homeId = newHome.id,
                            name = "María",
                            lastName = "Gómez",
                            colorHex = "#F014A8",
                            role = MemberRole.CREATOR,
                            status = MemberStatus.ACCEPTED,
                            userId = "prueba_maria"
                        )
                        val juan = MemberEntityNew(
                            homeId = newHome.id,
                            name = "Juan",
                            lastName = "Pérez",
                            colorHex = "#2979FF",
                            role = MemberRole.MEMBER,
                            status = MemberStatus.ACCEPTED,
                            userId = "prueba_juan"
                        )
                        memberDao.addNew(maria)
                        memberDao.addNew(juan)

                        // Tarea pendiente
                        val task1 = TaskEntityNew(
                            title = "Limpiar la estufa",
                            roomId = newRoom.id,
                            homeId = newHome.id,
                            points = 15,
                            priority = PriorityLevel.ALTA
                        )
                        taskDao.addTaskNew(task1)
                        val inst1 = TaskInstanceEntityNew(
                            taskId = task1.id,
                            dueDate = hoy,
                            state = TaskState.PENDING
                        )
                        instanceDao.insertInstanceWithAssignedMembers(inst1, listOf(maria.id))

                        // Tarea completada
                        val task2 = TaskEntityNew(
                            title = "Sacar la basura",
                            roomId = newRoom.id,
                            points = 5,
                            priority = PriorityLevel.BAJA,
                            homeId = newHome.id,
                        )
                        taskDao.addTaskNew(task2)
                        val inst2 = TaskInstanceEntityNew(
                            taskId = task2.id,
                            dueDate = ayer,
                            state = TaskState.COMPLETED
                        )
                        instanceDao.insertInstanceWithAssignedMembers(inst2, listOf(juan.id))

                        // Tarea pausada
                        val task3 = TaskEntityNew(
                            title = "Organizar despensa",
                            roomId = newRoom.id,
                            points = 20,
                            priority = PriorityLevel.MEDIA,
                            homeId = newHome.id,
                            pausedUntil = pasadoMañana
                        )
                        taskDao.addTaskNew(task3)

                    }
                }
                */
            }
        }
    }
}