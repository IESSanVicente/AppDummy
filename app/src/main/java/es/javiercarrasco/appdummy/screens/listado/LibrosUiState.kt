package es.javiercarrasco.appdummy.screens.listado

import es.javiercarrasco.appdummy.data.model.Libro
import java.util.Collections.emptyList

// ─── screens/listado/LibrosUiState.kt ────────────────────────────────────────────────────────────
// Estados posibles de la pantalla de listado
sealed class LibrosUiState {
    // data object (Kotlin 1.9+): genera toString() legible ("Cargando"
    // en lugar del hash de referencia). Usar siempre para estados sin datos.
    data object Cargando : LibrosUiState()
    data class Exito(val libros: List<Libro>) : LibrosUiState()
    data class Error(val mensaje: String) : LibrosUiState()
}