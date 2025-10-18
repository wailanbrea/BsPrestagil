package com.example.bsprestagil

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
import com.example.bsprestagil.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    
    // NUEVO: Listener en tiempo real de Firestore
    private var realtimeSync: RealtimeFirestoreSync? = null
    
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
                    
                    // 🔥 NUEVO: Iniciar listeners en tiempo real
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
                    
                } catch (e: Exception) {
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
    
    override fun onDestroy() {
        super.onDestroy()
        // Detener listeners cuando se destruye la actividad
        realtimeSync?.stopAllListeners()
        Log.d("MainActivity", "🛑 Listeners detenidos")
    }
}