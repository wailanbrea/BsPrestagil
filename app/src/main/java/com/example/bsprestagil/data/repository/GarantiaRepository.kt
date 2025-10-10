package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.GarantiaDao
import com.example.bsprestagil.data.database.entities.GarantiaEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GarantiaRepository(
    private val garantiaDao: GarantiaDao
) {
    
    // Observar todas las garantías
    fun getAllGarantias(): Flow<List<GarantiaEntity>> {
        return garantiaDao.getAllGarantias()
    }
    
    // Observar una garantía específica
    fun getGarantiaById(garantiaId: String): Flow<GarantiaEntity?> {
        return garantiaDao.getGarantiaById(garantiaId)
    }
    
    // Observar garantías por estado
    fun getGarantiasByEstado(estado: String): Flow<List<GarantiaEntity>> {
        return garantiaDao.getGarantiasByEstado(estado)
    }
    
    // Insertar nueva garantía
    suspend fun insertGarantia(garantia: GarantiaEntity): String {
        val garantiaWithId = if (garantia.id.isEmpty()) {
            garantia.copy(
                id = UUID.randomUUID().toString(),
                pendingSync = true,
                lastSyncTime = 0L
            )
        } else {
            garantia.copy(pendingSync = true)
        }
        
        garantiaDao.insertGarantia(garantiaWithId)
        return garantiaWithId.id
    }
    
    // Actualizar garantía
    suspend fun updateGarantia(garantia: GarantiaEntity) {
        garantiaDao.updateGarantia(
            garantia.copy(
                pendingSync = true,
                lastSyncTime = System.currentTimeMillis()
            )
        )
    }
    
    // Eliminar garantía
    suspend fun deleteGarantia(garantia: GarantiaEntity) {
        garantiaDao.deleteGarantia(garantia)
    }
    
    // Obtener garantías pendientes de sincronizar
    suspend fun getGarantiasPendingSync(): List<GarantiaEntity> {
        return garantiaDao.getGarantiasPendingSync()
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(garantiaId: String) {
        garantiaDao.markAsSynced(garantiaId, System.currentTimeMillis())
    }
}

