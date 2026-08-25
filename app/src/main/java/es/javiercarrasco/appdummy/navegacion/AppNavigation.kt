package es.javiercarrasco.appdummy.navegacion

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import es.javiercarrasco.appdummy.screens.camara.PantallaCamara
import es.javiercarrasco.appdummy.screens.detalle.DetalleViewModel
import es.javiercarrasco.appdummy.screens.detalle.PantallaDetalle
import es.javiercarrasco.appdummy.screens.favoritos.PantallaFavoritos
import es.javiercarrasco.appdummy.screens.listado.PantallaListado
import es.javiercarrasco.appdummy.screens.nuevo.PantallaNuevoLibro

// Constante para la clave del resultado de la portada capturada en la pantalla de cámara.
private const val CLAVE_PORTADA = "portadaCapturada"

// ─── navegacion/AppNavigation.kt ─────────────────────────────────────────────────────────────────
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = { AppDummyBottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Listado,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable<Listado> {
                PantallaListado(    // <- Composable de la pantalla listado.
                    onNavegaADetalle = { id ->
                        navController.navigate(Detalle(id = id))    // <- Ruta de Navegación a la pantalla detalle con el id del libro.
                    },
                    // El FAB deja de recargar la lista estática y navega al formulario
                    onNavegaANuevoLibro = { navController.navigate(NuevoLibro) }
                )
            }

            composable<NuevoLibro> {
                PantallaNuevoLibro(
                    // Al guardar se vuelve al listado; Room notificará el cambio automáticamente
                    onGuardado = { navController.navigateUp() },
                    onCancelar = { navController.navigateUp() }
                )
            }

            composable<Detalle> { backStackEntry ->
                val ruta: Detalle = backStackEntry.toRoute<Detalle>()

                // Recupera la Uri de la portada capturada desde la pantalla de cámara, si existe.
                val uriCapturada by backStackEntry.savedStateHandle
                    .getStateFlow<Uri?>(CLAVE_PORTADA, null)
                    .collectAsStateWithLifecycle()

                if (uriCapturada != null) {
                    val viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.factoryConId(ruta.id))

                    // Si hay una Uri capturada, se asigna como portada local y se consume el resultado.
                    LaunchedEffect(uriCapturada) {
                        uriCapturada?.let { uri ->
                            viewModel.asignarPortadaLocal(uri)              // ← misma función que la galería
                            backStackEntry.savedStateHandle[CLAVE_PORTADA] = null   // consumir el resultado
                        }
                    }
                }

                PantallaDetalle(
                    libroId = ruta.id,
                    onVolver = { navController.navigateUp() },
                    onAbrirCamara = { libroId ->
                        navController.navigate(Camara(libroId = libroId))   // ← T7 Navega a la pantalla de cámara, pasando el id del libro
                    }
                )
            }

            composable<Favoritos> {
                PantallaFavoritos(
                    onNavegaADetalle = { id ->
                        navController.navigate(Detalle(id = id))
                    }
                )
            }

            composable<Camara> { backStackEntry ->
                val ruta: Camara = backStackEntry.toRoute<Camara>()
                PantallaCamara(
                    libroId = ruta.libroId,
                    onFotoCapturada = { uri ->
                        // Cuando se captura una foto, se guarda la Uri en el savedStateHandle
                        // de la entrada anterior del back stack (PantallaDetalle).
                        navController.previousBackStackEntry
                            ?.savedStateHandle?.set(CLAVE_PORTADA, uri)   // ← la Uri viaja

                        navController.navigateUp()
                    },
                    onVolver = { navController.navigateUp() }
                )
            }
        }
    }
}