package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.PagoDao
import com.example.bsprestagil.data.database.entities.PagoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PagoRepository(
    private val pagoDao: PagoDao
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: PagoDao get() = pagoDao
    
    // Observar todos los pagos
    fun getAllPagos(): Flow<List<PagoEntity>> {
        return pagoDao.getAllPagos()
    }
    
    // Observar un pago específico
    fun getPagoById(pagoId: String): Flow<PagoEntity?> {
        return pagoDao.getPagoById(pagoId)
    }
    
    // Observar pagos por préstamo
    fun getPagosByPrestamoId(prestamoId: String): Flow<List<PagoEntity>> {
        return pagoDao.getPagosByPrestamoId(prestamoId)
    }
    
    // Observar pagos por cliente
    fun getPagosByClienteId(clienteId: String): Flow<List<PagoEntity>> {
        return pagoDao.getPagosByClienteId(clienteId)
    }
    
    // Observar pagos por rango de fechas
    fun getPagosByDateRange(startDate: Long, endDate: Long): Flow<List<PagoEntity>> {
        return pagoDao.getPagosByDateRange(startDate, endDate)
    }
    
    // Insertar nuevo pago
    suspend fun insertPago(pago: PagoEntity): String {
        val pagoWithId = if (pago.id.isEmpty()) {
            pago.copy(
                id = UUID.randomUUID().toString(),
                pendingSync = true,
                lastSyncTime = 0L
            )
        } else {
            pago.copy(pendingSync = true)
        }
        
        pagoDao.insertPago(pagoWithId)
        return pagoWithId.id
    }
    
    // Actualizar pago
    suspend fun updatePago(pago: PagoEntity) {
        pagoDao.updatePago(
            pago.copy(
                pendingSync = true,
                lastSyncTime = System.currentTimeMillis()
            )
        )
    }
    
    // Eliminar pago
    suspend fun deletePago(pago: PagoEntity) {
        pagoDao.deletePago(pago)
    }
    
    // Obtener pagos pendientes de sincronizar
    suspend fun getPagosPendingSync(): List<PagoEntity> {
        return pagoDao.getPagosPendingSync()
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(pagoId: String): Int {
        return pagoDao.markAsSynced(pagoId, System.currentTimeMillis())
    }
    
    // Estadísticas
    suspend fun getTotalCobradoDesde(startDate: Long): Double {
        return pagoDao.getTotalCobradoDesde(startDate) ?: 0.0
    }
    
    suspend fun getTotalInteresesDesde(startDate: Long): Double {
        return pagoDao.getTotalInteresesDesde(startDate) ?: 0.0
    }
    
    suspend fun getTotalCapitalDesde(startDate: Long): Double {
        return pagoDao.getTotalCapitalDesde(startDate) ?: 0.0
    }
    
    suspend fun getTotalMoraDesde(startDate: Long): Double {
        return pagoDao.getTotalMoraDesde(startDate) ?: 0.0
    }
    
    suspend fun getCountPagosDesde(startDate: Long): Int {
        return pagoDao.getCountPagosDesde(startDate)
    }
    
    // Obtener pagos de hoy
    suspend fun getPagosDeHoy(): Pair<Double, Int> {
        val hoyInicio = getStartOfDay(System.currentTimeMillis())
        val total = getTotalCobradoDesde(hoyInicio)
        val count = getCountPagosDesde(hoyInicio)
        return Pair(total, count)
    }
    
    private fun getStartOfDay(timeMillis: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

