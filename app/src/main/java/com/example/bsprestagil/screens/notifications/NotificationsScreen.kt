package com.example.bsprestagil.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.database.entities.NotificacionEntity
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.viewmodels.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = viewModel()
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val notificaciones by viewModel.notificaciones.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Notificaciones",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { viewModel.marcarTodasComoLeidas() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Marcar todas como leídas")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notificaciones) { notificacion ->
                NotificationCard(
                    notificacion = notificacion,
                    dateFormat = dateFormat,
                    onClick = { 
                        viewModel.marcarComoLeida(notificacion.id)
                        // TODO: Navegar según el tipo de notificación
                    }
                )
            }
            
            if (notificaciones.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay notificaciones",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notificacion: NotificacionEntity,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val (icon, color) = when (notificacion.tipo) {
        "PAGO_VENCIDO" -> Icons.Default.Error to ErrorColor
        "PAGO_PROXIMO" -> Icons.Default.Warning to WarningColor
        "PAGO_RECIBIDO" -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        "NUEVO_CLIENTE" -> Icons.Default.PersonAdd to MaterialTheme.colorScheme.primary
        else -> Icons.Default.Notifications to MaterialTheme.colorScheme.primary
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) 
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacion.titulo,
                    fontSize = 16.sp,
                    fontWeight = if (notificacion.leida) FontWeight.Medium else FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notificacion.mensaje,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateFormat.format(Date(notificacion.fecha)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            if (!notificacion.leida) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}

