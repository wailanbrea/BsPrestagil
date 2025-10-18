package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.CuotaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuotaDao {
    // NUEVO: Filtrado por adminId para multi-tenancy
    @Query("SELECT * FROM cuotas WHERE prestamoId = :prestamoId AND adminId = :adminId ORDER BY numeroCuota ASC")
    fun getCuotasByPrestamoId(prestamoId: String, adminId: String): Flow<List<CuotaEntity>>
    
    @Query("SELECT * FROM cuotas WHERE id = :cuotaId AND adminId = :adminId")
    fun getCuotaById(cuotaId: String, adminId: String): Flow<CuotaEntity?>
    
    @Query("SELECT * FROM cuotas WHERE prestamoId = :prestamoId AND estado = :estado AND adminId = :adminId ORDER BY numeroCuota ASC")
    fun getCuotasByEstado(prestamoId: String, estado: String, adminId: String): Flow<List<CuotaEntity>>
    
    @Query("SELECT * FROM cuotas WHERE estado = 'VENCIDA' AND adminId = :adminId ORDER BY fechaVencimiento ASC")
    fun getCuotasVencidas(adminId: String): Flow<List<CuotaEntity>>
    
    @Query("SELECT * FROM cuotas WHERE prestamoId = :prestamoId AND estado = 'PENDIENTE' AND adminId = :adminId ORDER BY numeroCuota ASC LIMIT 1")
    suspend fun getProximaCuotaPendiente(prestamoId: String, adminId: String): CuotaEntity?
    
    @Query("SELECT * FROM cuotas WHERE pendingSync = 1 AND adminId = :adminId")
    suspend fun getCuotasPendingSync(adminId: String): List<CuotaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuota(cuota: CuotaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuotas(cuotas: List<CuotaEntity>)
    
    @Update
    suspend fun updateCuota(cuota: CuotaEntity)
    
    @Delete
    suspend fun deleteCuota(cuota: CuotaEntity)
    
    @Query("DELETE FROM cuotas WHERE prestamoId = :prestamoId AND adminId = :adminId")
    suspend fun deleteCuotasByPrestamoId(prestamoId: String, adminId: String)
    
    @Query("UPDATE cuotas SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :cuotaId AND adminId = :adminId")
    suspend fun markAsSynced(cuotaId: String, adminId: String, syncTime: Long): Int
    
    @Query("SELECT COUNT(*) FROM cuotas WHERE prestamoId = :prestamoId AND estado = 'PAGADA' AND adminId = :adminId")
    suspend fun countCuotasPagadas(prestamoId: String, adminId: String): Int
}

