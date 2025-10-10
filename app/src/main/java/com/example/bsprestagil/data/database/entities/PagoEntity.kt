package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    foreignKeys = [
        ForeignKey(
            entity = PrestamoEntity::class,
            parentColumns = ["id"],
            childColumns = ["prestamoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("prestamoId"), Index("clienteId")]
)
data class PagoEntity(
    @PrimaryKey
    val id: String,
    val prestamoId: String,
    val clienteId: String,
    val clienteNombre: String,
    val montoPagado: Double,
    val montoAInteres: Double,
    val montoACapital: Double,
    val montoMora: Double,
    val fechaPago: Long,
    val diasTranscurridos: Int,
    val interesCalculado: Double,
    val capitalPendienteAntes: Double,
    val capitalPendienteDespues: Double,
    val metodoPago: String, // EFECTIVO, TRANSFERENCIA, TARJETA, OTRO
    val recibidoPor: String,
    val notas: String,
    val reciboUrl: String,
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

