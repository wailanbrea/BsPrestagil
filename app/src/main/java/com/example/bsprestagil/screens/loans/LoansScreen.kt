package com.example.bsprestagil.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.LoanCard
import com.example.bsprestagil.data.models.EstadoPrestamo
import com.example.bsprestagil.data.models.Prestamo
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    navController: NavController
) {
    var filtroEstado by remember { mutableStateOf<EstadoPrestamo?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    val prestamos = remember {
        listOf(
            Prestamo(
                id = "1",
                clienteId = "c1",
                clienteNombre = "Juan Pérez González",
                montoOriginal = 10000.0,
                saldoPendiente = 6500.0,
                cuotasPagadas = 4,
                totalCuotas = 12,
                estado = EstadoPrestamo.ACTIVO,
                tasaInteres = 10.0
            ),
            Prestamo(
                id = "2",
                clienteId = "c2",
                clienteNombre = "María González López",
                montoOriginal = 5000.0,
                saldoPendiente = 5800.0,
                cuotasPagadas = 2,
                totalCuotas = 6,
                estado = EstadoPrestamo.ATRASADO,
                tasaInteres = 10.0
            ),
            Prestamo(
                id = "3",
                clienteId = "c3",
                clienteNombre = "Carlos Ramírez Sánchez",
                montoOriginal = 15000.0,
                saldoPendiente = 12000.0,
                cuotasPagadas = 1,
                totalCuotas = 10,
                estado = EstadoPrestamo.ACTIVO,
                tasaInteres = 12.0
            ),
            Prestamo(
                id = "4",
                clienteId = "c4",
                clienteNombre = "Ana Martínez García",
                montoOriginal = 8000.0,
                saldoPendiente = 0.0,
                cuotasPagadas = 8,
                totalCuotas = 8,
                estado = EstadoPrestamo.COMPLETADO,
                tasaInteres = 10.0
            ),
            Prestamo(
                id = "5",
                clienteId = "c5",
                clienteNombre = "Roberto Silva Torres",
                montoOriginal = 20000.0,
                saldoPendiente = 22500.0,
                cuotasPagadas = 0,
                totalCuotas = 12,
                estado = EstadoPrestamo.ATRASADO,
                tasaInteres = 15.0
            )
        )
    }
    
    val prestamosFiltrados = if (filtroEstado != null) {
        prestamos.filter { it.estado == filtroEstado }
    } else {
        prestamos
    }
    
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
                                filtroEstado = null
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Activos") },
                            onClick = {
                                filtroEstado = EstadoPrestamo.ACTIVO
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Atrasados") },
                            onClick = {
                                filtroEstado = EstadoPrestamo.ATRASADO
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Completados") },
                            onClick = {
                                filtroEstado = EstadoPrestamo.COMPLETADO
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cancelados") },
                            onClick = {
                                filtroEstado = EstadoPrestamo.CANCELADO
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
                        onClick = { filtroEstado = null },
                        label = { Text("Filtrado: ${filtroEstado!!.name}") },
                        trailingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    )
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
                    saldoPendiente = prestamo.saldoPendiente,
                    cuotasPagadas = prestamo.cuotasPagadas,
                    totalCuotas = prestamo.totalCuotas,
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
                            text = "No se encontraron préstamos",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

