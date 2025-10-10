package com.example.bsprestagil.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.mappers.toEntity
import com.example.bsprestagil.viewmodels.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
    clientId: String?,
    navController: NavController,
    clientsViewModel: ClientsViewModel = viewModel()
) {
    val isEditing = clientId != null
    
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var referencia1Nombre by remember { mutableStateOf("") }
    var referencia1Telefono by remember { mutableStateOf("") }
    var referencia1Relacion by remember { mutableStateOf("") }
    var referencia2Nombre by remember { mutableStateOf("") }
    var referencia2Telefono by remember { mutableStateOf("") }
    var referencia2Relacion by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Si estamos editando, cargar datos del cliente
    if (isEditing && clientId != null) {
        val cliente by clientsViewModel.getClienteById(clientId).collectAsState(initial = null)
        
        LaunchedEffect(cliente) {
            cliente?.let { c ->
                nombre = c.nombre
                telefono = c.telefono
                email = c.email
                direccion = c.direccion
                
                if (c.referencias.isNotEmpty()) {
                    referencia1Nombre = c.referencias[0].nombre
                    referencia1Telefono = c.referencias[0].telefono
                    referencia1Relacion = c.referencias[0].relacion
                }
                
                if (c.referencias.size > 1) {
                    referencia2Nombre = c.referencias[1].nombre
                    referencia2Telefono = c.referencias[1].telefono
                    referencia2Relacion = c.referencias[1].relacion
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = if (isEditing) "Editar cliente" else "Nuevo cliente",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar cliente",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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
            // Información básica
            Text(
                text = "Información básica",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Referencias
            Text(
                text = "Referencias",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Referencia 1",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = referencia1Nombre,
                        onValueChange = { referencia1Nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = referencia1Telefono,
                        onValueChange = { referencia1Telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    OutlinedTextField(
                        value = referencia1Relacion,
                        onValueChange = { referencia1Relacion = it },
                        label = { Text("Relación (ej: Hermano, Amigo)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Referencia 2",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = referencia2Nombre,
                        onValueChange = { referencia2Nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = referencia2Telefono,
                        onValueChange = { referencia2Telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    OutlinedTextField(
                        value = referencia2Relacion,
                        onValueChange = { referencia2Relacion = it },
                        label = { Text("Relación (ej: Hermano, Amigo)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de guardar
            Button(
                onClick = {
                    clientsViewModel.insertCliente(
                        nombre = nombre,
                        telefono = telefono,
                        email = email,
                        direccion = direccion,
                        referencia1Nombre = referencia1Nombre,
                        referencia1Telefono = referencia1Telefono,
                        referencia1Relacion = referencia1Relacion,
                        referencia2Nombre = referencia2Nombre,
                        referencia2Telefono = referencia2Telefono,
                        referencia2Relacion = referencia2Relacion
                    )
                    showSuccessDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = nombre.isNotBlank() && telefono.isNotBlank() && direccion.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Actualizar cliente" else "Guardar cliente",
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
            title = { Text("✅ Cliente guardado") },
            text = { Text("El cliente se guardó correctamente y se sincronizará con la nube.") },
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
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && isEditing && clientId != null) {
        val cliente by clientsViewModel.getClienteById(clientId).collectAsState(initial = null)
        
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("⚠️ Eliminar cliente") },
            text = {
                Column {
                    Text("¿Estás seguro que deseas eliminar este cliente?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cliente: ${cliente?.nombre ?: ""}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Esta acción NO se puede deshacer y se eliminará de la nube también.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cliente?.let { c ->
                            clientsViewModel.deleteCliente(c.toEntity())
                            showDeleteDialog = false
                            navController.navigateUp()
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    }
}

