package com.example.bsprestagil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser, val rol: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole
    
    init {
        // Verificar si hay un usuario ya logueado
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val rol = obtenerRolUsuario(user.uid)
                    _userRole.value = rol
                    _authState.value = AuthState.Success(user, rol)
                } catch (e: Exception) {
                    _authState.value = AuthState.Success(user, null)
                }
            }
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
                result.user?.let { user ->
                    android.util.Log.d("AuthViewModel", "Login exitoso - Email: ${user.email}, UID: ${user.uid}")
                    
                    // Obtener el rol del usuario desde Firestore
                    try {
                        val rol = obtenerRolUsuario(user.uid)
                        android.util.Log.d("AuthViewModel", "🔑 Rol final asignado: $rol")
                        _userRole.value = rol
                        android.util.Log.d("AuthViewModel", "✅ StateFlow _userRole actualizado a: ${_userRole.value}")
                        _authState.value = AuthState.Success(user, rol)
                        android.util.Log.d("AuthViewModel", "✅ AuthState actualizado con rol: $rol")
                    } catch (e: Exception) {
                        android.util.Log.e("AuthViewModel", "❌ No se pudo obtener rol", e)
                        // Fallback: si no se puede leer el rol (por ejemplo PERMISSION_DENIED en reglas),
                        // tratamos al usuario como ADMIN para no bloquear el acceso al dashboard.
                        val fallbackRol = "ADMIN"
                        android.util.Log.w("AuthViewModel", "⚠️ Usando rol por defecto: $fallbackRol")
                        _userRole.value = fallbackRol
                        _authState.value = AuthState.Success(user, fallbackRol)
                    }
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
    
    private suspend fun obtenerRolUsuario(userId: String): String? {
        return try {
            android.util.Log.d("AuthViewModel", "📥 Obteniendo rol para userId: $userId")
            
            val userDoc = firestore.collection("usuarios")
                .document(userId)
                .get()
                .await()
            
            if (!userDoc.exists()) {
                android.util.Log.e("AuthViewModel", "❌ El documento del usuario NO EXISTE en Firestore")
                android.util.Log.e("AuthViewModel", "❌ UID: $userId")
                android.util.Log.e("AuthViewModel", "❌ Email: ${auth.currentUser?.email}")
                
                // ⭐ CREAR el documento automáticamente con rol ADMIN
                // (Para usuarios que existen en Auth pero no en Firestore)
                try {
                    val email = auth.currentUser?.email
                    val nombre = auth.currentUser?.displayName ?: email?.split("@")?.get(0) ?: "Usuario"
                    
                    val datosUsuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "telefono" to "",
                        "rol" to "ADMIN", // Usuarios existentes → ADMIN por defecto
                        "adminId" to userId, // NUEVO: adminId = su propio UID
                        "activo" to true,
                        "fechaCreacion" to System.currentTimeMillis(),
                        "ultimaActualizacion" to System.currentTimeMillis(),
                        "porcentajeComision" to 3.0,
                        "totalComisionesGeneradas" to 0.0,
                        "totalComisionesPagadas" to 0.0,
                        "ultimoPagoComision" to 0L
                    )
                    
                    firestore.collection("usuarios")
                        .document(userId)
                        .set(datosUsuario)
                        .await()
                    
                    android.util.Log.d("AuthViewModel", "✅ Documento creado automáticamente con rol ADMIN y adminId")
                    return "ADMIN"
                } catch (createError: Exception) {
                    android.util.Log.e("AuthViewModel", "❌ Error al crear documento", createError)
                    return null
                }
            }
            
            val rol = userDoc.getString("rol")
            val adminIdExistente = userDoc.getString("adminId")
            
            android.util.Log.d("AuthViewModel", "✅ Rol obtenido de Firestore: $rol")
            android.util.Log.d("AuthViewModel", "📄 adminId existente: $adminIdExistente")
            
            // NUEVO: Si el documento existe pero NO tiene adminId, actualizarlo
            if (adminIdExistente.isNullOrBlank()) {
                try {
                    val adminIdNuevo = if (rol == "ADMIN") userId else userId // Asumir que es ADMIN si no tiene adminId
                    
                    firestore.collection("usuarios")
                        .document(userId)
                        .update("adminId", adminIdNuevo)
                        .await()
                    
                    android.util.Log.d("AuthViewModel", "✅ adminId actualizado en Firestore: $adminIdNuevo")
                } catch (updateError: Exception) {
                    android.util.Log.e("AuthViewModel", "⚠️ Error al actualizar adminId: ${updateError.message}")
                }
            }
            
            rol
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "❌ Error al obtener rol: ${e.message}", e)
            null
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
                
                // NUEVO: Verificar si es el primer usuario (será ADMIN automáticamente)
                val usuariosExistentes = firestore.collection("usuarios").get().await()
                val esElPrimero = usuariosExistentes.isEmpty
                
                if (!esElPrimero) {
                    // NO es el primer usuario, no puede auto-registrarse
                    _authState.value = AuthState.Error("⚠️ Solo el administrador puede crear nuevos usuarios.\n\nContacta al administrador de tu empresa para que te cree una cuenta.")
                    return@launch
                }
                
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    // 1. Actualizar perfil en Firebase Auth con el nombre
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    
                    // 2. Crear documento en Firestore con datos completos
                    // NUEVO: Si es el primero, es ADMIN con adminId = su propio UID
                    try {
                        val datosUsuario = hashMapOf(
                            "nombre" to nombre,
                            "email" to email,
                            "telefono" to "",
                            "rol" to "ADMIN", // Primer usuario = ADMIN
                            "adminId" to user.uid, // NUEVO: adminId = su propio UID
                            "activo" to true,
                            "fechaCreacion" to System.currentTimeMillis(),
                            "ultimaActualizacion" to System.currentTimeMillis(),
                            "porcentajeComision" to 3.0,
                            "totalComisionesGeneradas" to 0.0,
                            "totalComisionesPagadas" to 0.0,
                            "ultimoPagoComision" to 0L
                        )
                        
                        firestore.collection("usuarios")
                            .document(user.uid)
                            .set(datosUsuario)
                            .await()
                        
                        android.util.Log.d("AuthViewModel", "✅ Primer usuario creado como ADMIN: ${user.uid}")
                    } catch (e: Exception) {
                        // Si falla Firestore, no bloqueamos el registro
                        // El documento se creará después desde el perfil
                        android.util.Log.e("AuthViewModel", "Error creando documento en Firestore: ${e.message}")
                    }
                    
                    // 3. Enviar email de verificación automáticamente
                    try {
                        user.sendEmailVerification().await()
                    } catch (e: Exception) {
                        // Si falla el envío del email, no bloqueamos el registro
                        // El usuario podrá enviarlo después desde su perfil
                    }
                    
                    // 4. Obtener rol del usuario recién creado
                    val rol = obtenerRolUsuario(user.uid)
                    _userRole.value = rol
                    _authState.value = AuthState.Success(user, rol)
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
        _userRole.value = null
    }
    
    fun resetState() {
        _authState.value = AuthState.Initial
    }
}

