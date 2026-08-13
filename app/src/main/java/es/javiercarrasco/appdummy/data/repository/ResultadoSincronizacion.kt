package es.javiercarrasco.appdummy.data.repository

// ─── data/repository/ResultadoSincronizacion.kt ──────────────────────────────────────────────────
// Resumen de lo ocurrido durante una sincronización. NO contiene libros: los datos
// viajan a la UI por el Flow de Room, no por este objeto.
data class ResultadoSincronizacion(
    val actualizados: Int = 0,   // libros cuyos datos ha devuelto la API
    val sinCambios: Int = 0,     // libros comprobados sin resultado en la API
    val error: String? = null    // mensaje si la sincronización se interrumpió
) {
    val comprobados: Int get() = actualizados + sinCambios
    val haFallado: Boolean get() = error != null
}