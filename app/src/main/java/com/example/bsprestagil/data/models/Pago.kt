package com.example.bsprestagil.data.models

data class Pago(
    val id: String = "",
    val prestamoId: String = "",
    val cuotaId: String? = null, // Vinculación a la cuota pagada
    val numeroCuota: Int = 0, // Número de cuota para reporte
    val clienteId: String = "",
    val clienteNombre: String = "",
    val montoPagado: Double = 0.0, // Monto total que dio el cliente
    val montoAInteres: Double = 0.0, // Cuánto se aplicó al interés del período
    val montoACapital: Double = 0.0, // Cuánto se aplicó al capital
    val montoMora: Double = 0.0, // Mora cobrada (opcional)
    val fechaPago: Long = System.currentTimeMillis(),
    val diasTranscurridos: Int = 0, // Días desde el último pago
    val interesCalculado: Double = 0.0, // Interés del período calculado
    val capitalPendienteAntes: Double = 0.0, // Capital antes del pago
    val capitalPendienteDespues: Double = 0.0, // Capital después del pago
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    val recibidoPor: String = "",
    val notas: String = "",
    val reciboUrl: String = ""
)

enum class MetodoPago {
    EFECTIVO,
    TRANSFERENCIA,
    TARJETA,
    OTRO
}

