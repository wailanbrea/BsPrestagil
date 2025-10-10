package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM configuracion WHERE id = 1")
    fun getConfiguracion(): Flow<ConfiguracionEntity?>
    
    @Query("SELECT * FROM configuracion WHERE id = 1")
    suspend fun getConfiguracionSync(): ConfiguracionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguracion(configuracion: ConfiguracionEntity)
    
    @Update
    suspend fun updateConfiguracion(configuracion: ConfiguracionEntity)
    
    @Query("UPDATE configuracion SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = 1")
    suspend fun markAsSynced(syncTime: Long)
}

