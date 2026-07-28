package es.javiercarrasco.appdummy.data.repository

import es.javiercarrasco.appdummy.data.datasource.local.LocalDataSource
import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.flow.Flow

// ─── data/repository/LibrosRepository.kt ─────────────────────────────────────────────────────────
class LibrosRepository(private val localDataSource: LocalDataSource) {

    // La UI siempre observa desde Room
    fun observarLibros(): Flow<List<Libro>> = localDataSource.observarTodos()
    fun observarFavoritos(): Flow<List<Libro>> = localDataSource.observarFavoritos()
    fun observarPorId(id: String): Flow<Libro?> = localDataSource.observarPorId(id)

    suspend fun toggleFavorito(id: String) = localDataSource.toggleFavorito(id)
    suspend fun toggleLeido(id: String) = localDataSource.toggleLeido(id)
    suspend fun insertarIgnorando(libros: List<Libro>) = localDataSource.insertarIgnorando(libros)
    suspend fun obtenerAutores(): List<String>? = localDataSource.obtenerAutores()
    suspend fun obtenerPorId(id: String): Libro? = localDataSource.obtenerPorId(id)

    private val libros = listOf(
        Libro(
            id = "1",
            titulo = "Proyecto Hail Mary",
            autor = "Andy Weir",
            year = 2021,
            isbn = "9788418037016",
            cover = "https://covers.openlibrary.org/b/isbn/9788418037016-L.jpg",
            esFavorito = true,
            leido = false
        ),
        Libro(
            id = "2",
            titulo = "Juego de tronos",
            autor = "George R.R. Martin",
            year = 1996,
            isbn = "9780307951182",
            cover = "https://covers.openlibrary.org/b/isbn/9780307951182-L.jpg",
            esFavorito = true,
            leido = true
        ),
        Libro("3", "Festín de cuervos", "George R.R. Martin", 2005, "9780307951212", "https://covers.openlibrary.org/b/isbn/9780307951212-L.jpg", false, false),
        Libro("4", "Cementerio de Animales", "Stephen King", 1983, "9788401499845", "https://covers.openlibrary.org/b/isbn/9788401499845-L.jpg", false, true),
        Libro("5", "El juego de Ender", "Orson Scott Card", 1985, "9788498720068", "https://covers.openlibrary.org/b/isbn/9788498720068-L.jpg", false, true)
    )

    // Se modifica para hacer una carga inicial.
    fun getLibros(): List<Libro> {
        return libros
    }
}