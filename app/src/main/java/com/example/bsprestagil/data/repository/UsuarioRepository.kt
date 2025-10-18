package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.UsuarioDao
import com.example.bsprestagil.data.database.entities.UsuarioEntity
import com.example.bsprestagil.utils.AuthUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class UsuarioRepository(
    internal val usuarioDao: UsuarioDao
) {
    
    // NUEVO: Obtener adminId del usuario actual
    private fun getAdminId(): String = runBlocking { AuthUtils.getCurrentAdminId() }
    
    fun getAllUsuarios(): Flow<List<UsuarioEntity>> {
        val adminId = getAdminId()
        return usuarioDao.getAllUsuarios(adminId)
    }
    
    // Sin filtro adminId (se usa para auth y perfil)
    fun getUsuarioById(usuarioId: String): Flow<UsuarioEntity?> {
        return usuarioDao.getUsuarioById(usuarioId)
    }
    
    // Sin filtro adminId (se usa para login)
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity? {
        return usuarioDao.getUsuarioByEmail(email)
    }
    
    suspend fun getUsuariosPendingSync(): List<UsuarioEntity> {
        val adminId = AuthUtils.getCurrentAdminId()
        return usuarioDao.getUsuariosPendingSync(adminId)
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
        val adminId = AuthUtils.getCurrentAdminId()
        usuarioDao.markAsSynced(usuarioId, adminId, System.currentTimeMillis())
    }
}

