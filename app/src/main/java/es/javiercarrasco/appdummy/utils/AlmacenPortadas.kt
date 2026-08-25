package es.javiercarrasco.appdummy.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// ─── utils/AlmacenPortadas.kt ────────────────────────────────────────────────────────────────────

/**
 * Gestiona los ficheros de portada dentro del almacenamiento privado de la app.
 * No necesita permisos: `filesDir` pertenece a la propia aplicación.
 */
object AlmacenPortadas {

    private const val CARPETA = "portadas"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    private fun prefijo(libroId: String): String = libroId.substringAfterLast('/')

    /** Devuelve un fichero NUEVO en cada llamada: el nombre lleva marca de tiempo. */
    fun nuevoFicheroPara(context: Context, libroId: String): File =
        File(carpeta(context), "${prefijo(libroId)}-${System.currentTimeMillis()}.jpg")

    /** Borra las portadas anteriores del libro, conservando la indicada. */
    fun limpiarAnteriores(context: Context, libroId: String, conservar: File? = null) {
        carpeta(context).listFiles()
            ?.filter { it.name.startsWith("${prefijo(libroId)}-") && it != conservar }
            ?.forEach { it.delete() }
    }

    suspend fun copiarDesdeUri(
        context: Context,
        origen: Uri,
        libroId: String
    ): File? = withContext(Dispatchers.IO) {
        runCatching {
            // Viene de la cámara: el fichero ya está escrito en nuestra carpeta.
            if (origen.scheme == "file") {
                val yaEsNuestro = origen.path?.let { File(it) }
                if (yaEsNuestro != null &&
                    yaEsNuestro.parentFile == carpeta(context) &&
                    yaEsNuestro.length() > 0
                ) return@runCatching yaEsNuestro
            }

            val destino = nuevoFicheroPara(context, libroId)
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@runCatching null
            destino
        }.getOrNull()
    }
}