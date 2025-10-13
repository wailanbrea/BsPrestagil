package com.example.bsprestagil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.bsprestagil.navigation.NavGraph
import com.example.bsprestagil.notifications.AppNotificationManager
import com.example.bsprestagil.notifications.NotificationScheduler
import com.example.bsprestagil.ui.theme.BsPrestagilTheme
import com.example.bsprestagil.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
}