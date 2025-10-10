package com.example.bsprestagil.screens.collaterals

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
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.EstadoGarantia
import com.example.bsprestagil.data.models.Garantia
import com.example.bsprestagil.data.models.TipoGarantia
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialGarantiasScreen(
    navController: NavController
) {
    var filtroEstado by remember { mutableStateOf<EstadoGarantia?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Datos de ejemplo de historial
    val garantias = remember {
        listOf(
            Garantia(
                id = "1",
                tipo = TipoGarantia.VEHICULO,
                descripcion = "Motocicleta Honda 2018",
                valorEstimado = 25000.0,
                estado = EstadoGarantia.RETENIDA,
                fechaRegistro = System.currentTimeMillis()
            ),
            Garantia(
                id = "2",
                tipo = TipoGarantia.ELECTRODOMESTICO,
                descripcion = "Refrigerador Samsung",
                valorEstimado = 8000.0,
                estado = EstadoGarantia.DEVUELTA,
                fechaRegistro = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            ),
            Garantia(
                id = "3",
                tipo = TipoGarantia.ELECTRONICO,
                descripcion = "Laptop Dell Inspiron",
                valorEstimado = 12000.0,
                estado = EstadoGarantia.DEVUELTA,
                fechaRegistro = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000)
            ),
            Garantia(
                id = "4",
                tipo = TipoGarantia.JOYA,
                descripcion = "Anillo de oro 14k",
                valorEstimado = 5000.0,
                estado = EstadoGarantia.EJECUTADA,
                fechaRegistro = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
            )
        )
    }
    
    val garantiasFiltradas = if (filtroEstado != null) {
        garantias.filter { it.estado == filtroEstado }
    } else {
        garantias
    }
    
    // Estadísticas
    val totalRetenidas = garantias.count { it.estado == EstadoGarantia.RETENIDA }
    val totalDevueltas = garantias.count { it.estado == EstadoGarantia.DEVUELTA }
    val totalEjecutadas = garantias.count { it.estado == EstadoGarantia.EJECUTADA }
    val valorTotal = garantias.filter { it.estado == EstadoGarantia.RETENIDA }.sumOf { it.valorEstimado }
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Historial de Garantías",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas") },
                            onClick = {
                                filtroEstado = null
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Retenidas") },
                            onClick = {
                                filtroEstado = EstadoGarantia.RETENIDA
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Devueltas") },
                            onClick = {
                                filtroEstado = EstadoGarantia.DEVUELTA
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ejecutadas") },
                            onClick = {
                                filtroEstado = EstadoGarantia.EJECUTADA
                                showFilterMenu = false
                            }
                        )
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
            // Estadísticas
            item {
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
                            text = "📊 Resumen de Garantías",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatColumn("Retenidas", totalRetenidas, WarningColor)
                            StatColumn("Devueltas", totalDevueltas, SuccessColor)
                            StatColumn("Ejecutadas", totalEjecutadas, ErrorColor)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Valor total retenido:",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$${String.format("%,.0f", valorTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Filtro activo
            if (filtroEstado != null) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { filtroEstado = null },
                        label = { 
                            Text("Filtro: ${when(filtroEstado) {
                                EstadoGarantia.RETENIDA -> "Retenidas"
                                EstadoGarantia.DEVUELTA -> "Devueltas"
                                EstadoGarantia.EJECUTADA -> "Ejecutadas"
                                else -> ""
                            }}")
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                        }
                    )
                }
            }
            
            // Lista de garantías
            items(garantiasFiltradas) { garantia ->
                HistorialGarantiaCard(
                    garantia = garantia,
                    dateFormat = dateFormat,
                    onQRClick = {
                        navController.navigate(
                            Screen.QRGarantia.createRoute(
                                garantiaId = garantia.id,
                                clienteNombre = "Cliente",
                                descripcion = garantia.descripcion,
                                valorEstimado = garantia.valorEstimado,
                                tipo = garantia.tipo.name,
                                fechaRegistro = garantia.fechaRegistro
                            )
                        )
                    },
                    onClick = {
                        navController.navigate(Screen.CollateralDetail.createRoute(garantia.id))
                    }
                )
            }
            
            if (garantiasFiltradas.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay garantías con este filtro",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HistorialGarantiaCard(
    garantia: Garantia,
    dateFormat: SimpleDateFormat,
    onQRClick: () -> Unit,
    onClick: () -> Unit
) {
    val estadoColor = when (garantia.estado) {
        EstadoGarantia.RETENIDA -> WarningColor
        EstadoGarantia.DEVUELTA -> SuccessColor
        EstadoGarantia.EJECUTADA -> ErrorColor
    }
    
    val estadoTexto = when (garantia.estado) {
        EstadoGarantia.RETENIDA -> "Retenida"
        EstadoGarantia.DEVUELTA -> "Devuelta"
        EstadoGarantia.EJECUTADA -> "Ejecutada"
    }
    
    val tipoTexto = when(garantia.tipo) {
        TipoGarantia.VEHICULO -> "Vehículo"
        TipoGarantia.ELECTRODOMESTICO -> "Electrodoméstico"
        TipoGarantia.ELECTRONICO -> "Electrónico"
        TipoGarantia.JOYA -> "Joya"
        TipoGarantia.MUEBLE -> "Mueble"
        TipoGarantia.OTRO -> "Otro"
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = estadoColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(garantia.estado) {
                                EstadoGarantia.RETENIDA -> Icons.Default.Lock
                                EstadoGarantia.DEVUELTA -> Icons.Default.CheckCircle
                                EstadoGarantia.EJECUTADA -> Icons.Default.Gavel
                            },
                            contentDescription = null,
                            tint = estadoColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = garantia.descripcion,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tipoTexto,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = estadoColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = estadoTexto,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = estadoColor
                            )
                        }
                        Text(
                            text = dateFormat.format(Date(garantia.fechaRegistro)),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%,.0f", garantia.valorEstimado)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Divider()
            
            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onQRClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver QR", fontSize = 13.sp)
                }
                
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detalles", fontSize = 13.sp)
                }
            }
        }
    }
}

