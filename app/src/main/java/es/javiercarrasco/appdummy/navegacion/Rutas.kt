package es.javiercarrasco.appdummy.navegacion

import kotlinx.serialization.Serializable

// ─── navegacion/Rutas.kt ─────────────────────────────────────────────────────────────────────────

@Serializable
data object Inicio
@Serializable
data object Listado
@Serializable
data class Detalle(val id: String)
@Serializable
data object Favoritos
@Serializable
data object NuevoLibro        // ← ruta de T5

// ─── NUEVO en T7 ─────────────────────────────────────────────────────────────
// La cámara necesita saber a qué libro pertenece la portada que va a capturar:
// el id viaja como argumento tipado de la ruta (T3).
@Serializable data class Camara(val libroId: String)
@Serializable data object Reproductor      // audio y vídeo de muestra