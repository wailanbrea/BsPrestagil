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
        fechaInicio: Long,
        diaCobroPreferido: Int? = null, // NUEVO: Día del mes para cobro (1-31)
        adminId: String // NUEVO: Multi-tenant
    ): List<CuotaEntity> {
        val cuotas = mutableListOf<CuotaEntity>()
        
        // Generar tabla según el sistema seleccionado
        val tablaAmortizacion = AmortizacionUtils.generarTablaSegunSistema(
            capitalInicial = montoOriginal,
            tasaInteresPorPeriodo = tasaInteresPorPeriodo,
            numeroCuotas = numeroCuotas,
            tipoSistema = tipoAmortizacion
        )
        
        // Calcular todas las fechas de vencimiento
        val fechasVencimiento = calcularFechasVencimiento(
            fechaInicio = fechaInicio,
            numeroCuotas = numeroCuotas,
            frecuencia = frecuenciaPago,
            diaCobroPreferido = diaCobroPreferido
        )
        
        // Convertir cada fila de la tabla en una CuotaEntity
        tablaAmortizacion.forEach { fila ->
            val fechaVencimiento = fechasVencimiento[fila.numeroCuota - 1]
            
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
                notas = "Interés proyectado: $${String.format("%.2f", fila.interes)}, Capital: $${String.format("%.2f", fila.capital)}",
                adminId = adminId // NUEVO: Multi-tenant
            )
            
            cuotas.add(cuota)
        }
        
        return cuotas
    }
    
    /**
     * Calcula todas las fechas de vencimiento de las cuotas basándose en el día de cobro preferido
     */
    private fun calcularFechasVencimiento(
        fechaInicio: Long,
        numeroCuotas: Int,
        frecuencia: FrecuenciaPago,
        diaCobroPreferido: Int?
    ): List<Long> {
        val fechas = mutableListOf<Long>()
        
        if (frecuencia == FrecuenciaPago.DIARIO || diaCobroPreferido == null) {
            // Para diario o sin día preferido, usar cálculo tradicional
            for (i in 1..numeroCuotas) {
                fechas.add(calcularFechaVencimiento(fechaInicio, i, frecuencia))
            }
        } else if (frecuencia == FrecuenciaPago.QUINCENAL) {
            // Para quincenal con día preferido
            val primerVencimiento = obtenerProximaFechaCobro(fechaInicio, diaCobroPreferido)
            fechas.add(primerVencimiento)
            
            // Resto de cuotas: cada 15 días
            for (i in 1 until numeroCuotas) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = primerVencimiento
                calendar.add(Calendar.DAY_OF_MONTH, i * 15)
                fechas.add(calendar.timeInMillis)
            }
        } else if (frecuencia == FrecuenciaPago.MENSUAL) {
            // Para mensual con día preferido
            for (i in 0 until numeroCuotas) {
                val fecha = if (i == 0) {
                    obtenerProximaFechaCobro(fechaInicio, diaCobroPreferido)
                } else {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = fechas[i - 1]
                    calendar.add(Calendar.MONTH, 1)
                    ajustarDiaDelMes(calendar, diaCobroPreferido)
                    calendar.timeInMillis
                }
                fechas.add(fecha)
            }
        }
        
        return fechas
    }
    
    /**
     * Obtiene la próxima fecha de cobro basada en el día preferido
     */
    private fun obtenerProximaFechaCobro(fechaActual: Long, diaPreferido: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaActual
        
        // Intentar establecer el día en el mes actual
        calendar.set(Calendar.DAY_OF_MONTH, diaPreferido)
        ajustarDiaDelMes(calendar, diaPreferido)
        
        // Si la fecha ya pasó, mover al siguiente mes
        if (calendar.timeInMillis <= fechaActual) {
            calendar.add(Calendar.MONTH, 1)
            ajustarDiaDelMes(calendar, diaPreferido)
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * Ajusta el día del mes si no existe (ej: 31 en febrero → 28/29)
     */
    private fun ajustarDiaDelMes(calendar: Calendar, diaDeseado: Int) {
        val mesActual = calendar.get(Calendar.MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        val maxDiasEnMes = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val diaFinal = minOf(diaDeseado, maxDiasEnMes)
        
        calendar.set(Calendar.DAY_OF_MONTH, diaFinal)
        
        // Verificar que no cambió el mes
        if (calendar.get(Calendar.MONTH) != mesActual) {
            calendar.set(Calendar.MONTH, mesActual)
            calendar.set(Calendar.DAY_OF_MONTH, maxDiasEnMes)
        }
    }
    
    /**
     * Calcula la fecha de vencimiento de una cuota específica (método original)
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
     * Genera cuotas adicionales cuando se vence el plazo original
     * NUEVO: Para préstamos personales que se extienden más allá del plazo inicial
     * 
     * @param prestamoId ID del préstamo
     * @param capitalPendiente Capital que aún debe el cliente
     * @param tasaInteresPorPeriodo Tasa de interés del préstamo
     * @param frecuenciaPago Frecuencia de pago
     * @param ultimaCuota Número de la última cuota generada
     * @param cuotasAdicionales Cuántas cuotas adicionales generar (default: 1 mes a la vez)
     * @return Lista de cuotas adicionales generadas
     */
    fun generarCuotasAdicionales(
        prestamoId: String,
        capitalPendiente: Double,
        tasaInteresPorPeriodo: Double,
        frecuenciaPago: FrecuenciaPago,
        ultimaCuota: Int,
        fechaUltimaCuota: Long,
        adminId: String, // NUEVO: Multi-tenant
        cuotasAdicionales: Int = 1
    ): List<CuotaEntity> {
        
        if (capitalPendiente <= 0.0) {
            return emptyList() // No generar cuotas si ya está pagado
        }
        
        val cuotasNuevas = mutableListOf<CuotaEntity>()
        val diasPorPeriodo = when (frecuenciaPago) {
            FrecuenciaPago.DIARIO -> 1
            FrecuenciaPago.QUINCENAL -> 15
            FrecuenciaPago.MENSUAL -> 30
        }
        
        // Calcular interés de cada cuota adicional
        val interesPorCuota = calcularInteresPeriodo(capitalPendiente, tasaInteresPorPeriodo)
        
        // Generar cuotas adicionales
        var fechaVencimiento = fechaUltimaCuota
        repeat(cuotasAdicionales) { index ->
            val numeroCuota = ultimaCuota + index + 1
            
            // Calcular fecha de vencimiento
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = fechaVencimiento
            calendar.add(Calendar.DAY_OF_MONTH, diasPorPeriodo)
            fechaVencimiento = calendar.timeInMillis
            
            // Crear cuota adicional (solo interés, el capital se paga cuando pueda)
            val cuotaNueva = CuotaEntity(
                id = "",
                prestamoId = prestamoId,
                numeroCuota = numeroCuota,
                fechaVencimiento = fechaVencimiento,
                montoCuotaMinimo = interesPorCuota, // Cuota mínima = solo interés
                capitalPendienteAlInicio = capitalPendiente,
                montoPagado = 0.0,
                montoAInteres = 0.0,
                montoACapital = 0.0,
                montoMora = 0.0,
                estado = "PENDIENTE",
                fechaPago = null,
                notas = "Cuota adicional generada automáticamente - Interés proyectado: $${String.format("%,.2f", interesPorCuota)}, Capital: variable según pago",
                adminId = adminId, // NUEVO: Multi-tenant
                pendingSync = true,
                lastSyncTime = 0L,
                firebaseId = null
            )
            
            cuotasNuevas.add(cuotaNueva)
        }
        
        android.util.Log.d("CronogramaUtils", "🔄 Generadas $cuotasAdicionales cuotas adicionales para préstamo $prestamoId")
        android.util.Log.d("CronogramaUtils", "  - Capital pendiente: $${String.format("%,.2f", capitalPendiente)}")
        android.util.Log.d("CronogramaUtils", "  - Interés por cuota: $${String.format("%,.2f", interesPorCuota)}")
        android.util.Log.d("CronogramaUtils", "  - Cuotas: ${ultimaCuota + 1} a ${ultimaCuota + cuotasAdicionales}")
        
        return cuotasNuevas
    }
    
    private fun calcularInteresPeriodo(capitalPendiente: Double, tasaInteresPorPeriodo: Double): Double {
        return capitalPendiente * (tasaInteresPorPeriodo / 100.0)
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

