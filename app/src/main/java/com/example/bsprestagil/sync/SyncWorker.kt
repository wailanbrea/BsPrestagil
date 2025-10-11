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
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "⚙️ SyncWorker.doWork() INICIADO")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        return@withContext try {
            // Verificar conectividad antes de sincronizar
            if (!isNetworkAvailable()) {
                Log.w(TAG, "❌ Sin conexión a internet - Reintentando más tarde")
                return@withContext Result.retry()
            }
            
            Log.d(TAG, "✅ Conexión a internet disponible")
            
            // Sincronizar cada entidad (subir cambios locales a Firebase)
            syncClientes()
            syncPrestamos()
            syncPagos()
            syncCuotas()
            syncGarantias()
            syncConfiguracion()
            
            // Descargar cambios de Firebase a local (sincronización bidireccional)
            downloadFromFirebase()
            
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "✅ SyncWorker.doWork() COMPLETADO EXITOSAMENTE")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.e(TAG, "❌ ERROR en SyncWorker.doWork()")
            Log.e(TAG, "❌ Exception: ${e.message}")
            Log.e(TAG, "❌ Stack trace:", e)
            Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            // Si hay error, reintentar
            Result.retry()
        }
    }
    
    private suspend fun syncClientes() {
        try {
            val clientesPending = clienteRepository.getClientesPendingSync()
            Log.d(TAG, "📝 Sincronizando CLIENTES: ${clientesPending.size} pendientes")
            
            var sincronizados = 0
            var fallidos = 0
            
            clientesPending.forEach { cliente ->
                try {
                    val result = firebaseService.syncCliente(cliente)
                    if (result.isSuccess) {
                        Log.d(TAG, "  🔄 Marcando cliente ${cliente.id.take(8)} como sincronizado...")
                        clienteRepository.markAsSynced(cliente.id)
                        sincronizados++
                        Log.d(TAG, "  ✅ Cliente ${cliente.nombre} sincronizado y marcado en BD")
                    } else {
                        fallidos++
                        Log.w(TAG, "  ⚠️ Cliente ${cliente.nombre} falló: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "  ❌ Error al sincronizar cliente ${cliente.nombre}: ${e.message}")
                }
            }
            
            Log.d(TAG, "📊 CLIENTES: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error general en syncClientes: ${e.message}", e)
        }
    }
    
    private suspend fun syncPrestamos() {
        try {
            val prestamosPending = prestamoRepository.getPrestamosPendingSync()
            Log.d(TAG, "💰 Sincronizando PRÉSTAMOS: ${prestamosPending.size} pendientes")
            
            var sincronizados = 0
            var fallidos = 0
            
            prestamosPending.forEach { prestamo ->
                try {
                    val result = firebaseService.syncPrestamo(prestamo)
                    if (result.isSuccess) {
                        prestamoRepository.markAsSynced(prestamo.id)
                        sincronizados++
                    } else {
                        fallidos++
                        Log.w(TAG, "  ⚠️ Préstamo ${prestamo.id.take(8)} falló")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "  ❌ Error en préstamo: ${e.message}")
                }
            }
            
            Log.d(TAG, "📊 PRÉSTAMOS: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error general en syncPrestamos: ${e.message}", e)
        }
    }
    
    private suspend fun syncPagos() {
        try {
            val pagosPending = pagoRepository.getPagosPendingSync()
            Log.d(TAG, "💵 Sincronizando PAGOS: ${pagosPending.size} pendientes")
            
            var sincronizados = 0
            var fallidos = 0
            
            pagosPending.forEach { pago ->
                try {
                    val result = firebaseService.syncPago(pago)
                    if (result.isSuccess) {
                        pagoRepository.markAsSynced(pago.id)
                        sincronizados++
                    } else {
                        fallidos++
                        Log.w(TAG, "  ⚠️ Pago ${pago.id.take(8)} falló")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "  ❌ Error en pago: ${e.message}")
                }
            }
            
            Log.d(TAG, "📊 PAGOS: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error general en syncPagos: ${e.message}", e)
        }
    }
    
    private suspend fun syncCuotas() {
        try {
            val cuotasPending = cuotaRepository.getCuotasPendingSync()
            Log.d(TAG, "📅 Sincronizando CUOTAS: ${cuotasPending.size} pendientes")
            
            var sincronizados = 0
            var fallidos = 0
            
            cuotasPending.forEach { cuota ->
                try {
                    val result = firebaseService.syncCuota(cuota)
                    if (result.isSuccess) {
                        cuotaRepository.markAsSynced(cuota.id)
                        sincronizados++
                    } else {
                        fallidos++
                        Log.w(TAG, "  ⚠️ Cuota ${cuota.id.take(8)} falló")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "  ❌ Error en cuota: ${e.message}")
                }
            }
            
            Log.d(TAG, "📊 CUOTAS: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error general en syncCuotas: ${e.message}", e)
        }
    }
    
    private suspend fun syncGarantias() {
        try {
            val garantiasPending = garantiaRepository.getGarantiasPendingSync()
            Log.d(TAG, "🔐 Sincronizando GARANTÍAS: ${garantiasPending.size} pendientes")
            
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
                        Log.w(TAG, "  ⚠️ Garantía ${garantia.id.take(8)} falló")
                    }
                } catch (e: Exception) {
                    fallidos++
                    Log.e(TAG, "  ❌ Error en garantía: ${e.message}")
                }
            }
            
            Log.d(TAG, "📊 GARANTÍAS: $sincronizados sincronizados, $fallidos fallidos")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error general en syncGarantias: ${e.message}", e)
        }
    }
    
    private suspend fun syncConfiguracion() {
        try {
            val config = configuracionRepository.getConfiguracionSync()
            Log.d(TAG, "⚙️ Sincronizando CONFIGURACIÓN")
            
            if (config?.pendingSync == true) {
                val result = firebaseService.syncConfiguracion(config)
                if (result.isSuccess) {
                    configuracionRepository.markAsSynced()
                    Log.d(TAG, "  ✅ Configuración sincronizada")
                } else {
                    Log.w(TAG, "  ⚠️ Configuración falló: ${result.exceptionOrNull()?.message}")
                }
            } else {
                Log.d(TAG, "  ℹ️ No hay cambios en configuración")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en syncConfiguracion: ${e.message}", e)
        }
    }
    
    private suspend fun downloadFromFirebase() {
        try {
            Log.d(TAG, "⬇️ DESCARGANDO CAMBIOS DESDE FIREBASE")
            
            val firebaseToRoomSync = FirebaseToRoomSync(
                clienteRepository = clienteRepository,
                prestamoRepository = prestamoRepository,
                pagoRepository = pagoRepository,
                cuotaRepository = cuotaRepository,
                garantiaRepository = garantiaRepository,
                configuracionRepository = configuracionRepository
            )
            firebaseToRoomSync.fullSync()
            
            Log.d(TAG, "✅ Descarga desde Firebase completada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en downloadFromFirebase: ${e.message}", e)
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

