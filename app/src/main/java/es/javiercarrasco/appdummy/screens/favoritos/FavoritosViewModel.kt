package es.javiercarrasco.appdummy.screens.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import es.javiercarrasco.appdummy.AppDummyApplication
import es.javiercarrasco.appdummy.data.model.Libro
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import es.javiercarrasco.appdummy.screens.listado.LibrosUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Eventos de UI — ocurren una sola vez
sealed class LibrosEvento {
    data class MostrarSnackbar(val mensaje: String) : LibrosEvento()
    data class NavegarADetalle(val id: Int) : LibrosEvento()
}

// ─── screens/favoritos/FavoritosViewModel.kt ─────────────────────────────────────────────────────
class FavoritosViewModel(
    private val repository: LibrosRepository
) : ViewModel() {

    // Estado del campo de búsqueda — independiente del UiState principal
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // Estado principal de la UI
    // stateIn convierte el Flow frío del repositorio en un StateFlow caliente
    // SharingStarted.WhileSubscribed(5_000): el upstream se cancela 5 segundos
    // después de que el último suscriptor desaparezca. Esto cubre rotaciones de
    // pantalla (< 5s) sin mantener recursos cuando la app va a segundo plano.
    val uiState: StateFlow<LibrosUiState> =
        repository.observarFavoritos()
            .map<List<Libro>, LibrosUiState> {
                cargarAutores() // cargar autores para el filtro de la UI
                LibrosUiState.Exito(it)
            }
            .catch { error -> emit(LibrosUiState.Error(error.message ?: "Error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LibrosUiState.Cargando
            )

    // Estado del campo de búsqueda — independiente del UiState principal
    private val _autores = MutableStateFlow(listOf("Todos"))
    val autores: StateFlow<List<String>> = _autores.asStateFlow()

    private val _autorSeleccionado = MutableStateFlow("Todos")
    val autorSeleccionado: StateFlow<String> = _autorSeleccionado.asStateFlow()

    // replay = 0: los eventos no se repiten para nuevos colectores
    // extraBufferCapacity = 1: evita suspensión si no hay colector en ese instante
    private val _eventos = MutableSharedFlow<LibrosEvento>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val eventos: SharedFlow<LibrosEvento> = _eventos.asSharedFlow()

    fun cargarAutores() {
        viewModelScope.launch {
            val autores = repository.obtenerAutores() ?: emptyList()
            _autores.value = listOf("Todos") + autores
        }
    }

    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun toggleFavorito(libro: Libro) {
        viewModelScope.launch {
            repository.toggleFavorito(libro.id)
            // No es necesario actualizar el uiState manualmente:
            // el Flow de Room detecta el cambio y emite la nueva lista automáticamente

            _eventos.emit(
                LibrosEvento.MostrarSnackbar(
                    if (libro.esFavorito) "\"${libro.titulo}\" eliminado de favoritos"
                    else "\"${libro.titulo}\" añadido a favoritos"
                )
            )
        }
    }

    fun toggleLeido(libro: Libro) {
        viewModelScope.launch {
            repository.toggleLeido(libro.id)
            _eventos.emit(
                LibrosEvento.MostrarSnackbar(
                    if (libro.leido) "\"${libro.titulo}\" marcado como no leído"
                    else "\"${libro.titulo}\" marcado como leído"
                )
            )
        }
    }

    fun actualizarAutorSeleccionado(autor: String) {
        _autorSeleccionado.value = autor
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as AppDummyApplication
                FavoritosViewModel(app.container.librosRepository)
            }
        }
    }
}