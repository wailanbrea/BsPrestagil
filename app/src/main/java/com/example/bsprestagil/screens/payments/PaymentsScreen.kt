package com.example.bsprestagil.screens.payments

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.R
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.EmptyStateComponent
import com.example.bsprestagil.components.SwipeToDeleteItem
import com.example.bsprestagil.data.mappers.toEntity
import com.example.bsprestagil.data.models.MetodoPago
import com.example.bsprestagil.data.models.Pago
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.utils.AuthUtils
import com.example.bsprestagil.viewmodels.PaymentsViewModel
import com.example.bsprestagil.viewmodels.UsersViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    navController: NavController,
    paymentsViewModel: PaymentsViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel(),
    authViewModel: com.example.bsprestagil.viewmodels.AuthViewModel = viewModel()
) {
    // ⭐ Leer rol y usuario actual del AuthViewModel (viene de Firestore)
    val userRole by authViewModel.userRole.collectAsState()
    
    // ⭐ Si el rol aún no se ha cargado, mostrar loading
    if (userRole == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid
    val currentUserEmail = currentUser?.email
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val pagos by paymentsViewModel.pagos.collectAsState()
    val totalCobradoHoy by paymentsViewModel.totalCobradoHoy.collectAsState()
    val countPagosHoy by paymentsViewModel.countPagosHoy.collectAsState()
    
    var showCobradorFilterDialog by remember { mutableStateOf(false) }
    var cobradorFiltroSeleccionado by remember { mutableStateOf<String?>(null) }
    var cobradorFiltroNombre by remember { mutableStateOf<String?>(null) }
    
    // Cargar cobradores
    val usuarios by usersViewModel.usuarios.collectAsState()
    val cobradores = usuarios.filter { it.activo }
    
    // ⭐ Si es COBRADOR, filtrar automáticamente sus pagos en el ViewModel
    LaunchedEffect(Unit) {
        android.util.Log.d("PaymentsScreen", "UserRole: $userRole, Email: $currentUserEmail")
    }
    
    LaunchedEffect(userRole, currentUserEmail) {
        if (userRole == "COBRADOR" && currentUserEmail != null) {
            android.util.Log.d("PaymentsScreen", "Aplicando filtro de pagos: $currentUserEmail")
            paymentsViewModel.setCobradorFilter(currentUserEmail)
        } else {
            android.util.Log.d("PaymentsScreen", "Quitando filtro de pagos")
            paymentsViewModel.setCobradorFilter(null)
        }
    }
    
    // Los pagos ya vienen filtrados del ViewModel
    val pagosFiltrados = pagos
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.payments),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${pagosFiltrados.size} de ${pagos.size} pagos",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCobradorFilterDialog = true }) {
                        Icon(
                            Icons.Default.WorkOutline,
                            contentDescription = stringResource(R.string.filter_by_collector),
                            tint = if (cobradorFiltroSeleccionado != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                userRole = userRole
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
            // Resumen de cobros
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
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.total_collected_today),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${String.format("%,.2f", totalCobradoHoy)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$countPagosHoy pagos registrados",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Chip de filtro activo
            if (cobradorFiltroSeleccionado != null) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = {
                            cobradorFiltroSeleccionado = null
                            cobradorFiltroNombre = null
                        },
                        label = { Text("Cobrador: ${cobradorFiltroNombre ?: ""}") },
                        leadingIcon = {
                            Icon(Icons.Default.WorkOutline, contentDescription = null)
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                        }
                    )
                }
            }
            
            item {
                Text(
                    text = stringResource(R.string.payment_history),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (pagosFiltrados.isEmpty()) {
                item {
                    if (cobradorFiltroSeleccionado != null) {
                        EmptyStateComponent(
                            icon = Icons.Default.FilterList,
                            title = stringResource(R.string.no_payments_registered),
                            message = stringResource(R.string.no_payments_for_collector),
                            actionText = stringResource(R.string.clear_filter),
                            onActionClick = {
                                cobradorFiltroSeleccionado = null
                                cobradorFiltroNombre = null
                            }
                        )
                    } else {
                        EmptyStateComponent(
                            icon = Icons.Default.Payment,
                            title = stringResource(R.string.no_payments_registered),
                            message = if (userRole == "COBRADOR") {
                                stringResource(R.string.no_payments_yet)
                            } else {
                                stringResource(R.string.no_payments_in_system)
                            },
                            actionText = null,
                            onActionClick = null
                        )
                    }
                }
            } else {
                items(
                    items = pagosFiltrados,
                    key = { it.id }
                ) { pago ->
                    SwipeToDeleteItem(
                        isAdmin = (userRole == "ADMIN"),
                        itemName = "${pago.clienteNombre} - $${String.format("%.2f", pago.montoPagado)}",
                        itemType = "pago",
                        onDelete = {
                            kotlinx.coroutines.runBlocking {
                                paymentsViewModel.deletePagoById(pago.id)
                            }
                        }
                    ) {
                        PaymentCard(
                            pago = pago,
                            dateFormat = dateFormat,
                            onClick = {
                                navController.navigate(Screen.PaymentDetail.createRoute(pago.id))
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de filtro por cobrador
    if (showCobradorFilterDialog) {
        AlertDialog(
            onDismissRequest = { showCobradorFilterDialog = false },
            icon = {
                Icon(
                    Icons.Default.WorkOutline,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text(stringResource(R.string.filter_by_collector)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opción: Todos
                    Card(
                        onClick = {
                            cobradorFiltroSeleccionado = null
                            cobradorFiltroNombre = null
                            showCobradorFilterDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.People, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.all_collectors), fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    // Lista de cobradores
                    cobradores.forEach { cobrador ->
                        Card(
                            onClick = {
                                cobradorFiltroSeleccionado = cobrador.id
                                cobradorFiltroNombre = cobrador.nombre
                                showCobradorFilterDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WorkOutline, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cobrador.nombre, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    
                    if (cobradores.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_collectors_available),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCobradorFilterDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
fun PaymentCard(
    pago: Pago,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SuccessColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pago.clienteNombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFormat.format(Date(pago.fechaPago)),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${pago.diasTranscurridos} días",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "→ Interés: $${String.format("%.0f", pago.montoAInteres)} • Capital: $${String.format("%.0f", pago.montoACapital)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (pago.montoMora > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Mora: $${String.format("%.2f", pago.montoMora)}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%,.2f", pago.montoPagado)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when(pago.metodoPago) {
                        MetodoPago.EFECTIVO -> stringResource(R.string.cash)
                        MetodoPago.TRANSFERENCIA -> stringResource(R.string.transfer)
                        MetodoPago.TARJETA -> stringResource(R.string.card)
                        MetodoPago.OTRO -> stringResource(R.string.other)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

