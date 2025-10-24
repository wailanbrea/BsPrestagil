package com.example.bsprestagil

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.repository.*
import com.example.bsprestagil.firebase.FirebaseToRoomSync
import com.example.bsprestagil.firebase.RealtimeFirestoreSync
import com.example.bsprestagil.navigation.NavGraph
import com.example.bsprestagil.notifications.AppNotificationManager
import com.example.bsprestagil.notifications.NotificationScheduler
import com.example.bsprestagil.ui.theme.BsPrestagilTheme
import com.example.bsprestagil.utils.LocaleContextWrapper
import com.example.bsprestagil.utils.LocaleManager
import com.example.bsprestagil.utils.NetworkUtils
import com.example.bsprestagil.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : FragmentActivity() {
    
    // NUEVO: Listener en tiempo real de Firestore
    private var realtimeSync: RealtimeFirestoreSync? = null
    
    /**
     * Aplica el locale guardado al contexto base
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(applyLocale(newBase))
    }
    
    /**
     * Aplica el locale al contexto usando el enfoque nativo de Android
     */
    private fun applyLocale(context: Context): Context {
        return try {
            val savedLanguage = runBlocking { LocaleManager.getCurrentLanguageSync(context) }
            LocaleContextWrapper.wrap(context, savedLanguage)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error applying locale: ${e.message}")
            context
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Instalar SplashScreen API oficial
        installSplashScreen()
        
        // NUEVO: Sincronizar datos al iniciar la app (si hay usuario autenticado)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val database = AppDatabase.getDatabase(applicationContext)
            val clienteRepository = ClienteRepository(database.clienteDao())
            val prestamoRepository = PrestamoRepository(database.prestamoDao())
            val pagoRepository = PagoRepository(database.pagoDao())
            val cuotaRepository = CuotaRepository(database.cuotaDao())
            val garantiaRepository = GarantiaRepository(database.garantiaDao())
            val configuracionRepository = ConfiguracionRepository(database.configuracionDao())
            val usuarioRepository = UsuarioRepository(database.usuarioDao())
            
            lifecycleScope.launch {
                try {
                    Log.d("MainActivity", "🔄 Sincronizando datos al iniciar app...")
                    
                    // Verificar conectividad antes de sincronizar
                    if (NetworkUtils.checkNetworkStatus(this@MainActivity)) {
                        val firebaseToRoomSync = FirebaseToRoomSync(
                            clienteRepository = clienteRepository,
                            prestamoRepository = prestamoRepository,
                            pagoRepository = pagoRepository,
                            cuotaRepository = cuotaRepository,
                            garantiaRepository = garantiaRepository,
                            configuracionRepository = configuracionRepository,
                            usuarioRepository = usuarioRepository
                        )
                        
                        firebaseToRoomSync.fullSync()
                        Log.d("MainActivity", "✅ Sincronización inicial completada")
                    } else {
                        Log.w("MainActivity", "⚠️ Sin conexión a internet. Sincronización pospuesta.")
                    }
                    
                    // 🔥 NUEVO: Iniciar listeners en tiempo real solo si hay conexión
                    if (NetworkUtils.checkNetworkStatus(this@MainActivity)) {
                        Log.d("MainActivity", "🔥 Iniciando sincronización en tiempo real...")
                        realtimeSync = RealtimeFirestoreSync(
                            clienteRepository = clienteRepository,
                            prestamoRepository = prestamoRepository,
                            pagoRepository = pagoRepository,
                            cuotaRepository = cuotaRepository,
                            garantiaRepository = garantiaRepository,
                            usuarioRepository = usuarioRepository
                        )
                        realtimeSync?.startAllListeners()
                        Log.d("MainActivity", "✅ Listeners en tiempo real activos")
                    } else {
                        Log.w("MainActivity", "⚠️ Sin conexión. Listeners en tiempo real no iniciados.")
                    }
                    
                } catch (e: Exception) {
                    NetworkUtils.handleNetworkError(e, "sincronización inicial")
                    Log.e("MainActivity", "❌ Error en sincronización inicial: ${e.message}")
                    // No bloquear el inicio de la app si falla
                }
            }
        }
        
        // Inicializar sistema de notificaciones
        AppNotificationManager.createNotificationChannels(this)
        NotificationScheduler.scheduleNotificationCheck(this)
        
        enableEdgeToEdge()
        setContent {
            BsPrestagilTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
    
    /**
     * Maneja cambios de configuración (incluyendo cambios de idioma)
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Aplicar el locale guardado cuando cambie la configuración
        lifecycleScope.launch {
            try {
                val savedLanguage = LocaleManager.getCurrentLanguageSync(this@MainActivity)
                LocaleManager.applyLocale(this@MainActivity, savedLanguage)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error applying locale on config change: ${e.message}")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Detener listeners cuando se destruye la actividad
        realtimeSync?.stopAllListeners()
        Log.d("MainActivity", "🛑 Listeners detenidos")
    }
}