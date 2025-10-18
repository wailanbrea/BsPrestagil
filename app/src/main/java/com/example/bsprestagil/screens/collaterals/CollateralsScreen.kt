package com.example.bsprestagil.screens.collaterals

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bsprestagil.components.SwipeToDeleteItem
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.mappers.toEntity
import com.example.bsprestagil.data.models.EstadoGarantia
import com.example.bsprestagil.data.models.Garantia
import com.example.bsprestagil.data.models.TipoGarantia
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.utils.AuthUtils
import com.example.bsprestagil.viewmodels.AuthViewModel
import com.example.bsprestagil.viewmodels.CollateralsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollateralsScreen(
    navController: NavController,
    collateralsViewModel: CollateralsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val userRole by authViewModel.userRole.collectAsState()
    val garantias = remember {
        listOf(
            Garantia(
                id = "1",
                tipo = TipoGarantia.VEHICULO,
                descripcion = "Motocicleta Honda 2018",
                valorEstimado = 25000.0,
                estado = EstadoGarantia.RETENIDA
            ),
            Garantia(
                id = "2",
                tipo = TipoGarantia.ELECTRODOMESTICO,
                descripcion = "Refrigerador Samsung",
                valorEstimado = 8000.0,
                estado = EstadoGarantia.RETENIDA
            ),
            Garantia(
                id = "3",
                tipo = TipoGarantia.ELECTRONICO,
                descripcion = "Laptop Dell Inspiron",
                valorEstimado = 12000.0,
                estado = EstadoGarantia.DEVUELTA
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Garantías",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.QRScanner.route)
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.HistorialGarantias.route)
                    }) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.AddEditCollateral.createRoute())
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = garantias,
                key = { it.id }
            ) { garantia ->
                SwipeToDeleteItem(
                    isAdmin = (userRole == "ADMIN"),
                    itemName = garantia.descripcion,
                    itemType = "garantía",
                    onDelete = {
                        kotlinx.coroutines.runBlocking {
                            collateralsViewModel.deleteGarantiaById(garantia.id)
                        }
                    }
                ) {
                    CollateralCard(
                        garantia = garantia,
                        onClick = {
                            navController.navigate(Screen.CollateralDetail.createRoute(garantia.id))
                        },
                        onQRClick = {
                            navController.navigate(
                                Screen.QRGarantia.createRoute(
                                    garantiaId = garantia.id,
                                    clienteNombre = "Cliente", // TODO: Obtener nombre real del cliente
                                    descripcion = garantia.descripcion,
                                    valorEstimado = garantia.valorEstimado,
                                    tipo = garantia.tipo.name,
                                    fechaRegistro = garantia.fechaRegistro
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CollateralCard(
    garantia: Garantia,
    onClick: () -> Unit,
    onQRClick: () -> Unit
) {
    val estadoColor = when (garantia.estado) {
        EstadoGarantia.RETENIDA -> WarningColor
        EstadoGarantia.DEVUELTA -> SuccessColor
        EstadoGarantia.EJECUTADA -> ErrorColor
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = garantia.descripcion,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when(garantia.tipo) {
                        TipoGarantia.VEHICULO -> "Vehículo"
                        TipoGarantia.ELECTRODOMESTICO -> "Electrodoméstico"
                        TipoGarantia.ELECTRONICO -> "Electrónico"
                        TipoGarantia.JOYA -> "Joya"
                        TipoGarantia.MUEBLE -> "Mueble"
                        TipoGarantia.OTRO -> "Otro"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = estadoColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = when(garantia.estado) {
                            EstadoGarantia.RETENIDA -> "Retenida"
                            EstadoGarantia.DEVUELTA -> "Devuelta"
                            EstadoGarantia.EJECUTADA -> "Ejecutada"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = estadoColor
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%,.0f", garantia.valorEstimado)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Valor est.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            }
            
            Divider()
            
            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onQRClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver QR", fontSize = 13.sp)
                }
                
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detalles", fontSize = 13.sp)
                }
            }
        }
    }
}

