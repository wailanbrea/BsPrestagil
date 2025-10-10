package com.example.bsprestagil.screens.payments

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
import com.example.bsprestagil.data.models.MetodoPago

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    loanId: String,
    navController: NavController
) {
    var monto by remember { mutableStateOf("") }
    var montoMora by remember { mutableStateOf("0") }
    var metodoPago by remember { mutableStateOf(MetodoPago.EFECTIVO) }
    var notas by remember { mutableStateOf("") }
    var expandedMetodo by remember { mutableStateOf(false) }
    
    val metodosDisponibles = MetodoPago.values().toList()
    
    // Datos del préstamo (ejemplo)
    val clienteNombre = "Juan Pérez González"
    val montoCuota = 1000.0
    val numeroCuota = 5
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Registrar pago",
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
            // Información del préstamo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Préstamo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = clienteNombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cuota #$numeroCuota",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${String.format("%,.2f", montoCuota)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Text(
                text = "Detalles del pago",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto del pago *") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                placeholder = { Text(String.format("%.2f", montoCuota)) }
            )
            
            OutlinedTextField(
                value = montoMora,
                onValueChange = { montoMora = it },
                label = { Text("Monto de mora") },
                leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") }
            )
            
            // Método de pago
            ExposedDropdownMenuBox(
                expanded = expandedMetodo,
                onExpandedChange = { expandedMetodo = it }
            ) {
                OutlinedTextField(
                    value = when(metodoPago) {
                        MetodoPago.EFECTIVO -> "Efectivo"
                        MetodoPago.TRANSFERENCIA -> "Transferencia"
                        MetodoPago.TARJETA -> "Tarjeta"
                        MetodoPago.OTRO -> "Otro"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Método de pago *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMetodo)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedMetodo,
                    onDismissRequest = { expandedMetodo = false }
                ) {
                    metodosDisponibles.forEach { metodo ->
                        DropdownMenuItem(
                            text = {
                                Text(when(metodo) {
                                    MetodoPago.EFECTIVO -> "Efectivo"
                                    MetodoPago.TRANSFERENCIA -> "Transferencia"
                                    MetodoPago.TARJETA -> "Tarjeta"
                                    MetodoPago.OTRO -> "Otro"
                                })
                            },
                            onClick = {
                                metodoPago = metodo
                                expandedMetodo = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Resumen
            if (monto.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val montoNum = monto.toDoubleOrNull() ?: 0.0
                        val moraNum = montoMora.toDoubleOrNull() ?: 0.0
                        val total = montoNum + moraNum
                        
                        Text(
                            text = "Total a registrar",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${String.format("%,.2f", total)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    // TODO: Guardar pago
                    navController.navigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = monto.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar pago",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

