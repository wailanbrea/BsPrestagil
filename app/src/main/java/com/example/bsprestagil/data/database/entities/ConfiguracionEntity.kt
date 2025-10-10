package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion")
data class ConfiguracionEntity(
    @PrimaryKey
    val id: Int = 1, // Solo habrá un registro de configuración
    val tasaInteresBase: Double,
    val tasaMoraBase: Double,
    val nombreNegocio: String,
    val telefonoNegocio: String,
    val direccionNegocio: String,
    val logoUrl: String,
    val mensajeRecibo: String,
    val notificacionesActivas: Boolean,
    val envioWhatsApp: Boolean,
    val envioSMS: Boolean,
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

