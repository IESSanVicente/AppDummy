package es.javiercarrasco.appdummy.data.datasource.local

import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.flow.Flow

// ─── data/datasource/local/LocalDataSource.kt — añadir ───────────────────────────────────────────
class LocalDataSource(private val dao: LibrosDao) {
    // Consultas reactivas — exponen el Flow del DAO
    fun observarTodos(): Flow<List<Libro>> = dao.observarTodos()
    fun observarFavoritos(): Flow<List<Libro>> = dao.observarFavoritos()
    fun observarPorTitulo(busqueda: String): Flow<List<Libro>> =
        dao.observarPorTitulo(busqueda)

    fun observarPorId(id: String): Flow<Libro?> = dao.observarPorId(id)

    // Escritura — delega directamente en el DAO - Modificación T6
    suspend fun upsertConservandoFavorito(libros: List<Libro>, instante: Long) =
        dao.upsertConservandoFavorito(libros, instante)

    suspend fun toggleFavorito(id: String) = dao.toggleFavorito(id)
    suspend fun toggleLeido(id: String) = dao.toggleLeido(id)
    suspend fun insertarIgnorando(libros: List<Libro>) = dao.insertarIgnorando(libros)
    suspend fun obtenerAutores(): List<String>? = dao.obtenerAutores()
    suspend fun obtenerPorId(id: String): Libro? = dao.obtenerPorId(id)

    // @Upsert: si el id ya existe actualiza la fila, si no la inserta
    suspend fun guardar(libro: Libro) = dao.upsert(libro)

    // ─── Sincronización (T6) ────────────────────────────────────────────────
    suspend fun obtenerCaducados(anteriorA: Long, maximo: Int): List<Libro> =
        dao.obtenerCaducados(anteriorA, maximo)

    fun observarUltimaSincronizacion(): Flow<Long?> = dao.observarUltimaSincronizacion()

    suspend fun marcarComprobado(id: String, instante: Long) =
        dao.marcarComprobado(id, instante)

    // Recibe el id LOCAL y los datos REMOTOS por separado: son dos cosas distintas
    suspend fun refrescarDesdeRed(id: String, datos: Libro, instante: Long) =
        dao.actualizarDesdeRed(
            id = id,
            titulo = datos.titulo,
            autor = datos.autor,
            year = datos.year,
            isbn = datos.isbn,
            cover = datos.cover,
            actualizadoEn = instante
        )

    // ─── Añadir a LocalDataSource (T7) ───────────────────────────────────────────
    suspend fun actualizarPortadaLocal(id: String, ruta: String?) =
        dao.actualizarPortadaLocal(id, ruta)
}