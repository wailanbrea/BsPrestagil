package com.example.bsprestagil.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.LoanCard
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.EstadoPrestamo
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.viewmodels.ClientsViewModel
import com.example.bsprestagil.viewmodels.LoansViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: String,
    navController: NavController,
    clientsViewModel: ClientsViewModel = viewModel(),
    loansViewModel: LoansViewModel = viewModel()
) {
    // Cargar datos del cliente
    val cliente by clientsViewModel.getClienteById(clientId).collectAsState(initial = null)
    val prestamosCliente by loansViewModel.getPrestamosByClienteId(clientId).collectAsState(initial = emptyList())
    
    val clienteNombre = cliente?.nombre ?: "Cargando..."
    val clienteTelefono = cliente?.telefono ?: ""
    val clienteEmail = cliente?.email ?: ""
    val clienteDireccion = cliente?.direccion ?: ""
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Detalles del cliente",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.AddEditClient.createRoute(clientId))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
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
            // Información del cliente
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = clienteNombre,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        InfoRow(icon = Icons.Default.Phone, text = clienteTelefono)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(icon = Icons.Default.Email, text = clienteEmail)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(icon = Icons.Default.LocationOn, text = clienteDireccion)
                    }
                }
            }
            
            // Botones de acción
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Llamar */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Llamar")
                    }
                    
                    Button(
                        onClick = {
                            navController.navigate(Screen.AddLoan.createRoute(clientId))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuevo préstamo")
                    }
                }
            }
            
            // Préstamos del cliente
            item {
                Text(
                    text = "Préstamos (${prestamosCliente.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            items(prestamosCliente) { prestamo ->
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
            
            if (prestamosCliente.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin préstamos activos",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

