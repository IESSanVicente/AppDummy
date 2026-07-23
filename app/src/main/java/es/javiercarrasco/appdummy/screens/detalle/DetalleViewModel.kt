package es.javiercarrasco.appdummy.screens.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import es.javiercarrasco.appdummy.data.repository.LibrosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── screens/detalle/DetalleViewModel.kt ─────────────────────────────────────────────────────────
class DetalleViewModel(
    private val libroId: Int,
    private val repository: LibrosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargarDetalle()
    }

    private fun cargarDetalle() {
        viewModelScope.launch {
            val libro = repository.getLibroPorId(libroId)
            _uiState.value = if (libro != null) DetalleUiState.Exito(libro)
            else DetalleUiState.NoEncontrado
        }
    }

    companion object {
        fun factoryConId(id: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DetalleViewModel(
                    libroId = id,
                    repository = LibrosRepository()
                )
            }
        }
    }
}