package com.example.bsprestagil.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    
    private const val NOTIFICATION_WORK_NAME = "notification_check_work"
    
    /**
     * Programa verificaciones periódicas de notificaciones
     * Se ejecuta cada 6 horas
     */
    fun scheduleNotificationCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(
            6, TimeUnit.HOURS,
            30, TimeUnit.MINUTES // Ventana de flexibilidad
        )
            .setConstraints(constraints)
            .addTag("notifications")
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                15, TimeUnit.MINUTES
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOTIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )
    }
    
    /**
     * Ejecuta una verificación inmediata de notificaciones
     */
    fun checkNow(context: Context) {
        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .addTag("notifications_immediate")
            .build()
        
        WorkManager.getInstance(context).enqueue(notificationWork)
    }
    
    /**
     * Cancela todas las verificaciones programadas
     */
    fun cancelNotificationChecks(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(NOTIFICATION_WORK_NAME)
    }
}

