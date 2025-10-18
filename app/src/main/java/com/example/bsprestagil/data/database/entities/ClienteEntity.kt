package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.bsprestagil.data.database.converters.Converters

@Entity(tableName = "clientes")
@TypeConverters(Converters::class)
data class ClienteEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val email: String,
    val fotoUrl: String,
    val referencias: List<ReferenciaEntity>,
    val fechaRegistro: Long,
    val prestamosActivos: Int,
    val historialPagos: String, // AL_DIA, ATRASADO, MOROSO
    
    // NUEVO: Multi-tenant - Empresa a la que pertenece
    val adminId: String, // UID del ADMIN dueño
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

data class ReferenciaEntity(
    val nombre: String = "",
    val telefono: String = "",
    val relacion: String = ""
)

