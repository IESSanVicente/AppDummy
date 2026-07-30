package es.javiercarrasco.appdummy.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

// ─── data/datasource/remote/dto/BusquedaResponseDto.kt ───────────────────────────────────────────
// Envuelve la respuesta de search.json: los resultados llegan dentro de "docs"
data class BusquedaResponseDto(

    @SerializedName("numFound")
    val numFound: Int? = null,

    @SerializedName("start")
    val start: Int? = null,

    @SerializedName("docs")
    val docs: List<LibroDto>? = null
)
