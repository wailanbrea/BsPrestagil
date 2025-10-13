package com.example.bsprestagil.screens.collaterals

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.utils.PhotoUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CollateralDetailScreen(
    collateralId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Datos de ejemplo
    val descripcion = "Motocicleta Honda 2018"
    val tipo = "Vehículo"
    val valorEstimado = 25000.0
    val estado = "Retenida"
    val fechaRegistro = System.currentTimeMillis()
    val notas = "Color roja, placa ABC-123, en buen estado"
    
    // Cargar fotos de la garantía
    val fotos = remember { PhotoUtils.obtenerFotosGarantia(context, collateralId) }
    var fotoSeleccionada by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Detalle de garantía",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(
                            Screen.QRGarantia.createRoute(
                                garantiaId = collateralId,
                                clienteNombre = "Cliente",
                                descripcion = descripcion,
                                valorEstimado = valorEstimado,
                                tipo = tipo,
                                fechaRegistro = fechaRegistro
                            )
                        )
                    }) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Ver QR")
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
            // Fotos de la garantía
            if (fotos.isNotEmpty()) {
                item {
                    Text(
                        text = "📷 Fotos del artículo (${fotos.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(fotos) { fotoUri ->
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = "Foto de garantía",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { fotoSeleccionada = fotoUri },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            
            // Información de la garantía
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = descripcion,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tipo,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Detalles
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        DetailRow("Estado", estado)
                        Divider()
                        DetailRow("Valor estimado", "$${String.format("%,.2f", valorEstimado)}")
                        Divider()
                        DetailRow("Fecha de registro", dateFormat.format(Date(fechaRegistro)))
                        
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
            
            // Botones de acción
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.AddEditCollateral.createRoute(collateralId))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar")
                    }
                    
                    Button(
                        onClick = {
                            navController.navigate(
                                Screen.QRGarantia.createRoute(
                                    garantiaId = collateralId,
                                    clienteNombre = "Cliente",
                                    descripcion = descripcion,
                                    valorEstimado = valorEstimado,
                                    tipo = tipo,
                                    fechaRegistro = fechaRegistro
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver QR")
                    }
                }
            }
        }
    }
    
    // Diálogo para ver foto en pantalla completa
    if (fotoSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { fotoSeleccionada = null },
            title = { Text("Foto de garantía") },
            text = {
                AsyncImage(
                    model = fotoSeleccionada,
                    contentDescription = "Foto ampliada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { fotoSeleccionada = null }) {
                    Text("Cerrar")
                }
            }
        )
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
