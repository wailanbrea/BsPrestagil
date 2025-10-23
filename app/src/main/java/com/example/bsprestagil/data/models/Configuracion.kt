package com.example.bsprestagil.data.models

data class Configuracion(
    // Configuración general
    val tasaInteresBase: Double = 10.0,
    val tasaMoraBase: Double = 5.0,
    val nombreNegocio: String = "Prestágil",
    val telefonoNegocio: String = "",
    val direccionNegocio: String = "",
    val logoUrl: String = "",
    val mensajeRecibo: String = "Gracias por su pago",
    val notificacionesActivas: Boolean = true,
    val envioWhatsApp: Boolean = true,
    val envioSMS: Boolean = false,
    
    // ===== CONFIGURACIÓN DE FACTURA/CONTRATO =====
    // Información legal de la empresa
    val rncEmpresa: String = "",
    val emailEmpresa: String = "",
    val sitioWebEmpresa: String = "",
    
    // Encabezado del contrato
    val tituloContrato: String = "CONTRATO DE PRÉSTAMO PERSONAL",
    val encabezadoContrato: String = "Entre las partes aquí identificadas, se establece el siguiente contrato de préstamo bajo los términos y condiciones que se detallan:",
    
    // Términos y condiciones
    val terminosCondiciones: String = """1. El PRESTATARIO se compromete a pagar el préstamo en las fechas acordadas.
2. Los intereses se calculan según el sistema de amortización elegido.
3. El no pago genera intereses moratorios según la tasa establecida.
4. La garantía quedará retenida hasta el pago total del préstamo.""",
    
    // Cláusulas adicionales
    val clausulasPenalizacion: String = "En caso de incumplimiento, se aplicará la tasa de mora establecida sobre el saldo pendiente.",
    val clausulasGarantia: String = "El PRESTATARIO entrega como garantía los bienes descritos, los cuales quedarán bajo custodia del PRESTAMISTA hasta la liquidación total del préstamo.",
    val clausulasLegales: String = "Este contrato se rige por las leyes vigentes. Cualquier disputa será resuelta en los tribunales competentes.",
    
    // Pie de página
    val pieContrato: String = "Gracias por confiar en nosotros. Para consultas, contáctenos a los números indicados.",
    val mensajeAdicionalContrato: String = "",
    
    // Opciones de visualización
    val mostrarTablaAmortizacion: Boolean = true,
    val mostrarDesglosePago: Boolean = true,
    val incluirEspacioFirmas: Boolean = true,
    val numeroCopiasContrato: Int = 2
)

