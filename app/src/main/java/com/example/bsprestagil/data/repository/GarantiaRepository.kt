package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.GarantiaDao
import com.example.bsprestagil.data.database.entities.GarantiaEntity
import com.example.bsprestagil.firebase.FirebaseService
import com.example.bsprestagil.utils.AuthUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.UUID

class GarantiaRepository(
    private val garantiaDao: GarantiaDao,
    private val firebaseService: FirebaseService = FirebaseService()
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: GarantiaDao get() = garantiaDao
    
    // NUEVO: Obtener adminId del usuario actual
    private fun getAdminId(): String = runBlocking { AuthUtils.getCurrentAdminId() }
    
    // Observar todas las garantías
    fun getAllGarantias(): Flow<List<GarantiaEntity>> {
        val adminId = getAdminId()
        return garantiaDao.getAllGarantias(adminId)
    }
    
    // Observar una garantía específica
    fun getGarantiaById(garantiaId: String): Flow<GarantiaEntity?> {
        val adminId = getAdminId()
        return garantiaDao.getGarantiaById(garantiaId, adminId)
    }
    
    // Observar garantías por estado
    fun getGarantiasByEstado(estado: String): Flow<List<GarantiaEntity>> {
        val adminId = getAdminId()
        return garantiaDao.getGarantiasByEstado(estado, adminId)
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
        
        // Guardar en Room local
        garantiaDao.insertGarantia(garantiaWithId)
        
        // NUEVO: Sincronizar INMEDIATAMENTE a Firebase
        try {
            android.util.Log.d("GarantiaRepository", "📤 Sincronizando garantía a Firebase...")
            val syncResult = firebaseService.syncGarantia(garantiaWithId)
            
            if (syncResult.isSuccess) {
                val firebaseId = syncResult.getOrNull()
                
                // Actualizar con firebaseId
                if (firebaseId != null && firebaseId != garantiaWithId.firebaseId) {
                    val garantiaConFirebaseId = garantiaWithId.copy(
                        firebaseId = firebaseId,
                        pendingSync = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    garantiaDao.updateGarantia(garantiaConFirebaseId)
                    android.util.Log.d("GarantiaRepository", "✅ Garantía sincronizada con firebaseId: $firebaseId")
                } else {
                    markAsSynced(garantiaWithId.id)
                    android.util.Log.d("GarantiaRepository", "✅ Garantía sincronizada inmediatamente")
                }
            } else {
                android.util.Log.w("GarantiaRepository", "⚠️ Sincronización falló, se reintentará")
            }
        } catch (e: Exception) {
            android.util.Log.e("GarantiaRepository", "❌ Error en sync: ${e.message}")
        }
        
        return garantiaWithId.id
    }
    
    // Actualizar garantía
    suspend fun updateGarantia(garantia: GarantiaEntity) {
        val garantiaActualizada = garantia.copy(
            pendingSync = true,
            lastSyncTime = System.currentTimeMillis()
        )
        
        // Actualizar en Room local
        garantiaDao.updateGarantia(garantiaActualizada)
        
        // NUEVO: Sincronizar INMEDIATAMENTE a Firebase
        try {
            val syncResult = firebaseService.syncGarantia(garantiaActualizada)
            
            if (syncResult.isSuccess) {
                markAsSynced(garantiaActualizada.id)
                android.util.Log.d("GarantiaRepository", "✅ Garantía actualizada en Firebase")
            }
        } catch (e: Exception) {
            android.util.Log.e("GarantiaRepository", "❌ Error en sync: ${e.message}")
        }
    }
    
    // Eliminar garantía
    suspend fun deleteGarantia(garantia: GarantiaEntity) {
        android.util.Log.d("GarantiaRepository", "🗑️ Eliminando garantía: ${garantia.descripcion}")
        
        // Eliminar de Room (local)
        garantiaDao.deleteGarantia(garantia)
        android.util.Log.d("GarantiaRepository", "✅ Garantía eliminada de Room")
        
        // Eliminar de Firebase
        try {
            if (garantia.firebaseId != null) {
                android.util.Log.d("GarantiaRepository", "📤 Eliminando de Firebase...")
                firebaseService.deleteGarantia(garantia.id, garantia.firebaseId)
                android.util.Log.d("GarantiaRepository", "✅ Garantía eliminada de Firebase")
            }
        } catch (e: Exception) {
            android.util.Log.e("GarantiaRepository", "❌ Error eliminando de Firebase: ${e.message}")
        }
    }
    
    // Obtener garantías pendientes de sincronizar
    suspend fun getGarantiasPendingSync(): List<GarantiaEntity> {
        val adminId = AuthUtils.getCurrentAdminId()
        return garantiaDao.getGarantiasPendingSync(adminId)
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(garantiaId: String) {
        val adminId = AuthUtils.getCurrentAdminId()
        garantiaDao.markAsSynced(garantiaId, adminId, System.currentTimeMillis())
    }
}

