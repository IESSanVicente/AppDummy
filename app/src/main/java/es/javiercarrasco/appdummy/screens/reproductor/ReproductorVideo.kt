package es.javiercarrasco.appdummy.screens.reproductor

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

// ─── screens/reproductor/ReproductorVideo.kt ─────────────────────────────────────────────────────

@OptIn(UnstableApi::class)
@Composable
fun ReproductorVideo(
    urlVideo: String,
    modifier: Modifier = Modifier,
    viewModel: VideoViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Carga el contenido solo cuando cambia la URL, no en cada recomposición.
    LaunchedEffect(urlVideo) { viewModel.cargar(urlVideo) }

    // DisposableEffect ejecuta código al entrar en la composición y garantiza
    // una limpieza en onDispose al salir. Es la herramienta adecuada cuando hay
    // que registrar y dar de baja un observador; LaunchedEffect no ofrece
    // ese punto de limpieza.
    DisposableEffect(lifecycleOwner) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_STOP -> viewModel.player.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observador) }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = viewModel.player
                useController = true    // controles de reproducción integrados
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        // onRelease se ejecuta cuando la vista abandona la composición:
        // se desvincula el reproductor, pero NO se libera (vive en el ViewModel).
        onRelease = { vista -> vista.player = null },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}