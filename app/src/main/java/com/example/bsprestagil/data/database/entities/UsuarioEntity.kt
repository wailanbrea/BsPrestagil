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
    val rol: String, // ADMIN, COBRADOR, SUPERVISOR
    val activo: Boolean = true,
    val fechaCreacion: Long,
    
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

