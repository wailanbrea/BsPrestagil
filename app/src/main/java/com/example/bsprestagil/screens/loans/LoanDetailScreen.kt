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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bsprestagil.components.InfoCard
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.SuccessColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    navController: NavController
) {
    // Datos de ejemplo
    val clienteNombre = "Juan Pérez González"
    val montoOriginal = 10000.0
    val tasaInteres = 10.0
    val plazoMeses = 12
    val cuotasPagadas = 4
    val totalCuotas = 12
    val saldoPendiente = 6500.0
    val montoInteres = 1000.0
    val totalAPagar = 11000.0
    val fechaInicio = System.currentTimeMillis()
    val fechaVencimiento = fechaInicio + (plazoMeses * 30L * 24 * 60 * 60 * 1000)
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Detalles del préstamo",
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
            // Estado del préstamo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessColor.copy(alpha = 0.1f)
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
                                text = "ACTIVO",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessColor
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SuccessColor
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
                    onClick = { navController.navigate(Screen.ClientDetail.createRoute("c1")) },
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
                        title = "Monto original",
                        value = "$${String.format("%,.2f", montoOriginal)}",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Tasa de interés",
                        value = "${tasaInteres.toInt()}%",
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
                        title = "Total a pagar",
                        value = "$${String.format("%,.2f", totalAPagar)}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Saldo pendiente",
                        value = "$${String.format("%,.2f", saldoPendiente)}",
                        color = SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Progreso de pagos
            item {
                Text(
                    text = "Progreso de pagos",
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
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$cuotasPagadas de $totalCuotas cuotas",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${(cuotasPagadas * 100) / totalCuotas}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { cuotasPagadas.toFloat() / totalCuotas.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = SuccessColor,
                            trackColor = SuccessColor.copy(alpha = 0.2f)
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
                                text = "Fecha de vencimiento",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = dateFormat.format(Date(fechaVencimiento)),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Plazo",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$plazoMeses meses",
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

