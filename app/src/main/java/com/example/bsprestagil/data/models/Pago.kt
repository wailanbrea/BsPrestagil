package com.example.bsprestagil.data.models

data class Pago(
    val id: String = "",
    val prestamoId: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val monto: Double = 0.0,
    val montoCuota: Double = 0.0,
    val montoMora: Double = 0.0,
    val fechaPago: Long = System.currentTimeMillis(),
    val fechaVencimiento: Long = 0L,
    val numeroCuota: Int = 0,
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

