package com.example.bsprestagil.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.bsprestagil.data.database.entities.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    // NUEVO: Filtrado por adminId para multi-tenancy
    @Query("SELECT * FROM clientes WHERE adminId = :adminId ORDER BY fechaRegistro DESC")
    fun getAllClientes(adminId: String): Flow<List<ClienteEntity>>
    
    // Paging 3: Para listas grandes con paginación
    @Query("SELECT * FROM clientes WHERE adminId = :adminId ORDER BY fechaRegistro DESC")
    fun getAllClientesPaged(adminId: String): PagingSource<Int, ClienteEntity>
    
    @Query("SELECT * FROM clientes WHERE id = :clienteId AND adminId = :adminId")
    fun getClienteById(clienteId: String, adminId: String): Flow<ClienteEntity?>
    
    @Query("SELECT * FROM clientes WHERE id = :clienteId AND adminId = :adminId")
    suspend fun getClienteByIdSync(clienteId: String, adminId: String): ClienteEntity?
    
    @Query("SELECT * FROM clientes WHERE adminId = :adminId AND (nombre LIKE '%' || :query || '%' OR telefono LIKE '%' || :query || '%')")
    fun searchClientes(query: String, adminId: String): Flow<List<ClienteEntity>>
    
    @Query("SELECT * FROM clientes WHERE pendingSync = 1 AND adminId = :adminId")
    suspend fun getClientesPendingSync(adminId: String): List<ClienteEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCliente(cliente: ClienteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClientes(clientes: List<ClienteEntity>)
    
    @Update
    suspend fun updateCliente(cliente: ClienteEntity)
    
    @Delete
    suspend fun deleteCliente(cliente: ClienteEntity)
    
    @Query("UPDATE clientes SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :clienteId AND adminId = :adminId")
    suspend fun markAsSynced(clienteId: String, adminId: String, syncTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM clientes WHERE adminId = :adminId")
    suspend fun getClientesCount(adminId: String): Int
    
    @Query("SELECT COUNT(*) FROM clientes WHERE adminId = :adminId AND historialPagos = :estado")
    suspend fun getClientesCountByEstado(adminId: String, estado: String): Int
}

