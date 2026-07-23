package es.javiercarrasco.appdummy.navegacion

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import es.javiercarrasco.appdummy.screens.detalle.PantallaDetalle
import es.javiercarrasco.appdummy.screens.listado.PantallaListado

// ─── navegacion/AppNavigation.kt ─────────────────────────────────────────────────────────────────
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            AppDummyBottomBar(navController = navController)
        }
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
                    }
                )
            }

            composable<Detalle> { backStackEntry ->
                val ruta: Detalle = backStackEntry.toRoute<Detalle>()
                PantallaDetalle(
                    libroId = ruta.id,
                    onVolver = { navController.navigateUp() }
                )
            }

//            composable<Favoritos> {
//                PantallaFavoritos(
//                    onNavegaADetalle = { id ->
//                        navController.navigate(Detalle(id = id))
//                    }
//                )
//            }
        }
    }
}