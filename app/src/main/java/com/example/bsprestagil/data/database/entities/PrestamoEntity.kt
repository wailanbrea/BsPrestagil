package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prestamos",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId")]
)
data class PrestamoEntity(
    @PrimaryKey
    val id: String,
    val clienteId: String,
    val clienteNombre: String,
    val montoOriginal: Double,
    val tasaInteres: Double,
    val plazoMeses: Int,
    val frecuenciaPago: String, // DIARIO, SEMANAL, QUINCENAL, MENSUAL
    val garantiaId: String?,
    val fechaInicio: Long,
    val fechaVencimiento: Long,
    val estado: String, // ACTIVO, ATRASADO, COMPLETADO, CANCELADO
    val saldoPendiente: Double,
    val totalAPagar: Double,
    val cuotasPagadas: Int,
    val totalCuotas: Int,
    val notas: String,
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

