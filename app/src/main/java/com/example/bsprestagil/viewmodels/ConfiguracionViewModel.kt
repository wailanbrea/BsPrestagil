package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.ConfiguracionEntity
import com.example.bsprestagil.data.mappers.toConfiguracion
import com.example.bsprestagil.data.models.Configuracion
import com.example.bsprestagil.data.repository.ConfiguracionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConfiguracionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val configuracionRepository = ConfiguracionRepository(database.configuracionDao())
    
    private val _configuracion = MutableStateFlow<Configuracion?>(null)
    val configuracion: StateFlow<Configuracion?> = _configuracion.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadConfiguracion()
    }
    
    private fun loadConfiguracion() {
        viewModelScope.launch {
            configuracionRepository.getConfiguracion()
                .map { it?.toConfiguracion() }
                .collect { config ->
                    _configuracion.value = config
                }
        }
    }
    
    fun updateTasaInteres(nuevaTasa: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val config = configuracionRepository.getConfiguracionSync()
                if (config != null) {
                    configuracionRepository.updateConfiguracion(
                        config.copy(tasaInteresBase = nuevaTasa)
                    )
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
    
    fun updateTasaMora(nuevaTasa: Double) {
        viewModelScope.launch {
            try {
                val config = configuracionRepository.getConfiguracionSync()
                if (config != null) {
                    configuracionRepository.updateConfiguracion(
                        config.copy(tasaMoraBase = nuevaTasa)
                    )
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    fun updateConfiguracion(nuevaConfig: ConfiguracionEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                configuracionRepository.updateConfiguracion(nuevaConfig)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}

