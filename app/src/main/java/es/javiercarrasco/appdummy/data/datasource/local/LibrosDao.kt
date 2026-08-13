package es.javiercarrasco.appdummy.data.datasource.local

import androidx.room.*
import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.flow.Flow

// ─── data/datasource/local/LibrosDao.kt — añadir/modificar ───────────────────────────────────────
@Dao
interface LibrosDao {
    // ─── Consultas reactivas (Flow, SIN suspend) ──────────────────────────────
    // Room observa la tabla y emite una nueva lista cada vez que hay cambios

    @Query("SELECT * FROM libros ORDER BY year DESC")
    fun observarTodos(): Flow<List<Libro>>

    @Query("SELECT * FROM libros WHERE es_favorito = 1 ORDER BY titulo ASC")
    fun observarFavoritos(): Flow<List<Libro>>

    @Query("SELECT * FROM libros WHERE titulo LIKE '%' || :busqueda || '%' ORDER BY year DESC")
    fun observarPorTitulo(busqueda: String): Flow<List<Libro>>

    // Observar un solo libro (devuelve null si no existe)
    @Query("SELECT * FROM libros WHERE id = :id")
    fun observarPorId(id: String): Flow<Libro?>

    // ─── Consulta puntual (suspend, SIN Flow) ─────────────────────────────────
    // Obtiene el valor una sola vez, sin observar cambios posteriores

    @Query("SELECT * FROM libros WHERE id = :id")
    suspend fun obtenerPorId(id: String): Libro?

    @Query("SELECT DISTINCT autor FROM libros ORDER BY autor ASC")
    suspend fun obtenerAutores(): List<String>?

    // ─── Escritura (siempre suspend) ─────────────────────────────────────────

    // @Insert con OnConflictStrategy.IGNORE: si el libro ya existe, lo ignora
    // Devuelve el rowId de cada fila insertada (-1 si fue ignorada por conflicto)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarIgnorando(libros: List<Libro>): List<Long>

    // @Insert con REPLACE: elimina la fila existente y la reinserta
    // Provoca pérdida de campos locales como esFavorito — usar con cuidado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReemplazando(libro: Libro)

    // @Upsert (disponible desde Room 2.5): intenta INSERT, si hay conflicto hace UPDATE
    // También sobreescribe esFavorito si el objeto tiene esFavorito=false
    @Upsert
    suspend fun upsert(libro: Libro)

    @Update
    suspend fun actualizar(libro: Libro)

    @Delete
    suspend fun eliminar(libro: Libro)

    // Toggle de favorito: invierte el valor booleano en la base de datos
    @Query("UPDATE libros SET es_favorito = NOT es_favorito WHERE id = :id")
    suspend fun toggleFavorito(id: String)

    // Toggle de leído: invierte el valor booleano en la base de datos
    @Query("UPDATE libros SET leido = NOT leido WHERE id = :id")
    suspend fun toggleLeido(id: String)

    // ─── Sincronización (T6) ────────────────────────────────────────────────

    // Devuelve los libros candidatos a refrescarse:
    //   · deben tener ISBN (sin ISBN no se pueden localizar en Open Library)
    //   · deben estar "caducados": comprobados antes del instante :anteriorA
    // Se ordenan del más antiguo al más reciente y se limita la cantidad para no
    // encadenar decenas de peticiones en una sola sincronización.
    @Query("""
        SELECT * FROM libros
        WHERE isbn <> '' AND actualizado_en < :anteriorA
        ORDER BY actualizado_en ASC
        LIMIT :maximo
    """)
    suspend fun obtenerCaducados(anteriorA: Long, maximo: Int): List<Libro>

    // Instante de la sincronización más reciente de toda la tabla.
    // Devuelve null si la tabla está vacía (MAX() sobre cero filas es NULL).
    @Query("SELECT MAX(actualizado_en) FROM libros")
    fun observarUltimaSincronizacion(): Flow<Long?>

    // MODIFICADO respecto a T4: se añade la columna actualizado_en.
    // Sigue sin tocar es_favorito ni leido: son propiedad del usuario.
    @Query("""
        UPDATE libros SET
            titulo = :titulo,
            autor = :autor,
            year = :year,
            isbn = :isbn,
            cover = :cover,
            actualizado_en = :actualizadoEn
        WHERE id = :id
    """)
    suspend fun actualizarDesdeRed(
        id: String,
        titulo: String,
        autor: String,
        year: Int?,
        isbn: String,
        cover: String?,
        actualizadoEn: Long
    )

    // Marca un libro como comprobado sin modificar sus datos. Se usa cuando Open
    // Library no devuelve resultados para ese ISBN: el libro se ha comprobado, así
    // que no debe volver a pedirse en la siguiente sincronización.
    @Query("UPDATE libros SET actualizado_en = :instante WHERE id = :id")
    suspend fun marcarComprobado(id: String, instante: Long)

    // MODIFICADO respecto a T4: propaga la marca de tiempo a la actualización
    @Transaction
    suspend fun upsertConservandoFavorito(libros: List<Libro>, instante: Long) {
        val resultados = insertarIgnorando(libros)
        libros.forEachIndexed { indice, libro ->
            if (resultados[indice] == -1L) {
                actualizarDesdeRed(
                    id = libro.id,
                    titulo = libro.titulo,
                    autor = libro.autor,
                    year = libro.year,
                    isbn = libro.isbn,
                    cover = libro.cover,
                    actualizadoEn = instante
                )
            }
        }
    }
}