package es.javiercarrasco.appdummy.screens.componentes

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// ─── screens/componentes/SolicitudPermiso.kt ─────────────────────────────────────────────────────

/**
 * Envuelve un contenido que necesita un permiso peligroso.
 *
 * Muestra [contenido] únicamente cuando el permiso está concedido; en caso
 * contrario presenta una explicación y el botón adecuado a cada situación.
 *
 * @param permiso constante de [android.Manifest.permission].
 * @param explicacion texto que justifica ante el usuario por qué se necesita.
 * @param contenido interfaz protegida por el permiso.
 */
@Composable
fun SolicitudPermiso(
    permiso: String,
    explicacion: String,
    contenido: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Estado inicial: ¿el permiso ya estaba concedido de una sesión anterior?
    var concedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permiso) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // Controla si ya se ha mostrado el diálogo del sistema en esta pantalla.
    var solicitado by remember { mutableStateOf(false) }

    // Lanzador del diálogo del sistema.
    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { resultado ->
        concedido = resultado
        solicitado = true
    }

    when {
        // 1. Permiso concedido: se muestra la interfaz real.
        concedido -> contenido()

        // 2. Primera vez: se pide directamente, sin pantallas intermedias.
        !solicitado -> LaunchedEffect(permiso) { lanzador.launch(permiso) }

        // 3. Denegado: hay que distinguir dos casos muy distintos.
        else -> {
            // shouldShowRequestPermissionRationale devuelve true si el usuario
            // ha denegado el permiso pero el sistema todavía permite volver a
            // pedirlo. Si devuelve false tras una denegación, el usuario ha
            // marcado "no volver a preguntar": el diálogo ya no aparecerá y la
            // única salida son los ajustes del sistema.
            val puedeReintentar = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity, permiso
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = explicacion,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                if (puedeReintentar) {
                    Button(onClick = { lanzador.launch(permiso) }) {
                        Text("Conceder permiso")
                    }
                } else {
                    OutlinedButton(onClick = {
                        // Intent implícito hacia la ficha de la app en Ajustes (T1D).
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }) {
                        Text("Abrir Ajustes del sistema")
                    }
                }
            }
        }
    }
}