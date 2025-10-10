package com.example.bsprestagil

import android.app.Application
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.ConfiguracionRepository
import com.example.bsprestagil.sync.SyncManager
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BsPrestagilApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Inicializar base de datos y configuración
        applicationScope.launch {
            initializeDatabase()
        }
        
        // Configurar sincronización periódica
        SyncManager.setupPeriodicSync(this)
    }
    
    private suspend fun initializeDatabase() {
        val database = AppDatabase.getDatabase(this)
        val configuracionRepository = ConfiguracionRepository(database.configuracionDao())
        
        // Crear configuración por defecto si no existe
        configuracionRepository.initializeConfiguracionIfNeeded()
    }
}

