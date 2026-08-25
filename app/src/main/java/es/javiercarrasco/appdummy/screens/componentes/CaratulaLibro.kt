package es.javiercarrasco.appdummy.screens.componentes

// ─── screens/componentes/CaratulaLibro.kt ────────────────────────────────────────────────────────
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import es.javiercarrasco.appdummy.R
import java.io.File

@Composable
fun CaratulaLibro(
    coverUrl: String?,
    portadaLocal: String?,          // ← NUEVO en T7
    titulo: String,
    modifier: Modifier = Modifier
) {
    // Prioridad: portada propia del usuario > portada de Open Library > nocover.jpg
    val origen: Any? = portadaLocal?.let { File(it) } ?: coverUrl

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(origen)
            .crossfade(true)
//            .memoryCacheKey(origen.toString())
//            .diskCacheKey(origen.toString())
            .build(),
        // placeholder: imagen mientras se descarga
        placeholder = painterResource(R.drawable.nocover),
        // error: imagen si la descarga falla o el servidor devuelve 404
        error = painterResource(R.drawable.nocover),
        contentDescription = "Portada de $titulo",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(2f / 3f)      // proporción habitual de una portada de libro
            .clip(RoundedCornerShape(8.dp))
    )
}