package es.javiercarrasco.appdummy.data.repository

import es.javiercarrasco.appdummy.data.datasource.local.LocalDataSource
import es.javiercarrasco.appdummy.data.datasource.remote.RemoteDataSource
import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

// ─── data/repository/LibrosRepository.kt ─────────────────────────────────────────────────────────
class LibrosRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {
    companion object {
        // Un libro se considera "caducado" pasada una semana desde su comprobación
        private val PERIODO_VALIDEZ = 7.days

        // Tope de peticiones por sincronización: con la pausa de 1,1 s, 20 libros
        // son unos 22 segundos. El resto se refrescará en la siguiente pasada.
        private const val MAXIMO_POR_SINCRONIZACION = 20

        // Open Library admite 1 petición/segundo a las apps anónimas y 3 a las
        // identificadas con User-Agent. Se deja margen sobre el límite estricto.
        private val PAUSA_ENTRE_PETICIONES = 1_100.milliseconds
    }


    // ─── Lectura local: la UI SIEMPRE observa desde Room ────────────────────

    fun observarLibros(): Flow<List<Libro>> = localDataSource.observarTodos()
    fun observarFavoritos(): Flow<List<Libro>> = localDataSource.observarFavoritos()
    fun observarPorId(id: String): Flow<Libro?> = localDataSource.observarPorId(id)

    // NUEVO en T6: el filtrado por título deja de hacerse en la UI y pasa a SQL
    fun observarPorTitulo(texto: String): Flow<List<Libro>> =
        localDataSource.observarPorTitulo(texto)

    fun observarUltimaSincronizacion(): Flow<Long?> =
        localDataSource.observarUltimaSincronizacion()

    // ─── Escritura local ────────────────────────────────────────────────────

    suspend fun toggleFavorito(id: String) = localDataSource.toggleFavorito(id)
    suspend fun toggleLeido(id: String) = localDataSource.toggleLeido(id)
    suspend fun obtenerAutores(): List<String>? = localDataSource.obtenerAutores()
    suspend fun obtenerPorId(id: String): Libro? = localDataSource.obtenerPorId(id)
    suspend fun guardarLibro(libro: Libro) = localDataSource.guardar(libro)

    // ─── Lectura remota (T5) ────────────────────────────────────────────────────

    suspend fun buscarEnApiPorTitulo(titulo: String): Result<List<Libro>> =
        ejecutarPeticion { remoteDataSource.buscarPorTitulo(titulo) }

    suspend fun buscarEnApiPorIsbn(isbn: String): Result<Libro?> =
        ejecutarPeticion { remoteDataSource.buscarPorIsbn(isbn) }

    // ─── Sincronización offline-first (T6) ──────────────────────────────────

    // Refresca los metadatos de los libros guardados. No devuelve libros:
    // escribe en Room y es el Flow de Room quien avisa a la interfaz.
    //
    // forzar = false → solo los libros caducados (uso normal, al abrir la app)
    // forzar = true  → todos los libros con ISBN (pull-to-refresh del usuario)
    suspend fun sincronizarBiblioteca(forzar: Boolean = false): ResultadoSincronizacion {

        val ahora = System.currentTimeMillis()
        val limite = if (forzar) ahora else ahora - PERIODO_VALIDEZ.inWholeMilliseconds

        val pendientes = localDataSource.obtenerCaducados(limite, MAXIMO_POR_SINCRONIZACION)
        if (pendientes.isEmpty()) return ResultadoSincronizacion()

        var actualizados = 0
        var sinCambios = 0

        for ((indice, libro) in pendientes.withIndex()) {

            // Respeto del rate limit: se espera ANTES de cada petición menos la primera
            if (indice > 0) delay(PAUSA_ENTRE_PETICIONES)

            // getOrElse permite abandonar el bucle en cuanto falla la red. Lo ya
            // escrito en Room se conserva: no hay nada que deshacer.
            val remoto = ejecutarPeticion { remoteDataSource.buscarPorIsbn(libro.isbn) }
                .getOrElse { error ->
                    return ResultadoSincronizacion(actualizados, sinCambios, error.message)
                }

            val instante = System.currentTimeMillis()

            if (remoto == null) {
                // La API no conoce ese ISBN: se anota la comprobación y se continúa
                localDataSource.marcarComprobado(libro.id, instante)
                sinCambios++
            } else {
                localDataSource.refrescarDesdeRed(
                    id = libro.id,                       // ← id LOCAL, no el de la API
                    // Si la API devolviese el ISBN vacío, se conserva el del usuario
                    datos = remoto.copy(isbn = remoto.isbn.ifBlank { libro.isbn }),
                    instante = instante
                )
                actualizados++
            }
        }

        return ResultadoSincronizacion(actualizados, sinCambios)
    }

    // ─── Traducción de excepciones a Result (T5, sin cambios) ───────────────

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