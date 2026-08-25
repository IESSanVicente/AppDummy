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
import java.io.File

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
     * Origen: selector de medios. La Uri es ajena y temporal, hay que copiarla.
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

    /**
     * Origen: cámara. CameraX ya ha escrito el JPEG en su destino definitivo,
     * así que copiarlo sería duplicar trabajo: basta con registrar su ruta.
     */
    fun asignarPortadaCapturada(uri: Uri) {
        viewModelScope.launch {
            val contexto = getApplication<Application>()
            // Uri.fromFile() conserva la ruta absoluta en uri.path.
            val fichero = uri.path?.let { File(it) }

            if (fichero != null && fichero.exists() && fichero.length() > 0) {
                repository.guardarPortadaLocal(libroId, fichero.absolutePath)
                AlmacenPortadas.limpiarAnteriores(contexto, libroId, conservar = fichero)
            }
            cargarDetalle()
        }
    }

    /**
     * Quitar la portada propia: se olvida la ruta y se borran todos los ficheros del libro.
     */
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