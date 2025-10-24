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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.R
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.FrecuenciaPago
import com.example.bsprestagil.data.models.MetodoPago
import com.example.bsprestagil.data.models.TipoPago
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.utils.InteresUtils
import com.example.bsprestagil.viewmodels.ConfiguracionViewModel
import com.example.bsprestagil.viewmodels.LoansViewModel
import com.example.bsprestagil.viewmodels.PaymentsViewModel
import com.example.bsprestagil.viewmodels.CuotasViewModel
import com.example.bsprestagil.viewmodels.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    loanId: String,
    navController: NavController,
    loansViewModel: LoansViewModel = viewModel(),
    paymentsViewModel: PaymentsViewModel = viewModel(),
    configuracionViewModel: ConfiguracionViewModel = viewModel(),
    cuotasViewModel: CuotasViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel()
) {
    var montoPagado by remember { mutableStateOf("") }
    var montoMora by remember { mutableStateOf("0") }
    var cobrarMora by remember { mutableStateOf(false) }
    var metodoPago by remember { mutableStateOf(MetodoPago.EFECTIVO) }
    var notas by remember { mutableStateOf("") }
    var expandedMetodo by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showCobradorSelector by remember { mutableStateOf(false) }
    var cobradorSeleccionado by remember { mutableStateOf<String?>(null) }
    var cobradorNombre by remember { mutableStateOf<String?>(null) }
    
    // NUEVO: Variables para pagos flexibles
    var tipoPago by remember { mutableStateOf(TipoPago.NORMAL) }
    var montoInteresPersonalizado by remember { mutableStateOf("") }
    var montoCapitalPersonalizado by remember { mutableStateOf("") }
    var notaExoneracion by remember { mutableStateOf("") }
    
    val metodosDisponibles = MetodoPago.values().toList()
    
    // Cargar datos del préstamo
    val prestamo by loansViewModel.getPrestamoById(loanId).collectAsState(initial = null)
    
    // Cargar cuotas del préstamo
    val cuotas by cuotasViewModel.getCuotasByPrestamoId(loanId).collectAsState(initial = emptyList())
    val proximaCuota = cuotas.firstOrNull { it.estado == com.example.bsprestagil.data.models.EstadoCuota.PENDIENTE || it.estado == com.example.bsprestagil.data.models.EstadoCuota.VENCIDA }
    
    // Cargar lista de cobradores
    val usuarios by usersViewModel.usuarios.collectAsState()
    val cobradores = usuarios.filter { it.activo }
    
    // Inicializar cobrador con el del préstamo
    LaunchedEffect(prestamo) {
        prestamo?.let { p ->
            if (cobradorSeleccionado == null && p.cobradorId != null) {
                cobradorSeleccionado = p.cobradorId
                cobradorNombre = p.cobradorNombre
            }
        }
    }
    
    val clienteNombre = prestamo?.clienteNombre ?: "Cargando..."
    val capitalPendiente = prestamo?.capitalPendiente ?: 0.0
    val tasaInteresPorPeriodo = prestamo?.tasaInteresPorPeriodo ?: 0.0
    val fechaUltimoPago = prestamo?.ultimaFechaPago ?: System.currentTimeMillis()
    val frecuenciaPago = prestamo?.frecuenciaPago ?: FrecuenciaPago.MENSUAL
    val numeroCuotas = prestamo?.numeroCuotas ?: 0
    val cuotaSeleccionada = proximaCuota
    
    // Calcular días transcurridos desde el último pago
    val diasTranscurridos = InteresUtils.calcularDiasTranscurridos(
        fechaUltimoPago,
        System.currentTimeMillis()
    )
    
    // Extraer distribución EXACTA del cronograma
    val (interesProyectado, capitalProyectado) = if (cuotaSeleccionada != null) {
        try {
            val interesExtract = cuotaSeleccionada.notas
                .substringAfter("Interés proyectado: $")
                .substringBefore(",")
                .replace(",", "")
                .trim()
                .toDoubleOrNull() ?: 0.0
            
            val capitalExtract = cuotaSeleccionada.notas
                .substringAfter("Capital: $")
                .replace(",", "")
                .trim()
                .toDoubleOrNull() ?: 0.0
            
            Pair(interesExtract, capitalExtract)
        } catch (e: Exception) {
            Pair(0.0, 0.0)
        }
    } else {
        Pair(0.0, 0.0)
    }
    
    // Calcular interés (usar cronograma si está disponible)
    val interesCalculado = if (interesProyectado > 0) {
        interesProyectado
    } else {
        prestamo?.let {
            InteresUtils.calcularInteresProporcional(
                capitalPendiente = it.capitalPendiente,
                tasaInteresPorPeriodo = it.tasaInteresPorPeriodo,
                frecuenciaPago = it.frecuenciaPago,
                diasTranscurridos = diasTranscurridos
            )
        } ?: 0.0
    }
    
    // Calcular distribución del pago según el tipo seleccionado
    val montoPagadoNum = montoPagado.toDoubleOrNull() ?: 0.0
    val montoInteresPersonalizadoNum = montoInteresPersonalizado.toDoubleOrNull() ?: 0.0
    val montoCapitalPersonalizadoNum = montoCapitalPersonalizado.toDoubleOrNull() ?: 0.0
    
    // Usar distribución flexible si se seleccionó un tipo específico
    val (montoAInteres, montoACapital, advertenciaPago) = if (tipoPago != TipoPago.NORMAL) {
        // Usar la nueva lógica flexible
        InteresUtils.distribuirPagoFlexible(
            montoPagado = montoPagadoNum,
            interesAcumulado = interesCalculado,
            capitalPendiente = capitalPendiente,
            tipoPago = tipoPago,
            montoInteres = montoInteresPersonalizadoNum,
            montoCapital = montoCapitalPersonalizadoNum
        )
    } else {
        // Lógica original para pago NORMAL
        val (interes, capital) = if (cuotaSeleccionada != null && montoPagadoNum >= cuotaSeleccionada.montoCuotaMinimo) {
            // Paga cuota completa o más: usar distribución exacta del cronograma
            val excedente = montoPagadoNum - cuotaSeleccionada.montoCuotaMinimo
            Pair(
                interesProyectado,
                capitalProyectado + excedente
            )
        } else if (cuotaSeleccionada != null && montoPagadoNum > 0 && interesProyectado > 0) {
            // Pago parcial: distribución proporcional del cronograma
            val proporcion = montoPagadoNum / cuotaSeleccionada.montoCuotaMinimo
            Pair(
                interesProyectado * proporcion,
                capitalProyectado * proporcion
            )
        } else {
            // Fallback: cálculo tradicional
            InteresUtils.distribuirPago(
                montoPagado = montoPagadoNum,
                interesDelPeriodo = interesCalculado,
                capitalPendiente = capitalPendiente
            )
        }
        Triple(interes, capital, null)
    }
    
    val nuevoCapitalPendiente = (capitalPendiente - montoACapital).coerceAtLeast(0.0)
    
    // ⭐ Usuario actual (siempre email para filtrado consistente)
    val usuarioActualEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Admin"
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = stringResource(R.string.register_payment),
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
                        text = stringResource(R.string.loan_information),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = clienteNombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (cuotaSeleccionada != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cuota ${cuotaSeleccionada.numeroCuota} de $numeroCuotas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
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
                    
                    if (cuotaSeleccionada != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cuota mínima:",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$${String.format("%,.2f", cuotaSeleccionada.montoCuotaMinimo)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        if (interesProyectado > 0 && capitalProyectado > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Distribución del cronograma:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "  → Interés:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "$${String.format("%,.2f", interesProyectado)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "  → Capital:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "$${String.format("%,.2f", capitalProyectado)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = com.example.bsprestagil.ui.theme.SuccessColor
                                )
                            }
                        }
                    }
                }
            }
            
            // Mostrar distribución del pago en tiempo real
            if (montoPagadoNum > 0) {
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
                            text = "📊 Distribución de su pago:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Monto total:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "$${String.format("%,.2f", montoPagadoNum)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "→ A interés:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$${String.format("%,.2f", montoAInteres)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.bsprestagil.ui.theme.WarningColor
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "→ A capital:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$${String.format("%,.2f", montoACapital)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.bsprestagil.ui.theme.SuccessColor
                            )
                        }
                        
                        if (montoPagadoNum > (cuotaSeleccionada?.montoCuotaMinimo ?: 0.0)) {
                            val excedente = montoPagadoNum - (cuotaSeleccionada?.montoCuotaMinimo ?: 0.0)
                            Divider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "✨ Abono extraordinario:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.bsprestagil.ui.theme.SuccessColor
                                )
                                Text(
                                    text = "$${String.format("%,.2f", excedente)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.bsprestagil.ui.theme.SuccessColor
                                )
                            }
                        }
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nuevo saldo:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "$${String.format("%,.2f", nuevoCapitalPendiente)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (nuevoCapitalPendiente > 0) MaterialTheme.colorScheme.error else com.example.bsprestagil.ui.theme.SuccessColor
                            )
                        }
                    }
                }
            }
            
            Text(
                text = stringResource(R.string.payment_details),
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
            
            // NUEVO: Selector de tipo de pago
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.payment_type),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Pago NORMAL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoPago == TipoPago.NORMAL,
                            onClick = { tipoPago = TipoPago.NORMAL }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Normal (Interés + Capital)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.automatic_distribution),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    Divider()
                    
                    // SOLO INTERÉS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoPago == TipoPago.SOLO_INTERES,
                            onClick = { tipoPago = TipoPago.SOLO_INTERES }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Solo Interés",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.capital_not_reduced),
                                fontSize = 12.sp,
                                color = WarningColor
                            )
                        }
                    }
                    
                    Divider()
                    
                    // SOLO CAPITAL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoPago == TipoPago.SOLO_CAPITAL,
                            onClick = { tipoPago = TipoPago.SOLO_CAPITAL },
                            enabled = interesCalculado < 1.0 // Solo si no hay interés pendiente
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Solo Capital",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (interesCalculado >= 1.0) 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (interesCalculado >= 1.0) 
                                    stringResource(R.string.pay_interest_first)
                                else 
                                    stringResource(R.string.reduce_loan_term),
                                fontSize = 12.sp,
                                color = if (interesCalculado >= 1.0) 
                                    ErrorColor
                                else 
                                    SuccessColor
                            )
                        }
                    }
                    
                    Divider()
                    
                    // PERSONALIZADO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoPago == TipoPago.PERSONALIZADO,
                            onClick = { tipoPago = TipoPago.PERSONALIZADO }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.custom),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.you_decide_distribution),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Divider()
                    
                    // EXONERAR INTERÉS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoPago == TipoPago.EXONERAR_INTERES,
                            onClick = { tipoPago = TipoPago.EXONERAR_INTERES }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exonerar Interés ✨",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "El interés se perdona, todo va al capital",
                                fontSize = 12.sp,
                                color = SuccessColor
                            )
                        }
                    }
                }
            }
            
            // Campos adicionales para tipo PERSONALIZADO
            if (tipoPago == TipoPago.PERSONALIZADO) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.manual_distribution),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = montoInteresPersonalizado,
                            onValueChange = { montoInteresPersonalizado = it },
                            label = { Text(stringResource(R.string.amount_to_interest)) },
                            leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("$") },
                            supportingText = {
                                Text("Interés acumulado: $${String.format("%.2f", interesCalculado)}")
                            }
                        )
                        
                        OutlinedTextField(
                            value = montoCapitalPersonalizado,
                            onValueChange = { montoCapitalPersonalizado = it },
                            label = { Text(stringResource(R.string.amount_to_capital)) },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("$") },
                            supportingText = {
                                Text("Capital pendiente: $${String.format("%.2f", capitalPendiente)}")
                            }
                        )
                        
                        // Validación de suma
                        val totalPersonalizado = montoInteresPersonalizadoNum + montoCapitalPersonalizadoNum
                        if (totalPersonalizado > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total distribuido:", fontSize = 12.sp)
                                Text(
                                    text = "$${String.format("%.2f", totalPersonalizado)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalPersonalizado == montoPagadoNum) 
                                        SuccessColor 
                                    else if (totalPersonalizado > montoPagadoNum)
                                        ErrorColor
                                    else
                                        WarningColor
                                )
                            }
                            
                            if (totalPersonalizado != montoPagadoNum) {
                                Text(
                                    text = if (totalPersonalizado > montoPagadoNum)
                                        "❌ Excede el monto pagado"
                                    else
                                        "⚠️ No distribuiste todo el monto",
                                    fontSize = 11.sp,
                                    color = if (totalPersonalizado > montoPagadoNum) ErrorColor else WarningColor
                                )
                            }
                        }
                    }
                }
            }
            
            // Campo de nota obligatorio para EXONERAR_INTERES
            if (tipoPago == TipoPago.EXONERAR_INTERES) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SuccessColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exoneración de Interés",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessColor
                            )
                        }
                        
                        Text(
                            text = "El interés de $${String.format("%.2f", interesCalculado)} será exonerado (perdonado). Todo el pago irá directo al capital.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        OutlinedTextField(
                            value = notaExoneracion,
                            onValueChange = { notaExoneracion = it },
                            label = { Text("Motivo de exoneración *") },
                            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            placeholder = {
                                Text("Ej: Cliente preferencial, situación especial, buen historial, promoción, etc.")
                            },
                            supportingText = {
                                Text(
                                    text = "⚠️ Campo obligatorio - Justifica por qué se exonera el interés",
                                    fontSize = 11.sp,
                                    color = if (notaExoneracion.isBlank()) ErrorColor else SuccessColor
                                )
                            },
                            isError = notaExoneracion.isBlank()
                        )
                    }
                }
            }
            
            // Advertencia del tipo de pago (si hay)
            if (advertenciaPago != null && montoPagadoNum > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = WarningColor.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningColor
                        )
                        Text(
                            text = advertenciaPago,
                            fontSize = 13.sp,
                            color = WarningColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
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
                                text = stringResource(R.string.charge_penalty),
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
                            text = if (cobrarMora) stringResource(R.string.penalty_will_be_applied) else stringResource(R.string.no_penalty),
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
                        MetodoPago.EFECTIVO -> stringResource(R.string.cash)
                        MetodoPago.TRANSFERENCIA -> stringResource(R.string.transfer)
                        MetodoPago.TARJETA -> stringResource(R.string.card)
                        MetodoPago.OTRO -> stringResource(R.string.other)
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
                                    MetodoPago.EFECTIVO -> stringResource(R.string.cash)
                                    MetodoPago.TRANSFERENCIA -> stringResource(R.string.transfer)
                                    MetodoPago.TARJETA -> stringResource(R.string.card)
                                    MetodoPago.OTRO -> stringResource(R.string.other)
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
            
            // Cobrador que recibe el pago
            Text(
                text = stringResource(R.string.received_by),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (cobradorSeleccionado != null) {
                Card(
                    onClick = { showCobradorSelector = true },
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
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cobradorNombre ?: stringResource(R.string.no_name),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.change),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showCobradorSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.assign_collector))
                }
            }
            
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text(stringResource(R.string.additional_notes)) },
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
                        
                        // Agregar nota de exoneración si aplica
                        val notasFinal = if (tipoPago == TipoPago.EXONERAR_INTERES) {
                            val exoneracionInfo = "🎁 INTERÉS EXONERADO: $${String.format("%.2f", interesCalculado)}\n" +
                                                  "Motivo: $notaExoneracion\n\n"
                            if (notas.isNotBlank()) exoneracionInfo + notas else exoneracionInfo.trim()
                        } else {
                            notas
                        }
                        
                        paymentsViewModel.registrarPago(
                            prestamoId = loanId,
                            cuotaId = cuotaSeleccionada?.id,
                            numeroCuota = cuotaSeleccionada?.numeroCuota ?: (p.cuotasPagadas + 1),
                            clienteId = p.clienteId,
                            clienteNombre = p.clienteNombre,
                            montoPagado = montoNum,
                            montoMora = moraNum,
                            metodoPago = metodoPago,
                            recibidoPor = usuarioActualEmail,
                            notas = notasFinal
                        )
                        showSuccessDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = montoPagado.isNotBlank() && prestamo != null && 
                         (tipoPago != TipoPago.EXONERAR_INTERES || notaExoneracion.isNotBlank())
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.register_payment),
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
    
    // Diálogo selector de cobradores
    if (showCobradorSelector) {
        AlertDialog(
            onDismissRequest = { showCobradorSelector = false },
            title = { Text(stringResource(R.string.select_collector)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cobradores.isEmpty()) {
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
                                text = stringResource(R.string.no_collectors_available),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        cobradores.forEach { cobrador ->
                            Card(
                                onClick = {
                                    cobradorSeleccionado = cobrador.id
                                    cobradorNombre = cobrador.nombre
                                    showCobradorSelector = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = cobrador.nombre,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = cobrador.email,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCobradorSelector = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
