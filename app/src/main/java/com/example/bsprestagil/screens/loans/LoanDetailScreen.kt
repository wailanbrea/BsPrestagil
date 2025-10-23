package com.example.bsprestagil.screens.loans

import androidx.compose.foundation.BorderStroke
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
import com.example.bsprestagil.data.models.EstadoCuota
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.utils.ShareUtils
import com.example.bsprestagil.utils.PDFGenerator
import com.example.bsprestagil.viewmodels.LoansViewModel
import com.example.bsprestagil.viewmodels.CuotasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    navController: NavController,
    loansViewModel: LoansViewModel = viewModel(),
    cuotasViewModel: CuotasViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var generandoPDF by remember { mutableStateOf(false) }
    
    // Cargar datos del préstamo
    val prestamo by loansViewModel.getPrestamoById(loanId).collectAsState(initial = null)
    
    // Cargar cronograma de cuotas
    val cuotas by cuotasViewModel.getCuotasByPrestamoId(loanId).collectAsState(initial = emptyList())
    
    val clienteNombre = prestamo?.clienteNombre ?: "Cargando..."
    val montoOriginal = prestamo?.montoOriginal ?: 0.0
    val capitalPendiente = prestamo?.capitalPendiente ?: 0.0
    val tasaInteresPorPeriodo = prestamo?.tasaInteresPorPeriodo ?: 0.0
    val numeroCuotas = prestamo?.numeroCuotas ?: 0
    val cuotasPagadas = prestamo?.cuotasPagadas ?: 0
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
                    IconButton(
                        onClick = {
                            prestamo?.let { p ->
                                generandoPDF = true
                                scope.launch {
                                    try {
                                        val pdfUri = withContext(Dispatchers.IO) {
                                            PDFGenerator.generarPDFTablaAmortizacion(
                                                context = context,
                                                clienteNombre = p.clienteNombre,
                                                montoOriginal = p.montoOriginal,
                                                capitalPendiente = p.capitalPendiente,
                                                tasaInteresPorPeriodo = p.tasaInteresPorPeriodo,
                                                frecuenciaPago = p.frecuenciaPago.name,
                                                tipoAmortizacion = p.tipoAmortizacion.name,
                                                numeroCuotas = p.numeroCuotas,
                                                montoCuotaFija = p.montoCuotaFija,
                                                totalCapitalPagado = p.totalCapitalPagado,
                                                totalInteresesPagados = p.totalInteresesPagados,
                                                fechaInicio = p.fechaInicio
                                            )
                                        }
                                        
                                        generandoPDF = false
                                        
                                        pdfUri?.let { uri ->
                                            PDFGenerator.compartirPDF(context, uri, p.clienteNombre)
                                        }
                                    } catch (e: Exception) {
                                        generandoPDF = false
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        enabled = !generandoPDF
                    ) {
                        if (generandoPDF) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Compartir PDF")
                        }
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
            
            // Cobrador asignado (si existe)
            prestamo?.let { p ->
                if (p.cobradorId != null && p.cobradorNombre != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cobrador asignado",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = p.cobradorNombre,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.WorkOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            }
                        }
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
            
            // Información de extensiones si existen
            prestamo?.let { p ->
                if (p.numeroExtensiones > 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoCard(
                                title = "Monto extendido",
                                value = "$${String.format("%,.2f", p.montoExtendido)}",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            InfoCard(
                                title = "Monto total",
                                value = "$${String.format("%,.2f", p.montoTotal)}",
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Extensiones realizadas: ${p.numeroExtensiones}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (p.fechaUltimaExtension != null) {
                                        Text(
                                            text = "Última: ${dateFormat.format(Date(p.fechaUltimaExtension))}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        navController.navigate(Screen.HistorialExtensiones.createRoute(loanId))
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "Ver historial",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val periodoTexto = when(frecuenciaPago) {
                        com.example.bsprestagil.data.models.FrecuenciaPago.DIARIO -> "día"
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
            
            // Tabla de Amortización
            item {
                Text(
                    text = "Tabla de Amortización",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Encabezado de la tabla
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
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
            
            // Filas de datos
            items(cuotas.size) { index ->
                val cuota = cuotas[index]
                
                // Extraer los datos proyectados de las notas
                val notasParts = cuota.notas.split(",")
                val interesProyectado = notasParts.getOrNull(0)?.substringAfter("$")?.trim()?.toDoubleOrNull() ?: 0.0
                val capitalProyectado = notasParts.getOrNull(1)?.substringAfter("$")?.trim()?.toDoubleOrNull() ?: 0.0
                
                // Si la cuota está PAGADA, usar valores REALES, sino usar proyectados
                val cuotaMostrar = if (cuota.estado == EstadoCuota.PAGADA) cuota.montoPagado else cuota.montoCuotaMinimo
                val interesMostrar = if (cuota.estado == EstadoCuota.PAGADA) cuota.montoAInteres else interesProyectado
                val capitalMostrar = if (cuota.estado == EstadoCuota.PAGADA) cuota.montoACapital else capitalProyectado
                
                // Calcular balance acumulado real
                val balanceMostrar = if (index == 0) {
                    montoOriginal - capitalMostrar
                } else {
                    // Sumar capital pagado de todas las cuotas anteriores
                    val capitalAcumulado = cuotas.take(index + 1).sumOf { 
                        if (it.estado == EstadoCuota.PAGADA) it.montoACapital else capitalProyectado 
                    }
                    (montoOriginal - capitalAcumulado).coerceAtLeast(0.0)
                }
                
                val estadoCuotaColor = when(cuota.estado) {
                    EstadoCuota.PAGADA -> SuccessColor
                    EstadoCuota.PENDIENTE -> MaterialTheme.colorScheme.onSurface
                    EstadoCuota.VENCIDA -> com.example.bsprestagil.ui.theme.ErrorColor
                    EstadoCuota.PARCIAL -> MaterialTheme.colorScheme.primary
                    EstadoCuota.CANCELADA -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (cuota.estado == EstadoCuota.PAGADA)
                            SuccessColor.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (cuota.estado == EstadoCuota.PENDIENTE && index == cuotas.indexOfFirst { it.estado == EstadoCuota.PENDIENTE })
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else null
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${cuota.numeroCuota}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = estadoCuotaColor,
                                modifier = Modifier.weight(0.5f)
                            )
                            Text(
                                text = "$${String.format("%,.0f", cuotaMostrar)}",
                                fontSize = 11.sp,
                                fontWeight = if (cuota.estado == EstadoCuota.PAGADA) FontWeight.Bold else FontWeight.Normal,
                                color = if (cuota.estado == EstadoCuota.PAGADA) SuccessColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${String.format("%,.0f", capitalMostrar)}",
                                fontSize = 11.sp,
                                fontWeight = if (cuota.estado == EstadoCuota.PAGADA) FontWeight.Bold else FontWeight.Normal,
                                color = if (cuota.estado == EstadoCuota.PAGADA) SuccessColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${String.format("%,.0f", interesMostrar)}",
                                fontSize = 11.sp,
                                fontWeight = if (cuota.estado == EstadoCuota.PAGADA) FontWeight.Bold else FontWeight.Normal,
                                color = if (cuota.estado == EstadoCuota.PAGADA) com.example.bsprestagil.ui.theme.WarningColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${String.format("%,.0f", balanceMostrar)}",
                                fontSize = 11.sp,
                                fontWeight = if (balanceMostrar <= 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (balanceMostrar <= 0) SuccessColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Fecha de vencimiento y estado
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vence: ${dateFormat.format(Date(cuota.fechaVencimiento))}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            
                            val estadoTexto = when(cuota.estado) {
                                EstadoCuota.PAGADA -> "✅ Pagada"
                                EstadoCuota.PENDIENTE -> if (index == cuotas.indexOfFirst { it.estado == EstadoCuota.PENDIENTE }) "⏭️ Próxima" else "⏳ Pendiente"
                                EstadoCuota.VENCIDA -> "⚠️ Vencida"
                                EstadoCuota.PARCIAL -> "🔄 Parcial"
                                EstadoCuota.CANCELADA -> "🚫 Cancelada"
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = estadoCuotaColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = estadoTexto,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = estadoCuotaColor
                                )
                            }
                        }
                    }
                }
            }
            
            // Totales
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Resumen del cronograma",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total a pagar:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$${String.format("%,.2f", cuotas.sumOf { it.montoCuotaMinimo.toDouble() })}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total capital:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$${String.format("%,.2f", montoOriginal)}",
                                fontSize = 12.sp
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total intereses:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$${String.format("%,.2f", cuotas.sumOf { it.montoCuotaMinimo.toDouble() } - montoOriginal)}",
                                fontSize = 12.sp,
                                color = com.example.bsprestagil.ui.theme.WarningColor
                            )
                        }
                    }
                }
            }
            
            // Acciones
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón principal: Registrar pago
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botones de extensión (solo si el préstamo está activo)
                if (prestamo?.estado == EstadoPrestamo.ACTIVO && prestamo?.capitalPendiente ?: 0.0 > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón: Extender préstamo
                        OutlinedButton(
                            onClick = {
                                navController.navigate(Screen.ExtensionPrestamo.createRoute(loanId))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Extender",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Botón: Historial de extensiones
                        OutlinedButton(
                            onClick = {
                                navController.navigate(Screen.HistorialExtensiones.createRoute(loanId))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Historial",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Información sobre extensiones si existen
                    if (prestamo?.numeroExtensiones ?: 0 > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Este préstamo tiene ${prestamo?.numeroExtensiones ?: 0} extensión(es) por un total de $${String.format("%,.2f", prestamo?.montoExtendido ?: 0.0)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

