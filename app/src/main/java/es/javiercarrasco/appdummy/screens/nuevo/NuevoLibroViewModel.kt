package es.javiercarrasco.appdummy.screens.nuevo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import es.javiercarrasco.appdummy.AppDummyApplication
import es.javiercarrasco.appdummy.data.model.Libro
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import es.javiercarrasco.appdummy.utils.Isbn
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

// ─── screens/nuevo/NuevoLibroViewModel.kt ────────────────────────────────────────────────────────
@OptIn(FlowPreview::class)
class NuevoLibroViewModel(
    private val repository: LibrosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevoLibroUiState())
    val uiState: StateFlow<NuevoLibroUiState> = _uiState.asStateFlow()

    // Flujo interno que recibe cada pulsación en el campo "Título"
    private val consultaTitulo = MutableStateFlow("")

    init {
        viewModelScope.launch {
            consultaTitulo
                .debounce(500.milliseconds)  // espera 500 ms sin nuevas pulsaciones
                .distinctUntilChanged()          // ignora si el texto no ha cambiado
                .filter { it.length >= 3 }       // no buscar con menos de 3 caracteres
                // collectLatest cancela la búsqueda anterior si llega un texto nuevo:
                // así nunca se muestran resultados de una consulta ya obsoleta
                .collectLatest { texto -> buscarSugerencias(texto) }
        }
    }

    // ─── Actualización de los campos del formulario ─────────────────────────────

    fun actualizarTitulo(texto: String) {
        _uiState.update { it.copy(titulo = texto, mensaje = null) }
        consultaTitulo.value = texto             // dispara el debounce
    }

    fun actualizarAutor(texto: String) = _uiState.update { it.copy(autor = texto) }
    fun actualizarYear(texto: String) =
        _uiState.update { it.copy(year = texto.filter { c -> c.isDigit() }.take(4)) }

    fun actualizarIsbn(texto: String) =
        _uiState.update { it.copy(isbn = Isbn.normalizar(texto).take(13), mensaje = null) }

    // ─── Búsqueda por ISBN: se dispara al pulsar la lupa ────────────────────────

    fun buscarPorIsbn() {
        val isbn = _uiState.value.isbn

        if (!Isbn.esValido13(isbn)) {
            _uiState.update { it.copy(mensaje = "El ISBN-13 no es válido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(buscando = true, mensaje = null) }

            repository.buscarEnApiPorIsbn(isbn)
                .onSuccess { libro ->
                    if (libro == null) {
                        _uiState.update {
                            it.copy(
                                buscando = false,
                                mensaje = "Sin resultados para el ISBN $isbn."
                            )
                        }
                    } else {
                        rellenarCon(libro)
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(buscando = false, mensaje = error.message) }
                }
        }
    }

    // ─── Búsqueda por título: la lanza el debounce del init ─────────────────────

    private suspend fun buscarSugerencias(titulo: String) {
        _uiState.update { it.copy(buscando = true, mensaje = null) }

        repository.buscarEnApiPorTitulo(titulo)
            .onSuccess { libros ->
                _uiState.update { it.copy(buscando = false, sugerencias = libros) }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(buscando = false, sugerencias = emptyList(), mensaje = error.message)
                }
            }
    }

    // El usuario pulsa una sugerencia: se vuelca el libro en el formulario
    fun seleccionarSugerencia(libro: Libro) = rellenarCon(libro)

    private fun rellenarCon(libro: Libro) {
        _uiState.update { estado ->
            estado.copy(
                titulo = libro.titulo,
                autor = libro.autor,
                year = libro.year?.toString().orEmpty(),
                // Si la API no devuelve ISBN se conserva el que escribió el usuario
                isbn = libro.isbn.ifBlank { estado.isbn },
                cover = libro.cover,
                idOpenLibrary = libro.id,
                sugerencias = emptyList(),      // se ocultan las sugerencias
                buscando = false,
                mensaje = null
            )
        }
        // Evita que el debounce vuelva a buscar con el título recién rellenado
        consultaTitulo.value = libro.titulo
    }

    // ─── Alta en Room ───────────────────────────────────────────────────────────

    fun guardar() {
        val estado = _uiState.value
        if (!estado.puedeGuardar) return

        viewModelScope.launch {
            val libro = Libro(
                // Si el libro viene de la API se conserva su key ("/works/OL...").
                // Si el alta es manual se genera un id local único, para no colisionar
                // nunca con un identificador real de Open Library.
                id = estado.idOpenLibrary ?: "/local/${UUID.randomUUID()}",
                titulo = estado.titulo.trim(),
                autor = estado.autor.trim(),
                year = estado.year.toIntOrNull(),
                isbn = estado.isbn,
                cover = estado.cover
            )
            repository.guardarLibro(libro)
            _uiState.update { it.copy(guardado = true) }
        }
    }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as AppDummyApplication
                NuevoLibroViewModel(app.container.librosRepository)
            }
        }
    }
}