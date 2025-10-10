package com.example.bsprestagil.firebase

import com.example.bsprestagil.data.database.entities.*
import com.example.bsprestagil.data.repository.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Sincroniza datos desde Firebase hacia Room
 * Esto permite que cambios en Firebase Console se reflejen en la app
 */
class FirebaseToRoomSync(
    private val clienteRepository: ClienteRepository,
    private val prestamoRepository: PrestamoRepository,
    private val pagoRepository: PagoRepository,
    private val cuotaRepository: CuotaRepository,
    private val garantiaRepository: GarantiaRepository,
    private val configuracionRepository: ConfiguracionRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Descarga clientes de Firebase y actualiza Room
     */
    suspend fun syncClientesFromFirebase(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("clientes").get().await()
            val clientesFirebase = snapshot.documents.map { doc ->
                doc.data?.let { data ->
                    ClienteEntity(
                        id = data["id"] as? String ?: doc.id,
                        nombre = data["nombre"] as? String ?: "",
                        telefono = data["telefono"] as? String ?: "",
                        direccion = data["direccion"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        fotoUrl = data["fotoUrl"] as? String ?: "",
                        referencias = (data["referencias"] as? List<*>)?.mapNotNull { ref ->
                            (ref as? Map<*, *>)?.let {
                                ReferenciaEntity(
                                    nombre = it["nombre"] as? String ?: "",
                                    telefono = it["telefono"] as? String ?: "",
                                    relacion = it["relacion"] as? String ?: ""
                                )
                            }
                        } ?: emptyList(),
                        fechaRegistro = data["fechaRegistro"] as? Long ?: System.currentTimeMillis(),
                        prestamosActivos = (data["prestamosActivos"] as? Long)?.toInt() ?: 0,
                        historialPagos = data["historialPagos"] as? String ?: "AL_DIA",
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            // Insertar o actualizar en Room
            clientesFirebase.forEach { cliente ->
                clienteRepository.insertCliente(cliente.copy(pendingSync = false))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga préstamos de Firebase y actualiza Room
     */
    suspend fun syncPrestamosFromFirebase(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("prestamos").get().await()
            val prestamosFirebase = snapshot.documents.map { doc ->
                doc.data?.let { data ->
                    PrestamoEntity(
                        id = data["id"] as? String ?: doc.id,
                        clienteId = data["clienteId"] as? String ?: "",
                        clienteNombre = data["clienteNombre"] as? String ?: "",
                        montoOriginal = (data["montoOriginal"] as? Number)?.toDouble() ?: 0.0,
                        capitalPendiente = (data["capitalPendiente"] as? Number)?.toDouble() ?: 0.0,
                        tasaInteresPorPeriodo = (data["tasaInteresPorPeriodo"] as? Number)?.toDouble() ?: 0.0,
                        frecuenciaPago = data["frecuenciaPago"] as? String ?: "MENSUAL",
                        tipoAmortizacion = data["tipoAmortizacion"] as? String ?: "FRANCES",
                        numeroCuotas = (data["numeroCuotas"] as? Long)?.toInt() ?: 0,
                        montoCuotaFija = (data["montoCuotaFija"] as? Number)?.toDouble() ?: 0.0,
                        cuotasPagadas = (data["cuotasPagadas"] as? Long)?.toInt() ?: 0,
                        garantiaId = data["garantiaId"] as? String,
                        fechaInicio = data["fechaInicio"] as? Long ?: System.currentTimeMillis(),
                        ultimaFechaPago = data["ultimaFechaPago"] as? Long ?: System.currentTimeMillis(),
                        estado = data["estado"] as? String ?: "ACTIVO",
                        totalInteresesPagados = (data["totalInteresesPagados"] as? Number)?.toDouble() ?: 0.0,
                        totalCapitalPagado = (data["totalCapitalPagado"] as? Number)?.toDouble() ?: 0.0,
                        totalMorasPagadas = (data["totalMorasPagadas"] as? Number)?.toDouble() ?: 0.0,
                        notas = data["notas"] as? String ?: "",
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            prestamosFirebase.forEach { prestamo ->
                prestamoRepository.insertPrestamo(prestamo.copy(pendingSync = false))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga pagos de Firebase y actualiza Room
     */
    suspend fun syncPagosFromFirebase(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("pagos").get().await()
            val pagosFirebase = snapshot.documents.map { doc ->
                doc.data?.let { data ->
                    PagoEntity(
                        id = data["id"] as? String ?: doc.id,
                        prestamoId = data["prestamoId"] as? String ?: "",
                        cuotaId = data["cuotaId"] as? String,
                        numeroCuota = (data["numeroCuota"] as? Long)?.toInt() ?: 0,
                        clienteId = data["clienteId"] as? String ?: "",
                        clienteNombre = data["clienteNombre"] as? String ?: "",
                        montoPagado = (data["montoPagado"] as? Number)?.toDouble() ?: 0.0,
                        montoAInteres = (data["montoAInteres"] as? Number)?.toDouble() ?: 0.0,
                        montoACapital = (data["montoACapital"] as? Number)?.toDouble() ?: 0.0,
                        montoMora = (data["montoMora"] as? Number)?.toDouble() ?: 0.0,
                        fechaPago = data["fechaPago"] as? Long ?: System.currentTimeMillis(),
                        diasTranscurridos = (data["diasTranscurridos"] as? Long)?.toInt() ?: 0,
                        interesCalculado = (data["interesCalculado"] as? Number)?.toDouble() ?: 0.0,
                        capitalPendienteAntes = (data["capitalPendienteAntes"] as? Number)?.toDouble() ?: 0.0,
                        capitalPendienteDespues = (data["capitalPendienteDespues"] as? Number)?.toDouble() ?: 0.0,
                        metodoPago = data["metodoPago"] as? String ?: "EFECTIVO",
                        recibidoPor = data["recibidoPor"] as? String ?: "",
                        notas = data["notas"] as? String ?: "",
                        reciboUrl = data["reciboUrl"] as? String ?: "",
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            pagosFirebase.forEach { pago ->
                pagoRepository.insertPago(pago.copy(pendingSync = false))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga cuotas de Firebase y actualiza Room
     */
    suspend fun syncCuotasFromFirebase(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("cuotas").get().await()
            val cuotasFirebase = snapshot.documents.map { doc ->
                doc.data?.let { data ->
                    CuotaEntity(
                        id = data["id"] as? String ?: doc.id,
                        prestamoId = data["prestamoId"] as? String ?: "",
                        numeroCuota = (data["numeroCuota"] as? Long)?.toInt() ?: 0,
                        fechaVencimiento = data["fechaVencimiento"] as? Long ?: System.currentTimeMillis(),
                        montoCuotaMinimo = (data["montoCuotaMinimo"] as? Number)?.toDouble() ?: 0.0,
                        capitalPendienteAlInicio = (data["capitalPendienteAlInicio"] as? Number)?.toDouble() ?: 0.0,
                        montoPagado = (data["montoPagado"] as? Number)?.toDouble() ?: 0.0,
                        montoAInteres = (data["montoAInteres"] as? Number)?.toDouble() ?: 0.0,
                        montoACapital = (data["montoACapital"] as? Number)?.toDouble() ?: 0.0,
                        montoMora = (data["montoMora"] as? Number)?.toDouble() ?: 0.0,
                        fechaPago = data["fechaPago"] as? Long,
                        estado = data["estado"] as? String ?: "PENDIENTE",
                        notas = data["notas"] as? String ?: "",
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            cuotasFirebase.forEach { cuota ->
                cuotaRepository.insertCuota(cuota.copy(pendingSync = false))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sincronización completa bidireccional
     */
    suspend fun fullSync(): Result<Unit> {
        return try {
            syncClientesFromFirebase()
            syncPrestamosFromFirebase()
            syncPagosFromFirebase()
            syncCuotasFromFirebase()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

