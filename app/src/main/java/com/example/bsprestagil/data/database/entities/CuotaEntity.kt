package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cuotas",
    foreignKeys = [
        ForeignKey(
            entity = PrestamoEntity::class,
            parentColumns = ["id"],
            childColumns = ["prestamoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("prestamoId"), Index("numeroCuota")]
)
data class CuotaEntity(
    @PrimaryKey
    val id: String,
    val prestamoId: String,
    val numeroCuota: Int,
    val fechaVencimiento: Long,
    val montoCuotaMinimo: Double,
    val capitalPendienteAlInicio: Double,
    val montoPagado: Double,
    val montoAInteres: Double,
    val montoACapital: Double,
    val montoMora: Double,
    val fechaPago: Long?,
    val estado: String, // PENDIENTE, PAGADA, PARCIAL, VENCIDA
    val notas: String,
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

