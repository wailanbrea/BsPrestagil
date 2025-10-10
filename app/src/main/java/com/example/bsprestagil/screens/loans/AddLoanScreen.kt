package com.example.bsprestagil.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.viewmodels.ClientsViewModel
import com.example.bsprestagil.viewmodels.ConfiguracionViewModel
import com.example.bsprestagil.viewmodels.LoansViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanScreen(
    clientId: String?,
    navController: NavController,
    clientsViewModel: ClientsViewModel = viewModel(),
    loansViewModel: LoansViewModel = viewModel(),
    configuracionViewModel: ConfiguracionViewModel = viewModel()
) {
    var clienteSeleccionado by remember { mutableStateOf(clientId ?: "") }
    var clienteNombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var tasaInteres by remember { mutableStateOf("10") }
    var frecuenciaPago by remember { mutableStateOf(FrecuenciaPago.MENSUAL) }
    var garantiaOpcional by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var expandedFrecuencia by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showClientSelector by remember { mutableStateOf(false) }
    
    // Cargar lista de clientes
    val clientes by clientsViewModel.clientes.collectAsState()
    
    // Cargar tasa de interés desde configuración
    val configuracion by configuracionViewModel.configuracion.collectAsState()
    LaunchedEffect(configuracion) {
        configuracion?.let {
            tasaInteres = it.tasaInteresBase.toString()
        }
    }
    
    // Si viene un clientId, cargar el nombre
    if (clientId != null) {
        val cliente by clientsViewModel.getClienteById(clientId).collectAsState(initial = null)
        LaunchedEffect(cliente) {
            cliente?.let {
                clienteNombre = it.nombre
                clienteSeleccionado = it.id
            }
        }
    }
    
    val frecuenciasDisponibles = FrecuenciaPago.values().toList()
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Nuevo préstamo",
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
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
            
            if (clienteSeleccionado.isNotBlank()) {
                Card(
                    onClick = { if (clientId == null) showClientSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
                                    text = clienteNombre.ifBlank { "Cargando..." },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (clientId == null) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = { showClientSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
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
            
            OutlinedTextField(
                value = tasaInteres,
                onValueChange = { tasaInteres = it },
                label = { 
                    val periodoTexto = when(frecuenciaPago) {
                        FrecuenciaPago.DIARIO -> "Diaria"
                        FrecuenciaPago.SEMANAL -> "Semanal"
                        FrecuenciaPago.QUINCENAL -> "Quincenal"
                        FrecuenciaPago.MENSUAL -> "Mensual"
                    }
                    Text("Tasa de interés $periodoTexto *")
                },
                leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("%") },
                supportingText = {
                    val periodoDescripcion = when(frecuenciaPago) {
                        FrecuenciaPago.DIARIO -> "cada día"
                        FrecuenciaPago.SEMANAL -> "cada semana"
                        FrecuenciaPago.QUINCENAL -> "cada 15 días"
                        FrecuenciaPago.MENSUAL -> "cada mes"
                    }
                    Text(
                        "Se cobra por período ($periodoDescripcion)",
                        fontSize = 12.sp
                    )
                }
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
            
            if (garantiaOpcional.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Garantía agregada",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "ID: ${garantiaOpcional.take(8)}...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        IconButton(onClick = { garantiaOpcional = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Quitar garantía",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = { navController.navigate(Screen.AddEditCollateral.createRoute(null)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar garantía")
                }
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
                        val interesPorPeriodo = montoNum * (tasaNum / 100)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Capital a prestar:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "$${String.format("%,.2f", montoNum)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        
                        val periodoTexto = when(frecuenciaPago) {
                            FrecuenciaPago.DIARIO -> "día"
                            FrecuenciaPago.SEMANAL -> "semana"
                            FrecuenciaPago.QUINCENAL -> "quincena"
                            FrecuenciaPago.MENSUAL -> "mes"
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Interés por $periodoTexto:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "$${String.format("%,.2f", interesPorPeriodo)}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = "ℹ️ El cliente debe pagar el interés cada $periodoTexto. Cualquier monto extra se aplicará al capital.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    showConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = clienteSeleccionado.isNotBlank() && 
                         monto.isNotBlank() && 
                         tasaInteres.isNotBlank()
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
        
        // Diálogos
        // Diálogo de confirmación
        if (showConfirmDialog) {
        val montoNum = monto.toDoubleOrNull() ?: 0.0
        val tasaNum = tasaInteres.toDoubleOrNull() ?: 0.0
        val interesPorPeriodo = montoNum * (tasaNum / 100)
        
        val periodoTexto = when(frecuenciaPago) {
            FrecuenciaPago.DIARIO -> "diario"
            FrecuenciaPago.SEMANAL -> "semanal"
            FrecuenciaPago.QUINCENAL -> "quincenal"
            FrecuenciaPago.MENSUAL -> "mensual"
        }
        
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar préstamo") },
            text = {
                Column {
                    Text("¿Crear préstamo con los siguientes datos?", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cliente: $clienteNombre")
                    Text("Capital: $${String.format("%,.2f", montoNum)}", fontWeight = FontWeight.Bold)
                    Text("Tasa: $tasaNum% $periodoTexto")
                    Text("Interés por período: $${String.format("%,.2f", interesPorPeriodo)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "ℹ️ El cliente pagará el interés cada período. Cualquier monto extra reducirá el capital.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    loansViewModel.crearPrestamo(
                        clienteId = clienteSeleccionado,
                        clienteNombre = clienteNombre,
                        monto = montoNum,
                        tasaInteresPorPeriodo = tasaNum,
                        frecuenciaPago = frecuenciaPago,
                        garantiaId = if (garantiaOpcional.isNotBlank()) garantiaOpcional else null,
                        notas = notas
                    )
                    showConfirmDialog = false
                    showSuccessDialog = true
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("✅ Préstamo creado") },
            text = { Text("El préstamo se creó correctamente y se sincronizará con la nube.") },
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
    
    // Diálogo selector de clientes
    if (showClientSelector) {
        AlertDialog(
            onDismissRequest = { showClientSelector = false },
            title = { Text("Seleccionar cliente") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    if (clientes.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No hay clientes registrados",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    items(clientes.size) { index ->
                        val cliente = clientes[index]
                        Card(
                            onClick = {
                                clienteSeleccionado = cliente.id
                                clienteNombre = cliente.nombre
                                showClientSelector = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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
                                        text = cliente.nombre,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = cliente.telefono,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showClientSelector = false }) {
                    Text("Cancelar")
                }
            }
        )
        }
        } // Box
    } // Scaffold
} // AddLoanScreen

