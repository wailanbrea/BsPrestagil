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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.FrecuenciaPago
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.utils.InteresUtils
import com.example.bsprestagil.utils.AmortizacionUtils
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
    var tipoAmortizacion by remember { mutableStateOf(com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES) }
    var numeroCuotas by remember { mutableStateOf("12") }
    var garantiaOpcional by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var expandedFrecuencia by remember { mutableStateOf(false) }
    var expandedSistema by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showClientSelector by remember { mutableStateOf(false) }
    var mostrarTablaEnConfirmacion by remember { mutableStateOf(false) }
    
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
                        FrecuenciaPago.QUINCENAL -> "cada 15 días"
                        FrecuenciaPago.MENSUAL -> "cada mes"
                    }
                    Text(
                        text = "Se cobra por período ($periodoDescripcion)",
                        fontSize = 12.sp
                    )
                }
            )
            
            // Frecuencia de pago
            ExposedDropdownMenuBox(
                expanded = expandedFrecuencia,
                onExpandedChange = { expandedFrecuencia = it }
            ) {
                OutlinedTextField(
                    value = when(frecuenciaPago) {
                        FrecuenciaPago.DIARIO -> "Diario"
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
            
            // Sistema de amortización
            ExposedDropdownMenuBox(
                expanded = expandedSistema,
                onExpandedChange = { expandedSistema = it }
            ) {
                OutlinedTextField(
                    value = when(tipoAmortizacion) {
                        com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES -> "Sistema Francés (Cuota Fija)"
                        com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN -> "Sistema Alemán (Capital Fijo)"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sistema de amortización *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSistema)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                    supportingText = {
                        val descripcion = when(tipoAmortizacion) {
                            com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES -> "Cuota fija, interés decreciente"
                            com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN -> "Capital fijo, cuota decreciente"
                        }
                        Text(descripcion, fontSize = 12.sp)
                    }
                )
                
                ExposedDropdownMenu(
                    expanded = expandedSistema,
                    onDismissRequest = { expandedSistema = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Sistema Francés (Cuota Fija)", fontWeight = FontWeight.Bold)
                                Text(
                                    "Cuota fija, interés decreciente - Más común",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        onClick = {
                            tipoAmortizacion = com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES
                            expandedSistema = false
                        }
                    )
                    
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Sistema Alemán (Capital Fijo)", fontWeight = FontWeight.Bold)
                                Text(
                                    "Capital fijo, cuota decreciente - Menor interés total",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        onClick = {
                            tipoAmortizacion = com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN
                            expandedSistema = false
                        }
                    )
                }
            }
            
            // Número de cuotas
            OutlinedTextField(
                value = numeroCuotas,
                onValueChange = { numeroCuotas = it },
                label = { Text("Número de cuotas *") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    val periodoTexto = when(frecuenciaPago) {
                        FrecuenciaPago.DIARIO -> "días"
                        FrecuenciaPago.QUINCENAL -> "quincenas"
                        FrecuenciaPago.MENSUAL -> "meses"
                    }
                    Text(
                        text = "Pagos cada ${InteresUtils.frecuenciaATexto(frecuenciaPago).lowercase()}. Total: $numeroCuotas $periodoTexto",
                        fontSize = 12.sp
                    )
                }
            )
            
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
                         tasaInteres.isNotBlank() &&
                         numeroCuotas.isNotBlank()
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
    
    // Diálogo de confirmación
    if (showConfirmDialog) {
        val montoNum = monto.toDoubleOrNull() ?: 0.0
        val tasaNum = tasaInteres.toDoubleOrNull() ?: 0.0
        val cuotasNum = numeroCuotas.toIntOrNull() ?: 0
        val interesPorPeriodo = montoNum * (tasaNum / 100)
        
        val periodoTexto = when(frecuenciaPago) {
            FrecuenciaPago.DIARIO -> "diario"
            FrecuenciaPago.QUINCENAL -> "quincenal"
            FrecuenciaPago.MENSUAL -> "mensual"
        }
        
        // Generar tabla según el sistema seleccionado
        val tablaCalculada = AmortizacionUtils.generarTablaSegunSistema(
            capitalInicial = montoNum,
            tasaInteresPorPeriodo = tasaNum,
            numeroCuotas = cuotasNum,
            tipoSistema = tipoAmortizacion
        )
        
        val cuotaFijaCalculada = tablaCalculada.firstOrNull()?.cuotaFija ?: 0.0
        val totalAPagar = tablaCalculada.sumOf { it.cuotaFija }
        val totalIntereses = totalAPagar - montoNum
        
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar préstamo") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("¿Crear préstamo con los siguientes datos?", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cliente: $clienteNombre")
                    Text("Capital: $${String.format("%,.2f", montoNum)}", fontWeight = FontWeight.Bold)
                    Text("Tasa: $tasaNum% $periodoTexto")
                    Text("Número de cuotas: $cuotasNum")
                    Text("Sistema: ${when(tipoAmortizacion) {
                        com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES -> "Francés (Cuota Fija)"
                        com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN -> "Alemán (Capital Fijo)"
                    }}")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    when(tipoAmortizacion) {
                        com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES -> {
                            Text(
                                "CUOTA FIJA: $${String.format("%,.2f", cuotaFijaCalculada)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN -> {
                            Column {
                                Text(
                                    "PRIMERA CUOTA: $${String.format("%,.2f", cuotaFijaCalculada)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Última cuota: $${String.format("%,.2f", tablaCalculada.lastOrNull()?.cuotaFija ?: 0.0)}",
                                    fontSize = 12.sp,
                                    color = SuccessColor
                                )
                                Text(
                                    "(Cuota decreciente)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total a pagar: $${String.format("%,.2f", totalAPagar)}")
                    Text("Total intereses: $${String.format("%,.2f", totalIntereses)}")
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Botón para mostrar/ocultar tabla
                    OutlinedButton(
                        onClick = { mostrarTablaEnConfirmacion = !mostrarTablaEnConfirmacion },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (mostrarTablaEnConfirmacion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (mostrarTablaEnConfirmacion) "Ocultar tabla" else "Ver tabla de amortización")
                    }
                    
                    // Tabla de amortización
                    if (mostrarTablaEnConfirmacion) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Encabezado
                        Text(
                            text = "Cronograma de pagos:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // Filas de la tabla (mostrar solo primeras 3 y últimas 2)
                        val filasAMostrar = if (tablaCalculada.size > 5) {
                            tablaCalculada.take(3) + listOf(null) + tablaCalculada.takeLast(2)
                        } else {
                            tablaCalculada
                        }
                        
                        filasAMostrar.forEach { fila ->
                            if (fila == null) {
                                Text(
                                    text = "...",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${fila.numeroCuota}.",
                                        fontSize = 10.sp,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(
                                        text = "$${String.format("%,.0f", fila.cuotaFija)}",
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Int: $${String.format("%,.0f", fila.interes)}",
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "$${String.format("%,.0f", fila.balanceRestante)}",
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val infoTexto = when(tipoAmortizacion) {
                        com.example.bsprestagil.data.models.TipoAmortizacion.FRANCES -> 
                            "ℹ️ Sistema Francés: Cuota fija de $${String.format("%,.0f", cuotaFijaCalculada)} cada $periodoTexto."
                        com.example.bsprestagil.data.models.TipoAmortizacion.ALEMAN -> 
                            "ℹ️ Sistema Alemán: Cuota inicial $${String.format("%,.0f", cuotaFijaCalculada)}, última cuota $${String.format("%,.0f", tablaCalculada.lastOrNull()?.cuotaFija ?: 0.0)}. Cuota decreciente."
                    }
                    
                    Text(
                        infoTexto,
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
                        tipoAmortizacion = tipoAmortizacion,
                        numeroCuotas = cuotasNum,
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
}
