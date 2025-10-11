package com.example.bsprestagil.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.SettingsCard
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.sync.SyncManager
import com.example.bsprestagil.viewmodels.AuthViewModel
import com.example.bsprestagil.viewmodels.ConfiguracionViewModel
import com.example.bsprestagil.viewmodels.SyncViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    configuracionViewModel: ConfiguracionViewModel = viewModel(),
    syncViewModel: SyncViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var showInterestDialog by remember { mutableStateOf(false) }
    var tasaInteres by remember { mutableStateOf("10") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var syncing by remember { mutableStateOf(false) }
    
    val configuracion by configuracionViewModel.configuracion.collectAsState()
    val syncStatus by syncViewModel.syncStatus.collectAsState()
    
    // Actualizar tasa de interés cuando cambie la configuración
    LaunchedEffect(configuracion) {
        configuracion?.let {
            tasaInteres = it.tasaInteresBase.toString()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General
            item {
                Text(
                    text = "GENERAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            item {
                SettingsCard(
                    title = "Tasa de interés base",
                    subtitle = "${configuracion?.tasaInteresBase ?: 10.0}%",
                    onClick = { showInterestDialog = true }
                )
            }
            
            item {
                SettingsCard(
                    title = "Personalización de recibos",
                    subtitle = "Logo, datos del negocio",
                    onClick = { navController.navigate(Screen.PersonalizacionRecibo.route) }
                )
            }
            
            // Sincronización
            item {
                Text(
                    text = "SINCRONIZACIÓN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Estado de sincronización",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (syncStatus.enSincronizacion) {
                                    Text(
                                        text = "Sincronizando...",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (syncStatus.totalPendientes > 0) {
                                    Text(
                                        text = "${syncStatus.totalPendientes} elementos pendientes",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        text = "Todo sincronizado ✓",
                                        fontSize = 14.sp,
                                        color = com.example.bsprestagil.ui.theme.SuccessColor
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    syncViewModel.iniciarSincronizacion()
                                    SyncManager.forceSyncNow(context)
                                    scope.launch {
                                        kotlinx.coroutines.delay(2000)
                                        syncViewModel.loadSyncStatus()
                                    }
                                },
                                enabled = !syncStatus.enSincronizacion
                            ) {
                                if (syncStatus.enSincronizacion) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = "Sincronizar ahora")
                                }
                            }
                        }
                        
                        // Detalles de elementos pendientes
                        if (syncStatus.totalPendientes > 0) {
                            Divider()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Pendientes de sincronizar:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                if (syncStatus.clientesPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.clientesPendientes} Clientes",
                                        fontSize = 12.sp
                                    )
                                }
                                if (syncStatus.prestamosPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.prestamosPendientes} Préstamos",
                                        fontSize = 12.sp
                                    )
                                }
                                if (syncStatus.pagosPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.pagosPendientes} Pagos",
                                        fontSize = 12.sp
                                    )
                                }
                                if (syncStatus.cuotasPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.cuotasPendientes} Cuotas",
                                        fontSize = 12.sp
                                    )
                                }
                                if (syncStatus.garantiasPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.garantiasPendientes} Garantías",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        
                        // Última sincronización
                        if (syncStatus.ultimaSync > 0) {
                            Divider()
                            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                            Text(
                                text = "Última sincronización: ${dateFormat.format(java.util.Date(syncStatus.ultimaSync))}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            // Cobradores
            item {
                Text(
                    text = "COBRADORES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                SettingsCard(
                    title = "Gestionar cobradores",
                    subtitle = "Agregar o eliminar usuarios",
                    onClick = { /* TODO: Abrir pantalla de cobradores */ }
                )
            }
            
            // Notificaciones
            item {
                Text(
                    text = "NOTIFICACIONES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                var notificacionesActivas by remember { mutableStateOf(true) }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notificaciones",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Recordatorios de pago",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = notificacionesActivas,
                            onCheckedChange = { notificacionesActivas = it }
                        )
                    }
                }
            }
            
            item {
                var envioWhatsApp by remember { mutableStateOf(true) }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mensajes WhatsApp",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enviar recibos por WhatsApp",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = envioWhatsApp,
                            onCheckedChange = { envioWhatsApp = it }
                        )
                    }
                }
            }
            
            // Cuenta
            item {
                Text(
                    text = "CUENTA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                SettingsCard(
                    title = "Perfil",
                    subtitle = "Editar información personal",
                    onClick = { /* TODO: Abrir pantalla de perfil */ }
                )
            }
            
            item {
                SettingsCard(
                    title = "Cerrar sesión",
                    onClick = { showLogoutDialog = true },
                    showArrow = false
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Diálogo de tasa de interés
    if (showInterestDialog) {
        AlertDialog(
            onDismissRequest = { showInterestDialog = false },
            title = { Text("Tasa de interés base") },
            text = {
                OutlinedTextField(
                    value = tasaInteres,
                    onValueChange = { tasaInteres = it },
                    label = { Text("Porcentaje") },
                    suffix = { Text("%") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val nuevaTasa = tasaInteres.toDoubleOrNull()
                    if (nuevaTasa != null) {
                        configuracionViewModel.updateTasaInteres(nuevaTasa)
                    }
                    showInterestDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterestDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.logout()
                    showLogoutDialog = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

