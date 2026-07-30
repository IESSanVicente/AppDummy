package es.javiercarrasco.appdummy.data.datasource.remote.dto

import es.javiercarrasco.appdummy.data.datasource.remote.OpenLibrary
import es.javiercarrasco.appdummy.data.model.Libro

// ─── data/datasource/remote/dto/LibroMapper.kt ───────────────────────────────────────────────────

// Convierte un LibroDto en la entidad Libro de Room.
// Devuelve null si el documento no tiene los datos mínimos imprescindibles.
//
// isbnBuscado: cuando la búsqueda se ha hecho por ISBN, se conserva el que
// introdujo el usuario. Una obra (work) agrupa muchas ediciones, y la API puede
// devolver primero el ISBN de una edición distinta a la que se estaba buscando.
fun LibroDto.toLibro(isbnBuscado: String? = null): Libro? {

    // Sin identificador no hay clave primaria posible → se descarta el documento
    val idObra = key ?: return null
    val tituloObra = title?.takeIf { it.isNotBlank() } ?: return null

    // Del array de ISBN se prioriza el de 13 dígitos (formato actual)
    val isbn13 = isbnBuscado
        ?: isbn?.firstOrNull { it.length == 13 }
        ?: isbn?.firstOrNull()
        ?: ""

    return Libro(
        id = idObra,
        titulo = tituloObra,
        // El array de autores se aplana en una única cadena
        autor = authorName
            ?.filter { it.isNotBlank() }
            ?.joinToString(", ")
            ?.takeIf { it.isNotBlank() }
            ?: "Autor desconocido",
        year = firstPublishYear,
        isbn = isbn13,
        // Se prefiere la portada por cover_i; si no existe, se intenta por ISBN
        cover = OpenLibrary.urlCaratulaPorId(coverId)
            ?: OpenLibrary.urlCaratulaPorIsbn(isbn13),
        // esFavorito y leido NO se tocan: son datos locales que la API desconoce.
        // Se quedan con el valor por defecto (false) definido en la entidad.
    )
}

// mapNotNull aplica toLibro() a cada documento y descarta automáticamente
// los que han devuelto null, sin necesidad de un filter previo
fun List<LibroDto>.toLibros(): List<Libro> = mapNotNull { it.toLibro() }