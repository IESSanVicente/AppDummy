package es.javiercarrasco.appdummy.screens.componentes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ─── screens/componentes/SelectorPortada.kt ──────────────────────────────────────────────────────
@Composable
fun SelectorPortada(
    onImagenElegida: (Uri) -> Unit,
    onAbrirCamara: () -> Unit,
    modifier: Modifier = Modifier
) {
    // PickVisualMedia abre el selector del sistema y devuelve una Uri,
    // o null si el usuario cancela sin elegir nada.
    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onImagenElegida) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        OutlinedButton(onClick = onAbrirCamara) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Hacer foto")
        }

        OutlinedButton(onClick = {
            lanzador.launch(
                // ImageOnly filtra el selector para que no muestre vídeos.
                // Otras opciones: VideoOnly e ImageAndVideo.
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Icon(Icons.Default.Photo, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Galería")
        }
    }
}