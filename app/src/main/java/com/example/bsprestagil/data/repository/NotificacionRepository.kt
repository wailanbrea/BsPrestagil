package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.NotificacionDao
import com.example.bsprestagil.data.database.entities.NotificacionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NotificacionRepository(
    private val notificacionDao: NotificacionDao
) {
    
    fun getAllNotificaciones(): Flow<List<NotificacionEntity>> {
        return notificacionDao.getAllNotificaciones()
    }
    
    fun getNotificacionesNoLeidas(): Flow<List<NotificacionEntity>> {
        return notificacionDao.getNotificacionesNoLeidas()
    }
    
    fun getContadorNoLeidas(): Flow<Int> {
        return notificacionDao.getContadorNoLeidas()
    }
    
    suspend fun getNotificacionById(id: String): NotificacionEntity? {
        return notificacionDao.getNotificacionById(id)
    }
    
    suspend fun crearNotificacion(
        titulo: String,
        mensaje: String,
        tipo: String,
        prestamoId: String? = null,
        clienteId: String? = null,
        pagoId: String? = null,
        cobradorId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val notificacion = NotificacionEntity(
            id = id,
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipo,
            fecha = System.currentTimeMillis(),
            leida = false,
            prestamoId = prestamoId,
            clienteId = clienteId,
            pagoId = pagoId,
            cobradorId = cobradorId
        )
        notificacionDao.insertNotificacion(notificacion)
        return id
    }
    
    suspend fun marcarComoLeida(id: String) {
        notificacionDao.marcarComoLeida(id)
    }
    
    suspend fun marcarTodasComoLeidas() {
        notificacionDao.marcarTodasComoLeidas()
    }
    
    suspend fun eliminarNotificacion(notificacion: NotificacionEntity) {
        notificacionDao.deleteNotificacion(notificacion)
    }
    
    suspend fun limpiarNotificacionesAntiguas(diasAntiguedad: Int = 30) {
        val fechaLimite = System.currentTimeMillis() - (diasAntiguedad * 24L * 60 * 60 * 1000)
        notificacionDao.eliminarNotificacionesAntiguas(fechaLimite)
    }
}

