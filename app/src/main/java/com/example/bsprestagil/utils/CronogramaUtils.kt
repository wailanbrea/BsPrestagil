package com.example.bsprestagil.utils

import com.example.bsprestagil.data.database.entities.CuotaEntity
import com.example.bsprestagil.data.models.FrecuenciaPago
import java.util.Calendar

object CronogramaUtils {
    
    /**
     * Genera el cronograma completo de cuotas al crear un préstamo
     * Usa el Sistema Francés de Amortización (cuota fija)
     * 
     * @param prestamoId ID del préstamo
     * @param montoOriginal Capital prestado
     * @param tasaInteresPorPeriodo Tasa de interés (ej: 20% mensual)
     * @param frecuenciaPago Frecuencia de pago
     * @param numeroCuotas Número total de cuotas
     * @param fechaInicio Fecha de inicio del préstamo
     * @return Lista de cuotas generadas con distribución exacta
     */
    fun generarCronograma(
        prestamoId: String,
        montoOriginal: Double,
        tasaInteresPorPeriodo: Double,
        frecuenciaPago: FrecuenciaPago,
        numeroCuotas: Int,
        fechaInicio: Long
    ): List<CuotaEntity> {
        val cuotas = mutableListOf<CuotaEntity>()
        
        // Generar tabla de amortización profesional
        val tablaAmortizacion = AmortizacionUtils.generarTablaAmortizacion(
            capitalInicial = montoOriginal,
            tasaInteresPorPeriodo = tasaInteresPorPeriodo,
            numeroCuotas = numeroCuotas
        )
        
        // Convertir cada fila de la tabla en una CuotaEntity
        tablaAmortizacion.forEach { fila ->
            val fechaVencimiento = calcularFechaVencimiento(
                fechaInicio = fechaInicio,
                numeroCuota = fila.numeroCuota,
                frecuencia = frecuenciaPago
            )
            
            val cuota = CuotaEntity(
                id = "",  // Se asignará al insertar
                prestamoId = prestamoId,
                numeroCuota = fila.numeroCuota,
                fechaVencimiento = fechaVencimiento,
                montoCuotaMinimo = fila.cuotaFija, // Cuota FIJA (no mínimo)
                capitalPendienteAlInicio = montoOriginal - ((fila.numeroCuota - 1) * fila.capital), // Aproximado
                montoPagado = 0.0,
                montoAInteres = 0.0,
                montoACapital = 0.0,
                montoMora = 0.0,
                fechaPago = null,
                estado = "PENDIENTE",
                notas = "Interés proyectado: $${String.format("%.2f", fila.interes)}, Capital: $${String.format("%.2f", fila.capital)}"
            )
            
            cuotas.add(cuota)
        }
        
        return cuotas
    }
    
    /**
     * Calcula la fecha de vencimiento de una cuota específica
     */
    private fun calcularFechaVencimiento(
        fechaInicio: Long,
        numeroCuota: Int,
        frecuencia: FrecuenciaPago
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaInicio
        
        when (frecuencia) {
            FrecuenciaPago.DIARIO -> calendar.add(Calendar.DAY_OF_MONTH, numeroCuota)
            FrecuenciaPago.QUINCENAL -> calendar.add(Calendar.DAY_OF_MONTH, numeroCuota * 15)
            FrecuenciaPago.MENSUAL -> calendar.add(Calendar.MONTH, numeroCuota)
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * Actualiza una cuota con la información del pago realizado
     */
    fun actualizarCuotaConPago(
        cuota: CuotaEntity,
        montoPagado: Double,
        montoAInteres: Double,
        montoACapital: Double,
        montoMora: Double,
        fechaPago: Long
    ): CuotaEntity {
        // Determinar estado de la cuota después del pago
        val nuevoEstado = when {
            montoACapital > 0 && montoAInteres >= cuota.montoCuotaMinimo -> "PAGADA"
            montoAInteres >= cuota.montoCuotaMinimo -> "PAGADA"
            montoAInteres > 0 -> "PARCIAL"
            else -> "PENDIENTE"
        }
        
        return cuota.copy(
            montoPagado = cuota.montoPagado + montoPagado,
            montoAInteres = cuota.montoAInteres + montoAInteres,
            montoACapital = cuota.montoACapital + montoACapital,
            montoMora = cuota.montoMora + montoMora,
            fechaPago = fechaPago,
            estado = nuevoEstado
        )
    }
    
    /**
     * Verifica y marca cuotas vencidas
     */
    fun verificarCuotasVencidas(cuotas: List<CuotaEntity>, fechaActual: Long): List<CuotaEntity> {
        return cuotas.map { cuota ->
            if (cuota.estado == "PENDIENTE" && cuota.fechaVencimiento < fechaActual) {
                cuota.copy(estado = "VENCIDA")
            } else {
                cuota
            }
        }
    }
}

