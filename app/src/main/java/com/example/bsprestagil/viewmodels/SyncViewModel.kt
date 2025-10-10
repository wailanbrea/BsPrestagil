package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SyncStatus(
    val clientesPendientes: Int = 0,
    val prestamosPendientes: Int = 0,
    val pagosPendientes: Int = 0,
    val garantiasPendientes: Int = 0,
    val ultimaSync: Long = 0L
)

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val clienteRepository = ClienteRepository(database.clienteDao())
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val pagoRepository = PagoRepository(database.pagoDao())
    private val garantiaRepository = GarantiaRepository(database.garantiaDao())
    
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    init {
        loadSyncStatus()
    }
    
    fun loadSyncStatus() {
        viewModelScope.launch {
            try {
                val clientesPendientes = clienteRepository.getClientesPendingSync().size
                val prestamosPendientes = prestamoRepository.getPrestamosPendingSync().size
                val pagosPendientes = pagoRepository.getPagosPendingSync().size
                val garantiasPendientes = garantiaRepository.getGarantiasPendingSync().size
                
                _syncStatus.value = SyncStatus(
                    clientesPendientes = clientesPendientes,
                    prestamosPendientes = prestamosPendientes,
                    pagosPendientes = pagosPendientes,
                    garantiasPendientes = garantiasPendientes,
                    ultimaSync = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    fun getTotalPendientes(): Int {
        return _syncStatus.value.let {
            it.clientesPendientes + it.prestamosPendientes + 
            it.pagosPendientes + it.garantiasPendientes
        }
    }
}

