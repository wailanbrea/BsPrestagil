package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    
    @Query("SELECT * FROM notificaciones ORDER BY fecha DESC")
    fun getAllNotificaciones(): Flow<List<NotificacionEntity>>
    
    @Query("SELECT * FROM notificaciones WHERE leida = 0 ORDER BY fecha DESC")
    fun getNotificacionesNoLeidas(): Flow<List<NotificacionEntity>>
    
    @Query("SELECT COUNT(*) FROM notificaciones WHERE leida = 0")
    fun getContadorNoLeidas(): Flow<Int>
    
    @Query("SELECT * FROM notificaciones WHERE id = :id")
    suspend fun getNotificacionById(id: String): NotificacionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacion(notificacion: NotificacionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificaciones(notificaciones: List<NotificacionEntity>)
    
    @Update
    suspend fun updateNotificacion(notificacion: NotificacionEntity)
    
    @Delete
    suspend fun deleteNotificacion(notificacion: NotificacionEntity)
    
    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun marcarComoLeida(id: String)
    
    @Query("UPDATE notificaciones SET leida = 1")
    suspend fun marcarTodasComoLeidas()
    
    @Query("DELETE FROM notificaciones WHERE fecha < :fechaLimite")
    suspend fun eliminarNotificacionesAntiguas(fechaLimite: Long)
    
    @Query("DELETE FROM notificaciones")
    suspend fun eliminarTodas()
}

