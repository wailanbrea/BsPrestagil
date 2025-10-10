package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.PrestamoEntity
import com.example.bsprestagil.data.mappers.toPrestamo
import com.example.bsprestagil.data.models.EstadoPrestamo
import com.example.bsprestagil.data.models.FrecuenciaPago
import com.example.bsprestagil.data.models.Prestamo
import com.example.bsprestagil.data.repository.PrestamoRepository
import com.example.bsprestagil.data.repository.CuotaRepository
import com.example.bsprestagil.utils.CronogramaUtils
import com.example.bsprestagil.utils.AmortizacionUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoansViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val cuotaRepository = CuotaRepository(database.cuotaDao())
    
    private val _prestamos = MutableStateFlow<List<Prestamo>>(emptyList())
    val prestamos: StateFlow<List<Prestamo>> = _prestamos.asStateFlow()
    
    private val _filtroEstado = MutableStateFlow<EstadoPrestamo?>(null)
    val filtroEstado: StateFlow<EstadoPrestamo?> = _filtroEstado.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadPrestamos()
    }
    
    private fun loadPrestamos() {
        viewModelScope.launch {
            combine(
                prestamoRepository.getAllPrestamos(),
                _filtroEstado
            ) { prestamos, filtro ->
                if (filtro != null) {
                    prestamos.filter { it.estado == filtro.name }
                        .map { it.toPrestamo() }
                } else {
                    prestamos.map { it.toPrestamo() }
                }
            }.collect { prestamos ->
                _prestamos.value = prestamos
            }
        }
    }
    
    fun setFiltroEstado(estado: EstadoPrestamo?) {
        _filtroEstado.value = estado
    }
    
    fun getPrestamoById(prestamoId: String): Flow<Prestamo?> {
        return prestamoRepository.getPrestamoById(prestamoId)
            .map { it?.toPrestamo() }
    }
    
    fun getPrestamosByClienteId(clienteId: String): Flow<List<Prestamo>> {
        return prestamoRepository.getPrestamosByClienteId(clienteId)
            .map { entities -> entities.map { it.toPrestamo() } }
    }
    
    fun crearPrestamo(
        clienteId: String,
        clienteNombre: String,
        monto: Double,
        tasaInteresPorPeriodo: Double, // Ej: 20% mensual
        frecuenciaPago: FrecuenciaPago,
        numeroCuotas: Int, // NUEVO: número de cuotas
        garantiaId: String? = null,
        notas: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val fechaInicio = System.currentTimeMillis()
                
                // Calcular cuota fija usando Sistema Francés
                val montoCuotaFija = AmortizacionUtils.calcularCuotaFija(
                    capital = monto,
                    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                    numeroCuotas = numeroCuotas
                )
                
                val prestamo = PrestamoEntity(
                    id = "",
                    clienteId = clienteId,
                    clienteNombre = clienteNombre,
                    montoOriginal = monto,
                    capitalPendiente = monto, // Al inicio, el capital pendiente es el monto completo
                    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                    frecuenciaPago = frecuenciaPago.name,
                    tipoAmortizacion = "FRANCES", // Por defecto Sistema Francés
                    numeroCuotas = numeroCuotas,
                    montoCuotaFija = montoCuotaFija,
                    cuotasPagadas = 0,
                    garantiaId = garantiaId,
                    fechaInicio = fechaInicio,
                    ultimaFechaPago = fechaInicio, // Inicia con la fecha de creación
                    estado = "ACTIVO",
                    totalInteresesPagados = 0.0,
                    totalCapitalPagado = 0.0,
                    totalMorasPagadas = 0.0,
                    notas = notas
                )
                
                // Insertar préstamo y obtener el ID
                val prestamoId = prestamoRepository.insertPrestamo(prestamo)
                
                // Generar cronograma de cuotas
                val cronograma = CronogramaUtils.generarCronograma(
                    prestamoId = prestamoId,
                    montoOriginal = monto,
                    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                    frecuenciaPago = frecuenciaPago,
                    numeroCuotas = numeroCuotas,
                    fechaInicio = fechaInicio
                )
                
                // Insertar todas las cuotas
                cuotaRepository.insertCuotas(cronograma)
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                // Manejar error
            }
        }
    }
    
    fun updateEstadoPrestamo(prestamoId: String, nuevoEstado: EstadoPrestamo) {
        viewModelScope.launch {
            try {
                // TODO: Implementar actualización de estado
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}

