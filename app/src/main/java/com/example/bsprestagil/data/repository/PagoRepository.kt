package com.example.bsprestagil.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.bsprestagil.data.database.dao.PagoDao
import com.example.bsprestagil.data.database.entities.PagoEntity
import com.example.bsprestagil.data.mappers.toPago
import com.example.bsprestagil.data.models.Pago
import com.example.bsprestagil.firebase.FirebaseService
import com.example.bsprestagil.utils.AuthUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

class PagoRepository(
    private val pagoDao: PagoDao,
    private val firebaseService: FirebaseService = FirebaseService()
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: PagoDao get() = pagoDao
    
    // NUEVO: Obtener adminId del usuario actual
    private fun getAdminId(): String = runBlocking { AuthUtils.getCurrentAdminId() }
    
    // Observar todos los pagos (filtrado por adminId)
    fun getAllPagos(): Flow<List<PagoEntity>> {
        val adminId = getAdminId()
        return pagoDao.getAllPagos(adminId)
    }
    
    // Paging 3: Para listas grandes (30 items por página)
    fun getAllPagosPaged(): Flow<PagingData<Pago>> {
        val adminId = getAdminId()
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                prefetchDistance = 10
            ),
            pagingSourceFactory = { pagoDao.getAllPagosPaged(adminId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toPago() }
        }
    }
    
    // Observar un pago específico
    fun getPagoById(pagoId: String): Flow<PagoEntity?> {
        val adminId = getAdminId()
        return pagoDao.getPagoById(pagoId, adminId)
    }
    
    // Observar pagos por préstamo
    fun getPagosByPrestamoId(prestamoId: String): Flow<List<PagoEntity>> {
        val adminId = getAdminId()
        return pagoDao.getPagosByPrestamoId(prestamoId, adminId)
    }
    
    // Observar pagos por cliente
    fun getPagosByClienteId(clienteId: String): Flow<List<PagoEntity>> {
        val adminId = getAdminId()
        return pagoDao.getPagosByClienteId(clienteId, adminId)
    }
    
    // Observar pagos por rango de fechas
    fun getPagosByDateRange(startDate: Long, endDate: Long): Flow<List<PagoEntity>> {
        val adminId = getAdminId()
        return pagoDao.getPagosByDateRange(startDate, endDate, adminId)
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
        android.util.Log.d("PagoRepository", "🗑️ Eliminando pago: ${pago.id}")
        
        // Eliminar de Room (local)
        pagoDao.deletePago(pago)
        android.util.Log.d("PagoRepository", "✅ Pago eliminado de Room")
        
        // Eliminar de Firebase
        try {
            if (pago.firebaseId != null) {
                android.util.Log.d("PagoRepository", "📤 Eliminando de Firebase...")
                firebaseService.deletePago(pago.id, pago.firebaseId)
                android.util.Log.d("PagoRepository", "✅ Pago eliminado de Firebase")
            }
        } catch (e: Exception) {
            android.util.Log.e("PagoRepository", "❌ Error eliminando de Firebase: ${e.message}")
        }
    }
    
    // Obtener pagos pendientes de sincronizar
    suspend fun getPagosPendingSync(): List<PagoEntity> {
        val adminId = AuthUtils.getCurrentAdminId()
        return pagoDao.getPagosPendingSync(adminId)
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(pagoId: String): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return pagoDao.markAsSynced(pagoId, adminId, System.currentTimeMillis())
    }
    
    // Estadísticas
    suspend fun getTotalCobradoDesde(startDate: Long): Double {
        val adminId = AuthUtils.getCurrentAdminId()
        return pagoDao.getTotalCobradoDesde(startDate, adminId) ?: 0.0
    }
    
    suspend fun getTotalInteresesDesde(startDate: Long): Double {
        val adminId = AuthUtils.getCurrentAdminId()
        return pagoDao.getTotalInteresesDesde(startDate, adminId) ?: 0.0
    }
    
    suspend fun getTotalCapitalDesde(startDate: Long): Double {
        val adminId = AuthUtils.getCurrentAdminId()
        return pagoDao.getTotalCapitalDesde(startDate, adminId) ?: 0.0
    }
    
    suspend fun getTotalMoraDesde(startDate: Long): Double {
        val adminId = AuthUtils.getCurrentAdminId()
        return pagoDao.getTotalMoraDesde(startDate, adminId) ?: 0.0
    }
    
    suspend fun getCountPagosDesde(startDate: Long): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return pagoDao.getCountPagosDesde(startDate, adminId)
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

