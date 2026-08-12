package es.javiercarrasco.appdummy.data.repository

import es.javiercarrasco.appdummy.data.datasource.local.LocalDataSource
import es.javiercarrasco.appdummy.data.datasource.remote.RemoteDataSource
import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

// ─── data/repository/LibrosRepository.kt ─────────────────────────────────────────────────────────
class LibrosRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    // ─── Lectura local (T4): la UI SIEMPRE observa desde Room ───────────────────
    fun observarLibros(): Flow<List<Libro>> = localDataSource.observarTodos()
    fun observarFavoritos(): Flow<List<Libro>> = localDataSource.observarFavoritos()
    fun observarPorId(id: String): Flow<Libro?> = localDataSource.observarPorId(id)

    suspend fun toggleFavorito(id: String) = localDataSource.toggleFavorito(id)
    suspend fun toggleLeido(id: String) = localDataSource.toggleLeido(id)
    suspend fun obtenerAutores(): List<String>? = localDataSource.obtenerAutores()
    suspend fun obtenerPorId(id: String): Libro? = localDataSource.obtenerPorId(id)
    suspend fun insertarIgnorando(libros: List<Libro>) = localDataSource.insertarIgnorando(libros)

    // ─── Escritura local (T5): alta de un libro desde el formulario ─────────────
    suspend fun guardarLibro(libro: Libro) = localDataSource.guardar(libro)

    // ─── Lectura remota (T5) ────────────────────────────────────────────────────
    suspend fun buscarEnApiPorTitulo(titulo: String): Result<List<Libro>> =
        ejecutarPeticion { remoteDataSource.buscarPorTitulo(titulo) }

    suspend fun buscarEnApiPorIsbn(isbn: String): Result<Libro?> =
        ejecutarPeticion { remoteDataSource.buscarPorIsbn(isbn) }

    // Función genérica de orden superior: centraliza el try/catch de TODAS las
    // llamadas de red, evitando repetirlo en cada método del repositorio
    private suspend fun <T> ejecutarPeticion(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: IOException) {
            // Sin red: los datos locales de Room siguen disponibles
            Result.failure(Exception("Sin conexión a internet. Comprueba la red."))
        } catch (e: HttpException) {
            Result.failure(Exception(mensajeDeError(e.code())))
        }

    private fun mensajeDeError(codigo: Int): String = when (codigo) {
        403 -> "Acceso denegado: se ha superado el límite de peticiones."
        404 -> "Recurso no encontrado en Open Library."
        429 -> "Demasiadas peticiones. Inténtalo dentro de unos segundos."
        in 500..599 -> "Error del servidor de Open Library ($codigo)."
        else -> "Error HTTP $codigo"
    }
}