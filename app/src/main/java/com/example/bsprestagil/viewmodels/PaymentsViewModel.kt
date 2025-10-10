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
    
    private fun loadPagos() {
        viewModelScope.launch {
            pagoRepository.getAllPagos()
                .map { entities -> entities.map { it.toPago() } }
                .collect { pagos ->
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
                    
                    // Calcular el interés del período (proporcional a días transcurridos)
                    val interesCalculado = InteresUtils.calcularInteresProporcional(
                        capitalPendiente = prestamo.capitalPendiente,
                        tasaInteresPorPeriodo = prestamo.tasaInteresPorPeriodo,
                        frecuenciaPago = com.example.bsprestagil.data.models.FrecuenciaPago.valueOf(prestamo.frecuenciaPago),
                        diasTranscurridos = diasTranscurridos
                    )
                    
                    // Distribuir el pago entre interés y capital
                    val (montoAInteres, montoACapital) = InteresUtils.distribuirPago(
                        montoPagado = montoPagado,
                        interesDelPeriodo = interesCalculado,
                        capitalPendiente = prestamo.capitalPendiente
                    )
                    
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
                        }
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
            
            // Determinar nuevo estado
            val nuevoEstado = when {
                nuevoCapitalPendiente <= 0.0 -> "COMPLETADO"
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
}

