package es.javiercarrasco.appdummy.screens.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import es.javiercarrasco.appdummy.data.model.Libro
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import es.javiercarrasco.appdummy.screens.listado.LibrosUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Estado principal de la UI
    private val _uiState = MutableStateFlow<LibrosUiState>(LibrosUiState.Cargando)
    val uiState: StateFlow<LibrosUiState> = _uiState.asStateFlow()

    // Estado del campo de búsqueda — independiente del UiState principal
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

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

    init {
        // Se ejecuta al crear el ViewModel — carga los datos automáticamente
        cargarLibros()
        cargarAutores()
    }

    fun cargarLibros() {
        // viewModelScope: corrutina ligada al ViewModel, se cancela al destruirlo
        viewModelScope.launch {
            _uiState.value = LibrosUiState.Cargando
            try {
                val libros = repository.getLibros().filter { it.esFavorito } // Filtra solo los libros favoritos
                _uiState.value = LibrosUiState.Exito(libros)
            } catch (e: Exception) {
                _uiState.value = LibrosUiState.Error(
                    e.message ?: "Error desconocido al cargar los libros"
                )
            }
        }
    }

    fun cargarAutores() {
        viewModelScope.launch {
            try {
                val autores = repository.getAutores()
                _autores.value = listOf("Todos") + autores
            } catch (e: Exception) {
                _uiState.value = LibrosUiState.Error(
                    e.message ?: "Error desconocido al cargar los autores"
                )
            }
        }
    }

    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun toggleFavorito(libro: Libro) {
        _uiState.update { estado ->
            if (estado is LibrosUiState.Exito) {
                estado.copy(libros = estado.libros.map {
                    if (it.id == libro.id) it.copy(esFavorito = !it.esFavorito) else it
                })
            } else estado
        }
        viewModelScope.launch {
            _eventos.emit(
                LibrosEvento.MostrarSnackbar(
                    if (libro.esFavorito) "\"${libro.titulo}\" eliminado de favoritos"
                    else "\"${libro.titulo}\" añadido a favoritos"
                )
            )
        }
    }

    fun toggleLeido(libro: Libro) {
        _uiState.update { estado ->
            if (estado is LibrosUiState.Exito) {
                estado.copy(libros = estado.libros.map {
                    if (it.id == libro.id) it.copy(leido = !it.leido) else it
                })
            } else estado
        }
        viewModelScope.launch {
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
                FavoritosViewModel(LibrosRepository())
            }
        }
    }
}