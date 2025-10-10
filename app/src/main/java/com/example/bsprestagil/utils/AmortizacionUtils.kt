package com.example.bsprestagil.utils

import com.example.bsprestagil.data.models.FrecuenciaPago
import kotlin.math.pow

/**
 * Utilidades para cálculo de cuotas fijas (Sistema Francés de Amortización)
 * Usado en préstamos profesionales donde la cuota es FIJA
 */
object AmortizacionUtils {
    
    /**
     * Calcula la cuota FIJA usando el Sistema Francés (Método de Anualidad)
     * 
     * Fórmula: Cuota = P × [i × (1 + i)^n] / [(1 + i)^n - 1]
     * 
     * @param capital Monto del préstamo (Principal)
     * @param tasaInteresPorPeriodo Tasa de interés por período en decimal (ej: 20% = 0.20)
     * @param numeroCuotas Número de cuotas a pagar
     * @return Cuota fija mensual
     */
    fun calcularCuotaFija(
        capital: Double,
        tasaInteresPorPeriodo: Double,
        numeroCuotas: Int
    ): Double {
        if (capital <= 0 || numeroCuotas <= 0) return 0.0
        if (tasaInteresPorPeriodo <= 0) return capital / numeroCuotas
        
        val i = tasaInteresPorPeriodo / 100.0 // Convertir porcentaje a decimal
        val factor = (1 + i).pow(numeroCuotas.toDouble())
        
        val cuota = capital * (i * factor) / (factor - 1)
        
        return cuota
    }
    
    /**
     * Genera una tabla de amortización completa
     * 
     * @return Lista de tuplas: (numeroCuota, capital, interes, balance)
     */
    data class FilaAmortizacion(
        val numeroCuota: Int,
        val cuotaFija: Double,
        val capital: Double,
        val interes: Double,
        val balanceRestante: Double
    )
    
    fun generarTablaAmortizacion(
        capitalInicial: Double,
        tasaInteresPorPeriodo: Double,
        numeroCuotas: Int
    ): List<FilaAmortizacion> {
        val tabla = mutableListOf<FilaAmortizacion>()
        val cuotaFija = calcularCuotaFija(capitalInicial, tasaInteresPorPeriodo, numeroCuotas)
        
        var balanceActual = capitalInicial
        val i = tasaInteresPorPeriodo / 100.0
        
        for (numCuota in 1..numeroCuotas) {
            // Interés del período = Balance × tasa
            val interesCuota = balanceActual * i
            
            // Capital = Cuota - Interés
            val capitalCuota = cuotaFija - interesCuota
            
            // Nuevo balance
            balanceActual -= capitalCuota
            
            // Asegurar que en la última cuota el balance sea exactamente 0
            if (numCuota == numeroCuotas) {
                balanceActual = 0.0
            }
            
            tabla.add(
                FilaAmortizacion(
                    numeroCuota = numCuota,
                    cuotaFija = cuotaFija,
                    capital = capitalCuota,
                    interes = interesCuota,
                    balanceRestante = balanceActual.coerceAtLeast(0.0)
                )
            )
        }
        
        return tabla
    }
    
    /**
     * Calcula el total a pagar (capital + intereses)
     */
    fun calcularTotalAPagar(
        capitalInicial: Double,
        tasaInteresPorPeriodo: Double,
        numeroCuotas: Int
    ): Double {
        val cuotaFija = calcularCuotaFija(capitalInicial, tasaInteresPorPeriodo, numeroCuotas)
        return cuotaFija * numeroCuotas
    }
    
    /**
     * Calcula el total de intereses a pagar
     */
    fun calcularTotalIntereses(
        capitalInicial: Double,
        tasaInteresPorPeriodo: Double,
        numeroCuotas: Int
    ): Double {
        val totalAPagar = calcularTotalAPagar(capitalInicial, tasaInteresPorPeriodo, numeroCuotas)
        return totalAPagar - capitalInicial
    }
    
    /**
     * Valida si un monto es suficiente para cubrir la cuota
     */
    fun validarMontoCuota(
        montoPagado: Double,
        cuotaFija: Double,
        tolerancia: Double = 1.0 // Tolerancia de $1 peso
    ): Boolean {
        return montoPagado >= (cuotaFija - tolerancia)
    }
    
    /**
     * Calcula abono extraordinario al capital
     * Si el cliente paga más de la cuota, el excedente va directo al capital
     */
    fun calcularAbonoExtraordinario(
        montoPagado: Double,
        cuotaFija: Double
    ): Double {
        return if (montoPagado > cuotaFija) {
            montoPagado - cuotaFija
        } else {
            0.0
        }
    }
    
    /**
     * Genera texto formateado de la tabla de amortización para compartir/imprimir
     */
    fun generarTextoTablaAmortizacion(
        capitalInicial: Double,
        tasaInteresPorPeriodo: Double,
        numeroCuotas: Int,
        incluirEncabezado: Boolean = true
    ): String {
        val tabla = generarTablaAmortizacion(capitalInicial, tasaInteresPorPeriodo, numeroCuotas)
        
        return buildString {
            if (incluirEncabezado) {
                appendLine("📊 *TABLA DE AMORTIZACIÓN*")
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine()
            }
            
            // Encabezado de la tabla
            appendLine("No. | Cuota      | Capital    | Interés    | Balance")
            appendLine("----+------------+------------+------------+------------")
            
            // Filas de datos
            tabla.forEach { fila ->
                val numCuota = fila.numeroCuota.toString().padStart(3)
                val cuota = "$${String.format("%,.2f", fila.cuotaFija)}".padStart(10)
                val capital = "$${String.format("%,.2f", fila.capital)}".padStart(10)
                val interes = "$${String.format("%,.2f", fila.interes)}".padStart(10)
                val balance = "$${String.format("%,.2f", fila.balanceRestante)}".padStart(10)
                
                appendLine("$numCuota | $cuota | $capital | $interes | $balance")
            }
            
            appendLine("----+------------+------------+------------+------------")
            
            // Totales
            val totalCuotas = tabla.sumOf { it.cuotaFija }
            val totalCapital = tabla.sumOf { it.capital }
            val totalIntereses = tabla.sumOf { it.interes }
            
            appendLine()
            appendLine("*Totales:*")
            appendLine("• Total a pagar: $${String.format("%,.2f", totalCuotas)}")
            appendLine("• Total capital: $${String.format("%,.2f", totalCapital)}")
            appendLine("• Total intereses: $${String.format("%,.2f", totalIntereses)}")
        }
    }
}

