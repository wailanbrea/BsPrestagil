package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.GarantiaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GarantiaDao {
    @Query("SELECT * FROM garantias ORDER BY fechaRegistro DESC")
    fun getAllGarantias(): Flow<List<GarantiaEntity>>
    
    @Query("SELECT * FROM garantias WHERE id = :garantiaId")
    fun getGarantiaById(garantiaId: String): Flow<GarantiaEntity?>
    
    @Query("SELECT * FROM garantias WHERE id = :garantiaId")
    suspend fun getGarantiaByIdSync(garantiaId: String): GarantiaEntity?
    
    @Query("SELECT * FROM garantias WHERE estado = :estado")
    fun getGarantiasByEstado(estado: String): Flow<List<GarantiaEntity>>
    
    @Query("SELECT * FROM garantias WHERE pendingSync = 1")
    suspend fun getGarantiasPendingSync(): List<GarantiaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarantia(garantia: GarantiaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarantias(garantias: List<GarantiaEntity>)
    
    @Update
    suspend fun updateGarantia(garantia: GarantiaEntity)
    
    @Delete
    suspend fun deleteGarantia(garantia: GarantiaEntity)
    
    @Query("UPDATE garantias SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :garantiaId")
    suspend fun markAsSynced(garantiaId: String, syncTime: Long)
}

