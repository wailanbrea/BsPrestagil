package com.example.bsprestagil.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.*
import com.example.bsprestagil.firebase.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val database = AppDatabase.getDatabase(context)
    private val clienteRepository = ClienteRepository(database.clienteDao())
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val pagoRepository = PagoRepository(database.pagoDao())
    private val garantiaRepository = GarantiaRepository(database.garantiaDao())
    private val configuracionRepository = ConfiguracionRepository(database.configuracionDao())
    private val firebaseService = FirebaseService()
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Verificar conectividad antes de sincronizar
            if (!isNetworkAvailable()) {
                return@withContext Result.retry()
            }
            
            // Sincronizar cada entidad (subir cambios locales a Firebase)
            syncClientes()
            syncPrestamos()
            syncPagos()
            syncGarantias()
            syncConfiguracion()
            
            // Descargar cambios de Firebase a local (sincronización bidireccional)
            downloadFromFirebase()
            
            Result.success()
        } catch (e: Exception) {
            // Si hay error, reintentar
            Result.retry()
        }
    }
    
    private suspend fun syncClientes() {
        try {
            val clientesPending = clienteRepository.getClientesPendingSync()
            clientesPending.forEach { cliente ->
                val result = firebaseService.syncCliente(cliente)
                if (result.isSuccess) {
                    clienteRepository.markAsSynced(cliente.id)
                }
            }
        } catch (e: Exception) {
            // Log error pero continuar con otras entidades
        }
    }
    
    private suspend fun syncPrestamos() {
        try {
            val prestamosPending = prestamoRepository.getPrestamosPendingSync()
            prestamosPending.forEach { prestamo ->
                val result = firebaseService.syncPrestamo(prestamo)
                if (result.isSuccess) {
                    prestamoRepository.markAsSynced(prestamo.id)
                }
            }
        } catch (e: Exception) {
            // Log error pero continuar
        }
    }
    
    private suspend fun syncPagos() {
        try {
            val pagosPending = pagoRepository.getPagosPendingSync()
            pagosPending.forEach { pago ->
                val result = firebaseService.syncPago(pago)
                if (result.isSuccess) {
                    pagoRepository.markAsSynced(pago.id)
                }
            }
        } catch (e: Exception) {
            // Log error pero continuar
        }
    }
    
    private suspend fun syncGarantias() {
        try {
            val garantiasPending = garantiaRepository.getGarantiasPendingSync()
            garantiasPending.forEach { garantia ->
                val result = firebaseService.syncGarantia(garantia)
                if (result.isSuccess) {
                    garantiaRepository.markAsSynced(garantia.id)
                }
            }
        } catch (e: Exception) {
            // Log error pero continuar
        }
    }
    
    private suspend fun syncConfiguracion() {
        try {
            val config = configuracionRepository.getConfiguracionSync()
            if (config?.pendingSync == true) {
                val result = firebaseService.syncConfiguracion(config)
                if (result.isSuccess) {
                    configuracionRepository.markAsSynced()
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }
    
    private suspend fun downloadFromFirebase() {
        try {
            // Por ahora, esta función está preparada para futura implementación
            // de sincronización bidireccional completa desde Firebase a Room
            // TODO: Implementar descarga y merge de datos desde Firestore
        } catch (e: Exception) {
            // Log error
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
            as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

