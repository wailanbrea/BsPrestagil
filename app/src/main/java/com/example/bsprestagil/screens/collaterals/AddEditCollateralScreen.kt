package com.example.bsprestagil.screens.collaterals

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.models.TipoGarantia
import com.example.bsprestagil.data.models.EstadoGarantia
import com.example.bsprestagil.utils.PhotoUtils
import com.example.bsprestagil.viewmodels.CollateralsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCollateralScreen(
    collateralId: String?,
    navController: NavController,
    viewModel: CollateralsViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEditing = collateralId != null
    
    // Cargar garantía existente si estamos editando
    val garantiaExistente by viewModel.getGarantiaById(collateralId ?: "").collectAsState(initial = null)
    
    var descripcion by remember { mutableStateOf("") }
    var valorEstimado by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoGarantia.OTRO) }
    var estadoGarantia by remember { mutableStateOf(EstadoGarantia.RETENIDA) }
    var notas by remember { mutableStateOf("") }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedEstado by remember { mutableStateOf(false) }
    var fotosUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var fotoTemporal by remember { mutableStateOf<File?>(null) }
    
    // Cargar datos existentes si es edición
    LaunchedEffect(garantiaExistente) {
        garantiaExistente?.let { g ->
            descripcion = g.descripcion
            valorEstimado = g.valorEstimado.toString()
            tipo = g.tipo
            estadoGarantia = g.estado
            notas = g.notas
            fotosUris = g.fotosUrls
        }
    }
    
    val tiposDisponibles = TipoGarantia.values().toList()
    
    // Launcher para tomar foto
    val tomarFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && fotoTemporal != null) {
            fotosUris = fotosUris + PhotoUtils.guardarRutaFoto(fotoTemporal!!)
        }
    }
    
    // Launcher para seleccionar de galería
    val seleccionarFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        // Copiar las imágenes seleccionadas a nuestra carpeta
        uris.forEach { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = PhotoUtils.crearArchivoTemporal(
                    context,
                    collateralId ?: "temp_${System.currentTimeMillis()}"
                )
                val outputStream = file.outputStream()
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                fotosUris = fotosUris + file.absolutePath
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = if (isEditing) "Editar garantía" else "Nueva garantía",
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
            Text(
                text = "Información del artículo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Tipo de garantía
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = it }
            ) {
                OutlinedTextField(
                    value = when(tipo) {
                        TipoGarantia.VEHICULO -> "Vehículo"
                        TipoGarantia.ELECTRODOMESTICO -> "Electrodoméstico"
                        TipoGarantia.ELECTRONICO -> "Electrónico"
                        TipoGarantia.JOYA -> "Joya"
                        TipoGarantia.MUEBLE -> "Mueble"
                        TipoGarantia.OTRO -> "Otro"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de artículo *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedTipo,
                    onDismissRequest = { expandedTipo = false }
                ) {
                    tiposDisponibles.forEach { tipoItem ->
                        DropdownMenuItem(
                            text = {
                                Text(when(tipoItem) {
                                    TipoGarantia.VEHICULO -> "Vehículo"
                                    TipoGarantia.ELECTRODOMESTICO -> "Electrodoméstico"
                                    TipoGarantia.ELECTRONICO -> "Electrónico"
                                    TipoGarantia.JOYA -> "Joya"
                                    TipoGarantia.MUEBLE -> "Mueble"
                                    TipoGarantia.OTRO -> "Otro"
                                })
                            },
                            onClick = {
                                tipo = tipoItem
                                expandedTipo = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del artículo *") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Laptop Dell Inspiron 15") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = valorEstimado,
                onValueChange = { valorEstimado = it },
                label = { Text("Valor estimado *") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") }
            )
            
            // Estado de la garantía (solo al editar)
            if (isEditing) {
                ExposedDropdownMenuBox(
                    expanded = expandedEstado,
                    onExpandedChange = { expandedEstado = it }
                ) {
                    OutlinedTextField(
                        value = when(estadoGarantia) {
                            EstadoGarantia.RETENIDA -> "Retenida"
                            EstadoGarantia.DEVUELTA -> "Devuelta"
                            EstadoGarantia.EJECUTADA -> "Ejecutada"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedEstado,
                        onDismissRequest = { expandedEstado = false }
                    ) {
                        EstadoGarantia.values().forEach { estado ->
                            DropdownMenuItem(
                                text = {
                                    Text(when(estado) {
                                        EstadoGarantia.RETENIDA -> "Retenida"
                                        EstadoGarantia.DEVUELTA -> "Devuelta"
                                        EstadoGarantia.EJECUTADA -> "Ejecutada"
                                    })
                                },
                                onClick = {
                                    estadoGarantia = estado
                                    expandedEstado = false
                                }
                            )
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                placeholder = { Text("Detalles, marca, modelo, condición...") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sección de fotos
            Text(
                text = "📷 Fotos del artículo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (fotosUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(fotosUris) { fotoUri ->
                                Box {
                                    AsyncImage(
                                        model = fotoUri,
                                        contentDescription = "Foto de garantía",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Botón para eliminar foto
                                    IconButton(
                                        onClick = {
                                            PhotoUtils.eliminarFoto(fotoUri)
                                            fotosUris = fotosUris.filter { it != fotoUri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(32.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .size(16.dp),
                                                tint = MaterialTheme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val file = PhotoUtils.crearArchivoTemporal(
                                    context,
                                    collateralId ?: "temp_${System.currentTimeMillis()}"
                                )
                                fotoTemporal = file
                                val uri = PhotoUtils.obtenerUriParaFoto(context, file)
                                tomarFotoLauncher.launch(uri)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tomar foto")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                seleccionarFotoLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Galería")
                        }
                    }
                    
                    if (fotosUris.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Agrega fotos del artículo para mejor identificación",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${fotosUris.size} foto(s) agregada(s)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    val valorNum = valorEstimado.toDoubleOrNull() ?: 0.0
                    
                    if (descripcion.isNotBlank() && valorNum > 0) {
                        if (isEditing && collateralId != null) {
                            // Actualizar garantía existente
                            viewModel.updateGarantia(
                                garantiaId = collateralId,
                                tipo = tipo,
                                descripcion = descripcion,
                                valorEstimado = valorNum,
                                estado = estadoGarantia,
                                fotosUrls = fotosUris,
                                notas = notas
                            )
                        } else {
                            // Crear nueva garantía
                            scope.launch {
                                val garantiaId = viewModel.crearGarantia(
                                    tipo = tipo,
                                    descripcion = descripcion,
                                    valorEstimado = valorNum,
                                    fotosUrls = fotosUris,
                                    notas = notas
                                )
                                
                                // Devolver el resultado a la pantalla anterior
                                navController.previousBackStackEntry?.savedStateHandle?.set("garantiaId", garantiaId)
                                navController.previousBackStackEntry?.savedStateHandle?.set("garantiaDescripcion", descripcion)
                                android.util.Log.d("AddEditCollateral", "✅ Garantía creada: $garantiaId - $descripcion")
                                
                                showSuccessDialog = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = descripcion.isNotBlank() && (valorEstimado.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Actualizar garantía" else "Guardar garantía",
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
            title = { Text("✅ Garantía guardada") },
            text = { 
                Column {
                    Text("La garantía se guardó correctamente.")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (fotosUris.isNotEmpty()) {
                        Text(
                            text = "✅ ${fotosUris.size} foto(s) guardada(s)",
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
}

