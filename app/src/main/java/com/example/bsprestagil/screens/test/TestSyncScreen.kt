package com.example.bsprestagil.screens.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.data.database.AppDatabase
import com.example.bsprestagil.data.database.entities.*
import com.example.bsprestagil.data.repository.*
import com.example.bsprestagil.data.models.FrecuenciaPago
import com.example.bsprestagil.sync.SyncManager
import com.example.bsprestagil.utils.AmortizacionUtils
import com.example.bsprestagil.utils.CronogramaUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSyncScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val database = remember { AppDatabase.getDatabase(context) }
    val clienteRepository = remember { ClienteRepository(database.clienteDao()) }
    val prestamoRepository = remember { PrestamoRepository(database.prestamoDao()) }
    val pagoRepository = remember { PagoRepository(database.pagoDao()) }
    val cuotaRepository = remember { CuotaRepository(database.cuotaDao()) }
    
    var mensaje by remember { mutableStateOf("Listo para probar sincronización") }
    var loading by remember { mutableStateOf(false) }
    var clienteIdCreado by remember { mutableStateOf<String?>(null) }
    var prestamoIdCreado by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Prueba de Sincronización",
                onNavigateBack = onNavigateBack
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
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Prueba de Sincronización",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crea datos de prueba y sincroniza con Firebase",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = mensaje,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = if (mensaje.contains("Error")) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
            
            item {
                Button(
                    onClick = {
                        loading = true
                        scope.launch {
                            try {
                                // Crear cliente de prueba
                                val clienteId = clienteRepository.insertCliente(
                                    ClienteEntity(
                                        id = "",
                                        nombre = "Juan Pérez González",
                                        telefono = "+52 999 123 4567",
                                        direccion = "Calle Principal #123",
                                        email = "juan@example.com",
                                        fotoUrl = "",
                                        referencias = listOf(
                                            ReferenciaEntity("María Pérez", "+52 999 234 5678", "Hermana")
                                        ),
                                        fechaRegistro = System.currentTimeMillis(),
                                        prestamosActivos = 0,
                                        historialPagos = "AL_DIA"
                                    )
                                )
                                clienteIdCreado = clienteId
                                mensaje = "✅ Cliente creado con ID: $clienteId"
                                loading = false
                            } catch (e: Exception) {
                                mensaje = "❌ Error: ${e.message}"
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !loading
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (clienteIdCreado != null) "✅ Cliente creado" else "1. Crear Cliente de Prueba")
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (clienteIdCreado == null) {
                            mensaje = "⚠️ Primero debes crear un cliente"
                            return@Button
                        }
                        loading = true
                        scope.launch {
                            try {
                                // Calcular cuota fija con Sistema Francés
                                val montoCuotaFija = AmortizacionUtils.calcularCuotaFija(
                                    capital = 10000.0,
                                    tasaInteresPorPeriodo = 10.0,
                                    numeroCuotas = 12
                                )
                                
                                val fechaInicio = System.currentTimeMillis()
                                
                                val prestamoId = prestamoRepository.insertPrestamo(
                                    PrestamoEntity(
                                        id = "",
                                        clienteId = clienteIdCreado!!,
                                        clienteNombre = "Juan Pérez González",
                                        montoOriginal = 10000.0,
                                        capitalPendiente = 10000.0,
                                        tasaInteresPorPeriodo = 10.0,
                                        frecuenciaPago = "MENSUAL",
                                        numeroCuotas = 12,
                                        montoCuotaFija = montoCuotaFija,
                                        cuotasPagadas = 0,
                                        garantiaId = null,
                                        fechaInicio = fechaInicio,
                                        ultimaFechaPago = fechaInicio,
                                        estado = "ACTIVO",
                                        totalInteresesPagados = 0.0,
                                        totalCapitalPagado = 0.0,
                                        totalMorasPagadas = 0.0,
                                        notas = "Préstamo de prueba - 10% mensual x 12 cuotas - Sistema Francés"
                                    )
                                )
                                
                                // Generar cronograma de cuotas (Sistema Francés)
                                val cronograma = CronogramaUtils.generarCronograma(
                                    prestamoId = prestamoId,
                                    montoOriginal = 10000.0,
                                    tasaInteresPorPeriodo = 10.0,
                                    frecuenciaPago = FrecuenciaPago.MENSUAL,
                                    numeroCuotas = 12,
                                    fechaInicio = fechaInicio
                                )
                                
                                // Insertar todas las cuotas
                                cuotaRepository.insertCuotas(cronograma)
                                
                                prestamoIdCreado = prestamoId
                                mensaje = "✅ Préstamo creado: $${String.format("%.2f", montoCuotaFija)}/mes x 12 cuotas + cronograma completo"
                                loading = false
                            } catch (e: Exception) {
                                mensaje = "❌ Error: ${e.message}"
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !loading && clienteIdCreado != null
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (prestamoIdCreado != null) "✅ Préstamo creado" else "2. Crear Préstamo de Prueba")
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (prestamoIdCreado == null) {
                            mensaje = "⚠️ Primero debes crear un préstamo"
                            return@Button
                        }
                        loading = true
                        scope.launch {
                            try {
                                val pagoId = pagoRepository.insertPago(
                                    PagoEntity(
                                        id = "",
                                        prestamoId = prestamoIdCreado!!,
                                        cuotaId = null,
                                        numeroCuota = 1,
                                        clienteId = clienteIdCreado!!,
                                        clienteNombre = "Juan Pérez González",
                                        montoPagado = 1500.0,
                                        montoAInteres = 1000.0,
                                        montoACapital = 500.0,
                                        montoMora = 0.0,
                                        fechaPago = System.currentTimeMillis(),
                                        diasTranscurridos = 30,
                                        interesCalculado = 1000.0,
                                        capitalPendienteAntes = 10000.0,
                                        capitalPendienteDespues = 9500.0,
                                        metodoPago = "EFECTIVO",
                                        recibidoPor = "Admin",
                                        notas = "Pago cuota 1 - $1000 interés + $500 capital",
                                        reciboUrl = ""
                                    )
                                )
                                mensaje = "✅ Pago registrado con ID: $pagoId"
                                loading = false
                            } catch (e: Exception) {
                                mensaje = "❌ Error: ${e.message}"
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !loading && prestamoIdCreado != null
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("3. Registrar Pago de Prueba")
                }
            }
            
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            item {
                Button(
                    onClick = {
                        loading = true
                        mensaje = "🔄 Sincronizando con Firebase..."
                        SyncManager.forceSyncNow(context)
                        scope.launch {
                            kotlinx.coroutines.delay(3000)
                            mensaje = "✅ Sincronización iniciada. Revisa Firebase Console en unos segundos."
                            loading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔥 FORZAR SINCRONIZACIÓN AHORA")
                }
            }
            
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
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "📋 Instrucciones:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. Presiona los botones 1, 2 y 3 para crear datos", fontSize = 14.sp)
                        Text("2. Presiona 'FORZAR SINCRONIZACIÓN'", fontSize = 14.sp)
                        Text("3. Ve a Firebase Console > Firestore Database", fontSize = 14.sp)
                        Text("4. Verás las colecciones: clientes, prestamos, pagos", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⏱️ La sincronización automática ocurre cada 15 minutos",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            if (loading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


