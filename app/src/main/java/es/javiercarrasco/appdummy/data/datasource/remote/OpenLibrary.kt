package es.javiercarrasco.appdummy.data.datasource.remote

// ─── data/datasource/remote/OpenLibrary.kt ───────────────────────────────────────────────────────
object OpenLibrary {

    const val BASE_URL = "https://openlibrary.org/"
    const val COVERS_URL = "https://covers.openlibrary.org/b/"

    // Campos que se solicitan al servidor: reduce drásticamente el tamaño de la respuesta
    const val CAMPOS = "key,title,author_name,first_publish_year,isbn,cover_i"

    // Identifica la app ante Open Library (triplica el límite de peticiones)
    const val USER_AGENT = "AppDummy/1.0 (tu_correo@correo.com)"

    // Tamaños disponibles: S (small), M (medium), L (large)
    // ?default=false hace que el servidor devuelva 404 en lugar de una imagen en blanco,
    // lo que permite a Coil mostrar la imagen de error en vez de un rectángulo vacío
    fun urlCaratulaPorId(coverId: Int?, tamanyo: String = "L"): String? =
        coverId?.let { "${COVERS_URL}id/$it-$tamanyo.jpg?default=false" }

    fun urlCaratulaPorIsbn(isbn: String?, tamanyo: String = "L"): String? =
        isbn?.takeIf { it.isNotBlank() }
            ?.let { "${COVERS_URL}isbn/$it-$tamanyo.jpg?default=false" }
}