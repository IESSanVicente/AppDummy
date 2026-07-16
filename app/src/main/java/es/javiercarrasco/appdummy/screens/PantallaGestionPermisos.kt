package es.javiercarrasco.appdummy.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// ─── screens/PantallaGestionPermisos.kt ──────────────────────────────────────────────────────────

// Estado del permiso — representado como sealed class
sealed class EstadoPermiso {
    data object Concedido : EstadoPermiso()
    data object Pendiente : EstadoPermiso()
    data object Denegado : EstadoPermiso()
    data object DenegadoPermanentemente : EstadoPermiso()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionPermisos() {
    val context = LocalContext.current

    // Comprobación inicial del estado del permiso de cámara
    var estadoPermiso by remember {
        val concedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        mutableStateOf(if (concedido) EstadoPermiso.Concedido else EstadoPermiso.Pendiente)
    }

    // Lanzador para solicitar el permiso de cámara
    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        estadoPermiso = if (concedido) EstadoPermiso.Concedido else EstadoPermiso.Denegado
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Permisos de AppDummy") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono según estado
            val (icono, colorIcono, descripcion) = when (estadoPermiso) {
                EstadoPermiso.Concedido -> Triple(
                    Icons.Default.CheckCircle,
                    MaterialTheme.colorScheme.primary,
                    "Cámara disponible"
                )
                EstadoPermiso.Pendiente -> Triple(
                    Icons.Default.CameraAlt,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    "La app necesita acceso a la cámara para capturar imágenes de portadas"
                )
                EstadoPermiso.Denegado,
                EstadoPermiso.DenegadoPermanentemente -> Triple(
                    Icons.Default.Block,
                    MaterialTheme.colorScheme.error,
                    "Sin acceso a la cámara"
                )
            }

            Icon(icono, contentDescription = null, modifier = Modifier.size(72.dp), tint = colorIcono)
            Spacer(modifier = Modifier.height(16.dp))
            Text(descripcion, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))

            // Acción según estado
            when (estadoPermiso) {
                EstadoPermiso.Concedido -> Text("✓ Puedes usar la cámara en AppDummy")
                EstadoPermiso.Pendiente -> {
                    Button(
                        onClick = { solicitarPermiso.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Conceder permiso de cámara") }
                }
                EstadoPermiso.Denegado,
                EstadoPermiso.DenegadoPermanentemente -> {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Abrir Ajustes del sistema") }
                }
            }
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PantallaPermsisosPreview() {
    MaterialTheme {
        PantallaGestionPermisos()
    }
}