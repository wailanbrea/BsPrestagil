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
    val cobradorId: String? = null, // ID del cobrador asignado
    val cobradorNombre: String? = null, // Nombre del cobrador para mostrar
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
    
    // NUEVO: Multi-tenant - Empresa a la que pertenece
    val adminId: String, // UID del ADMIN dueño
    
    // Campos de extensión
    val montoExtendido: Double = 0.0,        // Monto total de extensiones
    val montoTotal: Double = montoOriginal,   // Monto original + extensiones
    val fechaUltimaExtension: Long? = null,   // Fecha de la última extensión
    val razonUltimaExtension: String? = null, // Razón de la última extensión
    val numeroExtensiones: Int = 0,           // Número de extensiones realizadas
    val esExtension: Boolean = false,         // Si este préstamo es una extensión
    val prestamoPadreId: String? = null,      // ID del préstamo original (si es extensión)
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

