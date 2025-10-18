package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String = "",
    val rol: String, // ADMIN, SUPERVISOR, COBRADOR
    val activo: Boolean = true,
    val fechaCreacion: Long,
    
    // NUEVO: Multi-tenant - Empresa a la que pertenece
    val adminId: String, // UID del ADMIN dueño (si rol=ADMIN, adminId=id)
    
    // Sistema de comisiones
    val porcentajeComision: Float = 3.0f, // Porcentaje de comisión (ej: 3%)
    val totalComisionesGeneradas: Double = 0.0, // Total histórico generado
    val totalComisionesPagadas: Double = 0.0, // Total histórico pagado
    val ultimoPagoComision: Long = 0L, // Fecha del último pago de comisión
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

