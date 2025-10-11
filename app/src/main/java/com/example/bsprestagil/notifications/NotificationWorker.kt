package com.example.bsprestagil.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.CuotaRepository
import com.example.bsprestagil.data.repository.NotificacionRepository
import com.example.bsprestagil.data.repository.PrestamoRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val database = AppDatabase.getDatabase(context)
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val cuotaRepository = CuotaRepository(database.cuotaDao())
    private val notificacionRepository = NotificacionRepository(database.notificacionDao())
    
    override suspend fun doWork(): Result {
        return try {
            verificarPagosVencidos()
            verificarPagosProximos()
            limpiarNotificacionesAntiguas()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun verificarPagosVencidos() {
        val prestamosActivos = prestamoRepository.getPrestamosByEstado("ACTIVO").firstOrNull() ?: return
        val ahora = System.currentTimeMillis()
        
        prestamosActivos.forEach { prestamo ->
            val cuotas = cuotaRepository.getCuotasByPrestamoId(prestamo.id).firstOrNull() ?: emptyList()
            
            cuotas.forEach { cuota ->
                if (cuota.estado == "PENDIENTE" && cuota.fechaVencimiento < ahora) {
                    val diasVencido = calcularDiasVencidos(cuota.fechaVencimiento)
                    
                    // Crear notificación solo si no existe una reciente
                    val yaNotificado = existeNotificacionReciente(
                        tipo = "PAGO_VENCIDO",
                        prestamoId = prestamo.id,
                        horas = 24
                    )
                    
                    if (!yaNotificado) {
                        // Crear notificación en la base de datos
                        notificacionRepository.crearNotificacion(
                            titulo = "Pago vencido",
                            mensaje = "${prestamo.clienteNombre} tiene un pago vencido desde hace $diasVencido día(s)",
                            tipo = "PAGO_VENCIDO",
                            prestamoId = prestamo.id,
                            clienteId = prestamo.clienteId
                        )
                        
                        // Mostrar notificación push
                        AppNotificationManager.showPagoVencidoNotification(
                            context = context,
                            notificationId = prestamo.id.hashCode(),
                            clienteNombre = prestamo.clienteNombre,
                            diasVencido = diasVencido
                        )
                    }
                }
            }
        }
    }
    
    private suspend fun verificarPagosProximos() {
        val prestamosActivos = prestamoRepository.getPrestamosByEstado("ACTIVO").firstOrNull() ?: return
        val ahora = System.currentTimeMillis()
        val tresDiasFuturo = ahora + (3 * 24 * 60 * 60 * 1000L)
        
        prestamosActivos.forEach { prestamo ->
            val cuotas = cuotaRepository.getCuotasByPrestamoId(prestamo.id).firstOrNull() ?: emptyList()
            
            cuotas.forEach { cuota ->
                if (cuota.estado == "PENDIENTE" && 
                    cuota.fechaVencimiento > ahora && 
                    cuota.fechaVencimiento <= tresDiasFuturo) {
                    
                    val diasRestantes = calcularDiasRestantes(cuota.fechaVencimiento)
                    
                    // Crear notificación solo si no existe una reciente
                    val yaNotificado = existeNotificacionReciente(
                        tipo = "PAGO_PROXIMO",
                        prestamoId = prestamo.id,
                        horas = 24
                    )
                    
                    if (!yaNotificado) {
                        // Crear notificación en la base de datos
                        notificacionRepository.crearNotificacion(
                            titulo = "Pago próximo a vencer",
                            mensaje = "${prestamo.clienteNombre} tiene un pago que vence en $diasRestantes día(s)",
                            tipo = "PAGO_PROXIMO",
                            prestamoId = prestamo.id,
                            clienteId = prestamo.clienteId
                        )
                        
                        // Mostrar notificación push
                        AppNotificationManager.showPagoProximoNotification(
                            context = context,
                            notificationId = (prestamo.id.hashCode() + 1000),
                            clienteNombre = prestamo.clienteNombre,
                            diasRestantes = diasRestantes
                        )
                    }
                }
            }
        }
    }
    
    private suspend fun limpiarNotificacionesAntiguas() {
        notificacionRepository.limpiarNotificacionesAntiguas(30)
    }
    
    private suspend fun existeNotificacionReciente(
        tipo: String,
        prestamoId: String,
        horas: Int
    ): Boolean {
        val notificaciones = notificacionRepository.getAllNotificaciones().firstOrNull() ?: return false
        val tiempoLimite = System.currentTimeMillis() - (horas * 60 * 60 * 1000L)
        
        return notificaciones.any { 
            it.tipo == tipo && 
            it.prestamoId == prestamoId && 
            it.fecha > tiempoLimite
        }
    }
    
    private fun calcularDiasVencidos(fechaVencimiento: Long): Int {
        val diferencia = System.currentTimeMillis() - fechaVencimiento
        return (diferencia / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
    }
    
    private fun calcularDiasRestantes(fechaVencimiento: Long): Int {
        val diferencia = fechaVencimiento - System.currentTimeMillis()
        return (diferencia / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
    }
}

