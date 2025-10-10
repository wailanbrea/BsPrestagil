package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.PagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {
    @Query("SELECT * FROM pagos ORDER BY fechaPago DESC")
    fun getAllPagos(): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE id = :pagoId")
    fun getPagoById(pagoId: String): Flow<PagoEntity?>
    
    @Query("SELECT * FROM pagos WHERE prestamoId = :prestamoId ORDER BY fechaPago DESC")
    fun getPagosByPrestamoId(prestamoId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE clienteId = :clienteId ORDER BY fechaPago DESC")
    fun getPagosByClienteId(clienteId: String): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE fechaPago BETWEEN :startDate AND :endDate")
    fun getPagosByDateRange(startDate: Long, endDate: Long): Flow<List<PagoEntity>>
    
    @Query("SELECT * FROM pagos WHERE pendingSync = 1")
    suspend fun getPagosPendingSync(): List<PagoEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPago(pago: PagoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPagos(pagos: List<PagoEntity>)
    
    @Update
    suspend fun updatePago(pago: PagoEntity)
    
    @Delete
    suspend fun deletePago(pago: PagoEntity)
    
    @Query("UPDATE pagos SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :pagoId")
    suspend fun markAsSynced(pagoId: String, syncTime: Long)
    
    @Query("SELECT SUM(montoPagado) FROM pagos WHERE fechaPago >= :startDate")
    suspend fun getTotalCobradoDesde(startDate: Long): Double?
    
    @Query("SELECT SUM(montoAInteres) FROM pagos WHERE fechaPago >= :startDate")
    suspend fun getTotalInteresesDesde(startDate: Long): Double?
    
    @Query("SELECT SUM(montoACapital) FROM pagos WHERE fechaPago >= :startDate")
    suspend fun getTotalCapitalDesde(startDate: Long): Double?
    
    @Query("SELECT SUM(montoMora) FROM pagos WHERE fechaPago >= :startDate")
    suspend fun getTotalMoraDesde(startDate: Long): Double?
    
    @Query("SELECT COUNT(*) FROM pagos WHERE fechaPago >= :startDate")
    suspend fun getCountPagosDesde(startDate: Long): Int
}

