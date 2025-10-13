package com.example.bsprestagil.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.*
import com.example.bsprestagil.firebase.FirebaseService
import com.example.bsprestagil.firebase.FirebaseToRoomSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val TAG = "SyncWorker"
    private val database = AppDatabase.getDatabase(context)
    private val clienteRepository = ClienteRepository(database.clienteDao())
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val pagoRepository = PagoRepository(database.pagoDao())
    private val cuotaRepository = CuotaRepository(database.cuotaDao())
    private val garantiaRepository = GarantiaRepository(database.garantiaDao())
    private val configuracionRepository = ConfiguracionRepository(database.configuracionDao())
    private val firebaseService = FirebaseService()
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "Iniciando sincronización")
        
        return@withContext try {
            // Verificar conectividad antes de sincronizar
            if (!isNetworkAvailable()) {
                Log.w(TAG, "Sin conexión a internet - Reintentando más tarde")
                return@withContext Result.retry()
            }
            
            // Sincronizar cada entidad (subir cambios locales a Firebase)
            syncClientes()
            syncPrestamos()
            syncPagos()
            syncCuotas()
            syncGarantias()
            syncConfiguracion()
            
            // Descargar cambios de Firebase a local (sincronización bidireccional)
            downloadFromFirebase()
            
            Log.i(TAG, "Sincronización completada exitosamente")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización: ${e.message}", e)
            Result.retry()
        }
    }
    
    private suspend fun syncClientes() {
        try {
            val clientesPending = clienteRepository.getClientesPendingSync()
            if (clientesPending.isEmpty()) return
            
            var sincronizados = 0
            var fallidos = 0
            
            clientesPending.forEach { cliente ->
                try {
                    val result = firebaseService.syncCliente(cliente)
                    if (result.isSuccess) {
                        val rowsAffected = clienteRepository.markAsSynced(cliente.id)
                        if (rowsAffected > 0) {
                            sincronizados++
                        } else {
                            fallidos++
                            Log.w(TAG, "Cliente ${cliente.nombre} no se actualizó en BD")
                        }
                    } else {
                        fallidos++
                        Log.w(TAG, "Error al sincronizar cliente ${cliente.nombre}: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "Error al sincronizar cliente ${cliente.nombre}: ${e.message}")
                }
            }
            
            Log.i(TAG, "Clientes: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncClientes: ${e.message}", e)
        }
    }
    
    private suspend fun syncPrestamos() {
        try {
            val prestamosPending = prestamoRepository.getPrestamosPendingSync()
            if (prestamosPending.isEmpty()) return
            
            var sincronizados = 0
            var fallidos = 0
            
            prestamosPending.forEach { prestamo ->
                try {
                    val result = firebaseService.syncPrestamo(prestamo)
                    if (result.isSuccess) {
                        val rowsAffected = prestamoRepository.markAsSynced(prestamo.id)
                        if (rowsAffected > 0) {
                            sincronizados++
                        } else {
                            fallidos++
                            Log.w(TAG, "Préstamo ${prestamo.id.take(8)} no se actualizó en BD")
                        }
                    } else {
                        fallidos++
                        Log.w(TAG, "Error al sincronizar préstamo: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "Error en préstamo: ${e.message}")
                }
            }
            
            Log.i(TAG, "Préstamos: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncPrestamos: ${e.message}", e)
        }
    }
    
    private suspend fun syncPagos() {
        try {
            val pagosPending = pagoRepository.getPagosPendingSync()
            if (pagosPending.isEmpty()) return
            
            var sincronizados = 0
            var fallidos = 0
            
            pagosPending.forEach { pago ->
                try {
                    val result = firebaseService.syncPago(pago)
                    if (result.isSuccess) {
                        val rowsAffected = pagoRepository.markAsSynced(pago.id)
                        if (rowsAffected > 0) {
                            sincronizados++
                        } else {
                            fallidos++
                            Log.w(TAG, "Pago ${pago.id.take(8)} no se actualizó en BD")
                        }
                    } else {
                        fallidos++
                        Log.w(TAG, "Error al sincronizar pago: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "Error en pago: ${e.message}")
                }
            }
            
            Log.i(TAG, "Pagos: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncPagos: ${e.message}", e)
        }
    }
    
    private suspend fun syncCuotas() {
        try {
            val cuotasPending = cuotaRepository.getCuotasPendingSync()
            if (cuotasPending.isEmpty()) return
            
            var sincronizados = 0
            var fallidos = 0
            
            cuotasPending.forEach { cuota ->
                try {
                    val result = firebaseService.syncCuota(cuota)
                    if (result.isSuccess) {
                        val rowsAffected = cuotaRepository.markAsSynced(cuota.id)
                        if (rowsAffected > 0) {
                            sincronizados++
                        } else {
                            fallidos++
                            Log.w(TAG, "Cuota ${cuota.id.take(8)} no se actualizó en BD")
                        }
                    } else {
                        fallidos++
                        Log.w(TAG, "Error al sincronizar cuota: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "Error en cuota: ${e.message}")
                }
            }
            
            Log.i(TAG, "Cuotas: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncCuotas: ${e.message}", e)
        }
    }
    
    private suspend fun syncGarantias() {
        try {
            val garantiasPending = garantiaRepository.getGarantiasPendingSync()
            if (garantiasPending.isEmpty()) return
            
            var sincronizados = 0
            var fallidos = 0
            
            garantiasPending.forEach { garantia ->
                try {
                    val result = firebaseService.syncGarantia(garantia)
                    if (result.isSuccess) {
                        garantiaRepository.markAsSynced(garantia.id)
                        sincronizados++
                    } else {
                        fallidos++
                        Log.w(TAG, "Error al sincronizar garantía: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "Error en garantía: ${e.message}")
                }
            }
            
            Log.i(TAG, "Garantías: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncGarantias: ${e.message}", e)
        }
    }
    
    private suspend fun syncConfiguracion() {
        try {
            val config = configuracionRepository.getConfiguracionSync()
            if (config?.pendingSync == true) {
                val result = firebaseService.syncConfiguracion(config)
                if (result.isSuccess) {
                    configuracionRepository.markAsSynced()
                    Log.i(TAG, "Configuración sincronizada")
                } else {
                    Log.w(TAG, "Error al sincronizar configuración: ${result.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en syncConfiguracion: ${e.message}", e)
        }
    }
    
    private suspend fun downloadFromFirebase() {
        try {
            val firebaseToRoomSync = FirebaseToRoomSync(
                clienteRepository = clienteRepository,
                prestamoRepository = prestamoRepository,
                pagoRepository = pagoRepository,
                cuotaRepository = cuotaRepository,
                garantiaRepository = garantiaRepository,
                configuracionRepository = configuracionRepository
            )
            firebaseToRoomSync.fullSync()
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando desde Firebase: ${e.message}", e)
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

