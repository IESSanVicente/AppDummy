package es.javiercarrasco.appdummy.screens.reproductor

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── screens/reproductor/ReproductorAudio.kt ─────────────────────────────────────────────────────

@Composable
fun ReproductorAudio(
    @RawRes recurso: Int,
    modifier: Modifier = Modifier,
    viewModel: AudioViewModel = viewModel()
) {
    val reproduciendo by viewModel.reproduciendo.collectAsStateWithLifecycle()
    val posicion by viewModel.posicionMs.collectAsStateWithLifecycle()
    val duracion by viewModel.duracionMs.collectAsStateWithLifecycle()

    // Estado local mientras el usuario arrastra el Slider: si se enviara cada
    // valor intermedio al reproductor, se producirían decenas de seekTo().
    var arrastre by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.MusicNote, contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        Slider(
            value = arrastre ?: posicion.toFloat(),
            valueRange = 0f..(duracion.takeIf { it > 0 }?.toFloat() ?: 1f),
            onValueChange = { arrastre = it },
            onValueChangeFinished = {
                arrastre?.let { viewModel.buscar(it.toInt()) }
                arrastre = null
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatearTiempo(posicion), style = MaterialTheme.typography.labelSmall)
            Text(formatearTiempo(duracion), style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(8.dp))

        FilledIconButton(
            onClick = {
                if (duracion == 0) viewModel.reproducirRecurso(recurso)
                else viewModel.alternarReproduccion()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (reproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (reproduciendo) "Pausar" else "Reproducir",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/** Convierte milisegundos en mm:ss. Función pura: fácil de probar con JUnit. */
fun formatearTiempo(ms: Int): String {
    val totalSegundos = ms / 1000
    return "%d:%02d".format(totalSegundos / 60, totalSegundos % 60)
}