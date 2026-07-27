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