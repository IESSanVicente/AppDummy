package es.javiercarrasco.appdummy.data.datasource.local

import android.content.Context
import androidx.room.*
import es.javiercarrasco.appdummy.data.model.Libro

// ─── data/datasource/local/AppDatabase.kt ────────────────────────────────────────────────────────
@Database(
    entities = [Libro::class],   // lista de todas las @Entity de la app
    version = 1,                 // versión del esquema — incrementar al hacer cambios
    exportSchema = true          // exportar esquema a /schemas para migraciones
)
@TypeConverters(Converters::class)   // registrar los converters
abstract class AppDatabase : RoomDatabase() {
    // Room genera la implementación concreta en tiempo de compilación
    abstract fun libroDao(): LibrosDao

    // Creación del singleton de la base de datos (dentro de de AppDatabase)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appdummy_database"
                )
                    // Para proyectos de aprendizaje: si cambia el esquema, borra y recrea la BD
                    // En producción real se escribirían objetos Migration en lugar de esto
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}