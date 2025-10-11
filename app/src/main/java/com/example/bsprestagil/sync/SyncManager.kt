package com.example.bsprestagil.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncManager {
    
    private const val TAG = "SyncManager"
    private const val SYNC_WORK_NAME = "BsPrestagilSync"
    
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
        Log.d(TAG, "═══════════════════════════════════════════")
        Log.d(TAG, "🔄 INICIANDO SINCRONIZACIÓN MANUAL")
        Log.d(TAG, "═══════════════════════════════════════════")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("manual_sync")
            .build()
        
        WorkManager.getInstance(context).enqueue(syncRequest)
        
        Log.d(TAG, "✅ Trabajo de sincronización encolado")
        Log.d(TAG, "📋 Work ID: ${syncRequest.id}")
        
        return syncRequest.id
    }
    
    /**
     * Cancela la sincronización periódica
     */
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
    }
}

