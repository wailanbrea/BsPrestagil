package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notificaciones")
data class NotificacionEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String, // PAGO_VENCIDO, PAGO_PROXIMO, PAGO_RECIBIDO, NUEVO_CLIENTE
    val fecha: Long,
    val leida: Boolean = false,
    val prestamoId: String? = null, // Referencia opcional al préstamo
    val clienteId: String? = null, // Referencia opcional al cliente
    val pagoId: String? = null, // Referencia opcional al pago
    val cobradorId: String? = null // Cobrador al que va dirigida la notificación
)

