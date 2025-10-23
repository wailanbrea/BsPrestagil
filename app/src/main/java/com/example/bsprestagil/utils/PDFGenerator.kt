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
import com.itextpdf.text.pdf.draw.LineSeparator
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
    
    /**
     * Genera contrato de préstamo completo con términos y condiciones
     * Incluye información de extensiones si existen
     */
    fun generarContratoPrestamo(
        context: Context,
        // Datos del cliente
        clienteNombre: String,
        clienteTelefono: String,
        clienteDireccion: String,
        clienteCedula: String,
        // Datos del préstamo
        montoPrestamo: Double,
        tasaInteres: Double,
        plazo: Int,
        frecuenciaPago: String,
        tipoAmortizacion: String,
        montoCuota: Double,
        fechaInicio: Long,
        // Garantías
        garantias: List<String>,
        // Configuración del contrato
        nombreNegocio: String,
        telefonoNegocio: String,
        direccionNegocio: String,
        rncEmpresa: String,
        emailEmpresa: String,
        tituloContrato: String,
        encabezadoContrato: String,
        terminosCondiciones: String,
        clausulasPenalizacion: String,
        clausulasGarantia: String,
        clausulasLegales: String,
        pieContrato: String,
        mostrarTablaAmortizacion: Boolean,
        incluirEspacioFirmas: Boolean,
        // Datos de extensiones (opcional)
        montoOriginal: Double? = null,
        montoExtendido: Double? = null,
        numeroExtensiones: Int? = null,
        fechaUltimaExtension: Long? = null,
        razonUltimaExtension: String? = null
    ): Uri? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaActual = SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())

            // Crear archivo temporal
            val pdfDir = File(context.cacheDir, "pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val pdfFile = File(pdfDir, "contrato_prestamo_${clienteNombre.replace(" ", "_")}_$fechaActual.pdf")
            val document = Document(PageSize.A4, 50f, 50f, 60f, 60f)

            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // === ENCABEZADO ===
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, COLOR_PRIMARY)
            val title = Paragraph(tituloContrato + "\n\n", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)

            // Información de la empresa
            val empresaFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor.BLACK)
            val empresaInfo = Paragraph()
            empresaInfo.add(Chunk("$nombreNegocio\n", Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)))
            if (direccionNegocio.isNotBlank()) empresaInfo.add(Chunk("$direccionNegocio\n", empresaFont))
            if (telefonoNegocio.isNotBlank()) empresaInfo.add(Chunk("Tel: $telefonoNegocio\n", empresaFont))
            if (rncEmpresa.isNotBlank()) empresaInfo.add(Chunk("RNC: $rncEmpresa\n", empresaFont))
            if (emailEmpresa.isNotBlank()) empresaInfo.add(Chunk("Email: $emailEmpresa\n", empresaFont))
            empresaInfo.add(Chunk("\n"))
            empresaInfo.alignment = Element.ALIGN_CENTER
            document.add(empresaInfo)

            // Fecha y número de contrato
            val fechaContrato = Paragraph(
                "Fecha: ${dateFormat.format(Date())}\nContrato No. ${System.currentTimeMillis()}\n\n",
                Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, BaseColor.GRAY)
            )
            fechaContrato.alignment = Element.ALIGN_RIGHT
            document.add(fechaContrato)

            // Línea separadora
            document.add(LineSeparator())
            document.add(Chunk.NEWLINE)

            // === INTRODUCCIÓN ===
            val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor.BLACK)
            val intro = Paragraph(encabezadoContrato + "\n\n", normalFont)
            intro.alignment = Element.ALIGN_JUSTIFIED
            document.add(intro)

            // === DATOS DE LAS PARTES ===
            val sectionFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, COLOR_PRIMARY)
            document.add(Paragraph("I. PARTES CONTRATANTES\n\n", sectionFont))

            // Tabla de partes
            val partesTable = PdfPTable(2)
            partesTable.widthPercentage = 100f
            partesTable.setWidths(floatArrayOf(1f, 2f))

            // PRESTAMISTA
            val prestamistalabel = Paragraph("EL PRESTAMISTA:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD))
            val cell1 = PdfPCell(prestamistalabel)
            cell1.border = Rectangle.NO_BORDER
            cell1.setPadding(5f)
            partesTable.addCell(cell1)

            val prestamistaDatos = Paragraph()
            prestamistaDatos.add(Chunk(nombreNegocio + "\n", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)))
            if (rncEmpresa.isNotBlank()) prestamistaDatos.add(Chunk("RNC: $rncEmpresa\n", normalFont))
            prestamistaDatos.add(Chunk("Dirección: $direccionNegocio\n", normalFont))
            prestamistaDatos.add(Chunk("Teléfono: $telefonoNegocio\n", normalFont))
            val cell2 = PdfPCell(prestamistaDatos)
            cell2.border = Rectangle.NO_BORDER
            cell2.setPadding(5f)
            partesTable.addCell(cell2)

            // PRESTATARIO
            val prestatarioLabel = Paragraph("EL PRESTATARIO:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD))
            val cell3 = PdfPCell(prestatarioLabel)
            cell3.border = Rectangle.NO_BORDER
            cell3.setPadding(5f)
            partesTable.addCell(cell3)

            val prestatarioDatos = Paragraph()
            prestatarioDatos.add(Chunk(clienteNombre + "\n", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)))
            if (clienteCedula.isNotBlank()) prestatarioDatos.add(Chunk("Cédula: $clienteCedula\n", normalFont))
            prestatarioDatos.add(Chunk("Dirección: $clienteDireccion\n", normalFont))
            prestatarioDatos.add(Chunk("Teléfono: $clienteTelefono\n", normalFont))
            val cell4 = PdfPCell(prestatarioDatos)
            cell4.border = Rectangle.NO_BORDER
            cell4.setPadding(5f)
            partesTable.addCell(cell4)

            document.add(partesTable)
            document.add(Chunk.NEWLINE)

            // === CONDICIONES DEL PRÉSTAMO ===
            document.add(Paragraph("II. CONDICIONES DEL PRÉSTAMO\n\n", sectionFont))

            val condicionesTable = PdfPTable(2)
            condicionesTable.widthPercentage = 100f
            condicionesTable.setWidths(floatArrayOf(1.5f, 2f))

            // Mostrar información de extensiones si existen
            if (montoOriginal != null && montoExtendido != null && montoExtendido > 0) {
                addCellToTable(condicionesTable, "Monto original:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
                addCellToTable(condicionesTable, "$${String.format("%,.2f", montoOriginal)}", normalFont, false)

                addCellToTable(condicionesTable, "Monto extendido:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
                addCellToTable(condicionesTable, "$${String.format("%,.2f", montoExtendido)}", normalFont, false)

                addCellToTable(condicionesTable, "Monto total actual:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
                addCellToTable(condicionesTable, "$${String.format("%,.2f", montoPrestamo)}", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, COLOR_PRIMARY), false)

                if (numeroExtensiones != null && numeroExtensiones > 0) {
                    addCellToTable(condicionesTable, "Número de extensiones:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
                    addCellToTable(condicionesTable, "$numeroExtensiones", normalFont, false)
                }

                if (fechaUltimaExtension != null) {
                    addCellToTable(condicionesTable, "Última extensión:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
                    addCellToTable(condicionesTable, dateFormat.format(Date(fechaUltimaExtension)), normalFont, false)
                }

                if (razonUltimaExtension != null && razonUltimaExtension.isNotBlank()) {
                    addCellToTable(condicionesTable, "Razón última extensión:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
                    addCellToTable(condicionesTable, razonUltimaExtension, normalFont, false)
                }
            } else {
                addCellToTable(condicionesTable, "Monto del préstamo:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
                addCellToTable(condicionesTable, "$${String.format("%,.2f", montoPrestamo)}", normalFont, false)
            }

            addCellToTable(condicionesTable, "Tasa de interés:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
            addCellToTable(condicionesTable, "$tasaInteres% $frecuenciaPago", normalFont, false)

            addCellToTable(condicionesTable, "Plazo:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
            addCellToTable(condicionesTable, "$plazo cuotas $frecuenciaPago", normalFont, false)

            addCellToTable(condicionesTable, "Sistema de amortización:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
            val sistemaTexto = if (tipoAmortizacion == "ALEMAN") "Alemán (Capital Fijo)" else "Francés (Cuota Fija)"
            addCellToTable(condicionesTable, sistemaTexto, normalFont, false)

            addCellToTable(condicionesTable, "Cuota aproximada:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
            addCellToTable(condicionesTable, "$${String.format("%,.2f", montoCuota)}", normalFont, false)

            addCellToTable(condicionesTable, "Fecha de inicio:", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD), false)
            addCellToTable(condicionesTable, dateFormat.format(Date(fechaInicio)), normalFont, false)

            document.add(condicionesTable)
            document.add(Chunk.NEWLINE)

            // === GARANTÍAS ===
            if (garantias.isNotEmpty()) {
                document.add(Paragraph("III. GARANTÍAS\n\n", sectionFont))
                val garantiasText = Paragraph("El PRESTATARIO entrega como garantía:\n\n", normalFont)
                document.add(garantiasText)

                val list = com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED)
                garantias.forEach { garantia ->
                    list.add(ListItem(garantia, normalFont))
                }
                document.add(list)
                document.add(Chunk.NEWLINE)
            }

            // === TÉRMINOS Y CONDICIONES ===
            document.add(Paragraph("IV. TÉRMINOS Y CONDICIONES\n\n", sectionFont))

            // Dividir términos por líneas
            val terminos = terminosCondiciones.split("\n")
            terminos.forEach { termino ->
                if (termino.isNotBlank()) {
                    val p = Paragraph(termino + "\n", normalFont)
                    p.alignment = Element.ALIGN_JUSTIFIED
                    p.spacingAfter = 5f
                    document.add(p)
                }
            }
            document.add(Chunk.NEWLINE)

            // === CLÁUSULAS ESPECÍFICAS ===
            document.add(Paragraph("V. CLÁUSULAS ESPECÍFICAS\n\n", sectionFont))

            if (clausulasPenalizacion.isNotBlank()) {
                val clausulaTitle = Paragraph("A) Cláusula de Penalización:\n", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD))
                document.add(clausulaTitle)
                val clausulaText = Paragraph(clausulasPenalizacion + "\n\n", normalFont)
                clausulaText.alignment = Element.ALIGN_JUSTIFIED
                document.add(clausulaText)
            }

            if (clausulasGarantia.isNotBlank() && garantias.isNotEmpty()) {
                val clausulaTitle = Paragraph("B) Cláusula de Garantías:\n", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD))
                document.add(clausulaTitle)
                val clausulaText = Paragraph(clausulasGarantia + "\n\n", normalFont)
                clausulaText.alignment = Element.ALIGN_JUSTIFIED
                document.add(clausulaText)
            }

            if (clausulasLegales.isNotBlank()) {
                val clausulaTitle = Paragraph("C) Cláusulas Legales:\n", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD))
                document.add(clausulaTitle)
                val clausulaText = Paragraph(clausulasLegales + "\n\n", normalFont)
                clausulaText.alignment = Element.ALIGN_JUSTIFIED
                document.add(clausulaText)
            }

            // === PIE DE PÁGINA ===
            if (pieContrato.isNotBlank()) {
                document.add(Chunk.NEWLINE)
                val pieText = Paragraph(pieContrato + "\n\n", Font(Font.FontFamily.HELVETICA, 9f, Font.ITALIC, BaseColor.GRAY))
                pieText.alignment = Element.ALIGN_CENTER
                document.add(pieText)
            }

            // === FIRMAS ===
            if (incluirEspacioFirmas) {
                document.newPage()
                document.add(Chunk.NEWLINE)
                document.add(Chunk.NEWLINE)
                document.add(Chunk.NEWLINE)

                val firmasTable = PdfPTable(2)
                firmasTable.widthPercentage = 100f
                firmasTable.setWidths(floatArrayOf(1f, 1f))

                val firmaFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor.BLACK)

                // Firma PRESTAMISTA
                val firmaPrestamista = Paragraph()
                firmaPrestamista.add(Chunk("\n\n\n\n_______________________________\n", firmaFont))
                firmaPrestamista.add(Chunk(nombreNegocio + "\n", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)))
                firmaPrestamista.add(Chunk("EL PRESTAMISTA\n", Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, BaseColor.GRAY)))
                if (rncEmpresa.isNotBlank()) firmaPrestamista.add(Chunk("RNC: $rncEmpresa", Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.GRAY)))
                firmaPrestamista.alignment = Element.ALIGN_CENTER

                val cellFirma1 = PdfPCell(firmaPrestamista)
                cellFirma1.border = Rectangle.NO_BORDER
                cellFirma1.setPadding(10f)
                firmasTable.addCell(cellFirma1)

                // Firma PRESTATARIO
                val firmaPrestatario = Paragraph()
                firmaPrestatario.add(Chunk("\n\n\n\n_______________________________\n", firmaFont))
                firmaPrestatario.add(Chunk(clienteNombre + "\n", Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)))
                firmaPrestatario.add(Chunk("EL PRESTATARIO\n", Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, BaseColor.GRAY)))
                if (clienteCedula.isNotBlank()) firmaPrestatario.add(Chunk("Cédula: $clienteCedula", Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.GRAY)))
                firmaPrestatario.alignment = Element.ALIGN_CENTER

                val cellFirma2 = PdfPCell(firmaPrestatario)
                cellFirma2.border = Rectangle.NO_BORDER
                cellFirma2.setPadding(10f)
                firmasTable.addCell(cellFirma2)

                document.add(firmasTable)

                // Fecha de firma
                document.add(Chunk.NEWLINE)
                document.add(Chunk.NEWLINE)
                val fechaFirma = Paragraph(
                    "Fecha de firma: _______________________________",
                    Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, BaseColor.BLACK)
                )
                fechaFirma.alignment = Element.ALIGN_CENTER
                document.add(fechaFirma)
            }

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
}

