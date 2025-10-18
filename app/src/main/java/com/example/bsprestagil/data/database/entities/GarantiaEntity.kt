package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.bsprestagil.data.database.converters.Converters

@Entity(tableName = "garantias")
@TypeConverters(Converters::class)
data class GarantiaEntity(
    @PrimaryKey
    val id: String,
    val tipo: String, // VEHICULO, ELECTRODOMESTICO, ELECTRONICO, JOYA, MUEBLE, OTRO
    val descripcion: String,
    val valorEstimado: Double,
    val fotosUrls: List<String>,
    val estado: String, // RETENIDA, DEVUELTA, EJECUTADA
    val fechaRegistro: Long,
    val notas: String,
    
    // NUEVO: Multi-tenant - Empresa a la que pertenece
    val adminId: String, // UID del ADMIN dueño
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

