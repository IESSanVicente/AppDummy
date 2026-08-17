package es.javiercarrasco.appdummy.screens.listado

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import es.javiercarrasco.appdummy.screens.componentes.ItemLibro
import kotlinx.coroutines.*

// ─── screens/listado/PantallaListado.kt ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListado(
    viewModel: LibrosViewModel = viewModel(factory = LibrosViewModel.Factory),
    onNavegaADetalle: (String) -> Unit = {},  // callback de navegación (se conecta al NavHost en T3)
    onNavegaANuevoLibro: () -> Unit = {}      // ← nuevo callback
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val autorSeleccionado by viewModel.autorSeleccionado.collectAsStateWithLifecycle()
    val autores by viewModel.autores.collectAsStateWithLifecycle()
    val estaSincronizando by viewModel.estaSincronizando.collectAsStateWithLifecycle()
    val hayConexion by viewModel.hayConexion.collectAsStateWithLifecycle()
    val ultimaSincronizacion by viewModel.ultimaSincronizacion.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarJob by remember { mutableStateOf<Job?>(null) }

    // Consumo de los eventos de un solo uso emitidos por el ViewModel.
    // LaunchedEffect(Unit): la corrutina se lanza una vez y vive mientras el
    // composable permanezca en la composición.
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is LibrosEvento.MostrarSnackbar -> {
                    // Cancelar snackbar previo si existe
                    snackbarJob?.cancel()
                    // Descarta el Snackbar visible en pantalla
                    snackbarHostState.currentSnackbarData?.dismiss()
                    // Lanzar un nuevo Snackbar
                    snackbarJob = scope.launch {
                        snackbarHostState.showSnackbar(evento.mensaje)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AppDummy")
                        Text(
                            text = textoUltimaSincronizacion(ultimaSincronizacion),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sincronizar() },
                        enabled = !estaSincronizando
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            if (snackbarHostState.currentSnackbarData == null)
                AnimatedVisibility(snackbarHostState.currentSnackbarData == null) {
                    FloatingActionButton(onClick = onNavegaANuevoLibro) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir libro")
                    }
                }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            // ─── Aviso de falta de conexión ─────────────────────────────────
            AnimatedVisibility(visible = !hayConexion) { BannerSinConexion() }

            // ─── Búsqueda (el filtrado lo resuelve Room) ────────────────────
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::actualizarBusqueda,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar en mi biblioteca...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    AnimatedVisibility(visible = busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.actualizarBusqueda("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                singleLine = true
            )

            // ─── Chips de autores ───────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(autores) { autor ->
                    FilterChip(
                        selected = autor == autorSeleccionado,
                        onClick = { viewModel.actualizarAutorSeleccionado(autor) },
                        label = { Text(autor) }
                    )
                }
            }

            // ─── Contenido con pull-to-refresh ──────────────────────────────
            PullToRefreshBox(
                isRefreshing = estaSincronizando,
                onRefresh = { viewModel.sincronizar(forzar = true) },
                modifier = Modifier.weight(1f)
            ) {
                // when exhaustivo sobre la sealed class
                when (val estado = uiState) {
                    is LibrosUiState.Cargando ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }

                    is LibrosUiState.Error ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(estado.mensaje)
                        }

                    is LibrosUiState.Exito -> {
                        // IMPORTANTE: el contenido de PullToRefreshBox debe ser
                        // desplazable para que el gesto se detecte. Por eso el
                        // estado vacío también va dentro del LazyColumn.

                        // Filtrado de libros según la búsqueda y el autor seleccionado en UI,
                        // aunque la búsqueda ya se resuelve en SQL, el filtrado por autor
                        // se hace aquí en la UI. Esto permite que el filtrado por autor sea
                        // reactivo y no requiera una nueva consulta a la base de datos.
                        val librosFiltrados = estado.libros.filter {
                            val coincideBusqueda =
                                busqueda.isBlank() || it.titulo.contains(
                                    busqueda,
                                    ignoreCase = true
                                )
                            val coincideGenero =
                                autorSeleccionado == "Todos" || it.autor == autorSeleccionado
                            coincideBusqueda && coincideGenero
                        }
                        if (librosFiltrados.isEmpty()) {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            ) {
                                item {
                                    EstadoVacio(
                                        busqueda = busqueda,
                                        onAnyadir = onNavegaANuevoLibro
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(librosFiltrados, key = { it.id }) { libro ->
                                    ItemLibro(
                                        libro = libro,
                                        onClickItem = { onNavegaADetalle(libro.id) },
                                        onToggleFavorito = { viewModel.toggleFavorito(libro) },
                                        onToggleLeido = { viewModel.toggleLeido(libro) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Texto legible de la última sincronización. DateUtils.getRelativeTimeSpanString
// devuelve cadenas como "hace 3 horas", ya traducidas por el sistema al idioma
// del dispositivo, sin necesidad de formateadores ni de la API de java.time.
private fun textoUltimaSincronizacion(instante: Long?): String =
    if (instante == null || instante == 0L) "Sin sincronizar"
    else "Actualizado ${DateUtils.getRelativeTimeSpanString(instante)}"

@Composable
private fun BannerSinConexion() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "Sin conexión. Se muestran los datos guardados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EstadoVacio(busqueda: String, onAnyadir: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (busqueda.isBlank()) "Tu biblioteca está vacía"
            else "Ningún libro coincide con \"$busqueda\"",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Puedes buscarlo en Open Library y añadirlo a tu biblioteca.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAnyadir) { Text("Añadir libro") }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaListadoPreview() {
    PantallaListado()
}