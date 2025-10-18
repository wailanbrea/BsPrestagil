package com.example.bsprestagil.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.bsprestagil.data.database.dao.PrestamoDao
import com.example.bsprestagil.data.database.entities.PrestamoEntity
import com.example.bsprestagil.data.mappers.toPrestamo
import com.example.bsprestagil.data.models.Prestamo
import com.example.bsprestagil.firebase.FirebaseService
import com.example.bsprestagil.utils.AuthUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

class PrestamoRepository(
    private val prestamoDao: PrestamoDao,
    private val firebaseService: FirebaseService = FirebaseService()
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: PrestamoDao get() = prestamoDao
    
    // NUEVO: Obtener adminId del usuario actual
    private fun getAdminId(): String = runBlocking { AuthUtils.getCurrentAdminId() }
    
    // Observar todos los préstamos (filtrado por adminId)
    fun getAllPrestamos(): Flow<List<PrestamoEntity>> {
        val adminId = getAdminId()
        return prestamoDao.getAllPrestamos(adminId)
    }
    
    // Paging 3: Para listas grandes (30 items por página)
    fun getAllPrestamosPaged(): Flow<PagingData<Prestamo>> {
        val adminId = getAdminId()
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                prefetchDistance = 10
            ),
            pagingSourceFactory = { prestamoDao.getAllPrestamosPaged(adminId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toPrestamo() }
        }
    }
    
    // Observar un préstamo específico
    fun getPrestamoById(prestamoId: String): Flow<PrestamoEntity?> {
        val adminId = getAdminId()
        return prestamoDao.getPrestamoById(prestamoId, adminId)
    }
    
    // Observar préstamos por cliente
    fun getPrestamosByClienteId(clienteId: String): Flow<List<PrestamoEntity>> {
        val adminId = getAdminId()
        return prestamoDao.getPrestamosByClienteId(clienteId, adminId)
    }
    
    fun getPrestamosByCobradorId(cobradorId: String): Flow<List<PrestamoEntity>> {
        val adminId = getAdminId()
        return prestamoDao.getPrestamosByCobradorId(cobradorId, adminId)
    }
    
    // Observar préstamos por estado
    fun getPrestamosByEstado(estado: String): Flow<List<PrestamoEntity>> {
        val adminId = getAdminId()
        return prestamoDao.getPrestamosByEstado(estado, adminId)
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
        
        // Guardar en Room local
        prestamoDao.insertPrestamo(prestamoWithId)
        
        // NUEVO: Sincronizar INMEDIATAMENTE a Firebase
        try {
            android.util.Log.d("PrestamoRepository", "📤 Sincronizando préstamo a Firebase...")
            val syncResult = firebaseService.syncPrestamo(prestamoWithId)
            
            if (syncResult.isSuccess) {
                val firebaseId = syncResult.getOrNull()
                
                // Actualizar con firebaseId
                if (firebaseId != null && firebaseId != prestamoWithId.firebaseId) {
                    val prestamoConFirebaseId = prestamoWithId.copy(
                        firebaseId = firebaseId,
                        pendingSync = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    prestamoDao.updatePrestamo(prestamoConFirebaseId)
                    android.util.Log.d("PrestamoRepository", "✅ Préstamo sincronizado con firebaseId: $firebaseId")
                } else {
                    markAsSynced(prestamoWithId.id)
                    android.util.Log.d("PrestamoRepository", "✅ Préstamo sincronizado inmediatamente")
                }
            } else {
                android.util.Log.w("PrestamoRepository", "⚠️ Sincronización falló, se reintentará")
            }
        } catch (e: Exception) {
            android.util.Log.e("PrestamoRepository", "❌ Error en sync: ${e.message}")
        }
        
        return prestamoWithId.id
    }
    
    // Actualizar préstamo
    suspend fun updatePrestamo(prestamo: PrestamoEntity) {
        val prestamoActualizado = prestamo.copy(
            pendingSync = true,
            lastSyncTime = System.currentTimeMillis()
        )
        
        // Actualizar en Room local
        prestamoDao.updatePrestamo(prestamoActualizado)
        
        // NUEVO: Sincronizar INMEDIATAMENTE a Firebase
        try {
            val syncResult = firebaseService.syncPrestamo(prestamoActualizado)
            
            if (syncResult.isSuccess) {
                markAsSynced(prestamoActualizado.id)
                android.util.Log.d("PrestamoRepository", "✅ Préstamo actualizado en Firebase")
            }
        } catch (e: Exception) {
            android.util.Log.e("PrestamoRepository", "❌ Error en sync: ${e.message}")
        }
    }
    
    // Eliminar préstamo
    suspend fun deletePrestamo(prestamo: PrestamoEntity) {
        android.util.Log.d("PrestamoRepository", "🗑️ Eliminando préstamo: ${prestamo.id}")
        
        // Eliminar de Room (local)
        prestamoDao.deletePrestamo(prestamo)
        android.util.Log.d("PrestamoRepository", "✅ Préstamo eliminado de Room")
        
        // Eliminar de Firebase
        try {
            if (prestamo.firebaseId != null) {
                android.util.Log.d("PrestamoRepository", "📤 Eliminando de Firebase...")
                firebaseService.deletePrestamo(prestamo.id, prestamo.firebaseId)
                android.util.Log.d("PrestamoRepository", "✅ Préstamo eliminado de Firebase")
            }
        } catch (e: Exception) {
            android.util.Log.e("PrestamoRepository", "❌ Error eliminando de Firebase: ${e.message}")
        }
    }
    
    // Obtener préstamos pendientes de sincronizar
    suspend fun getPrestamosPendingSync(): List<PrestamoEntity> {
        val adminId = AuthUtils.getCurrentAdminId()
        return prestamoDao.getPrestamosPendingSync(adminId)
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(prestamoId: String): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return prestamoDao.markAsSynced(prestamoId, adminId, System.currentTimeMillis())
    }
    
    // Estadísticas
    suspend fun getPrestamosCountByEstado(estado: String): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return prestamoDao.getPrestamosCountByEstado(estado, adminId)
    }
    
    suspend fun getTotalPrestado(): Double {
        val adminId = AuthUtils.getCurrentAdminId()
        return prestamoDao.getTotalPrestado(adminId) ?: 0.0
    }
    
    suspend fun getCarteraVencida(): Double {
        val adminId = AuthUtils.getCurrentAdminId()
        return prestamoDao.getCarteraVencida(adminId) ?: 0.0
    }
    
    // Obtener préstamos recientes
    suspend fun getRecentPrestamos(limit: Int = 5): List<PrestamoEntity> {
        val adminId = AuthUtils.getCurrentAdminId()
        return prestamoDao.getRecentPrestamos(adminId, limit)
    }
}

