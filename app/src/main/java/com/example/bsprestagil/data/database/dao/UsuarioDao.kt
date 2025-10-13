package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>
    
    @Query("SELECT * FROM usuarios WHERE id = :usuarioId")
    fun getUsuarioById(usuarioId: String): Flow<UsuarioEntity?>
    
    @Query("SELECT * FROM usuarios WHERE id = :usuarioId")
    suspend fun getUsuarioByIdSync(usuarioId: String): UsuarioEntity?
    
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity?
    
    @Query("SELECT * FROM usuarios WHERE pendingSync = 1")
    suspend fun getUsuariosPendingSync(): List<UsuarioEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity)
    
    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)
    
    @Delete
    suspend fun deleteUsuario(usuario: UsuarioEntity)
    
    @Query("UPDATE usuarios SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :usuarioId")
    suspend fun markAsSynced(usuarioId: String, syncTime: Long)
}

