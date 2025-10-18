package com.example.bsprestagil.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.bsprestagil.data.database.entities.PrestamoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrestamoDao {
    // NUEVO: Filtrado por adminId para multi-tenancy
    @Query("SELECT * FROM prestamos WHERE adminId = :adminId ORDER BY fechaInicio DESC")
    fun getAllPrestamos(adminId: String): Flow<List<PrestamoEntity>>
    
    // Paging 3: Para listas grandes con paginación
    @Query("SELECT * FROM prestamos WHERE adminId = :adminId ORDER BY fechaInicio DESC")
    fun getAllPrestamosPaged(adminId: String): PagingSource<Int, PrestamoEntity>
    
    @Query("SELECT * FROM prestamos WHERE id = :prestamoId AND adminId = :adminId")
    fun getPrestamoById(prestamoId: String, adminId: String): Flow<PrestamoEntity?>
    
    @Query("SELECT * FROM prestamos WHERE id = :prestamoId AND adminId = :adminId")
    suspend fun getPrestamoByIdSync(prestamoId: String, adminId: String): PrestamoEntity?
    
    @Query("SELECT * FROM prestamos WHERE clienteId = :clienteId AND adminId = :adminId ORDER BY fechaInicio DESC")
    fun getPrestamosByClienteId(clienteId: String, adminId: String): Flow<List<PrestamoEntity>>
    
    @Query("SELECT * FROM prestamos WHERE cobradorId = :cobradorId AND adminId = :adminId ORDER BY fechaInicio DESC")
    fun getPrestamosByCobradorId(cobradorId: String, adminId: String): Flow<List<PrestamoEntity>>
    
    @Query("SELECT * FROM prestamos WHERE estado = :estado AND adminId = :adminId")
    fun getPrestamosByEstado(estado: String, adminId: String): Flow<List<PrestamoEntity>>
    
    @Query("SELECT * FROM prestamos WHERE pendingSync = 1 AND adminId = :adminId")
    suspend fun getPrestamosPendingSync(adminId: String): List<PrestamoEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestamo(prestamo: PrestamoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestamos(prestamos: List<PrestamoEntity>)
    
    @Update
    suspend fun updatePrestamo(prestamo: PrestamoEntity)
    
    @Delete
    suspend fun deletePrestamo(prestamo: PrestamoEntity)
    
    @Query("UPDATE prestamos SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :prestamoId AND adminId = :adminId")
    suspend fun markAsSynced(prestamoId: String, adminId: String, syncTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM prestamos WHERE estado = :estado AND adminId = :adminId")
    suspend fun getPrestamosCountByEstado(estado: String, adminId: String): Int
    
    @Query("SELECT SUM(montoOriginal) FROM prestamos WHERE estado IN ('ACTIVO', 'ATRASADO') AND adminId = :adminId")
    suspend fun getTotalPrestado(adminId: String): Double?
    
    @Query("SELECT SUM(capitalPendiente) FROM prestamos WHERE estado = 'ATRASADO' AND adminId = :adminId")
    suspend fun getCarteraVencida(adminId: String): Double?
    
    @Query("SELECT * FROM prestamos WHERE adminId = :adminId ORDER BY fechaInicio DESC LIMIT :limit")
    suspend fun getRecentPrestamos(adminId: String, limit: Int): List<PrestamoEntity>
    
    @Query("SELECT COUNT(*) FROM prestamos WHERE cobradorId = :cobradorId AND estado IN ('ACTIVO', 'ATRASADO') AND adminId = :adminId")
    suspend fun getPrestamosActivosByCobradorId(cobradorId: String, adminId: String): Int
    
    @Query("SELECT SUM(capitalPendiente) FROM prestamos WHERE cobradorId = :cobradorId AND estado IN ('ACTIVO', 'ATRASADO') AND adminId = :adminId")
    suspend fun getCapitalPendienteByCobradorId(cobradorId: String, adminId: String): Double?
}

