package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.CuotaDao
import com.example.bsprestagil.data.database.entities.CuotaEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CuotaRepository(
    private val cuotaDao: CuotaDao
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: CuotaDao get() = cuotaDao
    
    // Observar cuotas por préstamo
    fun getCuotasByPrestamoId(prestamoId: String): Flow<List<CuotaEntity>> {
        return cuotaDao.getCuotasByPrestamoId(prestamoId)
    }
    
    // Observar una cuota específica
    fun getCuotaById(cuotaId: String): Flow<CuotaEntity?> {
        return cuotaDao.getCuotaById(cuotaId)
    }
    
    // Observar cuotas por estado
    fun getCuotasByEstado(prestamoId: String, estado: String): Flow<List<CuotaEntity>> {
        return cuotaDao.getCuotasByEstado(prestamoId, estado)
    }
    
    // Observar cuotas vencidas
    fun getCuotasVencidas(): Flow<List<CuotaEntity>> {
        return cuotaDao.getCuotasVencidas()
    }
    
    // Obtener próxima cuota pendiente
    suspend fun getProximaCuotaPendiente(prestamoId: String): CuotaEntity? {
        return cuotaDao.getProximaCuotaPendiente(prestamoId)
    }
    
    // Insertar cuota
    suspend fun insertCuota(cuota: CuotaEntity): String {
        val cuotaWithId = if (cuota.id.isEmpty()) {
            cuota.copy(
                id = UUID.randomUUID().toString(),
                pendingSync = true,
                lastSyncTime = 0L
            )
        } else {
            cuota.copy(pendingSync = true)
        }
        
        cuotaDao.insertCuota(cuotaWithId)
        return cuotaWithId.id
    }
    
    // Insertar múltiples cuotas (para cronograma)
    suspend fun insertCuotas(cuotas: List<CuotaEntity>) {
        val cuotasWithIds = cuotas.map { cuota ->
            if (cuota.id.isEmpty()) {
                cuota.copy(
                    id = UUID.randomUUID().toString(),
                    pendingSync = true,
                    lastSyncTime = 0L
                )
            } else {
                cuota.copy(pendingSync = true)
            }
        }
        cuotaDao.insertCuotas(cuotasWithIds)
    }
    
    // Actualizar cuota
    suspend fun updateCuota(cuota: CuotaEntity) {
        cuotaDao.updateCuota(
            cuota.copy(
                pendingSync = true,
                lastSyncTime = System.currentTimeMillis()
            )
        )
    }
    
    // Eliminar cuota
    suspend fun deleteCuota(cuota: CuotaEntity) {
        cuotaDao.deleteCuota(cuota)
    }
    
    // Eliminar cuotas por préstamo
    suspend fun deleteCuotasByPrestamoId(prestamoId: String) {
        cuotaDao.deleteCuotasByPrestamoId(prestamoId)
    }
    
    // Obtener cuotas pendientes de sincronizar
    suspend fun getCuotasPendingSync(): List<CuotaEntity> {
        return cuotaDao.getCuotasPendingSync()
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(cuotaId: String): Int {
        return cuotaDao.markAsSynced(cuotaId, System.currentTimeMillis())
    }
    
    // Contar cuotas pagadas
    suspend fun countCuotasPagadas(prestamoId: String): Int {
        return cuotaDao.countCuotasPagadas(prestamoId)
    }
}

