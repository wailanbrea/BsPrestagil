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
import androidx.navigation.NavController
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.SettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    var showInterestDialog by remember { mutableStateOf(false) }
    var tasaInteres by remember { mutableStateOf("10") }
    
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
                    subtitle = "$tasaInteres%",
                    onClick = { showInterestDialog = true }
                )
            }
            
            item {
                SettingsCard(
                    title = "Personalización de recibos",
                    subtitle = "Logo, datos del negocio",
                    onClick = { /* TODO: Abrir pantalla de personalización */ }
                )
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
                    onClick = { /* TODO: Cerrar sesión */ },
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
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { showInterestDialog = false }) {
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
}

