package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.ClienteDao
import com.example.bsprestagil.data.database.entities.ClienteEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ClienteRepository(
    private val clienteDao: ClienteDao
) {
    
    // Observar todos los clientes
    fun getAllClientes(): Flow<List<ClienteEntity>> {
        return clienteDao.getAllClientes()
    }
    
    // Observar un cliente específico
    fun getClienteById(clienteId: String): Flow<ClienteEntity?> {
        return clienteDao.getClienteById(clienteId)
    }
    
    // Buscar clientes
    fun searchClientes(query: String): Flow<List<ClienteEntity>> {
        return clienteDao.searchClientes(query)
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
        
        clienteDao.insertCliente(clienteWithId)
        return clienteWithId.id
    }
    
    // Actualizar cliente
    suspend fun updateCliente(cliente: ClienteEntity) {
        clienteDao.updateCliente(
            cliente.copy(
                pendingSync = true,
                lastSyncTime = System.currentTimeMillis()
            )
        )
    }
    
    // Eliminar cliente
    suspend fun deleteCliente(cliente: ClienteEntity) {
        clienteDao.deleteCliente(cliente)
    }
    
    // Obtener clientes pendientes de sincronizar
    suspend fun getClientesPendingSync(): List<ClienteEntity> {
        return clienteDao.getClientesPendingSync()
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced(clienteId: String) {
        clienteDao.markAsSynced(clienteId, System.currentTimeMillis())
    }
    
    // Estadísticas
    suspend fun getClientesCount(): Int {
        return clienteDao.getClientesCount()
    }
    
    suspend fun getClientesByEstado(estado: String): Int {
        return clienteDao.getClientesCountByEstado(estado)
    }
}

