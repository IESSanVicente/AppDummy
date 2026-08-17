package es.javiercarrasco.appdummy.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

// ─── utils/ObservadorConectividad.kt ─────────────────────────────────────────────────────────────
// Traduce los callbacks de ConnectivityManager a un Flow<Boolean>.
// true  → hay una red conectada y VALIDADA (con acceso real a internet)
// false → no hay red, o la hay pero sin salida a internet (portal cautivo)
class ObservadorConectividad(context: Context) {

    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    val hayConexion: Flow<Boolean> = callbackFlow {

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) { trySend(true) }

            override fun onLost(network: Network) { trySend(false) }

            override fun onUnavailable() { trySend(false) }

            // NET_CAPABILITY_VALIDATED: el sistema ha comprobado que esta red tiene
            // salida real a internet. Estar conectado a un wifi NO basta.
            override fun onCapabilitiesChanged(
                network: Network,
                capacidades: NetworkCapabilities
            ) {
                trySend(
                    capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                )
            }
        }

        // Valor inicial: el Flow debe emitir el estado actual sin esperar a que algo
        // cambie, o la UI mostraría "sin conexión" hasta el primer evento del sistema
        trySend(estadoActual())

        connectivityManager.registerDefaultNetworkCallback(callback)

        // awaitClose es OBLIGATORIO en callbackFlow: mantiene viva la corrutina y,
        // al cancelarse el Flow (por ejemplo, cuando el ViewModel muere), ejecuta el
        // bloque para dar de baja el callback y no filtrar memoria.
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .distinctUntilChanged()   // ignora repeticiones del mismo estado
        .conflate()               // si llegan varios cambios seguidos, solo el último

    private fun estadoActual(): Boolean {
        val red = connectivityManager.activeNetwork ?: return false
        val capacidades = connectivityManager.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}