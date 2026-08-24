package es.javiercarrasco.appdummy.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ─── data/model/Libro.kt ─────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "libros",
    indices = [
        Index(value = ["titulo"]),
        Index(value = ["autor", "isbn"], unique = false)
    ]
)
data class Libro(
    // Se corresponde con "key" en el JSON: "/works/OL20933765W"
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "titulo")          // "title"
    val titulo: String,

    @ColumnInfo(name = "autor")           // "author_name" (array → se aplana)
    val autor: String,

    @ColumnInfo(name = "year")            // "first_publish_year"
    val year: Int? = 1900,

    @ColumnInfo(name = "isbn")            // "isbn" (array → se elige el ISBN-13)
    val isbn: String,

    @ColumnInfo(name = "cover")           // "cover_i" (entero → se construye la URL)
    val cover: String? = null,

    // ─── Campos exclusivamente locales ──────────────────────────────────────
    @ColumnInfo(name = "es_favorito")
    val esFavorito: Boolean = false,

    @ColumnInfo(name = "leido")
    val leido: Boolean = false,

    @ColumnInfo(name = "actualizado_en")    // T6
    val actualizadoEn: Long = 0L,

    // ─── NUEVO en T7 ─────────────────────────────────────────────────────
    // Ruta absoluta del fichero de portada dentro de filesDir.
    // null significa "este libro no tiene portada propia": se usará la de
    // Open Library o, en su defecto, nocover.jpg.
    @ColumnInfo(name = "portada_local") val portadaLocal: String? = null
)