package es.javiercarrasco.appdummy.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

// ─── data/datasource/remote/dto/LibroDto.kt ──────────────────────────────────────────────────────
// Representa EXACTAMENTE un documento del array "docs" de search.json.
// Todas las propiedades son nullable y tienen valor por defecto: Open Library
// OMITE los campos que no tiene, en lugar de enviarlos con valor null.
data class LibroDto(

    // "key": "/works/OL21745884W" — identificador de la obra
    @SerializedName("key")
    val key: String? = null,

    // "title": "Project Hail Mary"
    @SerializedName("title")
    val title: String? = null,

    // "author_name": ["Andy Weir"] — array, puede tener varios autores
    @SerializedName("author_name")
    val authorName: List<String>? = null,

    // "first_publish_year": 2021
    @SerializedName("first_publish_year")
    val firstPublishYear: Int? = null,

    // "isbn": ["0593135202", "9780593135204", ...] — mezcla ISBN-10 e ISBN-13
    @SerializedName("isbn")
    val isbn: List<String>? = null,

    // "cover_i": 11200092 — identificador interno de la portada, NO una URL
    @SerializedName("cover_i")
    val coverId: Int? = null
)