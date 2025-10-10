package com.example.bsprestagil.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object QRCodeGenerator {
    
    /**
     * Genera un código QR para una garantía
     */
    fun generarQRGarantia(
        garantiaId: String,
        clienteNombre: String,
        descripcion: String,
        valorEstimado: Double,
        tipo: String,
        fechaRegistro: Long
    ): Bitmap? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            // Contenido del QR
            val contenido = buildString {
                appendLine("🔒 GARANTÍA PRESTÁGIL")
                appendLine("━━━━━━━━━━━━━━━━")
                appendLine("ID: $garantiaId")
                appendLine("Cliente: $clienteNombre")
                appendLine("Artículo: $descripcion")
                appendLine("Tipo: $tipo")
                appendLine("Valor: $${String.format("%,.2f", valorEstimado)}")
                appendLine("Fecha: ${dateFormat.format(Date(fechaRegistro))}")
                appendLine("━━━━━━━━━━━━━━━━")
                appendLine("Escanea para verificar")
            }
            
            generarQRBitmap(contenido, 512)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Genera un bitmap de código QR
     */
    private fun generarQRBitmap(contenido: String, size: Int): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = 1
            
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                contenido,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Guarda el QR como imagen y devuelve la URI para compartir
     */
    fun guardarYCompartirQR(
        context: Context,
        bitmap: Bitmap,
        garantiaId: String
    ): android.net.Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            
            val file = File(cachePath, "QR_Garantia_$garantiaId.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Comparte el QR por WhatsApp o cualquier app
     */
    fun compartirQR(
        context: Context,
        bitmap: Bitmap,
        garantiaId: String,
        clienteNombre: String,
        descripcion: String,
        porWhatsApp: Boolean = true
    ) {
        val uri = guardarYCompartirQR(context, bitmap, garantiaId)
        
        uri?.let {
            val mensaje = buildString {
                appendLine("🔒 *CÓDIGO QR DE GARANTÍA*")
                appendLine()
                appendLine("Cliente: $clienteNombre")
                appendLine("Artículo: $descripcion")
                appendLine("ID: $garantiaId")
                appendLine()
                appendLine("Conserve este código para identificar su garantía.")
            }
            
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(android.content.Intent.EXTRA_STREAM, it)
                putExtra(android.content.Intent.EXTRA_TEXT, mensaje)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                
                if (porWhatsApp) {
                    setPackage("com.whatsapp")
                }
            }
            
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Si WhatsApp no está instalado, compartir genérico
                val intentGenerico = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(android.content.Intent.EXTRA_STREAM, it)
                    putExtra(android.content.Intent.EXTRA_TEXT, mensaje)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    android.content.Intent.createChooser(intentGenerico, "Compartir QR de Garantía")
                )
            }
        }
    }
}

