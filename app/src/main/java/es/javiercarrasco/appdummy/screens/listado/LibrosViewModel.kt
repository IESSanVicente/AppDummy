package es.javiercarrasco.appdummy.screens.listado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import es.javiercarrasco.appdummy.AppDummyApplication
import es.javiercarrasco.appdummy.data.model.Libro
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import es.javiercarrasco.appdummy.data.repository.ResultadoSincronizacion
import es.javiercarrasco.appdummy.utils.ObservadorConectividad
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// ─── screens/listado/LibrosViewModel.kt ──────────────────────────────────────────────────────────
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LibrosViewModel(
    private val repository: LibrosRepository,
    observadorConectividad: ObservadorConectividad
) : ViewModel() {

    // ─── Texto de búsqueda ──────────────────────────────────────────────────
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // ─── Estado de la sincronización ────────────────────────────────────────
    private val _estaSincronizando = MutableStateFlow(false)
    val estaSincronizando: StateFlow<Boolean> = _estaSincronizando.asStateFlow()

    // ─── Eventos de un solo consumo (Snackbar), heredado de T4 ──────────────
    // replay = 0: los eventos no se repiten para nuevos colectores
    // extraBufferCapacity = 1: evita suspensión si no hay colector en ese instante
    private val _eventos = MutableSharedFlow<LibrosEvento>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val eventos: SharedFlow<LibrosEvento> = _eventos.asSharedFlow()

    // Estado del campo de búsqueda — independiente del UiState principal
    private val _autores = MutableStateFlow(listOf("Todos"))
    val autores: StateFlow<List<String>> = _autores.asStateFlow()

    // ─── Búsqueda por autor ─────────────────────────────────────────────────
    private val _autorSeleccionado = MutableStateFlow("Todos")
    val autorSeleccionado: StateFlow<String> = _autorSeleccionado.asStateFlow()

    // ─── Conectividad: la UI la usa para avisar, NO para elegir la fuente ───
    val hayConexion: StateFlow<Boolean> = observadorConectividad.hayConexion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    // ─── Instante de la última sincronización, observado desde Room ─────────
    val ultimaSincronizacion: StateFlow<Long?> = repository.observarUltimaSincronizacion()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ─── Estado principal: la búsqueda se resuelve en SQL ───────────────────
    // flatMapLatest sustituye el Flow anterior por uno nuevo cada vez que cambia el
    // texto: al escribir se cancela la consulta previa y se abre otra sobre Room.
    // El resultado sigue siendo reactivo (si cambia la tabla, vuelve a emitir).
    val uiState: StateFlow<LibrosUiState> = _busqueda
        .debounce(300.milliseconds)      // no consultar en cada pulsación
        .distinctUntilChanged()
        .flatMapLatest { texto ->
            if (texto.isBlank()) repository.observarLibros()
            else repository.observarPorTitulo(texto)
        }
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

    init {
        // Sincronización silenciosa al crear el ViewModel: solo los caducados
        sincronizar(forzar = false)
    }

    fun cargarAutores() {
        viewModelScope.launch {
            val autores = repository.obtenerAutores() ?: emptyList()
            _autores.value = listOf("Todos") + autores
        }
    }

    fun actualizarAutorSeleccionado(autor: String) {
        _autorSeleccionado.value = autor
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

    // ─── Sincronización ─────────────────────────────────────────────────────

    fun sincronizar(forzar: Boolean = true) {
        // Guarda de reentrada: evita lanzar dos sincronizaciones simultáneas si el
        // usuario arrastra la lista mientras ya se está sincronizando
        if (_estaSincronizando.value) return

        viewModelScope.launch {
            if (!hayConexion.value) {
                _eventos.emit(
                    LibrosEvento.MostrarSnackbar("Sin conexión: se muestran los datos guardados.")
                )
                return@launch
            }

            _estaSincronizando.value = true
            try {
                val resultado = repository.sincronizarBiblioteca(forzar)
                // La sincronización automática del init solo avisa si ha hecho algo
                if (forzar || resultado.comprobados > 0 || resultado.haFallado) {
                    _eventos.emit(LibrosEvento.MostrarSnackbar(mensajeDe(resultado)))
                }
            } finally {
                // finally garantiza que el indicador se apaga aunque haya excepción
                _estaSincronizando.value = false
            }
        }
    }

    private fun mensajeDe(resultado: ResultadoSincronizacion): String = when {
        resultado.haFallado -> resultado.error ?: "No se ha podido sincronizar."
        resultado.comprobados == 0 -> "La biblioteca ya está actualizada."
        resultado.actualizados == 0 -> "Comprobados ${resultado.comprobados} libros, sin cambios."
        else -> "Actualizados ${resultado.actualizados} de ${resultado.comprobados} libros."
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as AppDummyApplication
                LibrosViewModel(
                    repository = app.container.librosRepository,
                    observadorConectividad = app.container.observadorConectividad
                )
            }
        }
    }
}