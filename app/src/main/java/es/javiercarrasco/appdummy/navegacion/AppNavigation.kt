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

/** Clave del resultado que la pantalla de cámara deja para la de detalle. */
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

                // El ViewModel se crea aquí, y no como parámetro por defecto de
                // PantallaDetalle, porque el efecto de más abajo necesita invocarlo.
                val viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.factoryConId(ruta.id))

                // El resultado que dejó PantallaCamara, si lo hay.
                val uriCapturada by backStackEntry.savedStateHandle
                    .getStateFlow<Uri?>(CLAVE_PORTADA, null)
                    .collectAsStateWithLifecycle()

                LaunchedEffect(uriCapturada) {
                    uriCapturada?.let { uri ->
                        viewModel.asignarPortadaCapturada(uri)
                        // Consumir el resultado: sin esto se volvería a aplicar en cada
                        // recomposición o al girar el dispositivo.
                        backStackEntry.savedStateHandle[CLAVE_PORTADA] = null
                    }
                }

                PantallaDetalle(
                    libroId = ruta.id,
                    viewModel = viewModel,
                    onVolver = { navController.navigateUp() },
                    onAbrirCamara = { libroId -> navController.navigate(Camara(libroId = libroId)) }
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
                        // La foto ya está escrita. Dejamos su Uri en la entrada anterior
                        // de la pila —la de Detalle— y volvemos.
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(CLAVE_PORTADA, uri)
                        navController.navigateUp()
                    },
                    onVolver = { navController.navigateUp() }
                )
            }
        }
    }
}