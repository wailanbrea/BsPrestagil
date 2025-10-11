package com.example.bsprestagil.data.models

data class Cuota(
    val id: String = "",
    val prestamoId: String = "",
    val numeroCuota: Int = 0,
    val fechaVencimiento: Long = 0L,
    val montoCuotaMinimo: Double = 0.0, // Mínimo a pagar (= interés del período)
    val capitalPendienteAlInicio: Double = 0.0, // Capital al inicio de esta cuota
    val montoPagado: Double = 0.0, // Cuánto pagó realmente
    val montoAInteres: Double = 0.0, // Cuánto fue a interés
    val montoACapital: Double = 0.0, // Cuánto fue a capital
    val montoMora: Double = 0.0, // Mora cobrada
    val fechaPago: Long? = null, // Cuándo pagó (null si no ha pagado)
    val estado: EstadoCuota = EstadoCuota.PENDIENTE,
    val notas: String = ""
)

enum class EstadoCuota {
    PENDIENTE,    // No ha vencido y no se ha pagado
    PAGADA,       // Pagada completamente
    PARCIAL,      // Pagada parcialmente (cubrió solo interés)
    VENCIDA,      // Pasó la fecha y no se ha pagado
    CANCELADA     // Cancelada por abono extraordinario (Sistema Francés)
}

