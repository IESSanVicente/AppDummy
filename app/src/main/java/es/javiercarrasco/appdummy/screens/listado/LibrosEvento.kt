package es.javiercarrasco.appdummy.screens.listado

// Eventos de UI — ocurren una sola vez
sealed class LibrosEvento {
    data class MostrarSnackbar(val mensaje: String) : LibrosEvento()
    // data class NavegarADetalle(val id: Int) : LibrosEvento()
}