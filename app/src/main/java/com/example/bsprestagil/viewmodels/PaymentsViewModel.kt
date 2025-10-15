package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.PagoEntity
import com.example.bsprestagil.data.mappers.toPago
import com.example.bsprestagil.data.models.MetodoPago
import com.example.bsprestagil.data.models.Pago
import com.example.bsprestagil.data.repository.PagoRepository
import com.example.bsprestagil.data.repository.PrestamoRepository
import com.example.bsprestagil.data.repository.CuotaRepository
import com.example.bsprestagil.utils.InteresUtils
import com.example.bsprestagil.utils.CronogramaUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val pagoRepository = PagoRepository(database.pagoDao())
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val cuotaRepository = CuotaRepository(database.cuotaDao())
    
    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos.asStateFlow()
    
    // NOTA: Para pagos usamos email porque recibidoPor guarda el email del usuario
    // Para mantener consistencia con otros ViewModels, el método se llama igual
    private val _cobradorEmail = MutableStateFlow<String?>(null)
    
    // Exponer todos los pagos como Flow (para filtrados externos)
    fun getAllPagos(): Flow<List<Pago>> {
        return pagoRepository.getAllPagos()
            .map { entities -> entities.map { it.toPago() } }
    }
    
    private val _totalCobradoHoy = MutableStateFlow(0.0)
    val totalCobradoHoy: StateFlow<Double> = _totalCobradoHoy.asStateFlow()
    
    private val _countPagosHoy = MutableStateFlow(0)
    val countPagosHoy: StateFlow<Int> = _countPagosHoy.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadPagos()
        loadEstadisticasHoy()
    }
    
    /**
     * Establece el filtro del cobrador para ver solo sus pagos
     * @param cobradorEmail Email del cobrador (porque recibidoPor guarda email)
     * Nota: Mantiene el nombre setCobradorFilter() por consistencia con otros ViewModels
     */
    fun setCobradorFilter(cobradorEmail: String?) {
        _cobradorEmail.value = cobradorEmail
    }
    
    private fun loadPagos() {
        viewModelScope.launch {
            combine(
                pagoRepository.getAllPagos(),
                _cobradorEmail
            ) { pagos, cobradorEmail ->
                if (cobradorEmail != null) {
                    // ⭐ Filtrar por email (recibidoPor siempre contiene el email del usuario)
                    pagos.filter { it.recibidoPor == cobradorEmail }.map { it.toPago() }
                } else {
                    pagos.map { it.toPago() }
                }
            }.collect { pagos ->
                _pagos.value = pagos
            }
        }
    }
    
    private fun loadEstadisticasHoy() {
        viewModelScope.launch {
            try {
                val (total, count) = pagoRepository.getPagosDeHoy()
                _totalCobradoHoy.value = total
                _countPagosHoy.value = count
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    fun getPagoById(pagoId: String): Flow<Pago?> {
        return pagoRepository.getPagoById(pagoId)
            .map { it?.toPago() }
    }
    
    fun getPagosByPrestamoId(prestamoId: String): Flow<List<Pago>> {
        return pagoRepository.getPagosByPrestamoId(prestamoId)
            .map { entities -> entities.map { it.toPago() } }
    }
    
    /**
     * Registra un pago usando el modelo de interés sobre saldo
     * El pago se distribuye: primero al interés, luego al capital
     * Se vincula a la cuota correspondiente
     */
    fun registrarPago(
        prestamoId: String,
        cuotaId: String?, // ID de la cuota que se está pagando
        numeroCuota: Int, // Número de cuota para reporte
        clienteId: String,
        clienteNombre: String,
        montoPagado: Double,
        montoMora: Double,
        metodoPago: MetodoPago,
        recibidoPor: String,
        notas: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Obtener el préstamo actual
                prestamoRepository.getPrestamoById(prestamoId).firstOrNull()?.let { prestamo ->
                    val fechaPagoActual = System.currentTimeMillis()
                    
                    // Calcular días transcurridos desde el último pago
                    val diasTranscurridos = InteresUtils.calcularDiasTranscurridos(
                        prestamo.ultimaFechaPago,
                        fechaPagoActual
                    )
                    
                    // Obtener la cuota correspondiente del cronograma (si existe)
                    val cuotaCronograma = if (cuotaId != null) {
                        cuotaRepository.getCuotaById(cuotaId).firstOrNull()
                    } else null
                    
                    // Calcular interés y capital según el cronograma
                    val (interesCalculado, montoAInteres, montoACapital) = if (cuotaCronograma != null) {
                        // Extraer distribución del cronograma desde las notas
                        // Formato: "Interés proyectado: $5000.00, Capital: $2346.59"
                        val interesProyectado = try {
                            cuotaCronograma.notas
                                .substringAfter("Interés proyectado: $")
                                .substringBefore(",")
                                .replace(",", "")
                                .trim()
                                .toDoubleOrNull() ?: 0.0
                        } catch (e: Exception) {
                            0.0
                        }
                        
                        val capitalProyectado = try {
                            cuotaCronograma.notas
                                .substringAfter("Capital: $")
                                .replace(",", "")
                                .trim()
                                .toDoubleOrNull() ?: 0.0
                        } catch (e: Exception) {
                            0.0
                        }
                        
                        // Si paga exactamente la cuota o más, usa la distribución del cronograma
                        if (montoPagado >= cuotaCronograma.montoCuotaMinimo) {
                            // Pago normal o con excedente
                            val excedente = montoPagado - cuotaCronograma.montoCuotaMinimo
                            Triple(
                                interesProyectado, // Interés del cronograma
                                interesProyectado, // A interés
                                capitalProyectado + excedente // A capital (incluye excedente)
                            )
                        } else {
                            // Pago parcial: distribución proporcional
                            val proporcion = montoPagado / cuotaCronograma.montoCuotaMinimo
                            Triple(
                                interesProyectado,
                                interesProyectado * proporcion,
                                capitalProyectado * proporcion
                            )
                        }
                    } else {
                        // Fallback al cálculo manual (si no hay cronograma)
                        val interesCalc = InteresUtils.calcularInteresProporcional(
                            capitalPendiente = prestamo.capitalPendiente,
                            tasaInteresPorPeriodo = prestamo.tasaInteresPorPeriodo,
                            frecuenciaPago = com.example.bsprestagil.data.models.FrecuenciaPago.valueOf(prestamo.frecuenciaPago),
                            diasTranscurridos = diasTranscurridos
                        )
                        val (interes, capital) = InteresUtils.distribuirPago(
                            montoPagado = montoPagado,
                            interesDelPeriodo = interesCalc,
                            capitalPendiente = prestamo.capitalPendiente
                        )
                        Triple(interesCalc, interes, capital)
                    }
                    
                    // Calcular el nuevo capital pendiente
                    val nuevoCapitalPendiente = (prestamo.capitalPendiente - montoACapital).coerceAtLeast(0.0)
                    
                    // Registrar el pago con toda la información detallada
                    val pago = PagoEntity(
                        id = "",
                        prestamoId = prestamoId,
                        cuotaId = cuotaId,
                        numeroCuota = numeroCuota,
                        clienteId = clienteId,
                        clienteNombre = clienteNombre,
                        montoPagado = montoPagado,
                        montoAInteres = montoAInteres,
                        montoACapital = montoACapital,
                        montoMora = montoMora,
                        fechaPago = fechaPagoActual,
                        diasTranscurridos = diasTranscurridos,
                        interesCalculado = interesCalculado,
                        capitalPendienteAntes = prestamo.capitalPendiente,
                        capitalPendienteDespues = nuevoCapitalPendiente,
                        metodoPago = metodoPago.name,
                        recibidoPor = recibidoPor,
                        notas = notas,
                        reciboUrl = ""
                    )
                    
                    pagoRepository.insertPago(pago)
                    
                    // Actualizar la cuota correspondiente si existe
                    var huboAbonoExtraordinario = false
                    if (cuotaId != null) {
                        cuotaRepository.getCuotaById(cuotaId).firstOrNull()?.let { cuota ->
                            val cuotaActualizada = CronogramaUtils.actualizarCuotaConPago(
                                cuota = cuota,
                                montoPagado = montoPagado,
                                montoAInteres = montoAInteres,
                                montoACapital = montoACapital,
                                montoMora = montoMora,
                                fechaPago = fechaPagoActual
                            )
                            cuotaRepository.updateCuota(cuotaActualizada)
                            
                            // Detectar si pagó más que la cuota mínima (abono extraordinario)
                            huboAbonoExtraordinario = montoPagado > cuota.montoCuotaMinimo
                        }
                    }
                    
                    // Si hubo abono extraordinario, recalcular cuotas futuras
                    if (huboAbonoExtraordinario) {
                        recalcularCuotasFuturasPorAbono(
                            prestamoId = prestamoId,
                            nuevoCapitalPendiente = nuevoCapitalPendiente,
                            tasaInteresPorPeriodo = prestamo.tasaInteresPorPeriodo,
                            tipoAmortizacion = prestamo.tipoAmortizacion
                        )
                    }
                    
                    // Actualizar el préstamo
                    actualizarPrestamoDespuesDePago(
                        prestamo = prestamo,
                        nuevoCapitalPendiente = nuevoCapitalPendiente,
                        montoAInteres = montoAInteres,
                        montoACapital = montoACapital,
                        montoMora = montoMora,
                        fechaPago = fechaPagoActual
                    )
                    
                    loadEstadisticasHoy() // Actualizar estadísticas
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                // Manejar error
            }
        }
    }
    
    private suspend fun actualizarPrestamoDespuesDePago(
        prestamo: com.example.bsprestagil.data.database.entities.PrestamoEntity,
        nuevoCapitalPendiente: Double,
        montoAInteres: Double,
        montoACapital: Double,
        montoMora: Double,
        fechaPago: Long
    ) {
        try {
            // Contar cuántas cuotas se han pagado completamente
            val cuotasPagadas = cuotaRepository.countCuotasPagadas(prestamo.id)
            
            // Determinar nuevo estado (con tolerancia de $1.00 para errores de redondeo)
            val nuevoEstado = when {
                nuevoCapitalPendiente < 1.0 -> "COMPLETADO"  // Tolerancia: menos de $1 = completado
                cuotasPagadas >= prestamo.numeroCuotas -> "COMPLETADO"
                else -> "ACTIVO" // Puede ser ATRASADO si hay mora, pero eso se maneja en otra parte
            }
            
            // Actualizar préstamo con los nuevos valores
            prestamoRepository.updatePrestamo(
                prestamo.copy(
                    capitalPendiente = nuevoCapitalPendiente,
                    cuotasPagadas = cuotasPagadas,
                    ultimaFechaPago = fechaPago,
                    totalInteresesPagados = prestamo.totalInteresesPagados + montoAInteres,
                    totalCapitalPagado = prestamo.totalCapitalPagado + montoACapital,
                    totalMorasPagadas = prestamo.totalMorasPagadas + montoMora,
                    estado = nuevoEstado
                )
            )
        } catch (e: Exception) {
            // Manejar error
        }
    }
    
    fun refresh() {
        loadPagos()
        loadEstadisticasHoy()
    }
    
    /**
     * Recalcula las cuotas futuras después de un abono extraordinario al capital
     * 
     * Sistema ALEMÁN: Recalcula el interés (capital se mantiene fijo)
     * Sistema FRANCÉS: Reduce el plazo (elimina cuotas finales)
     */
    private suspend fun recalcularCuotasFuturasPorAbono(
        prestamoId: String,
        nuevoCapitalPendiente: Double,
        tasaInteresPorPeriodo: Double,
        tipoAmortizacion: String
    ) {
        try {
            // Obtener todas las cuotas del préstamo
            val todasLasCuotas = cuotaRepository.getCuotasByPrestamoId(prestamoId).firstOrNull() ?: return
            
            // Convertir String a TipoAmortizacion
            val tipoEnum = when (tipoAmortizacion.uppercase()) {
                "ALEMAN" -> com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN
                "FRANCES" -> com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES
                else -> return
            }
            
            // Recalcular cuotas según el sistema
            val cuotasRecalculadas = CronogramaUtils.recalcularCuotasFuturas(
                todasLasCuotas = todasLasCuotas,
                capitalPendienteActual = nuevoCapitalPendiente,
                tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                tipoAmortizacion = tipoEnum
            )
            
            // Actualizar solo las cuotas que cambiaron (PENDIENTES y CANCELADAS)
            cuotasRecalculadas.forEach { cuotaRecalculada ->
                val cuotaOriginal = todasLasCuotas.find { it.id == cuotaRecalculada.id }
                if (cuotaOriginal != null && 
                    (cuotaRecalculada.montoCuotaMinimo != cuotaOriginal.montoCuotaMinimo || 
                     cuotaRecalculada.estado != cuotaOriginal.estado)) {
                    cuotaRepository.updateCuota(cuotaRecalculada)
                }
            }
        } catch (e: Exception) {
            // Log error pero no fallar el pago
            println("Error al recalcular cuotas futuras: ${e.message}")
        }
    }
}

