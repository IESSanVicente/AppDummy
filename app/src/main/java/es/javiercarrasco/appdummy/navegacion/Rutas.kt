package es.javiercarrasco.appdummy.navegacion

import kotlinx.serialization.Serializable

// ─── navegacion/Rutas.kt ─────────────────────────────────────────────────────────────────────────

@Serializable
data object Inicio
@Serializable
data object Listado
@Serializable
data class Detalle(val id: Int)
@Serializable
data object Favoritos
@Serializable
data object Leidos