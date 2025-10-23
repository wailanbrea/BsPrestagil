package com.example.bsprestagil.data.models

data class Prestamo(
    val id: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val cobradorId: String? = null, // ID del cobrador asignado
    val cobradorNombre: String? = null, // Nombre del cobrador
    val montoOriginal: Double = 0.0, // Capital inicial prestado
    val capitalPendiente: Double = 0.0, // Capital que aún debe (se reduce con pagos)
    val tasaInteresPorPeriodo: Double = 0.0, // Ej: 20% mensual, 10% quincenal
    val frecuenciaPago: FrecuenciaPago = FrecuenciaPago.MENSUAL, // Define el período de la tasa
    val tipoAmortizacion: TipoAmortizacion = TipoAmortizacion.FRANCES, // Sistema de amortización
    val numeroCuotas: Int = 0, // Número de cuotas pactadas (ej: 12 meses)
    val montoCuotaFija: Double = 0.0, // Cuota fija (Francés) o primera cuota (Alemán)
    val cuotasPagadas: Int = 0, // Cuántas cuotas se han completado
    val garantiaId: String? = null,
    val fechaInicio: Long = System.currentTimeMillis(),
    val ultimaFechaPago: Long = System.currentTimeMillis(), // Para calcular interés acumulado
    val estado: EstadoPrestamo = EstadoPrestamo.ACTIVO,
    val totalInteresesPagados: Double = 0.0, // Histórico de intereses pagados
    val totalCapitalPagado: Double = 0.0, // Histórico de capital pagado
    val totalMorasPagadas: Double = 0.0, // Histórico de moras pagadas
    val notas: String = "",
    
    // Campos de extensión
    val montoExtendido: Double = 0.0,        // Monto total de extensiones
    val montoTotal: Double = montoOriginal,   // Monto original + extensiones
    val fechaUltimaExtension: Long? = null,   // Fecha de la última extensión
    val razonUltimaExtension: String? = null, // Razón de la última extensión
    val numeroExtensiones: Int = 0,           // Número de extensiones realizadas
    val esExtension: Boolean = false,         // Si este préstamo es una extensión
    val prestamoPadreId: String? = null       // ID del préstamo original (si es extensión)
)

enum class FrecuenciaPago {
    DIARIO,
    QUINCENAL,
    MENSUAL
}

enum class TipoAmortizacion {
    FRANCES,  // Cuota fija
    ALEMAN    // Capital fijo (cuota decreciente)
}

enum class EstadoPrestamo {
    ACTIVO,
    ATRASADO,
    COMPLETADO,
    CANCELADO
}

