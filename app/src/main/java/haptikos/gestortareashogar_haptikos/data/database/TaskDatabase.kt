package haptikos.gestortareashogar_haptikos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import haptikos.gestortareashogar_haptikos.data.dao.ChallengeProgressDao
import haptikos.gestortareashogar_haptikos.data.dao.EarnedPointsDao
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
import haptikos.gestortareashogar_haptikos.data.entity.ChallengeProgressEntity
import haptikos.gestortareashogar_haptikos.data.entity.EarnedPointsEntity
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
        NotificationEntity::class,
        ChallengeProgressEntity::class,
        EarnedPointsEntity::class
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
    abstract fun challengeProgressDao(): ChallengeProgressDao
    abstract fun earnedPointsDao(): EarnedPointsDao

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

            }
        }
    }
}