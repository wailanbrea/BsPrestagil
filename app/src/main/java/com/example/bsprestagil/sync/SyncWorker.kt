package com.example.bsprestagil.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.*
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
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Verificar conectividad antes de sincronizar
            if (!isNetworkAvailable()) {
                return@withContext Result.retry()
            }
            
            // Sincronizar cada entidad
            syncClientes()
            syncPrestamos()
            syncPagos()
            syncGarantias()
            syncConfiguracion()
            
            Result.success()
        } catch (e: Exception) {
            // Si hay error, reintentar
            Result.retry()
        }
    }
    
    private suspend fun syncClientes() {
        try {
            val clientesPending = clienteRepository.getClientesPendingSync()
            // TODO: Cuando Firebase esté configurado, enviar a Firestore
            // Por ahora solo marcamos como sincronizados (simulación)
            clientesPending.forEach { cliente ->
                // firebaseSync.syncCliente(cliente)
                // clienteRepository.markAsSynced(cliente.id)
            }
        } catch (e: Exception) {
            // Log error pero continuar con otras entidades
        }
    }
    
    private suspend fun syncPrestamos() {
        try {
            val prestamosPending = prestamoRepository.getPrestamosPendingSync()
            // TODO: Cuando Firebase esté configurado, enviar a Firestore
            prestamosPending.forEach { prestamo ->
                // firebaseSync.syncPrestamo(prestamo)
                // prestamoRepository.markAsSynced(prestamo.id)
            }
        } catch (e: Exception) {
            // Log error pero continuar
        }
    }
    
    private suspend fun syncPagos() {
        try {
            val pagosPending = pagoRepository.getPagosPendingSync()
            // TODO: Cuando Firebase esté configurado, enviar a Firestore
            pagosPending.forEach { pago ->
                // firebaseSync.syncPago(pago)
                // pagoRepository.markAsSynced(pago.id)
            }
        } catch (e: Exception) {
            // Log error pero continuar
        }
    }
    
    private suspend fun syncGarantias() {
        try {
            val garantiasPending = garantiaRepository.getGarantiasPendingSync()
            // TODO: Cuando Firebase esté configurado, enviar a Firestore
            garantiasPending.forEach { garantia ->
                // firebaseSync.syncGarantia(garantia)
                // garantiaRepository.markAsSynced(garantia.id)
            }
        } catch (e: Exception) {
            // Log error pero continuar
        }
    }
    
    private suspend fun syncConfiguracion() {
        try {
            // TODO: Cuando Firebase esté configurado, sincronizar configuración
            // val config = configuracionRepository.getConfiguracionSync()
            // if (config?.pendingSync == true) {
            //     firebaseSync.syncConfiguracion(config)
            //     configuracionRepository.markAsSynced()
            // }
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

