package com.example.bsprestagil.viewmodels

import android.app.Application
import android.util.Log
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
    
    private val TAG = "SyncViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val clienteRepository = ClienteRepository(database.clienteDao())
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val pagoRepository = PagoRepository(database.pagoDao())
    private val garantiaRepository = GarantiaRepository(database.garantiaDao())
    private val cuotaRepository = CuotaRepository(database.cuotaDao())
    
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    init {
        Log.d(TAG, "🔧 SyncViewModel inicializado")
        loadSyncStatus()
    }
    
    fun loadSyncStatus() {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🔄 loadSyncStatus() INICIADO")
        Log.d(TAG, "⏰ Timestamp: ${System.currentTimeMillis()}")
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "📥 Obteniendo contadores de base de datos...")
                
                val clientesPendientesList = clienteRepository.getClientesPendingSync()
                val clientesPendientes = clientesPendientesList.size
                Log.d(TAG, "  📝 Clientes pendientes: $clientesPendientes")
                if (clientesPendientes > 0) {
                    Log.d(TAG, "     IDs: ${clientesPendientesList.map { it.id.take(8) }}")
                }
                
                val prestamosPendientesList = prestamoRepository.getPrestamosPendingSync()
                val prestamosPendientes = prestamosPendientesList.size
                Log.d(TAG, "  💰 Préstamos pendientes: $prestamosPendientes")
                if (prestamosPendientes > 0) {
                    Log.d(TAG, "     IDs: ${prestamosPendientesList.map { it.id.take(8) }}")
                }
                
                val pagosPendientesList = pagoRepository.getPagosPendingSync()
                val pagosPendientes = pagosPendientesList.size
                Log.d(TAG, "  💵 Pagos pendientes: $pagosPendientes")
                if (pagosPendientes > 0) {
                    Log.d(TAG, "     IDs: ${pagosPendientesList.map { it.id.take(8) }}")
                }
                
                val garantiasPendientesList = garantiaRepository.getGarantiasPendingSync()
                val garantiasPendientes = garantiasPendientesList.size
                Log.d(TAG, "  🔐 Garantías pendientes: $garantiasPendientes")
                
                val cuotasPendientesList = cuotaRepository.getCuotasPendingSync()
                val cuotasPendientes = cuotasPendientesList.size
                Log.d(TAG, "  📅 Cuotas pendientes: $cuotasPendientes")
                if (cuotasPendientes > 0) {
                    Log.d(TAG, "     IDs: ${cuotasPendientesList.map { it.id.take(8) }}")
                }
                
                val total = clientesPendientes + prestamosPendientes + pagosPendientes + 
                            garantiasPendientes + cuotasPendientes
                
                Log.d(TAG, "📊 TOTAL PENDIENTES: $total")
                
                _syncStatus.value = _syncStatus.value.copy(
                    clientesPendientes = clientesPendientes,
                    prestamosPendientes = prestamosPendientes,
                    pagosPendientes = pagosPendientes,
                    garantiasPendientes = garantiasPendientes,
                    cuotasPendientes = cuotasPendientes,
                    ultimaSync = System.currentTimeMillis(),
                    enSincronizacion = false
                )
                
                Log.d(TAG, "✅ Estado actualizado: ${_syncStatus.value}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en loadSyncStatus: ${e.message}", e)
                _syncStatus.value = _syncStatus.value.copy(enSincronizacion = false)
            }
        }
    }
    
    fun iniciarSincronizacion() {
        Log.d(TAG, "🚀 iniciarSincronizacion() - Cambiando estado a 'en sincronización'")
        _syncStatus.value = _syncStatus.value.copy(enSincronizacion = true)
    }
}

