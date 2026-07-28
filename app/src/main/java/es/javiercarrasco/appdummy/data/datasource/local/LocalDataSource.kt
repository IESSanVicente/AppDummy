package es.javiercarrasco.appdummy.data.datasource.local

import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.flow.Flow

// ─── data/datasource/local/LocalDataSource.kt ────────────────────────────────────────────────────
class LocalDataSource(private val dao: LibrosDao) {
    // Consultas reactivas — exponen el Flow del DAO
    fun observarTodas(): Flow<List<Libro>> = dao.observarTodos()
    fun observarFavoritas(): Flow<List<Libro>> = dao.observarFavoritos()
    fun observarPorTitulo(busqueda: String): Flow<List<Libro>> =
        dao.observarPorTitulo(busqueda)

    fun observarPorId(id: String): Flow<Libro?> = dao.observarPorId(id)

    // Escritura — delega directamente en el DAO
    suspend fun upsertConservandoFavorita(libros: List<Libro>) =
        dao.upsertConservandoFavorito(libros)

    suspend fun toggleFavorita(id: String) = dao.toggleFavorito(id)
}