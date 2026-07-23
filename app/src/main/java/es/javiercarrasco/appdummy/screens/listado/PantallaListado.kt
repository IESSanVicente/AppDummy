package es.javiercarrasco.appdummy.screens.listado

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import es.javiercarrasco.appdummy.R
import es.javiercarrasco.appdummy.data.model.Libro
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ─── screens/listado/PantallaListado.kt ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListado(
    viewModel: LibrosViewModel = viewModel(factory = LibrosViewModel.Factory),
    onNavegaADetalle: (Int) -> Unit = {}  // callback de navegación (se conecta al NavHost en T3)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var snackbarJob by remember { mutableStateOf<Job?>(null) }

    // collectAsStateWithLifecycle: activo en foreground, pausado en background
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val autorSeleccionado by viewModel.autorSeleccionado.collectAsStateWithLifecycle()
    val autores by viewModel.autores.collectAsStateWithLifecycle()

    // LaunchedEffect con Unit: se lanza una única vez al montar el Composable
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

                is LibrosEvento.NavegarADetalle -> { /* navController.navigate(ruta tipada — ver T3 )*/
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AppDummy") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Barra de búsqueda
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::actualizarBusqueda,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar libros...") },
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

            // Chips de autores
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

            // when exhaustivo sobre la sealed class
            when (val estado = uiState) {
                is LibrosUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is LibrosUiState.Exito -> {
                    val librosFiltrados = estado.libros.filter {
                        val coincideBusqueda =
                            busqueda.isBlank() || it.titulo.contains(busqueda, ignoreCase = true)
                        val coincideGenero =
                            autorSeleccionado == "Todos" || it.autor == autorSeleccionado
                        coincideBusqueda && coincideGenero
                    }
                    if (librosFiltrados.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Sin resultados para \"$busqueda\"",
                                    style = MaterialTheme.typography.bodyLarge
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
                                    onToggleLeido = { viewModel.toggleLeido(libro) },
                                    onToggleFavorito = { viewModel.toggleFavorito(libro) }
                                )
                            }
                        }
                    }
                }

                is LibrosUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = estado.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::cargarLibros) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemLibro(
    libro: Libro,
    onClickItem: () -> Unit = {},
    onToggleLeido: (Int) -> Unit = {},
    onToggleFavorito: (Int) -> Unit = {}
) {
    Card(onClick = onClickItem, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(libro.titulo, style = MaterialTheme.typography.titleSmall)
            if (Patterns.WEB_URL.matcher(libro.cover).matches())
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(libro.cover)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Portada de ${libro.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(200.dp)
                        .align(Alignment.CenterHorizontally)
                )
            else // Si no es una URL válida, se muestra una imagen por defecto
                AsyncImage(
                    model = R.drawable.nocover,
                    contentDescription = "Portada de ${libro.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(200.dp)
                )

            Text(
                text = "${libro.autor} • ${libro.year}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f),
                    onClick = { onToggleLeido(libro.id) }) {
                    Icon(
                        imageVector = if (libro.leido) Icons.Default.BookmarkAdded
                        else Icons.Default.BookmarkBorder,
                        contentDescription = if (libro.leido) "Quitar leído" else "Marcar como leído",
                        tint = if (libro.leido) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f),
                    onClick = { onToggleFavorito(libro.id) }) {
                    Icon(
                        imageVector = if (libro.esFavorito) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = if (libro.esFavorito) "Quitar favorito" else "Añadir favorito",
                        tint = if (libro.esFavorito) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaListadoPreview() {
    PantallaListado()
}