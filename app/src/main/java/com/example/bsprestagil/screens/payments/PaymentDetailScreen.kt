package com.example.bsprestagil.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.utils.ShareUtils
import com.example.bsprestagil.viewmodels.PaymentsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentDetailScreen(
    paymentId: String,
    navController: NavController,
    paymentsViewModel: PaymentsViewModel = viewModel()
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    // Cargar datos del pago
    val pago by paymentsViewModel.getPagoById(paymentId).collectAsState(initial = null)
    
    val clienteNombre = pago?.clienteNombre ?: "Cargando..."
    val monto = pago?.monto ?: 0.0
    val montoCuota = pago?.montoCuota ?: 0.0
    val montoMora = pago?.montoMora ?: 0.0
    val fechaPago = pago?.fechaPago ?: System.currentTimeMillis()
    val metodoPago = when(pago?.metodoPago?.name) {
        "EFECTIVO" -> "Efectivo"
        "TRANSFERENCIA" -> "Transferencia"
        "TARJETA" -> "Tarjeta"
        "OTRO" -> "Otro"
        else -> "N/A"
    }
    val numeroCuota = pago?.numeroCuota ?: 0
    val recibidoPor = pago?.recibidoPor ?: ""
    val notas = pago?.notas ?: ""
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Detalle del pago",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { /* TODO: Compartir recibo */ }) {
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SuccessColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pago registrado",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${String.format("%,.2f", monto)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessColor
                        )
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información del pago",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        DetailRow("Cliente", clienteNombre)
                        Divider()
                        DetailRow("Cuota", "#$numeroCuota")
                        Divider()
                        DetailRow("Monto cuota", "$${String.format("%,.2f", montoCuota)}")
                        if (montoMora > 0) {
                            Divider()
                            DetailRow("Mora", "$${String.format("%,.2f", montoMora)}")
                        }
                        Divider()
                        DetailRow("Método de pago", metodoPago)
                        Divider()
                        DetailRow("Fecha", dateFormat.format(Date(fechaPago)))
                        Divider()
                        DetailRow("Recibido por", recibidoPor)
                        if (notas.isNotBlank()) {
                            Divider()
                            Column {
                                Text(
                                    text = "Notas",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notas,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Button(
                    onClick = { /* TODO: Generar recibo PDF */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Descargar recibo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item {
                OutlinedButton(
                    onClick = {
                        ShareUtils.compartirReciboPorWhatsApp(
                            context = context,
                            clienteNombre = clienteNombre,
                            monto = monto,
                            montoCuota = montoCuota,
                            montoMora = montoMora,
                            numeroCuota = numeroCuota,
                            metodoPago = metodoPago,
                            fechaPago = fechaPago,
                            recibidoPor = recibidoPor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pago != null
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar por WhatsApp")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold
        )
    }
}

