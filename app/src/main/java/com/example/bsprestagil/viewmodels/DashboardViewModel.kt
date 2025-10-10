package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.mappers.toPrestamo
import com.example.bsprestagil.data.models.Prestamo
import com.example.bsprestagil.data.repository.PrestamoRepository
import com.example.bsprestagil.data.repository.PagoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalPrestado: Double = 0.0,
    val interesesGenerados: Double = 0.0,
    val carteraVencida: Double = 0.0,
    val prestamosActivos: Int = 0,
    val prestamosAtrasados: Int = 0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val prestamoRepository = PrestamoRepository(database.prestamoDao())
    private val pagoRepository = PagoRepository(database.pagoDao())
    
    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()
    
    private val _prestamosRecientes = MutableStateFlow<List<Prestamo>>(emptyList())
    val prestamosRecientes: StateFlow<List<Prestamo>> = _prestamosRecientes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Cargar estadísticas
            try {
                val totalPrestado = prestamoRepository.getTotalPrestado()
                val carteraVencida = prestamoRepository.getCarteraVencida()
                val prestamosActivos = prestamoRepository.getPrestamosCountByEstado("ACTIVO")
                val prestamosAtrasados = prestamoRepository.getPrestamosCountByEstado("ATRASADO")
                
                // Calcular intereses del mes actual
                val inicioMes = getStartOfMonth()
                val interesesMes = pagoRepository.getTotalCobradoDesde(inicioMes) - 
                                   pagoRepository.getTotalMoraDesde(inicioMes)
                
                _stats.value = DashboardStats(
                    totalPrestado = totalPrestado,
                    interesesGenerados = interesesMes,
                    carteraVencida = carteraVencida,
                    prestamosActivos = prestamosActivos,
                    prestamosAtrasados = prestamosAtrasados
                )
            } catch (e: Exception) {
                // Manejar error
            }
            
            // Cargar préstamos recientes
            prestamoRepository.getAllPrestamos()
                .map { entities -> entities.take(5).map { it.toPrestamo() } }
                .collect { prestamos ->
                    _prestamosRecientes.value = prestamos
                    _isLoading.value = false
                }
        }
    }
    
    fun refresh() {
        loadDashboardData()
    }
    
    private fun getStartOfMonth(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

