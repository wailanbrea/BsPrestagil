package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.NotificacionEntity
import com.example.bsprestagil.data.repository.NotificacionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val notificacionRepository = NotificacionRepository(database.notificacionDao())
    
    val notificaciones: StateFlow<List<NotificacionEntity>> = notificacionRepository
        .getAllNotificaciones()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val notificacionesNoLeidas: StateFlow<List<NotificacionEntity>> = notificacionRepository
        .getNotificacionesNoLeidas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val contadorNoLeidas: StateFlow<Int> = notificacionRepository
        .getContadorNoLeidas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    fun marcarComoLeida(notificacionId: String) {
        viewModelScope.launch {
            notificacionRepository.marcarComoLeida(notificacionId)
        }
    }
    
    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            notificacionRepository.marcarTodasComoLeidas()
        }
    }
    
    fun eliminarNotificacion(notificacion: NotificacionEntity) {
        viewModelScope.launch {
            notificacionRepository.eliminarNotificacion(notificacion)
        }
    }
}

