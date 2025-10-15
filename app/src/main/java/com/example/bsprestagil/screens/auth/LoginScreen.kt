package com.example.bsprestagil.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.utils.BiometricHelper
import com.example.bsprestagil.utils.BiometricStatus
import com.example.bsprestagil.utils.SecurePreferences
import com.example.bsprestagil.viewmodels.AuthState
import com.example.bsprestagil.viewmodels.AuthViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var enableBiometric by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    // Verificar si hay credenciales guardadas y capacidad biométrica
    val biometricStatus = remember { BiometricHelper.canAuthenticate(context) }
    val hasBiometric = biometricStatus == BiometricStatus.AVAILABLE
    
    // Cargar credenciales guardadas al inicio
    LaunchedEffect(Unit) {
        if (securePrefs.hasCredentials()) {
            email = securePrefs.savedEmail ?: ""
            rememberMe = securePrefs.rememberMe
            enableBiometric = securePrefs.biometricEnabled
        }
    }
    
    // Observar el estado de autenticación
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                val rol = state.rol
                Log.d("LoginScreen", "✅ Login exitoso. Rol: $rol")
                
                // Guardar credenciales si está marcado "Recordarme"
                if (rememberMe && email.isNotBlank() && password.isNotBlank()) {
                    securePrefs.saveCredentials(email, password, enableBiometric)
                    Log.d("LoginScreen", "💾 Credenciales guardadas. Biométrico: $enableBiometric")
                } else if (!rememberMe) {
                    securePrefs.clearCredentials()
                }
                
                // Verificar si es el primer login ANTES de resetear el estado
                try {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userDoc = firestore.collection("usuarios")
                            .document(userId)
                            .get()
                            .await()
                        
                        val primerLogin = userDoc.getBoolean("primerLogin") ?: false
                        
                        // Resetear el estado DESPUÉS de obtener los datos pero ANTES de navegar
                        authViewModel.resetState()
                        
                        if (primerLogin) {
                            // Redirigir a cambiar contraseña
                            Log.d("LoginScreen", "🔑 Primer login detectado, navegando a cambiar contraseña")
                            navController.navigate(Screen.CambiarPassword.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            // Navegar según el rol
                            when (rol) {
                                "COBRADOR" -> {
                                    Log.d("LoginScreen", "🚀 Navegando a CobradorDashboard")
                                    navController.navigate(Screen.CobradorDashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                                "ADMIN" -> {
                                    Log.d("LoginScreen", "🚀 Navegando a Dashboard (Admin)")
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                                else -> {
                                    Log.e("LoginScreen", "❌ Rol desconocido o null: $rol")
                                }
                            }
                        }
                    } else {
                        // No hay usuario autenticado, resetear y no navegar
                        authViewModel.resetState()
                        Log.e("LoginScreen", "❌ No hay usuario autenticado")
                    }
                } catch (e: Exception) {
                    // En caso de error, resetear el estado
                    authViewModel.resetState()
                    Log.e("LoginScreen", "❌ Error al verificar primer login: ${e.message}")
                }
            }
            else -> {}
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo y título
            Text(
                text = "Prestágil",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Gestiona tus préstamos fácilmente",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recordarme
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { 
                            rememberMe = it
                            if (!it) enableBiometric = false
                        }
                    )
                    Text("Recordarme")
                }
                
                // Olvidaste tu contraseña
                TextButton(
                    onClick = { /* TODO: Implementar recuperación de contraseña */ }
                ) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }
            
            // Habilitar biométrico (solo si "Recordarme" está activo y hay biométrico disponible)
            if (rememberMe && hasBiometric) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = enableBiometric,
                        onCheckedChange = { enableBiometric = it }
                    )
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Usar huella dactilar para iniciar sesión")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mostrar error si existe
            if (authState is AuthState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Botón de Login
            Button(
                onClick = { authViewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Botón de autenticación biométrica (solo si hay credenciales guardadas y biométrico habilitado)
            if (securePrefs.biometricEnabled && hasBiometric && securePrefs.hasCredentials()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        activity?.let {
                            BiometricHelper.showBiometricPrompt(
                                activity = it,
                                title = "Iniciar sesión con huella",
                                subtitle = "Usa tu huella dactilar",
                                negativeButtonText = "Cancelar",
                                onSuccess = {
                                    // Usar credenciales guardadas
                                    val savedEmail = securePrefs.savedEmail ?: ""
                                    val savedPassword = securePrefs.savedPassword ?: ""
                                    if (savedEmail.isNotBlank() && savedPassword.isNotBlank()) {
                                        authViewModel.login(savedEmail, savedPassword)
                                    }
                                },
                                onError = { error ->
                                    Log.e("LoginScreen", "Error biométrico: $error")
                                },
                                onFailed = {
                                    Log.w("LoginScreen", "Autenticación biométrica fallida")
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Usar huella dactilar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Registrarse
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                TextButton(onClick = { 
                    navController.navigate(Screen.Register.route)
                }) {
                    Text("Regístrate")
                }
            }
        }
    }
}

