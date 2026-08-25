package es.javiercarrasco.appdummy.screens.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.javiercarrasco.appdummy.R
import es.javiercarrasco.appdummy.screens.componentes.CaratulaLibro
import es.javiercarrasco.appdummy.screens.componentes.SelectorPortada
import es.javiercarrasco.appdummy.screens.reproductor.ReproductorAudio
import es.javiercarrasco.appdummy.screens.reproductor.ReproductorVideo

// ─── screens/detalle/PantallaDetalle.kt ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    libroId: String,
    viewModel: DetalleViewModel,          // ← MODIFICADO: ya no se crea por defecto aquí
    onVolver: () -> Unit,
    onAbrirCamara: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titulo = (uiState as? DetalleUiState.Exito)?.libro?.titulo ?: "Detalle"
                    Text(titulo)
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (val estado = uiState) {
                is DetalleUiState.Cargando ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is DetalleUiState.Exito -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CaratulaLibro(
                            coverUrl = estado.libro.cover,
                            portadaLocal = estado.libro.portadaLocal,   // ← T7
                            titulo = estado.libro.titulo,
                            modifier = Modifier.width(160.dp)
                        )

                        SelectorPortada(
                            // El ViewModel se encarga de copiar y persistir: la UI solo pasa la Uri.
                            onImagenElegida = { uri -> viewModel.asignarPortadaLocal(uri) },
                            // Aquí no hay Uri todavía: solo se abre la pantalla de cámara.
                            onAbrirCamara = { onAbrirCamara(estado.libro.id) }
                        )

                        // Solo tiene sentido ofrecer "quitar" si hay portada propia.
                        if (estado.libro.portadaLocal != null) {
                            TextButton(onClick = { viewModel.quitarPortadaLocal() }) {
                                Text("Quitar portada propia")
                            }
                        }

                        ReproductorAudio(
                            recurso = R.raw.muestra,
                            modifier = Modifier.wrapContentSize()
                        )

                        ReproductorVideo(
                            urlVideo = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/av1/360/Big_Buck_Bunny_360_10s_1MB.mp4",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Text(
                            text = estado.libro.titulo,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(estado.libro.autor) }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("${estado.libro.year}") }
                            )
                        }
                        HorizontalDivider()
                        Text(
                            text = "Sinopsis del libro.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is DetalleUiState.NoEncontrado -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Libro no encontrado")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onVolver) { Text("Volver al catálogo") }
                    }
                }
            }
        }
    }
}