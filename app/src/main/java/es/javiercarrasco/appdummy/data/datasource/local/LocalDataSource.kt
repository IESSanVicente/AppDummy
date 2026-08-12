package es.javiercarrasco.appdummy.data.datasource.local

import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.flow.Flow

// ─── data/datasource/local/LocalDataSource.kt ────────────────────────────────────────────────────
class LocalDataSource(private val dao: LibrosDao) {
    // Consultas reactivas — exponen el Flow del DAO
    fun observarTodos(): Flow<List<Libro>> = dao.observarTodos()
    fun observarFavoritos(): Flow<List<Libro>> = dao.observarFavoritos()
    fun observarPorTitulo(busqueda: String): Flow<List<Libro>> =
        dao.observarPorTitulo(busqueda)

    fun observarPorId(id: String): Flow<Libro?> = dao.observarPorId(id)

    // Escritura — delega directamente en el DAO
    suspend fun upsertConservandoFavorito(libros: List<Libro>) =
        dao.upsertConservandoFavorito(libros)

    suspend fun toggleFavorito(id: String) = dao.toggleFavorito(id)
    suspend fun toggleLeido(id: String) = dao.toggleLeido(id)
    suspend fun insertarIgnorando(libros: List<Libro>) = dao.insertarIgnorando(libros)
    suspend fun obtenerAutores(): List<String>? = dao.obtenerAutores()
    suspend fun obtenerPorId(id: String): Libro? = dao.obtenerPorId(id)

    // ─── data/datasource/local/LocalDataSource.kt — añadir este método ───────────────────────────────
    // @Upsert: si el id ya existe actualiza la fila, si no la inserta
    suspend fun guardar(libro: Libro) = dao.upsert(libro)
}