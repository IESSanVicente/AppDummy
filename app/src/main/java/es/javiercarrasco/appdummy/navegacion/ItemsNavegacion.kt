package es.javiercarrasco.appdummy.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// ─── navegacion/ItemsNavegacion.kt ───────────────────────────────────────────────────────────────

data class ItemNavegacion<T : Any>(
    val etiqueta: String,
    val ruta: T,
    val icono: ImageVector,
    val iconoSeleccionado: ImageVector = icono
)

val itemsNavegacionPrincipal = listOf(
    ItemNavegacion(
        etiqueta = "Catálogo",
        ruta = Listado, // <- Ruta de Navegación a la pantalla listado.
        icono = Icons.Default.Book,
        iconoSeleccionado = Icons.Filled.Book
    ),
    ItemNavegacion(
        etiqueta = "Favoritos",
        ruta = Favoritos,
        icono = Icons.Default.FavoriteBorder,
        iconoSeleccionado = Icons.Filled.Favorite
    ),
    ItemNavegacion(
        etiqueta = "Leídos",
        ruta = Leidos,
        icono = Icons.Default.BookmarkBorder,
        iconoSeleccionado = Icons.Filled.Bookmarks
    )
)