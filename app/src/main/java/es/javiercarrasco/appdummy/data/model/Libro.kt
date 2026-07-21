package es.javiercarrasco.appdummy.data.model

// ─── datal/model/Libro.kt ────────────────────────────────────────────────────────────────────────
// Modelo de datos — se reutilizará en B3 con ROOM y Retrofit2
data class Libro(
    val id: Int = 0,
    val titulo: String = "",
    val autor: String = "",
    val year: Int? = 1900,
    val isbn: String = "",
    val cover: String = "",
    val esFavorito: Boolean = false,
    val leido: Boolean = false
)
