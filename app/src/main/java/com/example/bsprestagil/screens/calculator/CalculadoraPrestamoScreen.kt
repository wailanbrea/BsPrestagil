package com.example.bsprestagil.screens.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.FrecuenciaPago
import com.example.bsprestagil.data.models.TipoAmortizacion
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.utils.AmortizacionUtils
import com.example.bsprestagil.utils.InteresUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculadoraPrestamoScreen(
    navController: NavController
) {
    var monto by remember { mutableStateOf("10000") }
    var tasaInteres by remember { mutableStateOf("10") }
    var numeroCuotas by remember { mutableStateOf("12") }
    var frecuenciaPago by remember { mutableStateOf(FrecuenciaPago.MENSUAL) }
    var tipoAmortizacion by remember { mutableStateOf(TipoAmortizacion.FRANCES) }
    var expandedFrecuencia by remember { mutableStateOf(false) }
    var expandedSistema by remember { mutableStateOf(false) }
    var mostrarTabla by remember { mutableStateOf(false) }
    
    val frecuenciasDisponibles = listOf(
        FrecuenciaPago.DIARIO,
        FrecuenciaPago.QUINCENAL,
        FrecuenciaPago.MENSUAL
    )
    
    // Cálculos
    val montoNum = monto.toDoubleOrNull() ?: 0.0
    val tasaNum = tasaInteres.toDoubleOrNull() ?: 0.0
    val cuotasNum = numeroCuotas.toIntOrNull() ?: 0
    
    val tablaAmortizacion = if (montoNum > 0 && tasaNum > 0 && cuotasNum > 0) {
        AmortizacionUtils.generarTablaSegunSistema(
            capitalInicial = montoNum,
            tasaInteresPorPeriodo = tasaNum,
            numeroCuotas = cuotasNum,
            tipoSistema = tipoAmortizacion
        )
    } else emptyList()
    
    val cuotaFija = tablaAmortizacion.firstOrNull()?.cuotaFija ?: 0.0
    val totalAPagar = tablaAmortizacion.sumOf { it.cuotaFija }
    val totalIntereses = totalAPagar - montoNum
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Calculadora de Préstamos",
                onNavigateBack = { navController.navigateUp() }
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
            // Descripción
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Simulador de préstamos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Calcula cuotas y tabla de amortización antes de crear el préstamo",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Formulario
            item {
                Text(
                    text = "Datos del préstamo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto a prestar") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") }
                )
            }
            
            item {
                OutlinedTextField(
                    value = tasaInteres,
                    onValueChange = { tasaInteres = it },
                    label = {
                        val periodoTexto = when(frecuenciaPago) {
                            FrecuenciaPago.DIARIO -> "Diaria"
                            FrecuenciaPago.QUINCENAL -> "Quincenal"
                            FrecuenciaPago.MENSUAL -> "Mensual"
                        }
                        Text("Tasa de interés $periodoTexto")
                    },
                    leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("%") }
                )
            }
            
            item {
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
                        label = { Text("Frecuencia de pago") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrecuencia)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
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
            }
            
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedSistema,
                    onExpandedChange = { expandedSistema = it }
                ) {
                    OutlinedTextField(
                        value = when(tipoAmortizacion) {
                            TipoAmortizacion.FRANCES -> "Sistema Francés (Cuota Fija)"
                            TipoAmortizacion.ALEMAN -> "Sistema Alemán (Capital Fijo)"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sistema de amortización") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSistema)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null) }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedSistema,
                        onDismissRequest = { expandedSistema = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sistema Francés (Cuota Fija)") },
                            onClick = {
                                tipoAmortizacion = TipoAmortizacion.FRANCES
                                expandedSistema = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sistema Alemán (Capital Fijo)") },
                            onClick = {
                                tipoAmortizacion = TipoAmortizacion.ALEMAN
                                expandedSistema = false
                            }
                        )
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = numeroCuotas,
                    onValueChange = { numeroCuotas = it },
                    label = { Text("Número de cuotas") },
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
                        Text("Pagos cada ${InteresUtils.frecuenciaATexto(frecuenciaPago).lowercase()}")
                    }
                )
            }
            
            // Resultado
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                Text(
                    text = "Resultado del cálculo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (cuotaFija > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when(tipoAmortizacion) {
                                TipoAmortizacion.FRANCES -> {
                                    Text(
                                        text = "CUOTA FIJA",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "$${String.format("%,.2f", cuotaFija)}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "cada ${InteresUtils.frecuenciaATexto(frecuenciaPago).lowercase()}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                                TipoAmortizacion.ALEMAN -> {
                                    Text(
                                        text = "CUOTA VARIABLE",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "$${String.format("%,.2f", cuotaFija)}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Primera cuota (decreciente)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Última: $${String.format("%,.2f", tablaAmortizacion.lastOrNull()?.cuotaFija ?: 0.0)}",
                                        fontSize = 14.sp,
                                        color = SuccessColor
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Capital a prestar:",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$${String.format("%,.2f", montoNum)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Divider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total a pagar:",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$${String.format("%,.2f", totalAPagar)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total intereses:",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$${String.format("%,.2f", totalIntereses)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningColor
                                )
                            }
                            
                            Divider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Número de cuotas:",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$cuotasNum",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                
                // Botón para mostrar tabla
                item {
                    Button(
                        onClick = { mostrarTabla = !mostrarTabla },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(
                            if (mostrarTabla) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (mostrarTabla) "Ocultar tabla de amortización" else "Ver tabla de amortización")
                    }
                }
                
                // Tabla de amortización
                if (mostrarTabla) {
                    item {
                        Text(
                            text = "Tabla de Amortización",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "No.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.5f)
                                )
                                Text(
                                    text = "Cuota",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Capital",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Interés",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Balance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    items(tablaAmortizacion.size) { index ->
                        val fila = tablaAmortizacion[index]
                        Card {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${fila.numeroCuota}",
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(0.5f)
                                )
                                Text(
                                    text = "$${String.format("%,.0f", fila.cuotaFija)}",
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$${String.format("%,.0f", fila.capital)}",
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$${String.format("%,.0f", fila.interes)}",
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$${String.format("%,.0f", fila.balanceRestante)}",
                                    fontSize = 11.sp,
                                    fontWeight = if (fila.balanceRestante <= 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (fila.balanceRestante <= 0) SuccessColor else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                // Acciones
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Navegar a crear préstamo con datos pre-llenados
                                navController.navigate(Screen.AddLoan.createRoute())
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Crear préstamo")
                        }
                        
                        Button(
                            onClick = {
                                // TODO: Compartir cálculo
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compartir")
                        }
                    }
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Ingresa todos los datos para ver el cálculo",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
            
            // Espacio final
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

