package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.PrestamoDao
import com.example.bsprestagil.data.database.entities.PrestamoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PrestamoRepository(
    private val prestamoDao: PrestamoDao
) {
    
    // Observar todos los préstamos
    fun getAllPrestamos(): Flow<List<PrestamoEntity>> {
        return prestamoDao.getAllPrestamos()
    }
    
    // Observar un préstamo específico
    fun getPrestamoById(prestamoId: String): Flow<PrestamoEntity?> {
        return prestamoDao.getPrestamoById(prestamoId)
    }
    
    // Observar préstamos por cliente
    fun getPrestamosByClienteId(clienteId: String): Flow<List<PrestamoEntity>> {
        return prestamoDao.getPrestamosByClienteId(clienteId)
    }
    
    // Observar préstamos por estado
    fun getPrestamosByEstado(estado: String): Flow<List<PrestamoEntity>> {
        return prestamoDao.getPrestamosByEstado(estado)
    }
    
    // Insertar nuevo préstamo
    suspend fun insertPrestamo(prestamo: PrestamoEntity): String {
        val prestamoWithId = if (prestamo.id.isEmpty()) {
            prestamo.copy(
                id = UUID.randomUUID().toString(),
                pendingSync = true,
                lastSyncTime = 0L
            )
        } else {
            prestamo.copy(pendingSync = true)
        }
        
        prestamoDao.insertPrestamo(prestamoWithId)
        return prestamoWithId.id
    }
    
    // Actualizar préstamo
    suspend fun updatePrestamo(prestamo: PrestamoEntity) {
        prestamoDao.updatePrestamo(
            prestamo.copy(
                pendingSync = true,
                lastSyncTime = System.currentTimeMillis()
            )
        )
    }
    
    // Eliminar préstamo
    suspend fun deletePrestamo(prestamo: PrestamoEntity) {
        prestamoDao.deletePrestamo(prestamo)
    }
    
    // Obtener préstamos pendientes de sincronizar
    suspend fun getPrestamosPendingSync(): List<PrestamoEntity> {
        return prestamoDao.getPrestamosPendingSync()
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(prestamoId: String) {
        prestamoDao.markAsSynced(prestamoId, System.currentTimeMillis())
    }
    
    // Estadísticas
    suspend fun getPrestamosCountByEstado(estado: String): Int {
        return prestamoDao.getPrestamosCountByEstado(estado)
    }
    
    suspend fun getTotalPrestado(): Double {
        return prestamoDao.getTotalPrestado() ?: 0.0
    }
    
    suspend fun getCarteraVencida(): Double {
        return prestamoDao.getCarteraVencida() ?: 0.0
    }
    
    // Obtener préstamos recientes
    suspend fun getRecentPrestamos(limit: Int = 5): List<PrestamoEntity> {
        return prestamoDao.getRecentPrestamos(limit)
    }
}

