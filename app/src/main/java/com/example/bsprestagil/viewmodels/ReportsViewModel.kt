package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.ClienteRepository
import com.example.bsprestagil.data.repository.PagoRepository
import com.example.bsprestagil.data.repository.PrestamoRepository
import com.example.bsprestagil.utils.CalculosUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReportStats(
    val totalCobrado: Double = 0.0,
    val totalIntereses: Double = 0.0,
    val prestamosActivos: Int = 0,
    val prestamosAtrasados: Int = 0,
    val prestamosCompletados: Int = 0,
    val totalClientes: Int = 0,
    val clientesAlDia: Int = 0,
    val clientesAtrasados: Int = 0,
    val clientesMorosos: Int = 0,
    val tasaMorosidad: Double = 0.0
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val pagoRepository = PagoRepository(database.pagoDao())
    private val clienteRepository = ClienteRepository(database.clienteDao())
    
    private val _stats = MutableStateFlow(ReportStats())
    val stats: StateFlow<ReportStats> = _stats.asStateFlow()
    
    private val _periodo = MutableStateFlow("Mes actual")
    val periodo: StateFlow<String> = _periodo.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadStats("Mes actual")
    }
    
    fun setPeriodo(nuevoPeriodo: String) {
        _periodo.value = nuevoPeriodo
        loadStats(nuevoPeriodo)
    }
    
    private fun loadStats(periodo: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val fechaInicio = when (periodo) {
                    "Hoy" -> CalculosUtils.getInicioDiaActual()
                    "Semana actual" -> CalculosUtils.getInicioSemanaActual()
                    "Mes actual" -> CalculosUtils.getInicioMesActual()
                    "Año actual" -> getInicioAnioActual()
                    else -> CalculosUtils.getInicioMesActual()
                }
                
                // Cobros del período
                val totalCobrado = pagoRepository.getTotalCobradoDesde(fechaInicio)
                val totalIntereses = pagoRepository.getTotalInteresesDesde(fechaInicio)
                val totalMora = pagoRepository.getTotalMoraDesde(fechaInicio)
                
                // Préstamos
                val prestamosActivos = prestamoRepository.getPrestamosCountByEstado("ACTIVO")
                val prestamosAtrasados = prestamoRepository.getPrestamosCountByEstado("ATRASADO")
                val prestamosCompletados = prestamoRepository.getPrestamosCountByEstado("COMPLETADO")
                
                // Clientes
                val totalClientes = clienteRepository.getClientesCount()
                val clientesAlDia = clienteRepository.getClientesByEstado("AL_DIA")
                val clientesAtrasados = clienteRepository.getClientesByEstado("ATRASADO")
                val clientesMorosos = clienteRepository.getClientesByEstado("MOROSO")
                
                // Tasa de morosidad
                val totalPrestamosActivos = prestamosActivos + prestamosAtrasados
                val tasaMorosidad = if (totalPrestamosActivos > 0) {
                    (prestamosAtrasados.toDouble() / totalPrestamosActivos.toDouble()) * 100
                } else {
                    0.0
                }
                
                _stats.value = ReportStats(
                    totalCobrado = totalCobrado,
                    totalIntereses = totalIntereses,
                    prestamosActivos = prestamosActivos,
                    prestamosAtrasados = prestamosAtrasados,
                    prestamosCompletados = prestamosCompletados,
                    totalClientes = totalClientes,
                    clientesAlDia = clientesAlDia,
                    clientesAtrasados = clientesAtrasados,
                    clientesMorosos = clientesMorosos,
                    tasaMorosidad = tasaMorosidad
                )
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
    
    private fun getInicioAnioActual(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.MONTH, 0)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun refresh() {
        loadStats(_periodo.value)
    }
}

