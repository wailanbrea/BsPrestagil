package com.example.bsprestagil.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.LoanCard
import com.example.bsprestagil.data.models.EstadoPrestamo
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.viewmodels.LoansViewModel
import com.example.bsprestagil.viewmodels.UsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    navController: NavController,
    loansViewModel: LoansViewModel = viewModel(),
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
    
    val prestamos by loansViewModel.prestamos.collectAsState()
    val filtroEstado by loansViewModel.filtroEstado.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var showCobradorFilterDialog by remember { mutableStateOf(false) }
    var cobradorFiltroSeleccionado by remember { mutableStateOf<String?>(null) }
    var cobradorFiltroNombre by remember { mutableStateOf<String?>(null) }
    
    // Cargar cobradores
    val usuarios by usersViewModel.usuarios.collectAsState()
    val cobradores = usuarios.filter { it.activo }
    
    // ⭐ Si es COBRADOR, filtrar automáticamente sus préstamos en el ViewModel
    LaunchedEffect(Unit) {
        android.util.Log.d("LoansScreen", "UserRole: $userRole, UserId: $currentUserId")
        if (userRole == "COBRADOR" && currentUserId != null) {
            android.util.Log.d("LoansScreen", "Aplicando filtro de cobrador: $currentUserId")
            loansViewModel.setCobradorFilter(currentUserId)
        } else {
            android.util.Log.d("LoansScreen", "Quitando filtro de cobrador")
            loansViewModel.setCobradorFilter(null)
        }
    }
    
    // Reaplicar filtro cada vez que cambie el rol o userId
    LaunchedEffect(userRole, currentUserId) {
        android.util.Log.d("LoansScreen", "Rol cambió a: $userRole")
        if (userRole == "COBRADOR" && currentUserId != null) {
            loansViewModel.setCobradorFilter(currentUserId)
        } else {
            loansViewModel.setCobradorFilter(null)
        }
    }
    
    // Los préstamos ya vienen filtrados del ViewModel
    val prestamosFiltrados = prestamos
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Préstamos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${prestamosFiltrados.size} de ${prestamos.size} préstamos",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    // ⭐ Solo prestamistas pueden filtrar por cobrador
                    if (userRole != "COBRADOR") {
                        // Filtro por cobrador
                        IconButton(onClick = { showCobradorFilterDialog = true }) {
                            Icon(
                                Icons.Default.WorkOutline,
                                contentDescription = "Filtrar por cobrador",
                                tint = if (cobradorFiltroSeleccionado != null) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // Filtro por estado
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                loansViewModel.setFiltroEstado(null)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Activos") },
                            onClick = {
                                loansViewModel.setFiltroEstado(EstadoPrestamo.ACTIVO)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Atrasados") },
                            onClick = {
                                loansViewModel.setFiltroEstado(EstadoPrestamo.ATRASADO)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Completados") },
                            onClick = {
                                loansViewModel.setFiltroEstado(EstadoPrestamo.COMPLETADO)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cancelados") },
                            onClick = {
                                loansViewModel.setFiltroEstado(EstadoPrestamo.CANCELADO)
                                showFilterMenu = false
                            }
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
        },
        floatingActionButton = {
            // ⭐ Solo prestamistas pueden crear préstamos
            if (userRole != "COBRADOR") {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddLoan.createRoute()) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo préstamo")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Chips de filtros activos
            if (filtroEstado != null || cobradorFiltroSeleccionado != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filtroEstado != null) {
                            FilterChip(
                                selected = true,
                                onClick = { loansViewModel.setFiltroEstado(null) },
                                label = { Text("Estado: ${filtroEstado!!.name}") },
                                trailingIcon = {
                                    Icon(Icons.Default.Cancel, contentDescription = null)
                                }
                            )
                        }
                        
                        if (cobradorFiltroSeleccionado != null) {
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
                }
            }
            
            items(prestamosFiltrados) { prestamo ->
                val estadoColor = when (prestamo.estado) {
                    EstadoPrestamo.ACTIVO -> SuccessColor
                    EstadoPrestamo.ATRASADO -> WarningColor
                    EstadoPrestamo.COMPLETADO -> MaterialTheme.colorScheme.primary
                    EstadoPrestamo.CANCELADO -> ErrorColor
                }
                
                val estadoTexto = when (prestamo.estado) {
                    EstadoPrestamo.ACTIVO -> "Activo"
                    EstadoPrestamo.ATRASADO -> "Atrasado"
                    EstadoPrestamo.COMPLETADO -> "Completado"
                    EstadoPrestamo.CANCELADO -> "Cancelado"
                }
                
                LoanCard(
                    clienteNombre = prestamo.clienteNombre,
                    montoOriginal = prestamo.montoOriginal,
                    capitalPendiente = prestamo.capitalPendiente,
                    totalCapitalPagado = prestamo.totalCapitalPagado,
                    estado = estadoTexto,
                    estadoColor = estadoColor,
                    onClick = {
                        navController.navigate(Screen.LoanDetail.createRoute(prestamo.id))
                    }
                )
            }
            
            if (prestamosFiltrados.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Text(
                            text = if (filtroEstado != null || cobradorFiltroSeleccionado != null) 
                                "No hay préstamos con estos filtros"
                            else 
                                "No hay préstamos registrados",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
            title = { Text("Filtrar por cobrador") },
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
                            Text("Todos los cobradores", fontWeight = FontWeight.Medium)
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
                            text = "No hay cobradores disponibles",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCobradorFilterDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

