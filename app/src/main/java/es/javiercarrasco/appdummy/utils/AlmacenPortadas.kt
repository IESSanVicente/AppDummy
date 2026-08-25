package es.javiercarrasco.appdummy.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// ─── utils/AlmacenPortadas.kt ────────────────────────────────────────────────────────────────────

object AlmacenPortadas {

    private const val CARPETA = "portadas"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    /**
     * Los identificadores de Open Library son rutas ("/works/OL45804W"),
     * y la barra no es válida en un nombre de fichero: nos quedamos con el último segmento.
     */
    private fun prefijo(libroId: String): String = libroId.substringAfterLast('/')

    /**
     * Devuelve un fichero NUEVO en cada llamada.
     *
     * La marca de tiempo no es un capricho: de ella depende que, al rehacer la
     * foto, cambie la ruta almacenada en Room. Si el nombre fuese fijo, la
     * entidad quedaría idéntica tras la segunda captura, el StateFlow
     * descartaría el valor por ser `equals` al anterior y la pantalla no se
     * refrescaría (véase el apartado 7.5).
     */
    fun nuevoFicheroPara(context: Context, libroId: String): File =
        File(carpeta(context), "${prefijo(libroId)}-${System.currentTimeMillis()}.jpg")

    /**
     * Copia el contenido de una Uri ajena (la que devuelve el selector de
     * medios) a un fichero propio recién creado.
     */
    suspend fun copiarDesdeUri(
        context: Context,
        origen: Uri,
        libroId: String
    ): File? = withContext(Dispatchers.IO) {
        runCatching {
            val destino = nuevoFicheroPara(context, libroId)
            context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@runCatching null
            destino
        }.getOrNull()
    }

    /**
     * Borra las portadas anteriores del libro, conservando la indicada.
     * Con `conservar = null` elimina todas: es lo que necesita "quitar portada".
     */
    fun limpiarAnteriores(context: Context, libroId: String, conservar: File? = null) {
        carpeta(context).listFiles()
            ?.filter { it.name.startsWith("${prefijo(libroId)}-") && it != conservar }
            ?.forEach { it.delete() }
    }
}