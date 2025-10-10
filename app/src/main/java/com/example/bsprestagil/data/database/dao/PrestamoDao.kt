package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.PrestamoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrestamoDao {
    @Query("SELECT * FROM prestamos ORDER BY fechaInicio DESC")
    fun getAllPrestamos(): Flow<List<PrestamoEntity>>
    
    @Query("SELECT * FROM prestamos WHERE id = :prestamoId")
    fun getPrestamoById(prestamoId: String): Flow<PrestamoEntity?>
    
    @Query("SELECT * FROM prestamos WHERE clienteId = :clienteId ORDER BY fechaInicio DESC")
    fun getPrestamosByClienteId(clienteId: String): Flow<List<PrestamoEntity>>
    
    @Query("SELECT * FROM prestamos WHERE estado = :estado")
    fun getPrestamosByEstado(estado: String): Flow<List<PrestamoEntity>>
    
    @Query("SELECT * FROM prestamos WHERE pendingSync = 1")
    suspend fun getPrestamosPendingSync(): List<PrestamoEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestamo(prestamo: PrestamoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestamos(prestamos: List<PrestamoEntity>)
    
    @Update
    suspend fun updatePrestamo(prestamo: PrestamoEntity)
    
    @Delete
    suspend fun deletePrestamo(prestamo: PrestamoEntity)
    
    @Query("UPDATE prestamos SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :prestamoId")
    suspend fun markAsSynced(prestamoId: String, syncTime: Long)
    
    @Query("SELECT COUNT(*) FROM prestamos WHERE estado = :estado")
    suspend fun getPrestamosCountByEstado(estado: String): Int
    
    @Query("SELECT SUM(montoOriginal) FROM prestamos WHERE estado IN ('ACTIVO', 'ATRASADO')")
    suspend fun getTotalPrestado(): Double?
    
    @Query("SELECT SUM(capitalPendiente) FROM prestamos WHERE estado = 'ATRASADO'")
    suspend fun getCarteraVencida(): Double?
    
    @Query("SELECT * FROM prestamos ORDER BY fechaInicio DESC LIMIT :limit")
    suspend fun getRecentPrestamos(limit: Int): List<PrestamoEntity>
}

