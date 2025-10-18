package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.UsuarioEntity
import com.example.bsprestagil.data.repository.UsuarioRepository
import com.example.bsprestagil.utils.AuthUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UsersViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "UsersViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val usuarioRepository = UsuarioRepository(database.usuarioDao())
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val functions: FirebaseFunctions = Firebase.functions
    
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()
    
    private val _creationError = MutableStateFlow<String?>(null)
    val creationError: StateFlow<String?> = _creationError.asStateFlow()
    
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
            // NUEVO: Obtener adminId del usuario actual
            val currentAdminId = AuthUtils.getCurrentAdminId()
            
            // Si el nuevo usuario es ADMIN, su adminId es su propio ID
            // Si es SUPERVISOR o COBRADOR, hereda el adminId del ADMIN actual
            val usuarioId = UUID.randomUUID().toString()
            val adminId = if (rol == "ADMIN") usuarioId else currentAdminId
            
            val usuario = UsuarioEntity(
                id = usuarioId,
                nombre = nombre,
                email = email,
                telefono = telefono,
                rol = rol,
                activo = true,
                fechaCreacion = System.currentTimeMillis(),
                adminId = adminId // NUEVO: Multi-tenant
            )
            usuarioRepository.insertUsuario(usuario)
        }
    }
    
    /**
     * Crea un usuario con cuenta de Firebase Auth usando Firebase Function
     * ✅ El admin NO pierde su sesión
     * ✅ Más seguro (creación desde el servidor)
     */
    fun crearUsuarioConCuenta(
        nombre: String,
        email: String,
        telefono: String,
        rol: String,
        password: String
    ) {
        viewModelScope.launch {
            try {
                _isCreating.value = true
                _creationError.value = null
                
                Log.i(TAG, "Creando cobrador con Firebase Function: $email")
                
                // ⭐ Validar que hay sesión activa
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    throw Exception("No hay sesión activa")
                }
                
                // ⭐ Llamar a Firebase Function (NO afecta la sesión actual)
                // NUEVO: Obtener adminId del usuario actual
                val currentAdminId = AuthUtils.getCurrentAdminId()
                
                val crearCobradorFn = functions.getHttpsCallable("crearCobrador")
                
                val data = hashMapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "telefono" to telefono,
                    "rol" to rol,
                    "password" to password,
                    "adminId" to currentAdminId // NUEVO: Multi-tenant
                )
                
                val result = crearCobradorFn.call(data).await()
                val response = result.data as Map<*, *>
                
                val success = response["success"] as? Boolean ?: false
                val uid = response["uid"] as? String
                
                if (success && uid != null) {
                    Log.i(TAG, "✅ Cobrador creado exitosamente. UID: $uid")
                    Log.i(TAG, "✅ El admin mantiene su sesión: ${currentUser.email}")
                    
                    // NUEVO: Obtener adminId del usuario actual
                    val currentAdminId = AuthUtils.getCurrentAdminId()
                    
                    // Si el nuevo usuario es ADMIN, su adminId es su propio UID
                    // Si es SUPERVISOR o COBRADOR, hereda el adminId del ADMIN actual
                    val adminIdNuevo = if (rol == "ADMIN") uid else currentAdminId
                    
                    // Guardar en Room local para sincronización
                    val usuarioLocal = UsuarioEntity(
                        id = uid,
                        nombre = nombre,
                        email = email,
                        telefono = telefono,
                        rol = rol,
                        activo = true,
                        fechaCreacion = System.currentTimeMillis(),
                        adminId = adminIdNuevo // NUEVO: Multi-tenant
                    )
                    usuarioRepository.insertUsuario(usuarioLocal)
                    
                    _isCreating.value = false
                    
                } else {
                    throw Exception("La función no retornó datos válidos")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear cobrador: ${e.message}", e)
                
                _creationError.value = when {
                    e.message?.contains("already-exists") == true || 
                    e.message?.contains("already") == true || 
                    e.message?.contains("already in use") == true -> 
                        "Este email ya está registrado"
                    e.message?.contains("permission-denied") == true ->
                        "No tienes permisos para crear cobradores"
                    e.message?.contains("unauthenticated") == true ->
                        "No estás autenticado. Vuelve a iniciar sesión"
                    e.message?.contains("invalid-argument") == true ->
                        e.message?.substringAfter("invalid-argument: ") ?: "Datos inválidos"
                    e.message?.contains("network") == true || 
                    e.message?.contains("UNAVAILABLE") == true -> 
                        "Error de conexión. Verifica tu internet"
                    e.message?.contains("No hay sesión") == true ->
                        "No hay sesión activa. Vuelve a iniciar sesión"
                    else -> 
                        "Error al crear cuenta: ${e.message}"
                }
                _isCreating.value = false
            }
        }
    }
    
    fun actualizarUsuario(usuario: UsuarioEntity) {
        viewModelScope.launch {
            usuarioRepository.updateUsuario(usuario)
        }
    }
    
    /**
     * Actualiza los datos de un cobrador
     */
    fun actualizarCobrador(
        usuarioId: String,
        nombre: String,
        telefono: String,
        rol: String,
        porcentajeComision: Float
    ) {
        viewModelScope.launch {
            try {
                val usuario = database.usuarioDao().getUsuarioByIdSync(usuarioId)
                usuario?.let { u ->
                    val usuarioActualizado = u.copy(
                        nombre = nombre,
                        telefono = telefono,
                        rol = rol,
                        porcentajeComision = porcentajeComision
                    )
                    usuarioRepository.updateUsuario(usuarioActualizado)
                    
                    // También actualizar en Firestore
                    firestore.collection("usuarios")
                        .document(usuarioId)
                        .update(
                            mapOf(
                                "nombre" to nombre,
                                "telefono" to telefono,
                                "rol" to rol,
                                "porcentajeComision" to porcentajeComision,
                                "fechaActualizacion" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    
                    Log.i(TAG, "Cobrador actualizado exitosamente: $usuarioId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar cobrador: ${e.message}", e)
            }
        }
    }
    
    fun eliminarUsuario(usuario: UsuarioEntity) {
        viewModelScope.launch {
            usuarioRepository.deleteUsuario(usuario)
        }
    }
    
    fun marcarComisionPagada(usuarioId: String, monto: Double) {
        viewModelScope.launch {
            try {
                val usuario = database.usuarioDao().getUsuarioByIdSync(usuarioId)
                usuario?.let { u ->
                    val usuarioActualizado = u.copy(
                        totalComisionesPagadas = u.totalComisionesPagadas + monto,
                        ultimoPagoComision = System.currentTimeMillis()
                    )
                    usuarioRepository.updateUsuario(usuarioActualizado)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    fun actualizarPorcentajeComision(usuarioId: String, porcentaje: Float) {
        viewModelScope.launch {
            try {
                val usuario = database.usuarioDao().getUsuarioByIdSync(usuarioId)
                usuario?.let { u ->
                    val usuarioActualizado = u.copy(porcentajeComision = porcentaje)
                    usuarioRepository.updateUsuario(usuarioActualizado)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    /**
     * Sincroniza usuarios desde Firestore a Room
     */
    fun syncUsuariosFromFirestore() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sincronizando usuarios desde Firestore...")
                val snapshot = firestore.collection("usuarios").get().await()
                
                val usuariosFirestore = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        UsuarioEntity(
                            id = doc.id,
                            nombre = data["nombre"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            telefono = data["telefono"] as? String ?: "",
                            rol = data["rol"] as? String ?: "COBRADOR",
                            activo = data["activo"] as? Boolean ?: true,
                            fechaCreacion = data["fechaCreacion"] as? Long ?: System.currentTimeMillis(),
                            adminId = data["adminId"] as? String ?: doc.id, // NUEVO: Multi-tenant
                            porcentajeComision = (data["porcentajeComision"] as? Number)?.toFloat() ?: 3.0f,
                            totalComisionesGeneradas = (data["totalComisionesGeneradas"] as? Number)?.toDouble() ?: 0.0,
                            totalComisionesPagadas = (data["totalComisionesPagadas"] as? Number)?.toDouble() ?: 0.0,
                            ultimoPagoComision = (data["ultimoPagoComision"] as? Long) ?: 0L,
                            pendingSync = false,
                            lastSyncTime = System.currentTimeMillis(),
                            firebaseId = doc.id
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear usuario ${doc.id}: ${e.message}")
                        null
                    }
                }
                
                // Insertar o actualizar en Room
                usuariosFirestore.forEach { usuario ->
                    usuarioRepository.insertUsuario(usuario)
                }
                
                Log.d(TAG, "✅ ${usuariosFirestore.size} usuarios sincronizados desde Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar usuarios: ${e.message}", e)
            }
        }
    }
    
    // Eliminar usuario (solo ADMIN)
    fun deleteUsuario(usuario: UsuarioEntity) {
        viewModelScope.launch {
            usuarioRepository.deleteUsuario(usuario)
        }
    }
}

