package es.javiercarrasco.appdummy.data.datasource.remote

import es.javiercarrasco.appdummy.data.datasource.remote.dto.BusquedaResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

// ─── data/datasource/remote/OpenLibraryApiService.kt ─────────────────────────────────────────────
interface OpenLibraryApiService {

    // Búsqueda por título (el parámetro title restringe la coincidencia al título de la obra)
    // GET https://openlibrary.org/search.json?title=hail+mary&fields=...&limit=10&page=1
    @GET("search.json")
    suspend fun buscarPorTitulo(
        @Query("title")  titulo: String,
        @Query("fields") campos: String = OpenLibrary.CAMPOS,
        @Query("limit")  limite: Int = 10,
        @Query("page")   pagina: Int = 1
    ): BusquedaResponseDto

    // Búsqueda genérica sobre el índice Solr: admite sintaxis de campo, como "isbn:9780593135204"
    // GET https://openlibrary.org/search.json?q=isbn:9780593135204&fields=...&limit=1
    @GET("search.json")
    suspend fun buscar(
        @Query("q")      consulta: String,
        @Query("fields") campos: String = OpenLibrary.CAMPOS,
        @Query("limit")  limite: Int = 10,
        @Query("page")   pagina: Int = 1
    ): BusquedaResponseDto
}