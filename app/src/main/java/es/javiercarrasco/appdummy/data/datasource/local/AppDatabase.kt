package es.javiercarrasco.appdummy.data.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import es.javiercarrasco.appdummy.data.model.Libro

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Crear una tabla nueva
        db.execSQL("ALTER TABLE libros ADD COLUMN portada_local TEXT DEFAULT NULL")
    }
}

// ─── data/datasource/local/AppDatabase.kt ────────────────────────────────────────────────────────
@Database(
    entities = [Libro::class],
    version = 3,                 // ← era 2 en T4: se ha añadido la columna portada_local en T7.
    exportSchema = true
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
//                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}