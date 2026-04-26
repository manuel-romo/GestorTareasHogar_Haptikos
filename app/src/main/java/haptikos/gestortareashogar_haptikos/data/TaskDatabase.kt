package haptikos.gestortareashogar_haptikos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class TaskDatabase: RoomDatabase(){
    abstract fun taskDao(): TaskDao

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
                    //.addCallback(TaskDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
        private class PokemonDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                scope.launch {
                    INSTANCE?.let { database ->
                        val dao = database.pokemonDao()
                        val initialPokemons = listOf(
                            PokemonEntity(name = "Pikachu", number = "025", type = "Eléctrico"),
                            PokemonEntity(name = "Bulbasaur", number = "001", type = "Planta"),
                            PokemonEntity(name = "Charmander", number = "004", type = "Fuego"),
                            PokemonEntity(name = "Squirtle", number = "007", type = "Agua"),
                            PokemonEntity(name = "Caterpie", number = "010", type = "Bicho"),
                            PokemonEntity(name = "Weedle", number = "013", type = "Bicho"),
                            PokemonEntity(name = "Pidgey", number = "016", type = "Normal"),
                            PokemonEntity(name = "Rattata", number = "019", type = "Normal"),
                            PokemonEntity(name = "Spearow", number = "021", type = "Normal"),
                            PokemonEntity(name = "Ekans", number = "023", type = "Veneno"),
                            PokemonEntity(name = "Arbok", number = "024", type = "Veneno"),
                            PokemonEntity(name = "Clefairy", number = "035", type = "Hada"),
                            PokemonEntity(name = "Clefable", number = "037", type = "Hada"),
                            PokemonEntity(name = "Vulpix", number = "036", type = "Fuego"),
                            PokemonEntity(name = "Ninetales", number = "004", type = "Fuego"),
                            PokemonEntity(name = "Jigglypuff", number = "039", type = "Normal"),
                            PokemonEntity(name = "Wigglytuff", number = "040", type = "Normal"),
                            PokemonEntity(name = "Zubat", number = "041", type = "Veneno"),
                            PokemonEntity(name = "Golbat", number = "042", type = "Veneno"),
                            PokemonEntity(name = "Oddish", number = "043", type = "Planta"),
                            PokemonEntity(name = "Gloom", number = "044", type = "Planta"),
                            PokemonEntity(name = "Vileplume", number = "045", type = "Planta"),
                            PokemonEntity(name = "Paras", number = "046", type = "Bicho")
                        )
                        initialPokemons.forEach { dao.add(it) }
                    }
                }
            }
        }
        */
    }
}