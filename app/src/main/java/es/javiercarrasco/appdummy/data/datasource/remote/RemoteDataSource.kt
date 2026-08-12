package es.javiercarrasco.appdummy.data.datasource.remote

import es.javiercarrasco.appdummy.data.datasource.remote.dto.toLibro
import es.javiercarrasco.appdummy.data.datasource.remote.dto.toLibros
import es.javiercarrasco.appdummy.data.model.Libro

// ─── data/datasource/remote/RemoteDataSource.kt ──────────────────────────────────────────────────
class RemoteDataSource(private val apiService: OpenLibraryApiService) {

    // Las funciones son suspend: se deben llamar desde una corrutina.
    // Si la petición falla, Retrofit lanza IOException (sin red) o HttpException (error HTTP).
    // El manejo de esas excepciones se realiza en el Repository (sección 8).

    suspend fun buscarPorTitulo(titulo: String, limite: Int = 10): List<Libro> =
        apiService.buscarPorTitulo(titulo = titulo, limite = limite)
            .docs                 // List<LibroDto>? — puede ser null si no hay resultados
            .orEmpty()
            .toLibros()           // ← frontera: aquí se deja de hablar en DTOs

    suspend fun buscarPorIsbn(isbn: String): Libro? =
        apiService.buscar(consulta = "isbn:$isbn", limite = 1)
            .docs
            ?.firstOrNull()
            // Se conserva el ISBN que escribió el usuario: la obra puede devolver
            // primero el de otra edición distinta a la buscada
            ?.toLibro(isbnBuscado = isbn)
}