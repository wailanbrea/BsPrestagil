package com.example.bsprestagil.data.database.dao

import androidx.room.*
import com.example.bsprestagil.data.database.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    // NUEVO: Filtrado por adminId para multi-tenancy (solo usuarios de la misma empresa)
    @Query("SELECT * FROM usuarios WHERE adminId = :adminId")
    fun getAllUsuarios(adminId: String): Flow<List<UsuarioEntity>>
    
    // Obtener usuario por ID (sin filtro de adminId para auth)
    @Query("SELECT * FROM usuarios WHERE id = :usuarioId")
    fun getUsuarioById(usuarioId: String): Flow<UsuarioEntity?>
    
    @Query("SELECT * FROM usuarios WHERE id = :usuarioId")
    suspend fun getUsuarioByIdSync(usuarioId: String): UsuarioEntity?
    
    // Email no filtra por adminId (para login)
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity?
    
    // Filtrar usuarios por rol dentro de la empresa
    @Query("SELECT * FROM usuarios WHERE rol = :rol AND adminId = :adminId")
    fun getUsuariosByRol(rol: String, adminId: String): Flow<List<UsuarioEntity>>
    
    @Query("SELECT * FROM usuarios WHERE pendingSync = 1 AND adminId = :adminId")
    suspend fun getUsuariosPendingSync(adminId: String): List<UsuarioEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity)
    
    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)
    
    @Delete
    suspend fun deleteUsuario(usuario: UsuarioEntity)
    
    @Query("UPDATE usuarios SET pendingSync = 0, lastSyncTime = :syncTime WHERE id = :usuarioId AND adminId = :adminId")
    suspend fun markAsSynced(usuarioId: String, adminId: String, syncTime: Long)
}

