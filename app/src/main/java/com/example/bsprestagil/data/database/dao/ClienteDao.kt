package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY fechaRegistro DESC")
    fun getAllClientes(): Flow<List<ClienteEntity>>
    
    @Query("SELECT * FROM clientes WHERE id = :clienteId")
    fun getClienteById(clienteId: String): Flow<ClienteEntity?>
    
    @Query("SELECT * FROM clientes WHERE nombre LIKE '%' || :query || '%' OR telefono LIKE '%' || :query || '%'")
    fun searchClientes(query: String): Flow<List<ClienteEntity>>
    
    @Query("SELECT * FROM clientes WHERE pendingSync = 1")
    suspend fun getClientesPendingSync(): List<ClienteEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCliente(cliente: ClienteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClientes(clientes: List<ClienteEntity>)
    
    @Update
    suspend fun updateCliente(cliente: ClienteEntity)
    
    @Delete
    suspend fun deleteCliente(cliente: ClienteEntity)
    
    @Query("UPDATE clientes SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :clienteId")
    suspend fun markAsSynced(clienteId: String, syncTime: Long)
    
    @Query("SELECT COUNT(*) FROM clientes")
    suspend fun getClientesCount(): Int
    
    @Query("SELECT COUNT(*) FROM clientes WHERE historialPagos = :estado")
    suspend fun getClientesCountByEstado(estado: String): Int
}

