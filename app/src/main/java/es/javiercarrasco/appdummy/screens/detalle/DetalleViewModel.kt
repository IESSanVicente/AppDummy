package es.javiercarrasco.appdummy.screens.detalle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import es.javiercarrasco.appdummy.AppDummyApplication
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import es.javiercarrasco.appdummy.utils.AlmacenPortadas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── screens/detalle/DetalleViewModel.kt ─────────────────────────────────────────────────────────
class DetalleViewModel(
    application: Application,
    private val libroId: String,
    private val repository: LibrosRepository
) : AndroidViewModel(application) {   // ← antes era ViewModel

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    private fun cargarDetalle() {
        viewModelScope.launch {
            val libro = repository.obtenerPorId(libroId)
            _uiState.value = if (libro != null) DetalleUiState.Exito(libro)
            else DetalleUiState.NoEncontrado
        }
    }

    /**
     * Recibe la Uri de una imagen (venga de la cámara o del selector de medios),
     * la copia al almacenamiento privado y persiste la ruta resultante.
     *
     * La UI solo entrega una Uri: no sabe dónde acaba el fichero ni cómo se guarda.
     */
    fun asignarPortadaLocal(uri: Uri) {
        viewModelScope.launch {
            val contexto = getApplication<Application>()
            val fichero = AlmacenPortadas.copiarDesdeUri(contexto, uri, libroId)
            repository.guardarPortadaLocal(libroId, fichero?.absolutePath)
            AlmacenPortadas.limpiarAnteriores(contexto, libroId, conservar = fichero)
            cargarDetalle()
        }
    }

    /** Elimina la portada propia y vuelve a mostrar la de Open Library. */
    fun quitarPortadaLocal() {
        viewModelScope.launch {
            val contexto = getApplication<Application>()
            repository.guardarPortadaLocal(libroId, null)
            AlmacenPortadas.limpiarAnteriores(contexto, libroId, conservar = null)
            cargarDetalle()
        }
    }

    companion object {
        fun factoryConId(id: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as AppDummyApplication
                DetalleViewModel(
                    application = app,
                    libroId = id,
                    repository = app.container.librosRepository
                )
            }
        }
    }
}