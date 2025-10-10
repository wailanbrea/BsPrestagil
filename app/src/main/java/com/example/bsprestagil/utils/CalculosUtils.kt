package com.example.bsprestagil.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

object CalculosUtils {
    
    /**
     * Calcula la mora basada en los días de retraso
     * @param montoCuota Monto de la cuota
     * @param tasaMora Tasa de mora (porcentaje)
     * @param fechaVencimiento Fecha en que debía pagar
     * @param fechaActual Fecha actual
     * @return Monto de mora calculado
     */
    fun calcularMora(
        montoCuota: Double,
        tasaMora: Double,
        fechaVencimiento: Long,
        fechaActual: Long = System.currentTimeMillis()
    ): Double {
        if (fechaActual <= fechaVencimiento) {
            return 0.0
        }
        
        val diasRetraso = getDiasEntre(fechaVencimiento, fechaActual)
        if (diasRetraso <= 0) return 0.0
        
        // Mora = montoCuota * (tasaMora/100) * días
        // O puedes usar una mora fija por día
        val moraPorDia = montoCuota * (tasaMora / 100)
        return moraPorDia * diasRetraso
    }
    
    /**
     * Calcula los días entre dos fechas
     */
    fun getDiasEntre(fecha1: Long, fecha2: Long): Int {
        val diff = fecha2 - fecha1
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
    
    /**
     * Calcula el número de cuotas según frecuencia y plazo
     */
    fun calcularNumeroCuotas(plazoMeses: Int, frecuencia: String): Int {
        return when (frecuencia) {
            "DIARIO" -> plazoMeses * 30
            "SEMANAL" -> plazoMeses * 4
            "QUINCENAL" -> plazoMeses * 2
            "MENSUAL" -> plazoMeses
            else -> plazoMeses
        }
    }
    
    /**
     * Calcula la fecha de la próxima cuota
     */
    fun calcularProximaFechaVencimiento(
        fechaInicio: Long,
        numeroCuota: Int,
        frecuencia: String
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaInicio
        
        when (frecuencia) {
            "DIARIO" -> calendar.add(Calendar.DAY_OF_MONTH, numeroCuota)
            "SEMANAL" -> calendar.add(Calendar.WEEK_OF_YEAR, numeroCuota)
            "QUINCENAL" -> calendar.add(Calendar.DAY_OF_MONTH, numeroCuota * 15)
            "MENSUAL" -> calendar.add(Calendar.MONTH, numeroCuota)
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * Formatea montos con símbolo de moneda
     */
    fun formatearMonto(monto: Double): String {
        return "$${String.format("%,.2f", monto)}"
    }
    
    /**
     * Obtiene el inicio del mes actual
     */
    fun getInicioMesActual(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Obtiene el inicio del día actual
     */
    fun getInicioDiaActual(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Obtiene el inicio de la semana actual
     */
    fun getInicioSemanaActual(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

