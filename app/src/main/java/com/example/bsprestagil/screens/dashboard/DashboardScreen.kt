package com.example.bsprestagil.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.InfoCard
import com.example.bsprestagil.components.LoanCard
import com.example.bsprestagil.data.models.EstadoPrestamo
import com.example.bsprestagil.data.models.Prestamo
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController
) {
    // Datos de ejemplo
    val totalPrestado = 125000.00
    val interesesGenerados = 15600.00
    val carteraVencida = 8500.00
    val prestamosActivos = 12
    
    val prestamosRecientes = remember {
        listOf(
            Prestamo(
                id = "1",
                clienteId = "c1",
                clienteNombre = "Juan Pérez",
                montoOriginal = 10000.0,
                saldoPendiente = 6500.0,
                cuotasPagadas = 4,
                totalCuotas = 12,
                estado = EstadoPrestamo.ACTIVO
            ),
            Prestamo(
                id = "2",
                clienteId = "c2",
                clienteNombre = "María González",
                montoOriginal = 5000.0,
                saldoPendiente = 5800.0,
                cuotasPagadas = 2,
                totalCuotas = 6,
                estado = EstadoPrestamo.ATRASADO
            ),
            Prestamo(
                id = "3",
                clienteId = "c3",
                clienteNombre = "Carlos Ramírez",
                montoOriginal = 15000.0,
                saldoPendiente = 12000.0,
                cuotasPagadas = 1,
                totalCuotas = 10,
                estado = EstadoPrestamo.ACTIVO
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Resumen de tu negocio",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    // Botón de prueba de sincronización (temporal)
                    IconButton(onClick = {
                        navController.navigate(Screen.TestSync.route)
                    }) {
                        Icon(Icons.Default.BugReport, contentDescription = "Prueba de Sync", tint = WarningColor)
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Notifications.route)
                    }) {
                        Badge(
                            containerColor = ErrorColor
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estadísticas principales
            item {
                Text(
                    text = "Resumen general",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        InfoCard(
                            title = "Capital prestado",
                            value = "$${String.format("%,.0f", totalPrestado)}",
                            subtitle = "$prestamosActivos préstamos activos",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                    
                    item {
                        InfoCard(
                            title = "Intereses generados",
                            value = "$${String.format("%,.0f", interesesGenerados)}",
                            subtitle = "Mes actual",
                            color = SuccessColor,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                    
                    item {
                        InfoCard(
                            title = "Cartera vencida",
                            value = "$${String.format("%,.0f", carteraVencida)}",
                            subtitle = "3 clientes morosos",
                            color = ErrorColor,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }
            
            // Accesos rápidos
            item {
                Text(
                    text = "Accesos rápidos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.PersonAdd,
                        label = "Nuevo cliente",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.AddEditClient.createRoute()) }
                    )
                    
                    QuickAccessCard(
                        icon = Icons.Default.AccountBalance,
                        label = "Nuevo préstamo",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.AddLoan.createRoute()) }
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.Assessment,
                        label = "Reportes",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Reports.route) }
                    )
                    
                    QuickAccessCard(
                        icon = Icons.Default.Security,
                        label = "Garantías",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Collaterals.route) }
                    )
                }
            }
            
            // Préstamos recientes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Préstamos recientes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(onClick = { navController.navigate(Screen.Loans.route) }) {
                        Text("Ver todos")
                    }
                }
            }
            
            items(prestamosRecientes) { prestamo ->
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
        }
    }
}

@Composable
fun QuickAccessCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

