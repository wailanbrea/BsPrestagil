package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.CuotaDao
import com.example.bsprestagil.data.database.entities.CuotaEntity
import com.example.bsprestagil.utils.AuthUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.UUID

class CuotaRepository(
    private val cuotaDao: CuotaDao
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: CuotaDao get() = cuotaDao
    
    // NUEVO: Obtener adminId del usuario actual
    private fun getAdminId(): String = runBlocking { AuthUtils.getCurrentAdminId() }
    
    // Observar cuotas por préstamo
    fun getCuotasByPrestamoId(prestamoId: String): Flow<List<CuotaEntity>> {
        val adminId = getAdminId()
        return cuotaDao.getCuotasByPrestamoId(prestamoId, adminId)
    }
    
    // Observar una cuota específica
    fun getCuotaById(cuotaId: String): Flow<CuotaEntity?> {
        val adminId = getAdminId()
        return cuotaDao.getCuotaById(cuotaId, adminId)
    }
    
    // Observar cuotas por estado
    fun getCuotasByEstado(prestamoId: String, estado: String): Flow<List<CuotaEntity>> {
        val adminId = getAdminId()
        return cuotaDao.getCuotasByEstado(prestamoId, estado, adminId)
    }
    
    // Observar cuotas vencidas
    fun getCuotasVencidas(): Flow<List<CuotaEntity>> {
        val adminId = getAdminId()
        return cuotaDao.getCuotasVencidas(adminId)
    }
    
    // Obtener próxima cuota pendiente
    suspend fun getProximaCuotaPendiente(prestamoId: String): CuotaEntity? {
        val adminId = AuthUtils.getCurrentAdminId()
        return cuotaDao.getProximaCuotaPendiente(prestamoId, adminId)
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
        val adminId = AuthUtils.getCurrentAdminId()
        cuotaDao.deleteCuotasByPrestamoId(prestamoId, adminId)
    }
    
    // Obtener cuotas pendientes de sincronizar
    suspend fun getCuotasPendingSync(): List<CuotaEntity> {
        val adminId = AuthUtils.getCurrentAdminId()
        return cuotaDao.getCuotasPendingSync(adminId)
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(cuotaId: String): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return cuotaDao.markAsSynced(cuotaId, adminId, System.currentTimeMillis())
    }
    
    // Contar cuotas pagadas
    suspend fun countCuotasPagadas(prestamoId: String): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return cuotaDao.countCuotasPagadas(prestamoId, adminId)
    }
}

