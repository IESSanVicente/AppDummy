package es.javiercarrasco.appdummy.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── datal/model/Libro.kt ────────────────────────────────────────────────────────────────────────
// Esta misma clase se reutilizará en B3-T5 con Retrofit2

@Entity(
    tableName = "libros",
    indices = [
        Index(value = ["titulo"]),                        // búsqueda por título
        Index(value = ["autor", "isbn"], unique = false)  // búsqueda combinada
    ]
)
data class Libro(
    // @PrimaryKey identifica de forma única cada fila
    // autoGenerate = true: Room asigna el id automáticamente (incremento)
    // autoGenerate = false (por defecto): el id lo proporcionamos nosotros
    // En AppDummy usamos el id de https://openlibrary.org/, así que autoGenerate = false
    @PrimaryKey
    val id: String,

    // @ColumnInfo permite personalizar el nombre de la columna en SQLite
    // Si no se especifica, Room usa el nombre de la propiedad
    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "autor")
    val autor: String,

    @ColumnInfo(name = "year")
    val year: Int? = 1900,

    @ColumnInfo(name = "isbn")
    val isbn: String,

    @ColumnInfo(name = "cover")
    val cover: String? = null,

    // Campos exclusivamente locales: no existe en la API de https://openlibrary.org/
    // Al deserializar con Gson/Retrofit, estos campos quedan a false (valor por defecto)
    @ColumnInfo(name = "es_favorito")
    val esFavorito: Boolean = false,

    @ColumnInfo(name = "leido")
    val leido: Boolean = false
)
