package com.example.bsprestagil.utils

import com.example.bsprestagil.data.models.FrecuenciaPago
import com.example.bsprestagil.data.models.TipoPago
import java.util.concurrent.TimeUnit

/**
 * Utilidades para cálculo de intereses sobre saldo
 */
object InteresUtils {
    
    /**
     * Calcula el interés del período basado en el capital pendiente
     * 
     * @param capitalPendiente Capital actual que debe el cliente
     * @param tasaInteresPorPeriodo Tasa de interés (ej: 20% mensual = 20.0)
     * @return Interés a pagar en este período
     */
    fun calcularInteresPeriodo(
        capitalPendiente: Double,
        tasaInteresPorPeriodo: Double
    ): Double {
        return capitalPendiente * (tasaInteresPorPeriodo / 100.0)
    }
    
    /**
     * Distribuye un pago entre interés y capital
     * 
     * @param montoPagado Monto total que paga el cliente
     * @param interesDelPeriodo Interés calculado del período
     * @param capitalPendiente Capital actual que debe
     * @return Pair(montoAInteres, montoACapital)
     */
    fun distribuirPago(
        montoPagado: Double,
        interesDelPeriodo: Double,
        capitalPendiente: Double
    ): Pair<Double, Double> {
        // Primero se paga el interés completo
        val montoAInteres = minOf(montoPagado, interesDelPeriodo)
        
        // Lo que sobra va al capital
        val montoACapital = maxOf(0.0, montoPagado - interesDelPeriodo)
        
        // No puede pagar más capital del que debe
        val montoACapitalFinal = minOf(montoACapital, capitalPendiente)
        
        return Pair(montoAInteres, montoACapitalFinal)
    }
    
    /**
     * Distribuye un pago según el tipo seleccionado por el usuario
     * NUEVO: Permite pagos flexibles (solo interés, solo capital, personalizado)
     * 
     * @param montoPagado Monto total que paga el cliente
     * @param interesAcumulado Interés acumulado hasta la fecha
     * @param capitalPendiente Capital que aún debe
     * @param tipoPago Tipo de pago que desea realizar
     * @param montoInteres (Opcional) Para tipo PERSONALIZADO: cuánto va a interés
     * @param montoCapital (Opcional) Para tipo PERSONALIZADO: cuánto va a capital
     * @return Triple(montoAInteres, montoACapital, advertencia)
     */
    fun distribuirPagoFlexible(
        montoPagado: Double,
        interesAcumulado: Double,
        capitalPendiente: Double,
        tipoPago: TipoPago,
        montoInteres: Double = 0.0,
        montoCapital: Double = 0.0
    ): Triple<Double, Double, String?> {
        
        return when (tipoPago) {
            TipoPago.NORMAL -> {
                // Distribución estándar: primero interés, luego capital
                val aInteres = minOf(montoPagado, interesAcumulado)
                val aCapital = minOf(maxOf(0.0, montoPagado - interesAcumulado), capitalPendiente)
                Triple(aInteres, aCapital, null)
            }
            
            TipoPago.SOLO_INTERES -> {
                // TODO el pago va al interés acumulado
                val aInteres = minOf(montoPagado, interesAcumulado)
                val advertencia = if (montoPagado < interesAcumulado) {
                    "Solo cubriste parte del interés. Aún debes $${String.format("%.2f", interesAcumulado - aInteres)} de interés."
                } else if (montoPagado > interesAcumulado) {
                    "Pagaste más del interés. El excedente de $${String.format("%.2f", montoPagado - aInteres)} no se aplicó."
                } else null
                
                Triple(aInteres, 0.0, advertencia)
            }
            
            TipoPago.SOLO_CAPITAL -> {
                // TODO el pago va al capital (solo si NO hay interés pendiente)
                if (interesAcumulado > 0) {
                    // No permitir pago de capital si hay interés pendiente
                    Triple(
                        0.0,
                        0.0,
                        "⚠️ Debes pagar primero el interés acumulado de $${String.format("%.2f", interesAcumulado)}"
                    )
                } else {
                    val aCapital = minOf(montoPagado, capitalPendiente)
                    Triple(0.0, aCapital, null)
                }
            }
            
            TipoPago.PERSONALIZADO -> {
                // Usuario especifica manualmente la distribución
                val totalDistribuido = montoInteres + montoCapital
                
                if (totalDistribuido > montoPagado) {
                    Triple(
                        0.0,
                        0.0,
                        "❌ La distribución ($${String.format("%.2f", totalDistribuido)}) excede el monto pagado ($${String.format("%.2f", montoPagado)})"
                    )
                } else {
                    val aInteres = minOf(montoInteres, interesAcumulado)
                    val aCapital = minOf(montoCapital, capitalPendiente)
                    
                    val advertencia = if (montoInteres > interesAcumulado) {
                        "⚠️ El monto a interés excede el interés acumulado"
                    } else if (montoCapital > capitalPendiente) {
                        "⚠️ El monto a capital excede el capital pendiente"
                    } else null
                    
                    Triple(aInteres, aCapital, advertencia)
                }
            }
            
            TipoPago.EXONERAR_INTERES -> {
                // Exonerar interés: Todo el pago va al capital, el interés se perdona
                val aCapital = minOf(montoPagado, capitalPendiente)
                val interesExonerado = interesAcumulado
                
                val advertencia = if (interesExonerado > 0) {
                    "✅ Interés exonerado: $${String.format("%.2f", interesExonerado)}. Todo el pago ($${String.format("%.2f", montoPagado)}) se aplicó al capital."
                } else {
                    "✅ Todo el pago se aplicó al capital."
                }
                
                Triple(0.0, aCapital, advertencia)
            }
        }
    }
    
