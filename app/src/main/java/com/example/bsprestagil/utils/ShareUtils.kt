package com.example.bsprestagil.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

object ShareUtils {
    
    /**
     * Genera texto de recibo y lo comparte por WhatsApp
     */
    fun compartirReciboPorWhatsApp(
        context: Context,
        clienteNombre: String,
        monto: Double,
        montoCuota: Double,
        montoMora: Double,
        numeroCuota: Int,
        metodoPago: String,
        fechaPago: Long,
        recibidoPor: String,
        telefonoCliente: String? = null
    ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        val mensajeRecibo = buildString {
            appendLine("🧾 *RECIBO DE PAGO*")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("*Prestágil*")
            appendLine()
            appendLine("📋 *DATOS DEL PAGO*")
            appendLine("Cliente: $clienteNombre")
            appendLine("Cuota #: $numeroCuota")
            appendLine()
            appendLine("💰 *MONTOS*")
            appendLine("Cuota: $${String.format("%,.2f", montoCuota)}")
            if (montoMora > 0) {
                appendLine("Mora: $${String.format("%,.2f", montoMora)}")
            }
            appendLine("*Total pagado: $${String.format("%,.2f", monto)}*")
            appendLine()
            appendLine("📅 Fecha: ${dateFormat.format(Date(fechaPago))}")
            appendLine("💳 Método: $metodoPago")
            appendLine("👤 Recibido por: $recibidoPor")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("✅ Gracias por su pago")
        }
        
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = if (telefonoCliente != null) {
                    // Abrir chat específico con el cliente
                    val numeroLimpio = telefonoCliente.replace(Regex("[^0-9]"), "")
                    Uri.parse("https://wa.me/$numeroLimpio?text=${Uri.encode(mensajeRecibo)}")
                } else {
                    // Abrir WhatsApp con el mensaje para que el usuario elija el contacto
                    Uri.parse("https://wa.me/?text=${Uri.encode(mensajeRecibo)}")
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si WhatsApp no está instalado, usar compartir genérico
            compartirTextoGenerico(context, mensajeRecibo, "Recibo de Pago")
        }
    }
    
    /**
     * Comparte texto usando el sistema de compartir de Android
     */
    fun compartirTextoGenerico(context: Context, texto: String, titulo: String = "Compartir") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
            putExtra(Intent.EXTRA_SUBJECT, titulo)
        }
        context.startActivity(Intent.createChooser(intent, titulo))
    }
    
    /**
     * Genera resumen de préstamo para compartir
     */
    fun compartirResumenPrestamo(
        context: Context,
        clienteNombre: String,
        montoOriginal: Double,
        capitalPendiente: Double,
        tasaInteresPorPeriodo: Double,
        frecuenciaPago: String,
        totalCapitalPagado: Double,
        totalInteresesPagados: Double,
        fechaInicio: Long
    ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        val periodoTexto = when(frecuenciaPago) {
            "DIARIO" -> "diario"
            "SEMANAL" -> "semanal"
            "QUINCENAL" -> "quincenal"
            "MENSUAL" -> "mensual"
            else -> "mensual"
        }
        
        val mensaje = buildString {
            appendLine("📊 *RESUMEN DE PRÉSTAMO*")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("*Prestágil*")
            appendLine()
            appendLine("Cliente: $clienteNombre")
            appendLine()
            appendLine("💰 *DETALLES FINANCIEROS*")
            appendLine("Capital prestado: $${String.format("%,.2f", montoOriginal)}")
            appendLine("Capital pendiente: $${String.format("%,.2f", capitalPendiente)}")
            appendLine("Capital pagado: $${String.format("%,.2f", totalCapitalPagado)}")
            appendLine()
            appendLine("Tasa de interés: ${tasaInteresPorPeriodo.toInt()}% $periodoTexto")
            appendLine("Intereses pagados: $${String.format("%,.2f", totalInteresesPagados)}")
            appendLine()
            appendLine("📅 Fecha de inicio: ${dateFormat.format(Date(fechaInicio))}")
            appendLine("📊 Progreso: ${((totalCapitalPagado / montoOriginal) * 100).toInt()}%")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━")
        }
        
        compartirTextoGenerico(context, mensaje, "Resumen de Préstamo")
    }
}

