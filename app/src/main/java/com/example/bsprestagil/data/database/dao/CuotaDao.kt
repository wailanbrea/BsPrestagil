package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.CuotaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuotaDao {
    @Query("SELECT * FROM cuotas WHERE prestamoId = :prestamoId ORDER BY numeroCuota ASC")
    fun getCuotasByPrestamoId(prestamoId: String): Flow<List<CuotaEntity>>
    
    @Query("SELECT * FROM cuotas WHERE id = :cuotaId")
    fun getCuotaById(cuotaId: String): Flow<CuotaEntity?>
    
    @Query("SELECT * FROM cuotas WHERE prestamoId = :prestamoId AND estado = :estado ORDER BY numeroCuota ASC")
    fun getCuotasByEstado(prestamoId: String, estado: String): Flow<List<CuotaEntity>>
    
    @Query("SELECT * FROM cuotas WHERE estado = 'VENCIDA' ORDER BY fechaVencimiento ASC")
    fun getCuotasVencidas(): Flow<List<CuotaEntity>>
    
    @Query("SELECT * FROM cuotas WHERE prestamoId = :prestamoId AND estado = 'PENDIENTE' ORDER BY numeroCuota ASC LIMIT 1")
    suspend fun getProximaCuotaPendiente(prestamoId: String): CuotaEntity?
    
    @Query("SELECT * FROM cuotas WHERE pendingSync = 1")
    suspend fun getCuotasPendingSync(): List<CuotaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuota(cuota: CuotaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuotas(cuotas: List<CuotaEntity>)
    
    @Update
    suspend fun updateCuota(cuota: CuotaEntity)
    
    @Delete
    suspend fun deleteCuota(cuota: CuotaEntity)
    
    @Query("DELETE FROM cuotas WHERE prestamoId = :prestamoId")
    suspend fun deleteCuotasByPrestamoId(prestamoId: String)
    
    @Query("UPDATE cuotas SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :cuotaId")
    suspend fun markAsSynced(cuotaId: String, syncTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM cuotas WHERE prestamoId = :prestamoId AND estado = 'PAGADA'")
    suspend fun countCuotasPagadas(prestamoId: String): Int
}

