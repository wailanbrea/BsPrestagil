package com.example.bsprestagil.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bsprestagil.MainActivity
import com.example.bsprestagil.R

object AppNotificationManager {
    
    private const val CHANNEL_ID_PAGOS = "pagos_channel"
    private const val CHANNEL_ID_GENERAL = "general_channel"
    private const val CHANNEL_NAME_PAGOS = "Pagos y Cobros"
    private const val CHANNEL_NAME_GENERAL = "General"
    
    /**
     * Crea los canales de notificación necesarios
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal de pagos (alta prioridad)
            val channelPagos = NotificationChannel(
                CHANNEL_ID_PAGOS,
                CHANNEL_NAME_PAGOS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de pagos vencidos y próximos a vencer"
                enableVibration(true)
                enableLights(true)
            }
            
            // Canal general (prioridad normal)
            val channelGeneral = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
            }
            
            notificationManager.createNotificationChannel(channelPagos)
            notificationManager.createNotificationChannel(channelGeneral)
        }
    }
    
    /**
     * Muestra una notificación de pago vencido
     */
    fun showPagoVencidoNotification(
        context: Context,
        notificationId: Int,
        clienteNombre: String,
        diasVencido: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "payments")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PAGOS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener este recurso
            .setContentTitle("⚠️ Pago Vencido")
            .setContentText("$clienteNombre tiene un pago vencido desde hace $diasVencido día(s)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$clienteNombre tiene un pago vencido desde hace $diasVencido día(s). Contacta al cliente para cobrar."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    /**
     * Muestra una notificación de pago próximo a vencer
     */
    fun showPagoProximoNotification(
        context: Context,
        notificationId: Int,
        clienteNombre: String,
        diasRestantes: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "payments")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PAGOS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📅 Pago Próximo")
            .setContentText("$clienteNombre tiene un pago que vence en $diasRestantes día(s)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$clienteNombre tiene un pago que vence en $diasRestantes día(s). Recuérdale realizar el pago."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    /**
     * Muestra una notificación de pago recibido
     */
    fun showPagoRecibidoNotification(
        context: Context,
        notificationId: Int,
        clienteNombre: String,
        monto: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "payments")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✅ Pago Recibido")
            .setContentText("$clienteNombre pagó $${"%.2f".format(monto)}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    /**
     * Muestra una notificación general
     */
    fun showGeneralNotification(
        context: Context,
        notificationId: Int,
        titulo: String,
        mensaje: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}

