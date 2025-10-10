package com.example.bsprestagil.data.models

data class Prestamo(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val montoOriginal: Double = 0.0,
    val tasaInteres: Double = 0.0,
    val plazoMeses: Int = 0,
    val frecuenciaPago: FrecuenciaPago = FrecuenciaPago.MENSUAL,
    val garantiaId: String? = null,
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaVencimiento: Long = 0L,
    val estado: EstadoPrestamo = EstadoPrestamo.ACTIVO,
    val saldoPendiente: Double = 0.0,
    val totalAPagar: Double = 0.0,
    val cuotasPagadas: Int = 0,
    val totalCuotas: Int = 0,
    val notas: String = ""
)

enum class FrecuenciaPago {
    DIARIO,
    SEMANAL,
    QUINCENAL,
    MENSUAL
}

enum class EstadoPrestamo {
    ACTIVO,
    ATRASADO,
    COMPLETADO,
    CANCELADO
}

