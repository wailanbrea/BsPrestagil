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
import com.example.bsprestagil.utils.AmortizacionUtils
import com.example.bsprestagil.utils.AuthUtils
import com.example.bsprestagil.utils.CronogramaUtils
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
    
    private val _cobradorId = MutableStateFlow<String?>(null)
    
    init {
        loadPrestamos()
    }
    
    /**
     * Establece el ID del cobrador para filtrar automáticamente sus préstamos
     * @param cobradorId UID de Firebase del cobrador (se guarda en cobradorId del préstamo)
     * Nota: Estrategia de filtrado por cobradorId en la tabla préstamos
     */
    fun setCobradorFilter(cobradorId: String?) {
        _cobradorId.value = cobradorId
    }
    
    private fun loadPrestamos() {
        viewModelScope.launch {
            combine(
                prestamoRepository.getAllPrestamos(),
                _filtroEstado,
                _cobradorId
            ) { prestamos, filtro, cobradorId ->
                var resultado = prestamos
                
                // Filtrar por cobrador si está establecido
                if (cobradorId != null) {
                    resultado = resultado.filter { it.cobradorId == cobradorId }
                }
                
                // Filtrar por estado si está establecido
                if (filtro != null) {
                    resultado = resultado.filter { it.estado == filtro.name }
                }
                
                resultado.map { it.toPrestamo() }
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
    
    fun getPrestamosByCobradorId(cobradorId: String): Flow<List<Prestamo>> {
        return prestamoRepository.getPrestamosByCobradorId(cobradorId)
            .map { entities -> entities.map { it.toPrestamo() } }
    }
    
    fun crearPrestamo(
        clienteId: String,
        clienteNombre: String,
        cobradorId: String? = null,
        cobradorNombre: String? = null,
        monto: Double,
        tasaInteresPorPeriodo: Double, // Ej: 20% mensual
        frecuenciaPago: FrecuenciaPago,
        tipoAmortizacion: com.example.bsprestagil.data.models.TipoAmortizacion, // Sistema Francés o Alemán
        numeroCuotas: Int, // NUEVO: número de cuotas
        diaCobroPreferido: Int? = null, // NUEVO: día del mes para cobro (1-31)
        garantiaId: String? = null,
        notas: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val fechaInicio = System.currentTimeMillis()
                
                // Calcular cuota según el sistema seleccionado
                val tablaAmortizacion = AmortizacionUtils.generarTablaSegunSistema(
                    capitalInicial = monto,
                    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                    numeroCuotas = numeroCuotas,
                    tipoSistema = tipoAmortizacion
                )
                
                // Primera cuota (en Francés es fija, en Alemán es la mayor)
                val montoCuotaFija = tablaAmortizacion.firstOrNull()?.cuotaFija ?: 0.0
                
                val adminId = AuthUtils.getCurrentAdminId()
                
                val prestamo = PrestamoEntity(
                    id = "",
                    clienteId = clienteId,
                    clienteNombre = clienteNombre,
                    cobradorId = cobradorId,
                    cobradorNombre = cobradorNombre,
                    montoOriginal = monto,
                    capitalPendiente = monto, // Al inicio, el capital pendiente es el monto completo
                    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                    frecuenciaPago = frecuenciaPago.name,
                    tipoAmortizacion = tipoAmortizacion.name,
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
                    notas = notas,
                    adminId = adminId // NUEVO: Multi-tenant
                )
                
                // Insertar préstamo y obtener el ID
                val prestamoId = prestamoRepository.insertPrestamo(prestamo)
                
                // Generar cronograma de cuotas
                val cronograma = CronogramaUtils.generarCronograma(
                    prestamoId = prestamoId,
                    montoOriginal = monto,
                    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
                    frecuenciaPago = frecuenciaPago,
                    tipoAmortizacion = tipoAmortizacion,
                    numeroCuotas = numeroCuotas,
                    fechaInicio = fechaInicio,
                    diaCobroPreferido = diaCobroPreferido, // NUEVO: Día de cobro
                    adminId = adminId // NUEVO: Multi-tenant
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
    
    // Eliminar préstamo (solo ADMIN)
    fun deletePrestamo(prestamo: PrestamoEntity) {
        viewModelScope.launch {
            prestamoRepository.deletePrestamo(prestamo)
        }
    }
    
    // Eliminar préstamo por ID (obtiene el entity completo de Room con firebaseId)
    fun deletePrestamoById(prestamoId: String) {
        viewModelScope.launch {
            try {
                val adminId = AuthUtils.getCurrentAdminId()
                val prestamoEntity = database.prestamoDao().getPrestamoByIdSync(prestamoId, adminId)
                
                if (prestamoEntity != null) {
                    android.util.Log.d("LoansViewModel", "🗑️ Eliminando préstamo: ${prestamoEntity.id}, firebaseId: ${prestamoEntity.firebaseId}")
                    prestamoRepository.deletePrestamo(prestamoEntity)
                }
            } catch (e: Exception) {
                android.util.Log.e("LoansViewModel", "❌ Error eliminando: ${e.message}")
            }
        }
    }
}

