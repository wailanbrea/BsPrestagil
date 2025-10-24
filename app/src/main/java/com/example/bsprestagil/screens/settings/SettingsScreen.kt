package com.example.bsprestagil.screens.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bsprestagil.R
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.SettingsCard
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.sync.SyncManager
import com.example.bsprestagil.viewmodels.AuthViewModel
import com.example.bsprestagil.viewmodels.ConfiguracionViewModel
import com.example.bsprestagil.viewmodels.SyncViewModel
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    configuracionViewModel: ConfiguracionViewModel = viewModel(),
    syncViewModel: SyncViewModel = viewModel()
) {
    // ⭐ Leer el rol directamente del AuthViewModel (viene de Firestore)
    val userRole by authViewModel.userRole.collectAsState()
    
    // ⭐ Si el rol aún no se ha cargado, mostrar loading
    if (userRole == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
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
                        text = stringResource(R.string.settings),
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
            BottomNavigationBar(
                navController = navController,
                userRole = userRole
            )
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
                    text = stringResource(R.string.general),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            item {
                SettingsCard(
                    title = stringResource(R.string.base_interest_rate),
                    subtitle = "${configuracion?.tasaInteresBase ?: 10.0}%",
                    onClick = { showInterestDialog = true }
                )
            }
            
            item {
                SettingsCard(
                    title = stringResource(R.string.receipt_customization),
                    subtitle = stringResource(R.string.logo_business_data),
                    onClick = { navController.navigate(Screen.PersonalizacionRecibo.route) }
                )
            }
            
            item {
                SettingsCard(
                    title = stringResource(R.string.invoice_contract_config),
                    subtitle = stringResource(R.string.terms_conditions_legal_clauses),
                    onClick = { navController.navigate(Screen.ConfiguracionFactura.route) }
                )
            }
            
            item {
                SettingsCard(
                    title = stringResource(R.string.language),
                    subtitle = stringResource(R.string.spanish_english),
                    onClick = { navController.navigate(Screen.LanguageSettings.route) }
                )
            }
            
            // Sincronización
            item {
                Text(
                    text = stringResource(R.string.synchronization),
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
                                    text = stringResource(R.string.sync_status),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (syncStatus.enSincronizacion) {
                                    Text(
                                        text = stringResource(R.string.syncing),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (syncStatus.totalPendientes > 0) {
                                    Text(
                                        text = "${syncStatus.totalPendientes} ${stringResource(R.string.pending_elements)}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.all_synced),
                                        fontSize = 14.sp,
                                        color = com.example.bsprestagil.ui.theme.SuccessColor
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (!syncStatus.enSincronizacion) {
                                        syncViewModel.iniciarSincronizacion()
                                        val workId = SyncManager.forceSyncNow(context)
                                        
                                        // Observar el trabajo hasta que termine
                                        val job = scope.launch {
                                            val workManager = WorkManager.getInstance(context)
                                            
                                            try {
                                                workManager.getWorkInfoByIdFlow(workId)
                                                    .takeWhile { workInfo ->
                                                        when (workInfo?.state) {
                                                            WorkInfo.State.SUCCEEDED,
                                                            WorkInfo.State.FAILED,
                                                            WorkInfo.State.CANCELLED,
                                                            null -> false
                                                            else -> true
                                                        }
                                                    }
                                                    .collect { }
                                                
                                                // Esperar y recargar el estado
                                                kotlinx.coroutines.delay(2000)
                                                syncViewModel.loadSyncStatus()
                                            } catch (e: Exception) {
                                                Log.e("SettingsScreen", "Error observando sincronización: ${e.message}", e)
                                                syncViewModel.loadSyncStatus()
                                            }
                                        }
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
                                    Icon(Icons.Default.Sync, contentDescription = stringResource(R.string.sync_now))
                                }
                            }
                        }
                        
                        // Detalles de elementos pendientes
                        if (syncStatus.totalPendientes > 0) {
                            Divider()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(R.string.pending_to_sync),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                if (syncStatus.clientesPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.clientesPendientes} ${stringResource(R.string.clients)}",
                                        fontSize = 12.sp
                                    )
                                }
                                if (syncStatus.prestamosPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.prestamosPendientes} ${stringResource(R.string.loans)}",
                                        fontSize = 12.sp
                                    )
                                }
                                if (syncStatus.pagosPendientes > 0) {
                                    Text(
                                        text = "• ${syncStatus.pagosPendientes} ${stringResource(R.string.payments)}",
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
                                        text = "• ${syncStatus.garantiasPendientes} ${stringResource(R.string.collaterals)}",
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
                                text = "${stringResource(R.string.last_sync)} ${dateFormat.format(java.util.Date(syncStatus.ultimaSync))}",
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
                    text = stringResource(R.string.collectors),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                SettingsCard(
                    title = stringResource(R.string.manage_collectors),
                    subtitle = stringResource(R.string.add_or_remove_users),
                    onClick = { navController.navigate(Screen.GestionCobradores.route) }
                )
            }
            
            // Notificaciones
            item {
                Text(
                    text = stringResource(R.string.notifications),
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
                                text = stringResource(R.string.notifications),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.payment_reminders),
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
                                text = stringResource(R.string.whatsapp_messages),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.send_receipts_whatsapp),
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
                    text = stringResource(R.string.account),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                SettingsCard(
                    title = stringResource(R.string.profile),
                    subtitle = stringResource(R.string.edit_personal_info),
                    onClick = { navController.navigate(Screen.Perfil.route) }
                )
            }
            
            item {
                SettingsCard(
                    title = stringResource(R.string.logout),
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
            title = { Text(stringResource(R.string.base_interest_rate)) },
            text = {
                OutlinedTextField(
                    value = tasaInteres,
                    onValueChange = { tasaInteres = it },
                    label = { Text(stringResource(R.string.percentage)) },
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
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterestDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout)) },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.logout()
                    showLogoutDialog = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

