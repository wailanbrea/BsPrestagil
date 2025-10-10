package com.example.bsprestagil.data.models

data class Cliente(
    val id: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val email: String = "",
    val fotoUrl: String = "",
    val referencias: List<Referencia> = emptyList(),
    val fechaRegistro: Long = System.currentTimeMillis(),
    val prestamosActivos: Int = 0,
    val historialPagos: EstadoPagos = EstadoPagos.AL_DIA
)

data class Referencia(
    val nombre: String = "",
    val telefono: String = "",
    val relacion: String = ""
)

enum class EstadoPagos {
    AL_DIA,
    ATRASADO,
    MOROSO
}

