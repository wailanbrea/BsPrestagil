package com.example.bsprestagil.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.data.database.entities.UsuarioEntity
import com.example.bsprestagil.data.models.EstadoPrestamo
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.viewmodels.UsersViewModel
import com.example.bsprestagil.viewmodels.LoansViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCobradoresScreen(
    navController: NavController,
    viewModel: UsersViewModel = viewModel(),
    loansViewModel: LoansViewModel = viewModel()
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val creationError by viewModel.creationError.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<UsuarioEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<UsuarioEntity?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var usuarioCreado by remember { mutableStateOf<Pair<String, String>?>(null) } // email, password
    var isSyncing by remember { mutableStateOf(false) }
    
    // Sincronizar usuarios al abrir la pantalla
    LaunchedEffect(Unit) {
        isSyncing = true
        viewModel.syncUsuariosFromFirestore()
        isSyncing = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Cobradores") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Agregar cobrador")
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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COBRADORES ACTIVOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (isSyncing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp
                            )
                            Text(
                                text = "Sincronizando...",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            if (usuarios.isEmpty() && !isSyncing) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "No hay cobradores registrados",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Agrega un nuevo cobrador con el botón +",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                items(usuarios) { usuario ->
                    CobradorCard(
                        usuario = usuario,
                        loansViewModel = loansViewModel,
                        onEdit = { showEditDialog = usuario },
                        onDelete = { showDeleteDialog = usuario }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Diálogo para agregar cobrador
    if (showAddDialog) {
        AddCobradorDialog(
            onDismiss = { showAddDialog = false },
            isCreating = isCreating,
            onConfirm = { nombre, email, telefono, rol, password ->
                viewModel.crearUsuarioConCuenta(
                    nombre = nombre,
                    email = email,
                    telefono = telefono,
                    rol = rol,
                    password = password
                )
                usuarioCreado = email to password
                showAddDialog = false
                showSuccessDialog = true
            }
        )
    }
    
    // Diálogo de éxito con credenciales
    if (showSuccessDialog && usuarioCreado != null) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("¡Cuenta creada exitosamente!") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "El cobrador ya puede iniciar sesión en la app con estas credenciales:",
                        fontSize = 14.sp
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Email:", fontWeight = FontWeight.Bold)
                                Text(
                                    usuarioCreado!!.first,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Contraseña:", fontWeight = FontWeight.Bold)
                                Text(
                                    usuarioCreado!!.second,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Text(
                        "⚠️ Guarda estas credenciales y compártelas con el cobrador de forma segura.",
                        fontSize = 12.sp,
                        color = WarningColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "El cobrador puede iniciar sesión en la app con estas credenciales.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    usuarioCreado = null
                }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entendido")
                }
            }
        )
    }
    
    // Mostrar error si existe
    LaunchedEffect(creationError) {
        creationError?.let { error ->
            // Aquí podrías mostrar un Snackbar o Toast con el error
            // Por ahora solo lo logueamos
            android.util.Log.e("GestionCobradores", error)
        }
    }
    
    // Diálogo para editar cobrador
    showEditDialog?.let { usuario ->
        EditCobradorDialog(
            usuario = usuario,
            onDismiss = { showEditDialog = null },
            onConfirm = { nombre, telefono, rol, porcentajeComision ->
                viewModel.actualizarCobrador(
                    usuarioId = usuario.id,
                    nombre = nombre,
                    telefono = telefono,
                    rol = rol,
                    porcentajeComision = porcentajeComision
                )
                showEditDialog = null
            }
        )
    }
    
    // Diálogo de confirmación para eliminar
    showDeleteDialog?.let { usuario ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("¿Eliminar cobrador?") },
            text = {
                Text("¿Estás seguro de eliminar a ${usuario.nombre}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarUsuario(usuario)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CobradorCard(
    usuario: UsuarioEntity,
    loansViewModel: LoansViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Obtener préstamos asignados al cobrador
    val prestamosAsignados by loansViewModel.getPrestamosByCobradorId(usuario.id)
        .collectAsState(initial = emptyList())
    
    val prestamosActivos = prestamosAsignados.count { 
        it.estado == EstadoPrestamo.ACTIVO || it.estado == EstadoPrestamo.ATRASADO 
    }
    val capitalPendiente = prestamosAsignados
        .filter { it.estado == EstadoPrestamo.ACTIVO || it.estado == EstadoPrestamo.ATRASADO }
        .sumOf { it.capitalPendiente }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (usuario.email.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = usuario.email,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                if (usuario.telefono.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = usuario.telefono,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (usuario.rol) {
                        "ADMIN" -> MaterialTheme.colorScheme.primary
                        "COBRADOR" -> com.example.bsprestagil.ui.theme.SuccessColor
                        else -> MaterialTheme.colorScheme.secondary
                    }.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = usuario.rol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (usuario.rol) {
                            "ADMIN" -> MaterialTheme.colorScheme.primary
                            "COBRADOR" -> com.example.bsprestagil.ui.theme.SuccessColor
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // Estadísticas del cobrador
        if (prestamosActivos > 0) {
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$prestamosActivos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Préstamos",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$${String.format("%,.0f", capitalPendiente)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.bsprestagil.ui.theme.SuccessColor
                    )
                    Text(
                        text = "Por cobrar",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCobradorDialog(
    onDismiss: () -> Unit,
    isCreating: Boolean,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("COBRADOR") }
    var expanded by remember { mutableStateOf(false) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var errorPassword by remember { mutableStateOf("") }
    
    val roles = listOf("ADMIN", "COBRADOR", "SUPERVISOR")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Cobrador") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        errorPassword = ""
                    },
                    label = { Text("Email *") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorPassword = ""
                    },
                    label = { Text("Contraseña *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                            Icon(
                                if (mostrarPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (mostrarPassword) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (mostrarPassword) 
                        androidx.compose.ui.text.input.VisualTransformation.None 
                    else 
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    supportingText = {
                        Text(
                            text = "Mínimo 6 caracteres",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
                
                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = { 
                        confirmarPassword = it
                        errorPassword = ""
                    },
                    label = { Text("Confirmar contraseña *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = errorPassword.isNotEmpty(),
                    supportingText = if (errorPassword.isNotEmpty()) {
                        { Text(errorPassword, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = rolSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol *") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol) },
                                onClick = {
                                    rolSeleccionado = rol
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> {
                            errorPassword = "El nombre es obligatorio"
                        }
                        email.isBlank() -> {
                            errorPassword = "El email es obligatorio"
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorPassword = "Email inválido"
                        }
                        password.length < 6 -> {
                            errorPassword = "La contraseña debe tener al menos 6 caracteres"
                        }
                        password != confirmarPassword -> {
                            errorPassword = "Las contraseñas no coinciden"
                        }
                        else -> {
                            errorPassword = ""
                            onConfirm(nombre, email, telefono, rolSeleccionado, password)
                        }
                    }
                },
                enabled = nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank() && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creando...")
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear cuenta")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCobradorDialog(
    usuario: UsuarioEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Float) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var telefono by remember { mutableStateOf(usuario.telefono) }
    var rolSeleccionado by remember { mutableStateOf(usuario.rol) }
    var porcentajeComision by remember { mutableStateOf(usuario.porcentajeComision.toString()) }
    var expanded by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf("") }
    
    val roles = listOf("ADMIN", "COBRADOR", "SUPERVISOR")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Cobrador") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { 
                        nombre = it
                        errorMensaje = ""
                    },
                    label = { Text("Nombre completo *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Email no se puede editar
                OutlinedTextField(
                    value = usuario.email,
                    onValueChange = {},
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "El email no se puede modificar",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                )
                
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = rolSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol *") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol) },
                                onClick = {
                                    rolSeleccionado = rol
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = porcentajeComision,
                    onValueChange = { 
                        if (it.isEmpty() || it.toFloatOrNull() != null) {
                            porcentajeComision = it
                            errorMensaje = ""
                        }
                    },
                    label = { Text("Porcentaje de comisión (%)") },
                    leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    supportingText = {
                        Text(
                            text = "Ej: 3.0 = 3%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
                
                if (errorMensaje.isNotEmpty()) {
                    Text(
                        text = errorMensaje,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        nombre.isBlank() -> {
                            errorMensaje = "El nombre es obligatorio"
                        }
                        else -> {
                            val porcentaje = porcentajeComision.toFloatOrNull() ?: 3.0f
                            errorMensaje = ""
                            onConfirm(nombre, telefono, rolSeleccionado, porcentaje)
                        }
                    }
                },
                enabled = nombre.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

