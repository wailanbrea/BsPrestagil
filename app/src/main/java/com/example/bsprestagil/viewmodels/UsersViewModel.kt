package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.UsuarioEntity
import com.example.bsprestagil.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class UsersViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val usuarioRepository = UsuarioRepository(database.usuarioDao())
    
    val usuarios: StateFlow<List<UsuarioEntity>> = usuarioRepository
        .getAllUsuarios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun crearUsuario(
        nombre: String,
        email: String,
        telefono: String,
        rol: String
    ) {
        viewModelScope.launch {
            val usuario = UsuarioEntity(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                email = email,
                telefono = telefono,
                rol = rol,
                activo = true,
                fechaCreacion = System.currentTimeMillis()
            )
            usuarioRepository.insertUsuario(usuario)
        }
    }
    
    fun actualizarUsuario(usuario: UsuarioEntity) {
        viewModelScope.launch {
            usuarioRepository.updateUsuario(usuario)
        }
    }
    
    fun eliminarUsuario(usuario: UsuarioEntity) {
        viewModelScope.launch {
            usuarioRepository.deleteUsuario(usuario)
        }
    }
}

