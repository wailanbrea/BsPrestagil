package com.example.bsprestagil.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    navController: NavController,
    loansViewModel: LoansViewModel = viewModel()
) {
    val prestamos by loansViewModel.prestamos.collectAsState()
    val filtroEstado by loansViewModel.filtroEstado.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    
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
                            text = "${prestamos.size} préstamos totales",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
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
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddLoan.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo préstamo")
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
            if (filtroEstado != null) {
                item {
                    FilterChip(
                        selected = true,
                        onClick = { loansViewModel.setFiltroEstado(null) },
                        label = { Text("Filtrado: ${filtroEstado!!.name}") },
                        trailingIcon = {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                        }
                    )
                }
            }
            
            items(prestamos) { prestamo ->
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
            
            if (prestamos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Text(
                            text = if (filtroEstado != null) 
                                "No hay préstamos con este filtro"
                            else 
                                "No hay préstamos registrados",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

