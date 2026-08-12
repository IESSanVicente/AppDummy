package es.javiercarrasco.appdummy.screens.nuevo

import es.javiercarrasco.appdummy.data.model.Libro

// ─── screens/nuevo/NuevoLibroUiState.kt ──────────────────────────────────────────────────────────
data class NuevoLibroUiState(
    // Campos del formulario (todos String: es lo que devuelve un OutlinedTextField)
    val titulo: String = "",
    val autor: String = "",
    val year: String = "",
    val isbn: String = "",
    val cover: String? = null,

    // id de Open Library del libro seleccionado; null si el alta es manual
    val idOpenLibrary: String? = null,

    // Resultados de la búsqueda por título
    val sugerencias: List<Libro> = emptyList(),

    val buscando: Boolean = false,
    val mensaje: String? = null,     // errores de red o de validación
    val guardado: Boolean = false    // se pone a true tras insertar en Room
) {
    // Propiedad calculada: la UI la usa para habilitar o no el botón Guardar
    val puedeGuardar: Boolean
        get() = titulo.isNotBlank() && autor.isNotBlank() && !buscando
}