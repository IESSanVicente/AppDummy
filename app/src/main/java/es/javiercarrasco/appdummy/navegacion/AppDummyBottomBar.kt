package es.javiercarrasco.appdummy.navegacion

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

// ─── navegacion/AppDummyBottomBar.kt ─────────────────────────────────────────────────────────────
@Composable
fun AppDummyBottomBar(navController: NavController) {
    // currentBackStackEntryAsState: State reactivo del destino actual
    // Cuando el usuario navega, este State cambia y la barra se recompone
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destinoActual = navBackStackEntry?.destination

    // Solo mostrar el BottomBar en las pantallas principales
    // (no en PantallaDetalle, por ejemplo)
    val mostrarBottomBar = itemsNavegacionPrincipal.any { item ->
        destinoActual?.hierarchy?.any { it.hasRoute(item.ruta::class) } == true
    }

    if (mostrarBottomBar) {
        NavigationBar {
            itemsNavegacionPrincipal.forEach { item ->
                // hierarchy: recorre la cadena de destinos desde el actual hasta la raíz
                // Útil con grafos anidados para resaltar el ítem correcto
                val seleccionado = destinoActual?.hierarchy?.any {
                    it.hasRoute(item.ruta::class)
                } == true

                NavigationBarItem(
                    selected = seleccionado,
                    onClick = {
                        navController.navigate(item.ruta) {
                            // popUpTo: vuelve al inicio antes de navegar
                            // evita acumular destinos en la pila al cambiar de pestaña
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true   // guarda el estado de la pantalla anterior
                            }
                            // launchSingleTop: no apila múltiples copias del mismo destino
                            launchSingleTop = true
                            // restoreState: restaura el estado guardado con saveState
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (seleccionado) item.iconoSeleccionado else item.icono,
                            contentDescription = item.etiqueta
                        )
                    },
                    label = { Text(item.etiqueta) }
                )
            }
        }
    }
}