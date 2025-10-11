package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.UsuarioDao
import com.example.bsprestagil.data.database.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    
    fun getAllUsuarios(): Flow<List<UsuarioEntity>> {
        return usuarioDao.getAllUsuarios()
    }
    
    fun getUsuarioById(usuarioId: String): Flow<UsuarioEntity?> {
        return usuarioDao.getUsuarioById(usuarioId)
    }
    
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity? {
        return usuarioDao.getUsuarioByEmail(email)
    }
    
    suspend fun getUsuariosPendingSync(): List<UsuarioEntity> {
        return usuarioDao.getUsuariosPendingSync()
    }
    
    suspend fun insertUsuario(usuario: UsuarioEntity) {
        usuarioDao.insertUsuario(usuario)
    }
    
    suspend fun updateUsuario(usuario: UsuarioEntity) {
        usuarioDao.updateUsuario(usuario.copy(pendingSync = true, lastSyncTime = System.currentTimeMillis()))
    }
    
    suspend fun deleteUsuario(usuario: UsuarioEntity) {
        usuarioDao.deleteUsuario(usuario)
    }
    
    suspend fun markAsSynced(usuarioId: String) {
        usuarioDao.markAsSynced(usuarioId, System.currentTimeMillis())
    }
}

