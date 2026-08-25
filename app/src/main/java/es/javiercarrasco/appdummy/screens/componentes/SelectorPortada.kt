package es.javiercarrasco.appdummy.screens.componentes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ─── screens/componentes/SelectorPortada.kt ──────────────────────────────────────────────────────

/**
 * Ofrece las dos vías para conseguir una portada propia.
 *
 * @param onImagenElegida se invoca con la Uri devuelta por el selector del sistema.
 * @param onAbrirCamara se invoca al pulsar "Hacer foto"; solo dispara la navegación,
 *                      porque en ese instante todavía no existe ninguna imagen.
 */
@Composable
fun SelectorPortada(
    onImagenElegida: (Uri) -> Unit,
    onAbrirCamara: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // uri es null si el usuario cierra el selector sin elegir nada.
        uri?.let(onImagenElegida)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        OutlinedButton(onClick = onAbrirCamara) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Hacer foto")
        }

        OutlinedButton(
            onClick = {
                lanzador.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Icon(Icons.Default.Photo, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Galería")
        }
    }
}