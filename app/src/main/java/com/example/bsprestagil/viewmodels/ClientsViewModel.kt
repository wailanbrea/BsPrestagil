package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.ClienteEntity
import com.example.bsprestagil.data.database.entities.ReferenciaEntity
import com.example.bsprestagil.data.mappers.toCliente
import com.example.bsprestagil.data.models.Cliente
import com.example.bsprestagil.data.repository.ClienteRepository
import com.example.bsprestagil.utils.AuthUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClientsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val clienteRepository = ClienteRepository(database.clienteDao())
    
    // Método actual: Flow<List> para filtros y búsquedas
    // Nota: Para listas sin filtros >100 items, considera usar clientesPaged
    private val _clientes = MutableStateFlow<List<Cliente>>(emptyList())
    val clientes: StateFlow<List<Cliente>> = _clientes.asStateFlow()
    
    // TODO FUTURO: Paging 3 para listas muy grandes (actualmente se usa Flow<List> por los filtros)
    // val clientesPaged = clienteRepository.getAllClientesPaged().cachedIn(viewModelScope)
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _clientesIdsPermitidos = MutableStateFlow<Set<String>?>(null)
    
    init {
        loadClientes()
    }
    
    /**
     * Establece los IDs de clientes que el cobrador puede ver
     * (basado en sus préstamos asignados)
     * @param clientesIds Set de IDs de clientes obtenidos de préstamos del cobrador
     * Nota: Estrategia de filtrado indirecta - se obtienen clienteIds de préstamos donde cobradorId == usuario
     */
    fun setClientesPermitidos(clientesIds: Set<String>?) {
        _clientesIdsPermitidos.value = clientesIds
    }
    
    private fun loadClientes() {
        viewModelScope.launch {
            combine(
                clienteRepository.getAllClientes(),
                _searchQuery,
                _clientesIdsPermitidos
            ) { clientes, query, idsPermitidos ->
                var resultado = clientes
                
                // Filtrar por IDs permitidos si está establecido (para cobradores)
                if (idsPermitidos != null) {
                    resultado = resultado.filter { it.id in idsPermitidos }
                }
                
                // Filtrar por búsqueda
                if (query.isNotBlank()) {
                    resultado = resultado.filter {
                        it.nombre.contains(query, ignoreCase = true) ||
                        it.telefono.contains(query, ignoreCase = true)
                    }
                }
                
                resultado.map { it.toCliente() }
            }.collect { filteredClientes ->
                _clientes.value = filteredClientes
            }
        }
    }
    
    fun searchClientes(query: String) {
        _searchQuery.value = query
    }
    
    fun getClienteById(clienteId: String): Flow<Cliente?> {
        return clienteRepository.getClienteById(clienteId)
            .map { it?.toCliente() }
    }
    
    fun insertCliente(
        nombre: String,
        telefono: String,
        email: String,
        direccion: String,
        referencia1Nombre: String = "",
        referencia1Telefono: String = "",
        referencia1Relacion: String = "",
        referencia2Nombre: String = "",
        referencia2Telefono: String = "",
        referencia2Relacion: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val referencias = mutableListOf<ReferenciaEntity>()
                if (referencia1Nombre.isNotBlank()) {
                    referencias.add(
                        ReferenciaEntity(
                            nombre = referencia1Nombre,
                            telefono = referencia1Telefono,
                            relacion = referencia1Relacion
                        )
                    )
                }
                if (referencia2Nombre.isNotBlank()) {
                    referencias.add(
                        ReferenciaEntity(
                            nombre = referencia2Nombre,
                            telefono = referencia2Telefono,
                            relacion = referencia2Relacion
                        )
                    )
                }
                
                val adminId = AuthUtils.getCurrentAdminId()
                
                val cliente = ClienteEntity(
                    id = "",
                    nombre = nombre,
                    telefono = telefono,
                    email = email,
                    direccion = direccion,
                    fotoUrl = "",
                    referencias = referencias,
                    fechaRegistro = System.currentTimeMillis(),
                    prestamosActivos = 0,
                    historialPagos = "AL_DIA",
                    adminId = adminId // NUEVO: Multi-tenant
                )
                
                clienteRepository.insertCliente(cliente)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                // Manejar error
            }
        }
    }
    
    fun updateCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                clienteRepository.updateCliente(cliente)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                // Manejar error
            }
        }
    }
    
    fun deleteCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            try {
                clienteRepository.deleteCliente(cliente)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    // Eliminar cliente por ID (obtiene el entity completo de Room con firebaseId)
    fun deleteClienteById(clienteId: String) {
        viewModelScope.launch {
            try {
                val adminId = AuthUtils.getCurrentAdminId()
                // Obtener el cliente COMPLETO de Room (con firebaseId)
                val clienteEntity = database.clienteDao().getClienteByIdSync(clienteId, adminId)
                
                if (clienteEntity != null) {
                    android.util.Log.d("ClientsViewModel", "🗑️ Eliminando cliente: ${clienteEntity.nombre}, firebaseId: ${clienteEntity.firebaseId}")
                    clienteRepository.deleteCliente(clienteEntity)
                } else {
                    android.util.Log.w("ClientsViewModel", "⚠️ Cliente no encontrado: $clienteId")
                }
            } catch (e: Exception) {
                android.util.Log.e("ClientsViewModel", "❌ Error eliminando cliente: ${e.message}")
            }
        }
    }
}

