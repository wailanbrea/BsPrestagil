package com.example.bsprestagil.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncManager {
    
    private const val TAG = "SyncManager"
    private const val SYNC_WORK_NAME = "BsPrestagilSync"
    private const val EXTENSION_PLAZO_WORK_NAME = "ExtensionPlazoWorker" // NUEVO
    
    /**
     * Configura la sincronización periódica
     * Se ejecuta cada 15 minutos cuando hay conexión
     */
    fun setupPeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * Fuerza una sincronización inmediata
     * @return ID del trabajo para poder observarlo
     */
    fun forceSyncNow(context: Context): java.util.UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("manual_sync")
            .build()
        
        WorkManager.getInstance(context).enqueue(syncRequest)
        Log.i(TAG, "Sincronización manual iniciada")
        
        return syncRequest.id
    }
    
    /**
     * Cancela la sincronización periódica
     */
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * NUEVO: Configura el worker de extensión automática de préstamos
     * Se ejecuta diariamente a las 2:00 AM para generar cuotas adicionales
     */
    fun setupExtensionPlazoWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Requiere internet para sincronizar
            .build()
        
        val extensionRequest = PeriodicWorkRequestBuilder<ExtensionPlazoWorker>(
            1, TimeUnit.DAYS // Ejecutar diariamente
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateDelayUntil2AM(), TimeUnit.MILLISECONDS) // Ejecutar a las 2 AM
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EXTENSION_PLAZO_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            extensionRequest
        )
        
        Log.i(TAG, "✅ ExtensionPlazoWorker programado (diariamente)")
    }
    
    /**
     * Calcula el delay hasta las 2:00 AM del siguiente día
     */
    private fun calculateDelayUntil2AM(): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 2)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // Si ya pasaron las 2 AM, programar para mañana
            if (before(now)) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        return target.timeInMillis - now.timeInMillis
    }
    
    /**
     * Fuerza la ejecución inmediata del worker de extensión de plazos
     * Útil para testing o activación manual
     */
    fun forceExtensionPlazoNow(context: Context): java.util.UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val extensionRequest = OneTimeWorkRequestBuilder<ExtensionPlazoWorker>()
            .setConstraints(constraints)
            .addTag("manual_extension")
            .build()
        
        WorkManager.getInstance(context).enqueue(extensionRequest)
        Log.i(TAG, "🔄 Extensión de plazos manual iniciada")
        
        return extensionRequest.id
    }
}

