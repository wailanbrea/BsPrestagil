package com.example.bsprestagil.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion")
data class ConfiguracionEntity(
    @PrimaryKey
    val id: Int = 1, // Solo habrá un registro de configuración
    
    // Configuración general
    val tasaInteresBase: Double,
    val tasaMoraBase: Double,
    val nombreNegocio: String,
    val telefonoNegocio: String,
    val direccionNegocio: String,
    val logoUrl: String,
    val mensajeRecibo: String,
    val notificacionesActivas: Boolean,
    val envioWhatsApp: Boolean,
    val envioSMS: Boolean,
    
    // Configuración de factura/contrato
    val rncEmpresa: String = "",
    val emailEmpresa: String = "",
    val sitioWebEmpresa: String = "",
    val tituloContrato: String = "CONTRATO DE PRÉSTAMO PERSONAL",
    val encabezadoContrato: String = "Entre las partes aquí identificadas, se establece el siguiente contrato de préstamo bajo los términos y condiciones que se detallan:",
    val terminosCondiciones: String = "1. El PRESTATARIO se compromete a pagar el préstamo en las fechas acordadas.\n2. Los intereses se calculan según el sistema de amortización elegido.\n3. El no pago genera intereses moratorios según la tasa establecida.\n4. La garantía quedará retenida hasta el pago total del préstamo.",
    val clausulasPenalizacion: String = "En caso de incumplimiento, se aplicará la tasa de mora establecida sobre el saldo pendiente.",
    val clausulasGarantia: String = "El PRESTATARIO entrega como garantía los bienes descritos, los cuales quedarán bajo custodia del PRESTAMISTA hasta la liquidación total del préstamo.",
    val clausulasLegales: String = "Este contrato se rige por las leyes vigentes. Cualquier disputa será resuelta en los tribunales competentes.",
    val pieContrato: String = "Gracias por confiar en nosotros. Para consultas, contáctenos a los números indicados.",
    val mensajeAdicionalContrato: String = "",
    val mostrarTablaAmortizacion: Boolean = true,
    val mostrarDesglosePago: Boolean = true,
    val incluirEspacioFirmas: Boolean = true,
    val numeroCopiasContrato: Int = 2,
    
    // Campos de sincronización
    val pendingSync: Boolean = true,
    val lastSyncTime: Long = 0L,
    val firebaseId: String? = null
)

