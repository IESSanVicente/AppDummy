package es.javiercarrasco.appdummy.screens.nuevo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import es.javiercarrasco.appdummy.screens.componentes.CaratulaLibro

// ─── screens/nuevo/PantallaNuevoLibro.kt ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNuevoLibro(
    viewModel: NuevoLibroViewModel = viewModel(factory = NuevoLibroViewModel.Factory),
    onGuardado: () -> Unit = {},
    onCancelar: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto de navegación: cuando el ViewModel confirma el guardado, se vuelve atrás.
    // La clave del LaunchedEffect es el propio flag, así solo se ejecuta al cambiar.
    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) onGuardado()
    }

    // Muestra los errores de red o de validación y limpia el mensaje después
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir libro") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Búsqueda por ISBN-13 ───────────────────────────────────────────
            OutlinedTextField(
                value = uiState.isbn,
                onValueChange = viewModel::actualizarIsbn,
                label = { Text("ISBN-13") },
                supportingText = { Text("13 dígitos; pulsa la lupa para buscar en Open Library") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = viewModel::buscarPorIsbn) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar por ISBN")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ─── Búsqueda por título (con debounce automático) ──────────────────
            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = viewModel::actualizarTitulo,
                label = { Text("Título *") },
                supportingText = { Text("A partir de 3 caracteres se buscan sugerencias") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.buscando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // ─── Sugerencias devueltas por la API ───────────────────────────────
            if (uiState.sugerencias.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(uiState.sugerencias, key = { it.id }) { libro ->
                            ListItem(
                                headlineContent = { Text(libro.titulo) },
                                supportingContent = {
                                    Text("${libro.autor} • ${libro.year ?: "s. f."}")
                                },
                                leadingContent = {
                                    CaratulaLibro(
                                        coverUrl = libro.cover,
                                        titulo = libro.titulo,
                                        modifier = Modifier.width(40.dp)
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.seleccionarSugerencia(libro)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // ─── Campos rellenados automáticamente (siguen siendo editables) ────
            OutlinedTextField(
                value = uiState.autor,
                onValueChange = viewModel::actualizarAutor,
                label = { Text("Autor *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.year,
                    onValueChange = viewModel::actualizarYear,
                    label = { Text("Año") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                // Previsualización de la carátula obtenida de la Covers API
                CaratulaLibro(
                    coverUrl = uiState.cover,
                    titulo = uiState.titulo,
                    modifier = Modifier.width(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::guardar,
                enabled = uiState.puedeGuardar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar en la biblioteca")
            }
        }
    }
}