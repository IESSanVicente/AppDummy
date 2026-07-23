package es.javiercarrasco.appdummy.screens.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import es.javiercarrasco.appdummy.navegacion.Detalle

// ─── screens/detalle/DetalleViewModel.kt ─────────────────────────────────────────────────────────

class DetalleViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // toRoute<T>() también está disponible en SavedStateHandle
    private val ruta: Detalle = savedStateHandle.toRoute<Detalle>()
    val libroId: Int = ruta.id


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DetalleViewModel(
                    savedStateHandle = this.createSavedStateHandle()
                )
            }
        }
    }
}