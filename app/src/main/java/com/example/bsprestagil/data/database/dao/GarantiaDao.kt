package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.GarantiaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GarantiaDao {
    // NUEVO: Filtrado por adminId para multi-tenancy
    @Query("SELECT * FROM garantias WHERE adminId = :adminId ORDER BY fechaRegistro DESC")
    fun getAllGarantias(adminId: String): Flow<List<GarantiaEntity>>
    
    @Query("SELECT * FROM garantias WHERE id = :garantiaId AND adminId = :adminId")
    fun getGarantiaById(garantiaId: String, adminId: String): Flow<GarantiaEntity?>
    
    @Query("SELECT * FROM garantias WHERE id = :garantiaId AND adminId = :adminId")
    suspend fun getGarantiaByIdSync(garantiaId: String, adminId: String): GarantiaEntity?
    
    @Query("SELECT * FROM garantias WHERE estado = :estado AND adminId = :adminId")
    fun getGarantiasByEstado(estado: String, adminId: String): Flow<List<GarantiaEntity>>
    
    @Query("SELECT * FROM garantias WHERE pendingSync = 1 AND adminId = :adminId")
    suspend fun getGarantiasPendingSync(adminId: String): List<GarantiaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarantia(garantia: GarantiaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarantias(garantias: List<GarantiaEntity>)
    
    @Update
    suspend fun updateGarantia(garantia: GarantiaEntity)
    
    @Delete
    suspend fun deleteGarantia(garantia: GarantiaEntity)
    
    @Query("UPDATE garantias SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :garantiaId AND adminId = :adminId")
    suspend fun markAsSynced(garantiaId: String, adminId: String, syncTime: Long)
}

