package com.example.bsprestagil.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.bsprestagil.data.database.entities.PagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {
    // NUEVO: Filtrado por adminId para multi-tenancy
    @Query("SELECT * FROM pagos WHERE adminId = :adminId ORDER BY fechaPago DESC")
    fun getAllPagos(adminId: String): Flow<List<PagoEntity>>
    
    // Paging 3: Para listas grandes con paginación
    @Query("SELECT * FROM pagos WHERE adminId = :adminId ORDER BY fechaPago DESC")
    fun getAllPagosPaged(adminId: String): PagingSource<Int, PagoEntity>
    
    @Query("SELECT * FROM pagos WHERE id = :pagoId AND adminId = :adminId")
    fun getPagoById(pagoId: String, adminId: String): Flow<PagoEntity?>
    
    @Query("SELECT * FROM pagos WHERE id = :pagoId AND adminId = :adminId")
    suspend fun getPagoByIdSync(pagoId: String, adminId: String): PagoEntity?
    
    @Query("SELECT * FROM pagos WHERE prestamoId = :prestamoId AND adminId = :adminId ORDER BY fechaPago DESC")
    fun getPagosByPrestamoId(prestamoId: String, adminId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE clienteId = :clienteId AND adminId = :adminId ORDER BY fechaPago DESC")
    fun getPagosByClienteId(clienteId: String, adminId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE fechaPago BETWEEN :startDate AND :endDate AND adminId = :adminId")
    fun getPagosByDateRange(startDate: Long, endDate: Long, adminId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE pendingSync = 1 AND adminId = :adminId")
    suspend fun getPagosPendingSync(adminId: String): List<PagoEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPago(pago: PagoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPagos(pagos: List<PagoEntity>)
    
    @Update
    suspend fun updatePago(pago: PagoEntity)
    
    @Delete
    suspend fun deletePago(pago: PagoEntity)
    
    @Query("UPDATE pagos SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :pagoId AND adminId = :adminId")
    suspend fun markAsSynced(pagoId: String, adminId: String, syncTime: Long): Int
    
    @Query("SELECT SUM(montoPagado) FROM pagos WHERE fechaPago >= :startDate AND adminId = :adminId")
    suspend fun getTotalCobradoDesde(startDate: Long, adminId: String): Double?
    
    @Query("SELECT SUM(montoAInteres) FROM pagos WHERE fechaPago >= :startDate AND adminId = :adminId")
    suspend fun getTotalInteresesDesde(startDate: Long, adminId: String): Double?
    
    @Query("SELECT SUM(montoACapital) FROM pagos WHERE fechaPago >= :startDate AND adminId = :adminId")
    suspend fun getTotalCapitalDesde(startDate: Long, adminId: String): Double?
    
    @Query("SELECT SUM(montoMora) FROM pagos WHERE fechaPago >= :startDate AND adminId = :adminId")
    suspend fun getTotalMoraDesde(startDate: Long, adminId: String): Double?
    
    @Query("SELECT COUNT(*) FROM pagos WHERE fechaPago >= :startDate AND adminId = :adminId")
    suspend fun getCountPagosDesde(startDate: Long, adminId: String): Int
}

