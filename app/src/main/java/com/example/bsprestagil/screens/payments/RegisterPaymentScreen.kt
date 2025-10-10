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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.FrecuenciaPago
import com.example.bsprestagil.data.models.MetodoPago
import com.example.bsprestagil.utils.InteresUtils
import com.example.bsprestagil.viewmodels.ConfiguracionViewModel
import com.example.bsprestagil.viewmodels.LoansViewModel
import com.example.bsprestagil.viewmodels.PaymentsViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    loanId: String,
    navController: NavController,
    loansViewModel: LoansViewModel = viewModel(),
    paymentsViewModel: PaymentsViewModel = viewModel(),
    configuracionViewModel: ConfiguracionViewModel = viewModel()
) {
    var montoPagado by remember { mutableStateOf("") }
    var montoMora by remember { mutableStateOf("0") }
    var cobrarMora by remember { mutableStateOf(false) }
    var metodoPago by remember { mutableStateOf(MetodoPago.EFECTIVO) }
    var notas by remember { mutableStateOf("") }
    var expandedMetodo by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val metodosDisponibles = MetodoPago.values().toList()
    
    // Cargar datos del préstamo
    val prestamo by loansViewModel.getPrestamoById(loanId).collectAsState(initial = null)
    val clienteNombre = prestamo?.clienteNombre ?: "Cargando..."
    val capitalPendiente = prestamo?.capitalPendiente ?: 0.0
    val tasaInteresPorPeriodo = prestamo?.tasaInteresPorPeriodo ?: 0.0
    val fechaUltimoPago = prestamo?.ultimaFechaPago ?: System.currentTimeMillis()
    val frecuenciaPago = prestamo?.frecuenciaPago ?: FrecuenciaPago.MENSUAL
    
    // Calcular días transcurridos desde el último pago
    val diasTranscurridos = InteresUtils.calcularDiasTranscurridos(
        fechaUltimoPago,
        System.currentTimeMillis()
    )
    
    // Calcular interés del período (proporcional a días transcurridos)
    val interesCalculado = prestamo?.let {
        InteresUtils.calcularInteresProporcional(
            capitalPendiente = it.capitalPendiente,
            tasaInteresPorPeriodo = it.tasaInteresPorPeriodo,
            frecuenciaPago = it.frecuenciaPago,
            diasTranscurridos = diasTranscurridos
        )
    } ?: 0.0
    
    // Calcular distribución del pago (interés vs capital)
    val montoPagadoNum = montoPagado.toDoubleOrNull() ?: 0.0
    val (montoAInteres, montoACapital) = InteresUtils.distribuirPago(
        montoPagado = montoPagadoNum,
        interesDelPeriodo = interesCalculado,
        capitalPendiente = capitalPendiente
    )
    val nuevoCapitalPendiente = (capitalPendiente - montoACapital).coerceAtLeast(0.0)
    
    // Usuario actual
    val usuarioActual = FirebaseAuth.getInstance().currentUser?.email ?: "Admin"
    
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información del préstamo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = clienteNombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Capital pendiente:",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${String.format("%,.2f", capitalPendiente)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Días transcurridos:",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$diasTranscurridos días",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Interés del período:",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${String.format("%,.2f", interesCalculado)}",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
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
                value = montoPagado,
                onValueChange = { montoPagado = it },
                label = { Text("Monto que paga el cliente *") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                supportingText = {
                    Text("Mínimo sugerido: $${String.format("%.2f", interesCalculado)}")
                }
            )
            
            // Desglose automático del pago
            if (montoPagadoNum > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📊 Distribución del pago",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("→ A interés:", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                "$${String.format("%,.2f", montoAInteres)}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("→ A capital:", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                "$${String.format("%,.2f", montoACapital)}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (montoACapital > 0) {
                            Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Nuevo capital pendiente:",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "$${String.format("%,.2f", nuevoCapitalPendiente)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (nuevoCapitalPendiente <= 0.0)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            
                            if (nuevoCapitalPendiente <= 0.0) {
                                Text(
                                    text = "🎉 ¡Este pago liquidará el préstamo!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        
                        if (montoPagadoNum < interesCalculado) {
                            Text(
                                text = "⚠️ El monto no cubre el interés completo. Solo se aplicará a interés.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Switch para cobrar mora
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (cobrarMora) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (cobrarMora) 
                                    MaterialTheme.colorScheme.error
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cobrar mora",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (cobrarMora)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (cobrarMora) "Se aplicará mora al pago" else "Sin mora",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = cobrarMora,
                        onCheckedChange = { 
                            cobrarMora = it
                            if (!it) {
                                montoMora = "0"
                            }
                        }
                    )
                }
            }
            
            // Campo de monto de mora (solo visible si cobrarMora está activo)
            if (cobrarMora) {
                OutlinedTextField(
                    value = montoMora,
                    onValueChange = { montoMora = it },
                    label = { Text("Monto de mora *") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        focusedLabelColor = MaterialTheme.colorScheme.error,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.error
                    )
                )
            }
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    prestamo?.let { p ->
                        val montoNum = montoPagado.toDoubleOrNull() ?: 0.0
                        val moraNum = if (cobrarMora) (montoMora.toDoubleOrNull() ?: 0.0) else 0.0
                        
                        paymentsViewModel.registrarPago(
                            prestamoId = loanId,
                            clienteId = p.clienteId,
                            clienteNombre = p.clienteNombre,
                            montoPagado = montoNum,
                            montoMora = moraNum,
                            metodoPago = metodoPago,
                            recibidoPor = usuarioActual,
                            notas = notas
                        )
                        showSuccessDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = montoPagado.isNotBlank() && prestamo != null
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
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("✅ Pago registrado") },
            text = { 
                Column {
                    Text("El pago se registró correctamente:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Monto: $${String.format("%.2f", montoPagadoNum)}")
                    Text("• A interés: $${String.format("%.2f", montoAInteres)}")
                    Text("• A capital: $${String.format("%.2f", montoACapital)}")
                    if (nuevoCapitalPendiente <= 0.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "🎉 ¡Préstamo completado!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("OK")
                }
            }
        )
    }
}