    /**
     * Calcula los días transcurridos entre dos fechas
     */
    fun calcularDiasTranscurridos(fechaInicio: Long, fechaFin: Long): Int {
        val diferencia = fechaFin - fechaInicio
        return TimeUnit.MILLISECONDS.toDays(diferencia).toInt()
    }
    
    /**
     * Calcula el interés proporcional basado en días transcurridos
     * (Para cuando el cliente paga antes o después del período completo)
     * 
     * @param capitalPendiente Capital actual
     * @param tasaInteresPorPeriodo Tasa de interés del período completo
     * @param frecuenciaPago Frecuencia del préstamo
     * @param diasTranscurridos Días desde el último pago
     * @return Interés proporcional a los días transcurridos
     */
    fun calcularInteresProporcional(
        capitalPendiente: Double,
        tasaInteresPorPeriodo: Double,
        frecuenciaPago: FrecuenciaPago,
        diasTranscurridos: Int
    ): Double {
        val diasDelPeriodo = when (frecuenciaPago) {
            FrecuenciaPago.DIARIO -> 1
            FrecuenciaPago.QUINCENAL -> 15
            FrecuenciaPago.MENSUAL -> 30
        }
        
        val interesDelPeriodoCompleto = calcularInteresPeriodo(capitalPendiente, tasaInteresPorPeriodo)
        
        // Interés proporcional: (interés del período / días del período) * días transcurridos
        return (interesDelPeriodoCompleto / diasDelPeriodo) * diasTranscurridos
    }
    
    /**
     * Convierte la frecuencia a texto en español
     */
    fun frecuenciaATexto(frecuencia: FrecuenciaPago): String {
        return when (frecuencia) {
            FrecuenciaPago.DIARIO -> "Diario"
            FrecuenciaPago.QUINCENAL -> "Quincenal"
            FrecuenciaPago.MENSUAL -> "Mensual"
        }
    }
    
    /**
     * Calcula el total proyectado a pagar (solo referencial)
     * En este modelo el préstamo termina cuando capital = 0
     * 
     * Asume que el cliente pagará solo el interés cada período
     * @return Estimación muy aproximada del total
     */
    fun calcularTotalProyectado(
        capitalPrestado: Double,
        tasaInteresPorPeriodo: Double,
        numeroPeriodosEstimados: Int
    ): Double {
        val interesesEstimados = (capitalPrestado * (tasaInteresPorPeriodo / 100.0)) * numeroPeriodosEstimados
        return capitalPrestado + interesesEstimados
    }
    
    /**
     * Calcula cuántos períodos faltan (estimación)
     * Si el cliente solo paga interés = infinito
     * Si paga más = depende del monto
     */
    fun estimarPeriodosRestantes(
        capitalPendiente: Double,
        tasaInteresPorPeriodo: Double,
        montoPromedioPorPago: Double
    ): Int {
        val interesPorPeriodo = calcularInteresPeriodo(capitalPendiente, tasaInteresPorPeriodo)
        
        // Si el pago promedio es menor o igual al interés, nunca terminará
        if (montoPromedioPorPago <= interesPorPeriodo) {
            return Int.MAX_VALUE // Infinito
        }
        
        // Simplificación: asumiendo capital constante
        val capitalPorPago = montoPromedioPorPago - interesPorPeriodo
        val periodosEstimados = (capitalPendiente / capitalPorPago).toInt()
        
        return maxOf(1, periodosEstimados)
    }
    
    /**
     * Calcula el progreso del préstamo en porcentaje
     */
    fun calcularProgreso(capitalOriginal: Double, capitalPendiente: Double): Float {
        if (capitalOriginal == 0.0) return 0f
        val capitalPagado = capitalOriginal - capitalPendiente
        return ((capitalPagado / capitalOriginal) * 100.0).toFloat().coerceIn(0f, 100f)
    }
}

