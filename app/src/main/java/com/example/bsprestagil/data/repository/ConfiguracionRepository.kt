package com.example.bsprestagil.data.repository

import com.example.bsprestagil.data.database.dao.ConfiguracionDao
import com.example.bsprestagil.data.database.entities.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow

class ConfiguracionRepository(
    private val configuracionDao: ConfiguracionDao
) {
    // Exponer el DAO para sincronización directa desde Firebase
    internal val dao: ConfiguracionDao get() = configuracionDao
    
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
                    // Configuración general
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
                    // Configuración de factura/contrato
                    rncEmpresa = "",
                    emailEmpresa = "",
                    sitioWebEmpresa = "",
                    tituloContrato = "CONTRATO DE PRÉSTAMO PERSONAL",
                    encabezadoContrato = "Entre las partes aquí identificadas, se establece el siguiente contrato de préstamo bajo los términos y condiciones que se detallan:",
                    terminosCondiciones = "1. El PRESTATARIO se compromete a pagar el préstamo en las fechas acordadas.\n2. Los intereses se calculan según el sistema de amortización elegido.\n3. El no pago genera intereses moratorios según la tasa establecida.\n4. La garantía quedará retenida hasta el pago total del préstamo.",
                    clausulasPenalizacion = "En caso de incumplimiento, se aplicará la tasa de mora establecida sobre el saldo pendiente.",
                    clausulasGarantia = "El PRESTATARIO entrega como garantía los bienes descritos, los cuales quedarán bajo custodia del PRESTAMISTA hasta la liquidación total del préstamo.",
                    clausulasLegales = "Este contrato se rige por las leyes vigentes. Cualquier disputa será resuelta en los tribunales competentes.",
                    pieContrato = "Gracias por confiar en nosotros. Para consultas, contáctenos a los números indicados.",
                    mensajeAdicionalContrato = "",
                    mostrarTablaAmortizacion = true,
                    mostrarDesglosePago = true,
                    incluirEspacioFirmas = true,
                    numeroCopiasContrato = 2,
                    // Sincronización
                    pendingSync = true,
                    lastSyncTime = System.currentTimeMillis()
                )
            )
        }
    }
}

