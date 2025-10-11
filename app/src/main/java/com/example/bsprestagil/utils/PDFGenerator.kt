package com.example.bsprestagil.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.bsprestagil.data.models.TipoAmortizacion
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PDFGenerator {
    
    // Colores corporativos
    private val COLOR_PRIMARY = BaseColor(17, 115, 212) // #1173d4
    private val COLOR_ACCENT = BaseColor(255, 152, 0) // Naranja
    private val COLOR_SUCCESS = BaseColor(76, 175, 80) // Verde
    private val COLOR_GRAY = BaseColor(128, 128, 128) // Gris
    
    /**
     * Genera PDF con tabla de amortización profesional
     */
    fun generarPDFTablaAmortizacion(
        context: Context,
        clienteNombre: String,
        montoOriginal: Double,
        capitalPendiente: Double,
        tasaInteresPorPeriodo: Double,
        frecuenciaPago: String,
        tipoAmortizacion: String,
        numeroCuotas: Int,
        montoCuotaFija: Double,
        totalCapitalPagado: Double,
        totalInteresesPagados: Double,
        fechaInicio: Long
    ): Uri? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaActual = SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())
            
            // Crear archivo temporal
            val pdfDir = File(context.cacheDir, "pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            
            val pdfFile = File(pdfDir, "resumen_prestamo_$fechaActual.pdf")
            val document = Document(PageSize.A4, 36f, 36f, 50f, 50f)
            
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()
            
            // ===== ENCABEZADO =====
            val titleFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD, COLOR_PRIMARY)
            val title = Paragraph("RESUMEN DE PRÉSTAMO\n\n", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            
            // Logo/Nombre empresa
            val empresaFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor.BLACK)
            val empresaNombre = Paragraph("Prestágil - Sistema de Gestión\n\n", empresaFont)
            empresaNombre.alignment = Element.ALIGN_CENTER
            document.add(empresaNombre)
            
            // Línea separadora
            document.add(Chunk.NEWLINE)
            
            // ===== INFORMACIÓN DEL CLIENTE =====
            val sectionFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, COLOR_PRIMARY)
            val normalFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, BaseColor.BLACK)
            
            val infoTable = PdfPTable(2)
            infoTable.widthPercentage = 100f
            infoTable.setWidths(floatArrayOf(1f, 2f))
            
            addCellToTable(infoTable, "Cliente:", normalFont, true)
            addCellToTable(infoTable, clienteNombre, normalFont, false)
            
            addCellToTable(infoTable, "Fecha de inicio:", normalFont, true)
            addCellToTable(infoTable, dateFormat.format(Date(fechaInicio)), normalFont, false)
            
            val periodoTexto = when(frecuenciaPago) {
                "DIARIO" -> "diario"
                "QUINCENAL" -> "quincenal"
                "MENSUAL" -> "mensual"
                else -> "mensual"
            }
            
            addCellToTable(infoTable, "Tasa de interés:", normalFont, true)
            addCellToTable(infoTable, "${tasaInteresPorPeriodo.toInt()}% $periodoTexto", normalFont, false)
            
            val sistemaTexto = when(tipoAmortizacion) {
                "ALEMAN" -> "Alemán (Capital Fijo)"
                else -> "Francés (Cuota Fija)"
            }
            
            addCellToTable(infoTable, "Sistema:", normalFont, true)
            addCellToTable(infoTable, sistemaTexto, normalFont, false)
            
            document.add(infoTable)
            document.add(Chunk.NEWLINE)
            
            // ===== RESUMEN FINANCIERO =====
            val summaryFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, BaseColor.WHITE)
            val summaryTable = PdfPTable(2)
            summaryTable.widthPercentage = 100f
            
            addHeaderCell(summaryTable, "Capital prestado:", COLOR_PRIMARY, summaryFont)
            addHeaderCell(summaryTable, "$${String.format("%,.2f", montoOriginal)}", COLOR_PRIMARY, summaryFont)
            
            addCellToTable(summaryTable, "Capital pendiente:", normalFont, false)
            addCellToTable(summaryTable, "$${String.format("%,.2f", capitalPendiente)}", normalFont, false)
            
            addCellToTable(summaryTable, "Capital pagado:", normalFont, false)
            addCellToTable(summaryTable, "$${String.format("%,.2f", totalCapitalPagado)}", Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, COLOR_SUCCESS), false)
            
            addCellToTable(summaryTable, "Intereses pagados:", normalFont, false)
            addCellToTable(summaryTable, "$${String.format("%,.2f", totalInteresesPagados)}", Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, COLOR_ACCENT), false)
            
            val progreso = if (montoOriginal > 0) ((totalCapitalPagado / montoOriginal) * 100).toInt() else 0
            addCellToTable(summaryTable, "Progreso:", normalFont, false)
            addCellToTable(summaryTable, "$progreso%", Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, COLOR_SUCCESS), false)
            
            document.add(summaryTable)
            document.add(Chunk.NEWLINE)
            
            // ===== TABLA DE AMORTIZACIÓN =====
            val tableTitle = Paragraph("TABLA DE AMORTIZACIÓN\n\n", sectionFont)
            tableTitle.alignment = Element.ALIGN_CENTER
            document.add(tableTitle)
            
            // Generar tabla según sistema
            val tipoSistema = when(tipoAmortizacion) {
                "ALEMAN" -> TipoAmortizacion.ALEMAN
                else -> TipoAmortizacion.FRANCES
            }
            
            val tablaAmortizacion = AmortizacionUtils.generarTablaSegunSistema(
                capitalInicial = montoOriginal,
                tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                numeroCuotas = numeroCuotas,
                tipoSistema = tipoSistema
            )
            
            // Crear tabla PDF
            val pdfTable = PdfPTable(5)
            pdfTable.widthPercentage = 100f
            pdfTable.setWidths(floatArrayOf(1f, 2f, 2f, 2f, 2f))
            
            // Encabezado de la tabla
            val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)
            addTableHeader(pdfTable, "No.", headerFont, COLOR_PRIMARY)
            addTableHeader(pdfTable, "Cuota", headerFont, COLOR_PRIMARY)
            addTableHeader(pdfTable, "Capital", headerFont, COLOR_PRIMARY)
            addTableHeader(pdfTable, "Interés", headerFont, COLOR_PRIMARY)
            addTableHeader(pdfTable, "Balance", headerFont, COLOR_PRIMARY)
            
            // Filas de datos
            val dataFont = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, BaseColor.BLACK)
            tablaAmortizacion.forEach { fila ->
                addTableCell(pdfTable, "${fila.numeroCuota}", dataFont, Element.ALIGN_CENTER)
                addTableCell(pdfTable, "$${String.format("%,.2f", fila.cuotaFija)}", dataFont, Element.ALIGN_RIGHT)
                addTableCell(pdfTable, "$${String.format("%,.2f", fila.capital)}", dataFont, Element.ALIGN_RIGHT)
                addTableCell(pdfTable, "$${String.format("%,.2f", fila.interes)}", dataFont, Element.ALIGN_RIGHT)
                addTableCell(pdfTable, "$${String.format("%,.2f", fila.balanceRestante)}", dataFont, Element.ALIGN_RIGHT)
            }
            
            // Fila de totales
            val totalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.BLACK)
            addTableCell(pdfTable, "", totalFont, Element.ALIGN_CENTER)
            addTableCell(pdfTable, "$${String.format("%,.2f", tablaAmortizacion.sumOf { it.cuotaFija })}", totalFont, Element.ALIGN_RIGHT, COLOR_ACCENT)
            addTableCell(pdfTable, "$${String.format("%,.2f", tablaAmortizacion.sumOf { it.capital })}", totalFont, Element.ALIGN_RIGHT, COLOR_SUCCESS)
            addTableCell(pdfTable, "$${String.format("%,.2f", tablaAmortizacion.sumOf { it.interes })}", totalFont, Element.ALIGN_RIGHT, COLOR_ACCENT)
            addTableCell(pdfTable, "$0.00", totalFont, Element.ALIGN_RIGHT, COLOR_SUCCESS)
            
            document.add(pdfTable)
            document.add(Chunk.NEWLINE)
            
            // Notas al pie
            val notasFont = Font(Font.FontFamily.HELVETICA, 8f, Font.ITALIC, COLOR_GRAY)
            val notas = Paragraph()
            notas.add(Chunk("Notas:\n", Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, COLOR_GRAY)))
            notas.add(Chunk("• Los resultados son calculados según el ${sistemaTexto}.\n", notasFont))
            notas.add(Chunk("• Se consideran meses de 30 días para cálculos.\n", notasFont))
            notas.add(Chunk("• Este documento es un resumen informativo.\n", notasFont))
            notas.add(Chunk("\n\nGenerado el: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n", notasFont))
            document.add(notas)
            
            // Footer
            val footer = Paragraph("\nPrestágil - Tu socio financiero", 
                Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, COLOR_PRIMARY))
            footer.alignment = Element.ALIGN_CENTER
            document.add(footer)
            
            document.close()
            
            // Retornar URI del archivo
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun addCellToTable(
        table: PdfPTable,
        text: String,
        font: Font,
        isBold: Boolean,
        backgroundColor: BaseColor? = null
    ) {
        val cell = PdfPCell(Phrase(text, font))
        cell.setPadding(8f)
        cell.border = Rectangle.NO_BORDER
        if (backgroundColor != null) {
            cell.backgroundColor = backgroundColor
        }
        table.addCell(cell)
    }
    
    private fun addTableHeader(
        table: PdfPTable,
        text: String,
        font: Font,
        backgroundColor: BaseColor
    ) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = backgroundColor
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(10f)
        cell.border = Rectangle.NO_BORDER
        table.addCell(cell)
    }
    
    private fun addTableCell(
        table: PdfPTable,
        text: String,
        font: Font,
        alignment: Int,
        backgroundColor: BaseColor? = null
    ) {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = alignment
        cell.setPadding(6f)
        cell.borderColor = BaseColor(220, 220, 220)
        cell.borderWidth = 0.5f
        if (backgroundColor != null) {
            cell.backgroundColor = backgroundColor.brighter().brighter()
        }
        table.addCell(cell)
    }
    
    private fun addHeaderCell(
        table: PdfPTable,
        text: String,
        backgroundColor: BaseColor,
        font: Font
    ) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = backgroundColor
        cell.setPadding(10f)
        cell.border = Rectangle.NO_BORDER
        table.addCell(cell)
    }
    
    /**
     * Comparte el PDF por WhatsApp u otra aplicación
     */
    fun compartirPDF(context: Context, pdfUri: Uri, clienteNombre: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, "📊 Resumen de préstamo para $clienteNombre")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Compartir resumen de préstamo"))
    }
    
    /**
     * Comparte por WhatsApp específicamente
     */
    fun compartirPDFPorWhatsApp(
        context: Context,
        pdfUri: Uri,
        clienteNombre: String,
        telefonoCliente: String? = null
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, "📊 Resumen de tu préstamo")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si WhatsApp no está instalado, usar compartir genérico
            compartirPDF(context, pdfUri, clienteNombre)
        }
    }
}

