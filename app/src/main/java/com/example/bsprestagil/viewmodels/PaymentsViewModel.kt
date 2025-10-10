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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val pagoRepository = PagoRepository(database.pagoDao())
    
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
    
    fun registrarPago(
        prestamoId: String,
        clienteId: String,
        clienteNombre: String,
        monto: Double,
        montoCuota: Double,
        montoMora: Double,
        numeroCuota: Int,
        metodoPago: MetodoPago,
        recibidoPor: String,
        notas: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val pago = PagoEntity(
                    id = "",
                    prestamoId = prestamoId,
                    clienteId = clienteId,
                    clienteNombre = clienteNombre,
                    monto = monto + montoMora,
                    montoCuota = montoCuota,
                    montoMora = montoMora,
                    fechaPago = System.currentTimeMillis(),
                    fechaVencimiento = System.currentTimeMillis(),
                    numeroCuota = numeroCuota,
                    metodoPago = metodoPago.name,
                    recibidoPor = recibidoPor,
                    notas = notas,
                    reciboUrl = ""
                )
                
                pagoRepository.insertPago(pago)
                loadEstadisticasHoy() // Actualizar estadísticas
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                // Manejar error
            }
        }
    }
    
    fun refresh() {
        loadPagos()
        loadEstadisticasHoy()
    }
}

