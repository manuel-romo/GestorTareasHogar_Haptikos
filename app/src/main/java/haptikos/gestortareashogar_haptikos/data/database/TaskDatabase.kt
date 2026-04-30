package haptikos.gestortareashogar_haptikos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import haptikos.gestortareashogar_haptikos.data.converter.Converters
import haptikos.gestortareashogar_haptikos.data.dao.MemberDao
import haptikos.gestortareashogar_haptikos.data.dao.RoomDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskDao
import haptikos.gestortareashogar_haptikos.data.dao.TaskInstanceDao
import haptikos.gestortareashogar_haptikos.data.entity.MemberEntity
import haptikos.gestortareashogar_haptikos.data.entity.RoomEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskEntity
import haptikos.gestortareashogar_haptikos.data.entity.TaskInstanceEntity
import haptikos.gestortareashogar_haptikos.data.enumerators.MemberRole
import haptikos.gestortareashogar_haptikos.data.enumerators.PriorityLevel
import haptikos.gestortareashogar_haptikos.data.enumerators.TaskState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [TaskEntity::class, TaskInstanceEntity::class, MemberEntity::class, RoomEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase: RoomDatabase(){
    abstract fun taskDao(): TaskDao
    abstract fun memberDao(): MemberDao
    abstract fun roomDao(): RoomDao
    abstract fun taskInstanceDao(): TaskInstanceDao

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

                // Inserción de datos de ejemplo
                INSTANCE?.let { database ->
                    scope.launch {
                        val taskDao = database.taskDao()
                        val instanceDao = database.taskInstanceDao()
                        val memberDao = database.memberDao()
                        val roomDao = database.roomDao()

                        // Definición de tiempos
                        val hoy = System.currentTimeMillis()
                        val ayer = hoy - (24 * 60 * 60 * 1000)
                        val mañana = hoy + (24 * 60 * 60 * 1000)

                        // Insertar Miembros
                        val m1 = MemberEntity(name = "María", lastName = "Gómez", colorHex = "#F014A8", role = MemberRole.CREATOR)
                        val m2 = MemberEntity(name = "Juan", lastName = "Pérez", colorHex = "#2979FF",  role = MemberRole.MEMBER)
                        val m3 = MemberEntity(name = "Ana", lastName = "López", colorHex = "#9C27B0",  role = MemberRole.ADMIN)
                        memberDao.add(m1)
                        memberDao.add(m2)
                        memberDao.add(m3)

                        // Insertar Habitaciones
                        val r1 = RoomEntity(name = "Cocina", colorHex = "#FF5252", icon = "🍳")
                        val r2 = RoomEntity(name = "Lavandería", colorHex = "#448AFF", icon = "🧺")
                        val r3 = RoomEntity(name = "Sala", colorHex = "#FF9800", icon = "營")
                        roomDao.add(r1)
                        roomDao.add(r2)
                        roomDao.add(r3)

                        // Crear Tareas
                        val plantillas = listOf(
                            TaskEntity(
                                title = "Limpiar la estufa",
                                room = r1,
                                points = 15,
                                members = listOf(m1, m2),
                                priority = PriorityLevel.ALTA
                            ),
                            TaskEntity(
                                title = "Doblar ropa limpia",
                                room = r2,
                                points = 10,
                                members = listOf(m3),
                                priority = PriorityLevel.MEDIA
                            ),
                            TaskEntity(
                                title = "Aspirar alfombra",
                                room = r3,
                                points = 20,
                                members = listOf(m1),
                                priority = PriorityLevel.BAJA
                            ),
                            TaskEntity(
                                title = "Comprar víveres",
                                room = null,
                                points = 30,
                                members = listOf(m2, m3),
                                priority = PriorityLevel.ALTA
                            )
                        )

                        plantillas.forEach { taskDao.add(it) }

                        // Crear Instancias de Tarea
                        val instancias = listOf(
                            // Tarea Pendiente para Hoy
                            TaskInstanceEntity(
                                task = plantillas[0],
                                dueDate = hoy,
                                assignedMembers = plantillas[0].members,
                                state = TaskState.PENDING,
                                pausedUntil = null
                            ),
                            // Tarea Pausada para Hoy
                            TaskInstanceEntity(
                                task = plantillas[1],
                                dueDate = hoy,
                                assignedMembers = plantillas[1].members,
                                state = TaskState.PAUSED,
                                // Pausada por 2 horas
                                pausedUntil = hoy + (2 * 60 * 60 * 1000)
                            ),
                            // Tarea Completada de Ayer
                            TaskInstanceEntity(
                                task = plantillas[2],
                                dueDate = ayer,
                                assignedMembers = plantillas[2].members,
                                state = TaskState.COMPLETED,
                                pausedUntil = null
                            ),
                            // Tarea Pendiente para Mañana
                            TaskInstanceEntity(
                                task = plantillas[3],
                                dueDate = mañana,
                                assignedMembers = plantillas[3].members,
                                state = TaskState.PENDING,
                                pausedUntil = null
                            )
                        )

                        instancias.forEach { instanceDao.add(it) }
                    }
                }
            }
        }
    }
}