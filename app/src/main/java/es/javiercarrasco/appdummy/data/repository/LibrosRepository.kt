package es.javiercarrasco.appdummy.data.repository

import es.javiercarrasco.appdummy.data.model.Libro
import kotlin.time.Duration.Companion.milliseconds

// ─── data/repository/LibrosRepository.kt ─────────────────────────────────────────────────────────
// En B3 este repositorio coordinará ROOM y Retrofit2.
// Por ahora usa datos estáticos para centrar el aprendizaje en la arquitectura.
class LibrosRepository {

    private val libros = listOf(
        Libro(
            id = 1,
            titulo = "Proyecto Hail Mary",
            autor = "Andy Weir",
            year = 2021,
            isbn = "9788418037016",
            cover = "https://covers.openlibrary.org/b/isbn/9788418037016-L.jpg",
            esFavorito = true,
            leido = false
        ),
        Libro(
            id = 2,
            titulo = "Juego de tronos",
            autor = "George R.R. Martin",
            year = 1996,
            isbn = "9780307951182",
            cover = "https://covers.openlibrary.org/b/isbn/9780307951182-L.jpg",
            esFavorito = true,
            leido = true
        ),
        Libro(3, "Festín de cuervos", "George R.R. Martin", 2005, "9780307951212", "https://covers.openlibrary.org/b/isbn/9780307951212-L.jpg", false, false),
        Libro(4, "Cementerio de Animales", "Stephen King", 1983, "9788401499845", "https://covers.openlibrary.org/b/isbn/9788401499845-L.jpg", false, true),
        Libro(5, "El juego de Ender", "Orson Scott Card", 1985, "9788498720068", "https://covers.openlibrary.org/b/isbn/9788498720068-L.jpg", false, true)
    )

    // suspend: puede suspenderse sin bloquear el hilo principal
    suspend fun getLibros(): List<Libro> {
        kotlinx.coroutines.delay(800.milliseconds)   // simula latencia de red
        return libros
    }

    suspend fun getAutores(): List<String> {
        kotlinx.coroutines.delay(300.milliseconds)
        return libros.map { it.autor }.distinct().sorted()
    }

    suspend fun getLibroPorId(id: Int): Libro? {
        kotlinx.coroutines.delay(300.milliseconds)
        return libros.find { it.id == id }
    }
}