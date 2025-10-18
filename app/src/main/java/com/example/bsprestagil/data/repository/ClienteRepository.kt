package com.example.bsprestagil.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.bsprestagil.data.database.dao.ClienteDao
import com.example.bsprestagil.data.database.entities.ClienteEntity
import com.example.bsprestagil.data.mappers.toCliente
import com.example.bsprestagil.data.models.Cliente
import com.example.bsprestagil.firebase.FirebaseService
import com.example.bsprestagil.utils.AuthUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

class ClienteRepository(
    private val clienteDao: ClienteDao,
    private val firebaseService: FirebaseService = FirebaseService()
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: ClienteDao get() = clienteDao
    
    // NUEVO: Obtener adminId del usuario actual
    private fun getAdminId(): String = runBlocking { AuthUtils.getCurrentAdminId() }
    
    // Observar todos los clientes (filtrado por adminId)
    fun getAllClientes(): Flow<List<ClienteEntity>> {
        val adminId = getAdminId()
        return clienteDao.getAllClientes(adminId)
    }
    
    // Paging 3: Para listas grandes (30 items por página)
    fun getAllClientesPaged(): Flow<PagingData<Cliente>> {
        val adminId = getAdminId()
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                prefetchDistance = 10
            ),
            pagingSourceFactory = { clienteDao.getAllClientesPaged(adminId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toCliente() }
        }
    }
    
    // Observar un cliente específico
    fun getClienteById(clienteId: String): Flow<ClienteEntity?> {
        val adminId = getAdminId()
        return clienteDao.getClienteById(clienteId, adminId)
    }
    
    // Buscar clientes
    fun searchClientes(query: String): Flow<List<ClienteEntity>> {
        val adminId = getAdminId()
        return clienteDao.searchClientes(query, adminId)
    }
    
    // Insertar nuevo cliente
    suspend fun insertCliente(cliente: ClienteEntity): String {
        val clienteWithId = if (cliente.id.isEmpty()) {
            cliente.copy(
                id = UUID.randomUUID().toString(),
                pendingSync = true,
                lastSyncTime = 0L
            )
        } else {
            cliente.copy(pendingSync = true)
        }
        
        // Guardar en Room local
        clienteDao.insertCliente(clienteWithId)
        
        // NUEVO: Sincronizar INMEDIATAMENTE a Firebase
        try {
            android.util.Log.d("ClienteRepository", "📤 Sincronizando cliente a Firebase inmediatamente...")
            val syncResult = firebaseService.syncCliente(clienteWithId)
            
            if (syncResult.isSuccess) {
                val firebaseId = syncResult.getOrNull()
                
                // Actualizar el cliente con el firebaseId
                if (firebaseId != null && firebaseId != clienteWithId.firebaseId) {
                    val clienteConFirebaseId = clienteWithId.copy(
                        firebaseId = firebaseId,
                        pendingSync = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    clienteDao.updateCliente(clienteConFirebaseId)
                    android.util.Log.d("ClienteRepository", "✅ Cliente sincronizado con firebaseId: $firebaseId")
                } else {
                    markAsSynced(clienteWithId.id)
                    android.util.Log.d("ClienteRepository", "✅ Cliente sincronizado inmediatamente")
                }
            } else {
                android.util.Log.w("ClienteRepository", "⚠️ Sincronización inmediata falló, se reintentará automáticamente")
            }
        } catch (e: Exception) {
            android.util.Log.e("ClienteRepository", "❌ Error en sincronización inmediata: ${e.message}")
            // No falla la operación, se sincronizará con el worker
        }
        
        return clienteWithId.id
    }
    
    // Actualizar cliente
    suspend fun updateCliente(cliente: ClienteEntity) {
        val clienteActualizado = cliente.copy(
            pendingSync = true,
            lastSyncTime = System.currentTimeMillis()
        )
        
        // Actualizar en Room local
        clienteDao.updateCliente(clienteActualizado)
        
        // NUEVO: Sincronizar INMEDIATAMENTE a Firebase
        try {
            android.util.Log.d("ClienteRepository", "📤 Sincronizando actualización a Firebase...")
            val syncResult = firebaseService.syncCliente(clienteActualizado)
            
            if (syncResult.isSuccess) {
                markAsSynced(clienteActualizado.id)
                android.util.Log.d("ClienteRepository", "✅ Cliente actualizado en Firebase")
            } else {
                android.util.Log.w("ClienteRepository", "⚠️ Actualización inmediata falló, se reintentará")
            }
        } catch (e: Exception) {
            android.util.Log.e("ClienteRepository", "❌ Error en sync: ${e.message}")
        }
    }
    
    // Eliminar cliente
    suspend fun deleteCliente(cliente: ClienteEntity) {
        android.util.Log.d("ClienteRepository", "🗑️ Eliminando cliente: ${cliente.nombre}")
        
        // Eliminar de Room (local)
        clienteDao.deleteCliente(cliente)
        android.util.Log.d("ClienteRepository", "✅ Cliente eliminado de Room")
        
        // NUEVO: Eliminar INMEDIATAMENTE de Firebase (nube)
        try {
            if (cliente.firebaseId != null) {
                android.util.Log.d("ClienteRepository", "📤 Eliminando de Firebase (firebaseId: ${cliente.firebaseId})...")
                val result = firebaseService.deleteCliente(cliente.id, cliente.firebaseId)
                
                if (result.isSuccess) {
                    android.util.Log.d("ClienteRepository", "✅ Cliente eliminado de Firebase")
                } else {
                    android.util.Log.w("ClienteRepository", "⚠️ Error eliminando de Firebase: ${result.exceptionOrNull()?.message}")
                }
            } else {
                android.util.Log.d("ClienteRepository", "⚠️ Cliente sin firebaseId, solo eliminado localmente")
            }
        } catch (e: Exception) {
            android.util.Log.e("ClienteRepository", "❌ Error eliminando de Firebase: ${e.message}")
        }
    }
    
    // Obtener clientes pendientes de sincronizar
    suspend fun getClientesPendingSync(): List<ClienteEntity> {
        val adminId = AuthUtils.getCurrentAdminId()
        return clienteDao.getClientesPendingSync(adminId)
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(clienteId: String): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return clienteDao.markAsSynced(clienteId, adminId, System.currentTimeMillis())
    }
    
    // Estadísticas
    suspend fun getClientesCount(): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return clienteDao.getClientesCount(adminId)
    }
    
    suspend fun getClientesByEstado(estado: String): Int {
        val adminId = AuthUtils.getCurrentAdminId()
        return clienteDao.getClientesCountByEstado(adminId, estado)
    }
}

