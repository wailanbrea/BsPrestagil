package com.example.bsprestagil.utils

import com.example.bsprestagil.data.models.FrecuenciaPago
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
            FrecuenciaPago.SEMANAL -> 7
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
            FrecuenciaPago.SEMANAL -> "Semanal"
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
        return ((capitalPagado / capitalOriginal) * 100f).coerceIn(0f, 100f)
    }
}

