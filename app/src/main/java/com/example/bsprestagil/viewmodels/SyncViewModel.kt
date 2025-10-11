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
    val cuotasPendientes: Int = 0,
    val ultimaSync: Long = 0L,
    val enSincronizacion: Boolean = false
) {
    val totalPendientes: Int
        get() = clientesPendientes + prestamosPendientes + pagosPendientes + 
                garantiasPendientes + cuotasPendientes
}

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val clienteRepository = ClienteRepository(database.clienteDao())
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val pagoRepository = PagoRepository(database.pagoDao())
    private val garantiaRepository = GarantiaRepository(database.garantiaDao())
    private val cuotaRepository = CuotaRepository(database.cuotaDao())
    
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
                val cuotasPendientes = cuotaRepository.getCuotasPendingSync().size
                
                _syncStatus.value = _syncStatus.value.copy(
                    clientesPendientes = clientesPendientes,
                    prestamosPendientes = prestamosPendientes,
                    pagosPendientes = pagosPendientes,
                    garantiasPendientes = garantiasPendientes,
                    cuotasPendientes = cuotasPendientes,
                    ultimaSync = System.currentTimeMillis(),
                    enSincronizacion = false
                )
            } catch (e: Exception) {
                _syncStatus.value = _syncStatus.value.copy(enSincronizacion = false)
            }
        }
    }
    
    fun iniciarSincronizacion() {
        _syncStatus.value = _syncStatus.value.copy(enSincronizacion = true)
    }
}

