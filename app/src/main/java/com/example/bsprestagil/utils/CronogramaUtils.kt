package com.example.bsprestagil.utils

import com.example.bsprestagil.data.database.entities.CuotaEntity
import com.example.bsprestagil.data.models.FrecuenciaPago
import java.util.Calendar

object CronogramaUtils {
    
    // Tolerancia para comparaciones de decimales ($1.00 para redondeos)
    // Si falta menos de $1.00, se considera pagado completo
    private const val TOLERANCIA_DECIMAL = 1.00
    
    /**
     * Compara dos valores con tolerancia decimal
     * @return true si a >= b (considerando tolerancia)
     */
    private fun esMayorOIgualConTolerancia(a: Double, b: Double): Boolean {
        return a >= (b - TOLERANCIA_DECIMAL)
    }
    
    /**
     * Genera el cronograma completo de cuotas al crear un préstamo
     * Soporta Sistema Francés (cuota fija) y Alemán (capital fijo)
     * 
     * @param prestamoId ID del préstamo
     * @param montoOriginal Capital prestado
     * @param tasaInteresPorPeriodo Tasa de interés (ej: 20% mensual)
     * @param frecuenciaPago Frecuencia de pago
     * @param tipoAmortizacion Sistema de amortización (Francés o Alemán)
     * @param numeroCuotas Número total de cuotas
     * @param fechaInicio Fecha de inicio del préstamo
     * @return Lista de cuotas generadas con distribución exacta
     */
    fun generarCronograma(
        prestamoId: String,
        montoOriginal: Double,
        tasaInteresPorPeriodo: Double,
        frecuenciaPago: FrecuenciaPago,
        tipoAmortizacion: com.example.bsprestagil.data.models.TipoAmortizacion,
        numeroCuotas: Int,
        fechaInicio: Long
    ): List<CuotaEntity> {
        val cuotas = mutableListOf<CuotaEntity>()
        
        // Generar tabla según el sistema seleccionado
        val tablaAmortizacion = AmortizacionUtils.generarTablaSegunSistema(
            capitalInicial = montoOriginal,
            tasaInteresPorPeriodo = tasaInteresPorPeriodo,
            numeroCuotas = numeroCuotas,
            tipoSistema = tipoAmortizacion
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
     * Incluye tolerancia de decimales para evitar errores de redondeo
     */
    fun actualizarCuotaConPago(
        cuota: CuotaEntity,
        montoPagado: Double,
        montoAInteres: Double,
        montoACapital: Double,
        montoMora: Double,
        fechaPago: Long
    ): CuotaEntity {
        // Calcular totales acumulados
        val totalPagado = cuota.montoPagado + montoPagado
        
        android.util.Log.d("CronogramaUtils", "📊 Actualizando cuota #${cuota.numeroCuota}:")
        android.util.Log.d("CronogramaUtils", "  - Cuota mínima: ${cuota.montoCuotaMinimo}")
        android.util.Log.d("CronogramaUtils", "  - Total pagado: $totalPagado")
        android.util.Log.d("CronogramaUtils", "  - Diferencia: ${cuota.montoCuotaMinimo - totalPagado}")
        
        // Determinar estado de la cuota después del pago (con tolerancia de decimales)
        val nuevoEstado = when {
            esMayorOIgualConTolerancia(totalPagado, cuota.montoCuotaMinimo) -> {
                android.util.Log.d("CronogramaUtils", "✅ Total pagado >= cuota mínima (con tolerancia) → PAGADA")
                "PAGADA"
            }
            totalPagado > 0 -> {
                android.util.Log.d("CronogramaUtils", "⏳ Pago parcial → PARCIAL")
                "PARCIAL"
            }
            else -> {
                android.util.Log.d("CronogramaUtils", "⏸️ Sin pago → PENDIENTE")
                "PENDIENTE"
            }
        }
        
        android.util.Log.d("CronogramaUtils", "🎯 Nuevo estado de cuota: $nuevoEstado")
        
        return cuota.copy(
            montoPagado = totalPagado,
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
    
    /**
     * Recalcula cuotas futuras después de un abono extraordinario al capital
     * 
     * Sistema ALEMÁN: Recalcula el interés de cada cuota (capital se mantiene fijo)
     * Sistema FRANCÉS: Ofrece recalcular el monto o reducir el plazo
     * 
     * @param todasLasCuotas Lista completa de cuotas
     * @param capitalPendienteActual Capital pendiente después del abono
     * @param tasaInteresPorPeriodo Tasa de interés del préstamo
     * @param tipoAmortizacion Sistema de amortización
     * @return Lista de cuotas actualizada con recálculo aplicado
     */
    fun recalcularCuotasFuturas(
        todasLasCuotas: List<CuotaEntity>,
        capitalPendienteActual: Double,
        tasaInteresPorPeriodo: Double,
        tipoAmortizacion: com.example.bsprestagil.data.models.TipoAmortizacion
    ): List<CuotaEntity> {
        return when (tipoAmortizacion) {
            com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN -> {
                recalcularSistemaAleman(todasLasCuotas, capitalPendienteActual, tasaInteresPorPeriodo)
            }
            com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES -> {
                recalcularSistemaFrances(todasLasCuotas, capitalPendienteActual, tasaInteresPorPeriodo)
            }
        }
    }
    
    /**
     * Recalcula Sistema ALEMÁN: Ajusta el interés de cuotas pendientes
     * El capital por cuota se mantiene fijo
     */
    private fun recalcularSistemaAleman(
        todasLasCuotas: List<CuotaEntity>,
        capitalPendienteActual: Double,
        tasaInteresPorPeriodo: Double
    ): List<CuotaEntity> {
        val cuotasPendientes = todasLasCuotas.filter { it.estado == "PENDIENTE" }
        
        if (cuotasPendientes.isEmpty()) {
            return todasLasCuotas
        }
        
        // Calcular capital fijo por cuota restante
        val capitalFijoPorCuota = capitalPendienteActual / cuotasPendientes.size
        var capitalRestante = capitalPendienteActual
        
        return todasLasCuotas.map { cuota ->
            if (cuota.estado == "PENDIENTE") {
                // Recalcular interés sobre el capital restante
                val interesRecalculado = capitalRestante * (tasaInteresPorPeriodo / 100.0)
                val nuevaCuotaTotal = capitalFijoPorCuota + interesRecalculado
                
                // Actualizar capital restante para la siguiente cuota
                capitalRestante -= capitalFijoPorCuota
                
                cuota.copy(
                    montoCuotaMinimo = nuevaCuotaTotal,
                    capitalPendienteAlInicio = capitalRestante + capitalFijoPorCuota,
                    notas = "Interés proyectado: $${String.format("%.2f", interesRecalculado)}, Capital: $${String.format("%.2f", capitalFijoPorCuota)} (Recalculado por abono extra)"
                )
            } else {
                cuota
            }
        }
    }
    
    /**
     * Recalcula Sistema FRANCÉS: Reduce el plazo (elimina cuotas finales)
     * Mantiene la cuota fija original
     */
    private fun recalcularSistemaFrances(
        todasLasCuotas: List<CuotaEntity>,
        capitalPendienteActual: Double,
        tasaInteresPorPeriodo: Double
    ): List<CuotaEntity> {
        val cuotasPendientes = todasLasCuotas.filter { it.estado == "PENDIENTE" }
        
        if (cuotasPendientes.isEmpty()) {
            return todasLasCuotas
        }
        
        // Obtener la cuota fija original
        val cuotaFija = cuotasPendientes.firstOrNull()?.montoCuotaMinimo ?: return todasLasCuotas
        
        // Calcular cuántas cuotas se necesitan para pagar el capital pendiente
        val nuevaCantidadCuotas = AmortizacionUtils.calcularNumeroCuotasNecesarias(
            capitalPendiente = capitalPendienteActual,
            cuotaFija = cuotaFija,
            tasaInteresPorPeriodo = tasaInteresPorPeriodo
        )
        
        var capitalRestante = capitalPendienteActual
        val cuotasActualizadas = mutableListOf<CuotaEntity>()
        
        todasLasCuotas.forEachIndexed { index, cuota ->
            if (cuota.estado != "PENDIENTE") {
                // Mantener cuotas ya pagadas
                cuotasActualizadas.add(cuota)
            } else {
                val posicionEnPendientes = cuotasPendientes.indexOf(cuota)
                
                if (posicionEnPendientes < nuevaCantidadCuotas) {
                    // Recalcular distribución de interés y capital
                    val interesRecalculado = capitalRestante * (tasaInteresPorPeriodo / 100.0)
                    val capitalRecalculado = cuotaFija - interesRecalculado
                    
                    capitalRestante -= capitalRecalculado
                    
                    cuotasActualizadas.add(
                        cuota.copy(
                            capitalPendienteAlInicio = capitalRestante + capitalRecalculado,
                            notas = "Interés proyectado: $${String.format("%.2f", interesRecalculado)}, Capital: $${String.format("%.2f", capitalRecalculado)} (Recalculado por abono extra)"
                        )
                    )
                } else {
                    // Marcar cuotas excedentes como CANCELADAS
                    cuotasActualizadas.add(
                        cuota.copy(
                            estado = "CANCELADA",
                            notas = "Cuota cancelada por abono extraordinario al capital"
                        )
                    )
                }
            }
        }
        
        return cuotasActualizadas
    }
}

