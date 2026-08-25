package es.javiercarrasco.appdummy.screens.camara

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.javiercarrasco.appdummy.screens.componentes.SolicitudPermiso
import es.javiercarrasco.appdummy.utils.AlmacenPortadas
import java.io.File

// ─── screens/camara/PantallaCamara.kt ────────────────────────────────────────────────────────────
@Composable
fun PantallaCamara(
    libroId: String,
    onFotoCapturada: (Uri) -> Unit,
    onVolver: () -> Unit
) {
    // Toda la pantalla queda protegida por el permiso de cámara.
    SolicitudPermiso(
        permiso = Manifest.permission.CAMERA,
        explicacion = "AppDummy necesita la cámara para fotografiar la portada de tus libros."
    ) {
        ContenidoCamara(libroId, onFotoCapturada, onVolver)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoCamara(
    libroId: String,
    onFotoCapturada: (Uri) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ── Casos de uso ────────────────────────────────────────────────────────
    // remember evita que se recreen en cada recomposición: si se recrearan,
    // la cámara se reiniciaría constantemente y la vista previa parpadearía.
    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember {
        ImageCapture.Builder()
            // MINIMIZE_LATENCY prioriza la rapidez del disparo;
            // MAXIMIZE_QUALITY aplicaría más procesado a costa de tardar más.
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Cámara seleccionada; es estado porque el usuario puede cambiarla.
    var selector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var capturando by remember { mutableStateOf(false) }

    // La vista previa se guarda en estado para poder revincular los casos de
    // uso cuando cambia la cámara seleccionada.
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // Vinculación de los casos de uso al ciclo de vida.
    // Se relanza cuando cambia el selector o cuando la vista ya está creada.
    LaunchedEffect(selector, previewView) {
        val vista = previewView ?: return@LaunchedEffect
        // awaitInstance es la versión suspend de getInstance(): permite esperar
        // al proveedor sin bloquear el hilo principal ni usar callbacks.
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        preview.surfaceProvider = vista.surfaceProvider
        try {
            // Antes de vincular hay que desvincular: la cámara es un recurso
            // exclusivo y no admite dos vinculaciones simultáneas.
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        } catch (e: Exception) {
            Log.e("CameraX", "Error al vincular los casos de uso", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portada") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selector = if (selector == CameraSelector.DEFAULT_BACK_CAMERA)
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else
                            CameraSelector.DEFAULT_BACK_CAMERA
                    }) {
                        Icon(Icons.Default.Cameraswitch, "Cambiar de cámara")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // FILL_CENTER recorta para llenar la vista;
                        // FIT_CENTER mostraría bandas negras.
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Botón de disparo.
            FilledIconButton(
                onClick = {
                    if (capturando) return@FilledIconButton
                    capturando = true
                    capturarFoto(
                        imageCapture = imageCapture,
                        destino = AlmacenPortadas.nuevoFicheroPara(context, libroId),
                        context = context,
                        onExito = { uri -> capturando = false; onFotoCapturada(uri) },
                        onError = { capturando = false }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(72.dp)
            ) {
                if (capturando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(Icons.Default.Camera, "Capturar", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

// ─── screens/camara/PantallaCamara.kt ─ ampliación ───────────────────────────────────────────────
/**
 * Dispara la cámara y guarda el resultado en [destino].
 *
 * @param onExito recibe la Uri del fichero escrito.
 */
private fun capturarFoto(
    imageCapture: ImageCapture,
    destino: File,
    context: Context,
    onExito: (Uri) -> Unit,
    onError: () -> Unit
) {
    // OutputFileOptions decide el destino. Con un File, la imagen se escribe
    // en almacenamiento privado y no se registra en la galería del sistema.
    val opciones = ImageCapture.OutputFileOptions.Builder(destino).build()

    imageCapture.takePicture(
        opciones,
        // El callback se ejecutará en el hilo principal: podemos tocar el estado de Compose.
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {

            override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                // savedUri solo tiene valor cuando la salida es MediaStore.
                // Al escribir en un File, viene a null y construimos la Uri nosotros.
                onExito(resultado.savedUri ?: Uri.fromFile(destino))
            }

            override fun onError(excepcion: ImageCaptureException) {
                Log.e("CameraX", "Error al guardar la fotografía", excepcion)
                onError()
            }
        }
    )
}