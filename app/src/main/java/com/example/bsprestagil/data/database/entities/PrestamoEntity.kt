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
    val capitalPendiente: Double,
    val tasaInteresPorPeriodo: Double,
    val frecuenciaPago: String, // DIARIO, QUINCENAL, MENSUAL
    val tipoAmortizacion: String, // FRANCES, ALEMAN
    val numeroCuotas: Int, // Número de cuotas pactadas
    val montoCuotaFija: Double, // Cuota fija (Francés) o primera cuota (Alemán)
    val cuotasPagadas: Int, // Cuántas cuotas se han pagado completamente
    val garantiaId: String?,
    val fechaInicio: Long,
    val ultimaFechaPago: Long,
    val estado: String, // ACTIVO, ATRASADO, COMPLETADO, CANCELADO
    val totalInteresesPagados: Double,
    val totalCapitalPagado: Double,
    val totalMorasPagadas: Double,
    val notas: String,
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

