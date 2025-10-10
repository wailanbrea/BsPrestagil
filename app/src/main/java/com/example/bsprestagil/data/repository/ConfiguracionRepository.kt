package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.ConfiguracionDao
import com.example.bsprestagil.data.database.entities.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow

class ConfiguracionRepository(
    private val configuracionDao: ConfiguracionDao
) {
    
    // Observar configuración
    fun getConfiguracion(): Flow<ConfiguracionEntity?> {
        return configuracionDao.getConfiguracion()
    }
    
    // Obtener configuración sincrónica
    suspend fun getConfiguracionSync(): ConfiguracionEntity? {
        return configuracionDao.getConfiguracionSync()
    }
    
    // Insertar configuración inicial
    suspend fun insertConfiguracion(configuracion: ConfiguracionEntity) {
        configuracionDao.insertConfiguracion(
            configuracion.copy(
                id = 1,
                pendingSync = true,
                lastSyncTime = System.currentTimeMillis()
            )
        )
    }
    
    // Actualizar configuración
    suspend fun updateConfiguracion(configuracion: ConfiguracionEntity) {
        configuracionDao.updateConfiguracion(
            configuracion.copy(
                id = 1,
                pendingSync = true,
                lastSyncTime = System.currentTimeMillis()
            )
        )
    }
    
    // Marcar como sincronizado
    suspend fun markAsSynced() {
        configuracionDao.markAsSynced(System.currentTimeMillis())
    }
    
    // Crear configuración por defecto si no existe
    suspend fun initializeConfiguracionIfNeeded() {
        val config = getConfiguracionSync()
        if (config == null) {
            insertConfiguracion(
                ConfiguracionEntity(
                    id = 1,
                    tasaInteresBase = 10.0,
                    tasaMoraBase = 5.0,
                    nombreNegocio = "Prestágil",
                    telefonoNegocio = "",
                    direccionNegocio = "",
                    logoUrl = "",
                    mensajeRecibo = "Gracias por su pago",
                    notificacionesActivas = true,
                    envioWhatsApp = true,
                    envioSMS = false,
                    pendingSync = true,
                    lastSyncTime = System.currentTimeMillis()
                )
            )
        }
    }
}

