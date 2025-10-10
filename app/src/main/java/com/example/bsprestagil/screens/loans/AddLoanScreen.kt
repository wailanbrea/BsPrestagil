package com.example.bsprestagil.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.FrecuenciaPago

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanScreen(
    clientId: String?,
    navController: NavController
) {
    var clienteSeleccionado by remember { mutableStateOf(clientId ?: "") }
    var monto by remember { mutableStateOf("") }
    var tasaInteres by remember { mutableStateOf("10") }
    var plazoMeses by remember { mutableStateOf("12") }
    var frecuenciaPago by remember { mutableStateOf(FrecuenciaPago.MENSUAL) }
    var garantiaOpcional by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var expandedFrecuencia by remember { mutableStateOf(false) }
    
    val frecuenciasDisponibles = FrecuenciaPago.values().toList()
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Nuevo préstamo",
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cliente
            Text(
                text = "Información del cliente",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (clientId != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cliente seleccionado",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Juan Pérez González", // TODO: Obtener nombre real
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = { /* TODO: Abrir selector de cliente */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar cliente")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Detalles del préstamo
            Text(
                text = "Detalles del préstamo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto del préstamo *") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tasaInteres,
                    onValueChange = { tasaInteres = it },
                    label = { Text("Tasa de interés *") },
                    leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("%") }
                )
                
                OutlinedTextField(
                    value = plazoMeses,
                    onValueChange = { plazoMeses = it },
                    label = { Text("Plazo *") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("meses") }
                )
            }
            
            // Frecuencia de pago
            ExposedDropdownMenuBox(
                expanded = expandedFrecuencia,
                onExpandedChange = { expandedFrecuencia = it }
            ) {
                OutlinedTextField(
                    value = when(frecuenciaPago) {
                        FrecuenciaPago.DIARIO -> "Diario"
                        FrecuenciaPago.SEMANAL -> "Semanal"
                        FrecuenciaPago.QUINCENAL -> "Quincenal"
                        FrecuenciaPago.MENSUAL -> "Mensual"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frecuencia de pago *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrecuencia)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedFrecuencia,
                    onDismissRequest = { expandedFrecuencia = false }
                ) {
                    frecuenciasDisponibles.forEach { frecuencia ->
                        DropdownMenuItem(
                            text = {
                                Text(when(frecuencia) {
                                    FrecuenciaPago.DIARIO -> "Diario"
                                    FrecuenciaPago.SEMANAL -> "Semanal"
                                    FrecuenciaPago.QUINCENAL -> "Quincenal"
                                    FrecuenciaPago.MENSUAL -> "Mensual"
                                })
                            },
                            onClick = {
                                frecuenciaPago = frecuencia
                                expandedFrecuencia = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Garantía (opcional)
            Text(
                text = "Garantía (opcional)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Button(
                onClick = { /* TODO: Abrir selector de garantía */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar garantía")
            }
            
            // Notas
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Resumen
            if (monto.isNotBlank() && tasaInteres.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Resumen del préstamo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        val montoNum = monto.toDoubleOrNull() ?: 0.0
                        val tasaNum = tasaInteres.toDoubleOrNull() ?: 0.0
                        val interes = montoNum * (tasaNum / 100)
                        val total = montoNum + interes
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Capital:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "$${String.format("%,.2f", montoNum)}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Interés ($tasaNum%):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "$${String.format("%,.2f", interes)}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total a pagar:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$${String.format("%,.2f", total)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    // TODO: Guardar préstamo
                    navController.navigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = clienteSeleccionado.isNotBlank() && 
                         monto.isNotBlank() && 
                         tasaInteres.isNotBlank() && 
                         plazoMeses.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crear préstamo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

