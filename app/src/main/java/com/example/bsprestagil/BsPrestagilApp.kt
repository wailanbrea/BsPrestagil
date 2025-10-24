package com.example.bsprestagil

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.ConfiguracionRepository
import com.example.bsprestagil.sync.SyncManager
import com.example.bsprestagil.utils.LocaleManager
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class BsPrestagilApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Aplicar idioma guardado al iniciar la aplicación
        applicationScope.launch {
            applySavedLanguage()
        }
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Inicializar base de datos y configuración
        applicationScope.launch {
            initializeDatabase()
        }
        
        // Configurar sincronización periódica
        SyncManager.setupPeriodicSync(this)
        
        // NUEVO: Configurar worker de extensión automática de plazos
        SyncManager.setupExtensionPlazoWorker(this)
    }
    
    /**
     * Aplica el idioma guardado en preferencias al iniciar la app
     */
    private suspend fun applySavedLanguage() {
        try {
            val savedLanguage = LocaleManager.getCurrentLanguageSync(this)
            LocaleManager.applyLocale(this, savedLanguage)
        } catch (e: Exception) {
            // Si hay error, usar idioma por defecto
            LocaleManager.applyLocale(this, LocaleManager.DEFAULT_LANGUAGE)
        }
    }
    
    /**
     * Override para aplicar el locale cuando el sistema cambia la configuración
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Aplicar el idioma guardado cuando cambie la configuración
        applicationScope.launch {
            try {
                val savedLanguage = LocaleManager.getCurrentLanguageSync(this@BsPrestagilApp)
                LocaleManager.applyLocale(this@BsPrestagilApp, savedLanguage)
            } catch (e: Exception) {
                // Si hay error, usar idioma por defecto
                LocaleManager.applyLocale(this@BsPrestagilApp, LocaleManager.DEFAULT_LANGUAGE)
            }
        }
    }
    
    private suspend fun initializeDatabase() {
        val database = AppDatabase.getDatabase(this)
        val configuracionRepository = ConfiguracionRepository(database.configuracionDao())
        
        // Crear configuración por defecto si no existe
        configuracionRepository.initializeConfiguracionIfNeeded()
    }
}

