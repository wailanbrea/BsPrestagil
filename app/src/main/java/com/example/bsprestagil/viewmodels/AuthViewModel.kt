package com.example.bsprestagil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    
    init {
        // Verificar si hay un usuario ya logueado
        auth.currentUser?.let {
            _authState.value = AuthState.Success(it)
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email y contraseña son requeridos")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let {
                    _authState.value = AuthState.Success(it)
                } ?: run {
                    _authState.value = AuthState.Error("Error al iniciar sesión")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("password") == true -> "Contraseña incorrecta"
                        e.message?.contains("user") == true -> "Usuario no encontrado"
                        e.message?.contains("network") == true -> "Error de conexión"
                        else -> e.message ?: "Error desconocido"
                    }
                )
            }
        }
    }
    
    fun register(email: String, password: String, nombre: String) {
        if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
            _authState.value = AuthState.Error("Todos los campos son requeridos")
            return
        }
        
        if (password.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    // Actualizar perfil con el nombre
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    _authState.value = AuthState.Success(user)
                } ?: run {
                    _authState.value = AuthState.Error("Error al crear la cuenta")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("already in use") == true -> "El email ya está registrado"
                        e.message?.contains("invalid email") == true -> "Email inválido"
                        e.message?.contains("weak password") == true -> "Contraseña muy débil"
                        else -> e.message ?: "Error al registrar"
                    }
                )
            }
        }
    }
    
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Initial
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun resetState() {
        _authState.value = AuthState.Initial
    }
}

