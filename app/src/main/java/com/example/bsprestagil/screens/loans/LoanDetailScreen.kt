package com.example.bsprestagil.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.InfoCard
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.EstadoPrestamo
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.utils.ShareUtils
import com.example.bsprestagil.viewmodels.LoansViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    navController: NavController,
    loansViewModel: LoansViewModel = viewModel()
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Cargar datos del préstamo
    val prestamo by loansViewModel.getPrestamoById(loanId).collectAsState(initial = null)
    
    val clienteNombre = prestamo?.clienteNombre ?: "Cargando..."
    val montoOriginal = prestamo?.montoOriginal ?: 0.0
    val capitalPendiente = prestamo?.capitalPendiente ?: 0.0
    val tasaInteresPorPeriodo = prestamo?.tasaInteresPorPeriodo ?: 0.0
    val fechaInicio = prestamo?.fechaInicio ?: System.currentTimeMillis()
    val ultimaFechaPago = prestamo?.ultimaFechaPago ?: System.currentTimeMillis()
    val estado = prestamo?.estado ?: EstadoPrestamo.ACTIVO
    val totalInteresesPagados = prestamo?.totalInteresesPagados ?: 0.0
    val totalCapitalPagado = prestamo?.totalCapitalPagado ?: 0.0
    val totalMorasPagadas = prestamo?.totalMorasPagadas ?: 0.0
    val frecuenciaPago = prestamo?.frecuenciaPago ?: com.example.bsprestagil.data.models.FrecuenciaPago.MENSUAL
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Detalles del préstamo",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = {
                        prestamo?.let { p ->
                            ShareUtils.compartirResumenPrestamo(
                                context = context,
                                clienteNombre = p.clienteNombre,
                                montoOriginal = p.montoOriginal,
                                capitalPendiente = p.capitalPendiente,
                                tasaInteresPorPeriodo = p.tasaInteresPorPeriodo,
                                frecuenciaPago = p.frecuenciaPago.name,
                                totalCapitalPagado = p.totalCapitalPagado,
                                totalInteresesPagados = p.totalInteresesPagados,
                                fechaInicio = p.fechaInicio
                            )
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado del préstamo
            item {
                val estadoColor = when (estado) {
                    EstadoPrestamo.ACTIVO -> SuccessColor
                    EstadoPrestamo.ATRASADO -> com.example.bsprestagil.ui.theme.WarningColor
                    EstadoPrestamo.COMPLETADO -> MaterialTheme.colorScheme.primary
                    EstadoPrestamo.CANCELADO -> com.example.bsprestagil.ui.theme.ErrorColor
                }
                
                val estadoTexto = when (estado) {
                    EstadoPrestamo.ACTIVO -> "ACTIVO"
                    EstadoPrestamo.ATRASADO -> "ATRASADO"
                    EstadoPrestamo.COMPLETADO -> "COMPLETADO"
                    EstadoPrestamo.CANCELADO -> "CANCELADO"
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = estadoColor.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estado del préstamo",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = estadoTexto,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = estadoColor
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = estadoColor
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(32.dp),
                                tint = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }
            
            // Cliente
            item {
                Card(
                    onClick = { 
                        prestamo?.let {
                            navController.navigate(Screen.ClientDetail.createRoute(it.clienteId))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cliente",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = clienteNombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            // Resumen financiero
            item {
                Text(
                    text = "Resumen financiero",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        title = "Capital original",
                        value = "$${String.format("%,.2f", montoOriginal)}",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Capital pendiente",
                        value = "$${String.format("%,.2f", capitalPendiente)}",
                        color = if (capitalPendiente > 0) MaterialTheme.colorScheme.error else SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val periodoTexto = when(frecuenciaPago) {
                        com.example.bsprestagil.data.models.FrecuenciaPago.DIARIO -> "día"
                        com.example.bsprestagil.data.models.FrecuenciaPago.SEMANAL -> "semana"
                        com.example.bsprestagil.data.models.FrecuenciaPago.QUINCENAL -> "quincena"
                        com.example.bsprestagil.data.models.FrecuenciaPago.MENSUAL -> "mes"
                    }
                    
                    InfoCard(
                        title = "Tasa por $periodoTexto",
                        value = "${tasaInteresPorPeriodo.toInt()}%",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Capital pagado",
                        value = "$${String.format("%,.2f", totalCapitalPagado)}",
                        color = SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        title = "Intereses pagados",
                        value = "$${String.format("%,.2f", totalInteresesPagados)}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    if (totalMorasPagadas > 0) {
                        InfoCard(
                            title = "Moras pagadas",
                            value = "$${String.format("%,.2f", totalMorasPagadas)}",
                            color = com.example.bsprestagil.ui.theme.WarningColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Progreso de pagos
            item {
                Text(
                    text = "Progreso del préstamo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
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
                        // Progreso del capital
                        val progresoCapital = if (montoOriginal > 0) {
                            ((totalCapitalPagado / montoOriginal) * 100).toInt()
                        } else 0
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Capital pagado",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$progresoCapital%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessColor
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progresoCapital / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = SuccessColor,
                            trackColor = SuccessColor.copy(alpha = 0.2f)
                        )
                        
                        Text(
                            text = "$${String.format("%,.2f", totalCapitalPagado)} de $${String.format("%,.2f", montoOriginal)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Fechas
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fecha de inicio",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = dateFormat.format(Date(fechaInicio)),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Último pago",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = dateFormat.format(Date(ultimaFechaPago)),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val periodoTexto = when(frecuenciaPago) {
                                com.example.bsprestagil.data.models.FrecuenciaPago.DIARIO -> "Diaria"
                                com.example.bsprestagil.data.models.FrecuenciaPago.SEMANAL -> "Semanal"
                                com.example.bsprestagil.data.models.FrecuenciaPago.QUINCENAL -> "Quincenal"
                                com.example.bsprestagil.data.models.FrecuenciaPago.MENSUAL -> "Mensual"
                            }
                            Text(
                                text = "Frecuencia",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = periodoTexto,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            // Acciones
            item {
                Button(
                    onClick = {
                        navController.navigate(Screen.RegisterPayment.createRoute(loanId))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registrar pago",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

