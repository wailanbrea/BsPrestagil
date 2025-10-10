package com.example.bsprestagil.data.models

data class Prestamo(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val montoOriginal: Double = 0.0, // Capital inicial prestado
    val capitalPendiente: Double = 0.0, // Capital que aún debe (se reduce con pagos)
    val tasaInteresPorPeriodo: Double = 0.0, // Ej: 20% mensual, 5% semanal
    val frecuenciaPago: FrecuenciaPago = FrecuenciaPago.MENSUAL, // Define el período de la tasa
    val garantiaId: String? = null,
    val fechaInicio: Long = System.currentTimeMillis(),
    val ultimaFechaPago: Long = System.currentTimeMillis(), // Para calcular interés acumulado
    val estado: EstadoPrestamo = EstadoPrestamo.ACTIVO,
    val totalInteresesPagados: Double = 0.0, // Histórico de intereses pagados
    val totalCapitalPagado: Double = 0.0, // Histórico de capital pagado
    val totalMorasPagadas: Double = 0.0, // Histórico de moras pagadas
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

