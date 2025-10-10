package com.example.bsprestagil.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.mappers.toCuota
import com.example.bsprestagil.data.models.Cuota
import com.example.bsprestagil.data.repository.CuotaRepository
import kotlinx.coroutines.flow.*

class CuotasViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val cuotaRepository = CuotaRepository(database.cuotaDao())
    
    fun getCuotasByPrestamoId(prestamoId: String): Flow<List<Cuota>> {
        return cuotaRepository.getCuotasByPrestamoId(prestamoId)
            .map { entities -> entities.map { it.toCuota() } }
    }
    
    fun getCuotaById(cuotaId: String): Flow<Cuota?> {
        return cuotaRepository.getCuotaById(cuotaId)
            .map { it?.toCuota() }
    }
}

