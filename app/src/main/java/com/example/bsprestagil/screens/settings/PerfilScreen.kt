package com.example.bsprestagil.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.R
import com.example.bsprestagil.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    
    var nombre by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var telefono by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("ADMIN") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showVerificationMessage by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var enviandoVerificacion by remember { mutableStateOf(false) }
    var isEmailVerified by remember { mutableStateOf(currentUser?.isEmailVerified ?: false) }
    var updateError by remember { mutableStateOf<String?>(null) }
    var cargandoDatos by remember { mutableStateOf(true) }
    
    // Cargar datos completos desde Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            try {
                val doc = firestore.collection("usuarios").document(uid).get().await()
                if (doc.exists()) {
                    nombre = doc.getString("nombre") ?: currentUser.displayName ?: ""
                    telefono = doc.getString("telefono") ?: ""
                    rol = doc.getString("rol") ?: "ADMIN"
                } else {
                    // Crear documento en Firestore si no existe
                    firestore.collection("usuarios").document(uid).set(
                        hashMapOf(
                            "nombre" to (currentUser.displayName ?: ""),
                            "email" to (currentUser.email ?: ""),
                            "telefono" to "",
                            "rol" to "ADMIN",
                            "fechaCreacion" to System.currentTimeMillis(),
                            "activo" to true
                        )
                    ).await()
                }
                cargandoDatos = false
            } catch (e: Exception) {
                cargandoDatos = false
            }
        }
    }
    
    // Recargar estado de verificación periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000) // Cada 5 segundos
            currentUser?.reload()?.addOnCompleteListener {
                isEmailVerified = currentUser?.isEmailVerified ?: false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Avatar y nombre
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Text(
                            text = nombre.ifEmpty { stringResource(R.string.user) },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        if (email.isNotEmpty()) {
                            Text(
                                text = email,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        
                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.edit_profile))
                        }
                    }
                }
            }
            
            // Información personal
            item {
                Text(
                    text = "INFORMACIÓN PERSONAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            item {
                InfoCard(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.full_name),
                    value = nombre.ifEmpty { stringResource(R.string.not_configured) }
                )
            }
            
            item {
                InfoCard(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.email_address),
                    value = email.ifEmpty { stringResource(R.string.not_configured) }
                )
            }
            
            item {
                InfoCard(
                    icon = Icons.Default.Phone,
                    label = stringResource(R.string.phone),
                    value = telefono.ifEmpty { stringResource(R.string.not_configured) }
                )
            }
            
            // Información de cuenta
            item {
                Text(
                    text = "INFORMACIÓN DE CUENTA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                if (cargandoDatos) {
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
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Cargando datos...")
                        }
                    }
                } else {
                    InfoCard(
                        icon = Icons.Default.Badge,
                        label = stringResource(R.string.account_type),
                        value = rol
                    )
                }
            }
            
            item {
                InfoCard(
                    icon = Icons.Default.VerifiedUser,
                    label = stringResource(R.string.account_status),
                    value = if (isEmailVerified) stringResource(R.string.verified) else stringResource(R.string.not_verified)
                )
            }
            
            // Banner de verificación si no está verificada
            if (!isEmailVerified && currentUser != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.bsprestagil.ui.theme.WarningColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = com.example.bsprestagil.ui.theme.WarningColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.unverified_account),
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.bsprestagil.ui.theme.WarningColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Verifica tu email para mayor seguridad y acceso completo.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            Button(
                                onClick = {
                                    enviandoVerificacion = true
                                    verificationError = null
                                    currentUser.sendEmailVerification()
                                        .addOnCompleteListener { task ->
                                            enviandoVerificacion = false
                                            if (task.isSuccessful) {
                                                showVerificationMessage = true
                                            } else {
                                                verificationError = task.exception?.message ?: "Error desconocido"
                                            }
                                        }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !enviandoVerificacion
                            ) {
                                if (enviandoVerificacion) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (enviandoVerificacion) stringResource(R.string.sending) else stringResource(R.string.send_verification_email))
                            }
                        }
                    }
                }
            }
            
            item {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val creationDate = currentUser?.metadata?.creationTimestamp?.let { 
                    dateFormat.format(java.util.Date(it))
                } ?: stringResource(R.string.unknown)
                
                InfoCard(
                    icon = Icons.Default.CalendarMonth,
                    label = stringResource(R.string.member_since),
                    value = creationDate
                )
            }
            
            // Mensaje de éxito de perfil
            if (showSuccessMessage) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.bsprestagil.ui.theme.SuccessColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = com.example.bsprestagil.ui.theme.SuccessColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.profile_updated_successfully),
                                color = com.example.bsprestagil.ui.theme.SuccessColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(3000)
                        showSuccessMessage = false
                    }
                }
            }
            
            // Mensaje de verificación enviada
            if (showVerificationMessage) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.bsprestagil.ui.theme.SuccessColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = com.example.bsprestagil.ui.theme.SuccessColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¡Email enviado!",
                                    color = com.example.bsprestagil.ui.theme.SuccessColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Revisa tu bandeja de entrada y haz clic en el enlace de verificación.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(5000)
                        showVerificationMessage = false
                    }
                }
            }
            
            // Mensaje de error de verificación
            verificationError?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.error_sending_email),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = error,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(5000)
                        verificationError = null
                    }
                }
            }
            
            // Mensaje de error de actualización
            updateError?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.error_updating_profile),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = error,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(5000)
                        updateError = null
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Diálogo para editar perfil
    if (showEditDialog) {
        EditPerfilDialog(
            nombreActual = nombre,
            emailActual = email,
            telefonoActual = telefono,
            onDismiss = { showEditDialog = false },
            onConfirm = { nuevoNombre, nuevoEmail, nuevoTelefono ->
                showEditDialog = false
                updateError = null
                
                // Actualizar en Firebase
                scope.launch {
                    try {
                        currentUser?.let { user ->
                            // 1. Actualizar displayName en Firebase Auth
                            if (nuevoNombre != nombre && nuevoNombre.isNotBlank()) {
                                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(nuevoNombre)
                                    .build()
                                user.updateProfile(profileUpdates).await()
                            }
                            
                            // 2. Actualizar TODOS los datos en Firestore
                            val datosActualizados = hashMapOf(
                                "nombre" to nuevoNombre,
                                "email" to (user.email ?: ""),
                                "telefono" to nuevoTelefono,
                                "rol" to rol,
                                "ultimaActualizacion" to System.currentTimeMillis()
                            )
                            
                            firestore.collection("usuarios")
                                .document(user.uid)
                                .set(datosActualizados, com.google.firebase.firestore.SetOptions.merge())
                                .await()
                            
                            // 3. Recargar usuario para confirmar cambios
                            user.reload().await()
                            
                            // 4. Actualizar UI con datos guardados
                            nombre = nuevoNombre
                            telefono = nuevoTelefono
                            email = user.email ?: email
                            
                            showSuccessMessage = true
                        }
                    } catch (e: Exception) {
                        updateError = e.message ?: "Error al actualizar perfil"
                    }
                }
            }
        )
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EditPerfilDialog(
    nombreActual: String,
    emailActual: String,
    telefonoActual: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(nombreActual) }
    var email by remember { mutableStateOf(emailActual) }
    var telefono by remember { mutableStateOf(telefonoActual) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.full_name)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombre, email, telefono) }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

