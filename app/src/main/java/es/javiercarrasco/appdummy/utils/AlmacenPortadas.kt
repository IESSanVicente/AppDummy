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

    /**
     * Devuelve el fichero destino de un libro. El identificador de Open Library
     * tiene forma "/works/OL20933765W", así que se toma el último segmento para
     * obtener un nombre de fichero válido.
     */
    fun ficheroPara(context: Context, libroId: String): File {
        val carpeta = File(context.filesDir, CARPETA).apply { mkdirs() }
        return File(carpeta, "${libroId.substringAfterLast('/')}.jpg")
    }

    /**
     * Copia el contenido de [origen] al fichero privado del libro.
     *
     * Es imprescindible copiar y no limitarse a guardar la Uri: las Uri que
     * devuelve el selector de medios conceden un permiso de lectura **temporal**
     * que se pierde al reiniciar el dispositivo. Si se persistiera la Uri en
     * Room, la portada dejaría de verse en la siguiente sesión.
     *
     * @return el fichero creado, o null si la copia falla.
     */
    suspend fun copiarDesdeUri(
        context: Context,
        origen: Uri,
        libroId: String
    ): File? = withContext(Dispatchers.IO) {   // E/S de disco fuera del hilo principal
        runCatching {
            val destino = ficheroPara(context, libroId)
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@runCatching null
            destino
        }.getOrNull()
    }

    /** Elimina la portada local de un libro, si existe. */
    fun borrar(context: Context, libroId: String): Boolean =
        ficheroPara(context, libroId).let { if (it.exists()) it.delete() else false }
}