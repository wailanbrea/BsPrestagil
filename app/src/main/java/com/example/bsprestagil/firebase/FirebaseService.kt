package com.example.bsprestagil.firebase

import com.example.bsprestagil.data.database.entities.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseService {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    // Colecciones de Firestore
    companion object {
        private const val COLLECTION_CLIENTES = "clientes"
        private const val COLLECTION_PRESTAMOS = "prestamos"
        private const val COLLECTION_PAGOS = "pagos"
        private const val COLLECTION_CUOTAS = "cuotas"
        private const val COLLECTION_GARANTIAS = "garantias"
        private const val COLLECTION_USUARIOS = "usuarios"
        private const val COLLECTION_CONFIGURACION = "configuracion"
    }
    
    // ==================== CLIENTES ====================
    
    suspend fun syncCliente(cliente: ClienteEntity): Result<String> {
        return try {
            val docRef = if (cliente.firebaseId != null) {
                firestore.collection(COLLECTION_CLIENTES).document(cliente.firebaseId)
            } else {
                firestore.collection(COLLECTION_CLIENTES).document()
            }
            
            val clienteMap = mapOf(
                "id" to cliente.id,
                "nombre" to cliente.nombre,
                "telefono" to cliente.telefono,
                "direccion" to cliente.direccion,
                "email" to cliente.email,
                "fotoUrl" to cliente.fotoUrl,
                "referencias" to cliente.referencias.map { ref ->
                    mapOf(
                        "nombre" to ref.nombre,
                        "telefono" to ref.telefono,
                        "relacion" to ref.relacion
                    )
                },
                "fechaRegistro" to cliente.fechaRegistro,
                "prestamosActivos" to cliente.prestamosActivos,
                "historialPagos" to cliente.historialPagos,
                "lastSyncTime" to System.currentTimeMillis()
            )
            
            docRef.set(clienteMap, SetOptions.merge()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getClientes(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CLIENTES).get().await()
            val clientes = snapshot.documents.mapNotNull { it.data }
            Result.success(clientes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCliente(clienteId: String, firebaseId: String?): Result<Unit> {
        return try {
            if (firebaseId != null) {
                firestore.collection(COLLECTION_CLIENTES).document(firebaseId).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== PRÉSTAMOS ====================
    
    suspend fun syncPrestamo(prestamo: PrestamoEntity): Result<String> {
        return try {
            val docRef = if (prestamo.firebaseId != null) {
                firestore.collection(COLLECTION_PRESTAMOS).document(prestamo.firebaseId)
            } else {
                firestore.collection(COLLECTION_PRESTAMOS).document()
            }
            
            val prestamoMap = mapOf(
                "id" to prestamo.id,
                "clienteId" to prestamo.clienteId,
                "clienteNombre" to prestamo.clienteNombre,
                "cobradorId" to prestamo.cobradorId,
                "cobradorNombre" to prestamo.cobradorNombre,
                "montoOriginal" to prestamo.montoOriginal,
                "capitalPendiente" to prestamo.capitalPendiente,
                "tasaInteresPorPeriodo" to prestamo.tasaInteresPorPeriodo,
                "frecuenciaPago" to prestamo.frecuenciaPago,
                "tipoAmortizacion" to prestamo.tipoAmortizacion,
                "numeroCuotas" to prestamo.numeroCuotas,
                "montoCuotaFija" to prestamo.montoCuotaFija,
                "cuotasPagadas" to prestamo.cuotasPagadas,
                "garantiaId" to prestamo.garantiaId,
                "fechaInicio" to prestamo.fechaInicio,
                "ultimaFechaPago" to prestamo.ultimaFechaPago,
                "estado" to prestamo.estado,
                "totalInteresesPagados" to prestamo.totalInteresesPagados,
                "totalCapitalPagado" to prestamo.totalCapitalPagado,
                "totalMorasPagadas" to prestamo.totalMorasPagadas,
                "notas" to prestamo.notas,
                "lastSyncTime" to System.currentTimeMillis()
            )
            
            docRef.set(prestamoMap, SetOptions.merge()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPrestamos(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_PRESTAMOS).get().await()
            val prestamos = snapshot.documents.mapNotNull { it.data }
            Result.success(prestamos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== PAGOS ====================
    
    suspend fun syncPago(pago: PagoEntity): Result<String> {
        return try {
            val docRef = if (pago.firebaseId != null) {
                firestore.collection(COLLECTION_PAGOS).document(pago.firebaseId)
            } else {
                firestore.collection(COLLECTION_PAGOS).document()
            }
            
            val pagoMap = mapOf(
                "id" to pago.id,
                "prestamoId" to pago.prestamoId,
                "cuotaId" to pago.cuotaId,
                "numeroCuota" to pago.numeroCuota,
                "clienteId" to pago.clienteId,
                "clienteNombre" to pago.clienteNombre,
                "montoPagado" to pago.montoPagado,
                "montoAInteres" to pago.montoAInteres,
                "montoACapital" to pago.montoACapital,
                "montoMora" to pago.montoMora,
                "fechaPago" to pago.fechaPago,
                "diasTranscurridos" to pago.diasTranscurridos,
                "interesCalculado" to pago.interesCalculado,
                "capitalPendienteAntes" to pago.capitalPendienteAntes,
                "capitalPendienteDespues" to pago.capitalPendienteDespues,
                "metodoPago" to pago.metodoPago,
                "recibidoPor" to pago.recibidoPor,
                "notas" to pago.notas,
                "reciboUrl" to pago.reciboUrl,
                "lastSyncTime" to System.currentTimeMillis()
            )
            
            docRef.set(pagoMap, SetOptions.merge()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPagos(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_PAGOS).get().await()
            val pagos = snapshot.documents.mapNotNull { it.data }
            Result.success(pagos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== CUOTAS ====================
    
    suspend fun syncCuota(cuota: CuotaEntity): Result<String> {
        return try {
            val docRef = if (cuota.firebaseId != null) {
                firestore.collection(COLLECTION_CUOTAS).document(cuota.firebaseId)
            } else {
                firestore.collection(COLLECTION_CUOTAS).document()
            }
            
            val cuotaMap = mapOf(
                "id" to cuota.id,
                "prestamoId" to cuota.prestamoId,
                "numeroCuota" to cuota.numeroCuota,
                "fechaVencimiento" to cuota.fechaVencimiento,
                "montoCuotaMinimo" to cuota.montoCuotaMinimo,
                "capitalPendienteAlInicio" to cuota.capitalPendienteAlInicio,
                "montoPagado" to cuota.montoPagado,
                "montoAInteres" to cuota.montoAInteres,
                "montoACapital" to cuota.montoACapital,
                "montoMora" to cuota.montoMora,
                "fechaPago" to cuota.fechaPago,
                "estado" to cuota.estado,
                "notas" to cuota.notas,
                "lastSyncTime" to System.currentTimeMillis()
            )
            
            docRef.set(cuotaMap, SetOptions.merge()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCuotas(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CUOTAS).get().await()
            val cuotas = snapshot.documents.mapNotNull { it.data }
            Result.success(cuotas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== GARANTÍAS ====================
    
    suspend fun syncGarantia(garantia: GarantiaEntity): Result<String> {
        return try {
            val docRef = if (garantia.firebaseId != null) {
                firestore.collection(COLLECTION_GARANTIAS).document(garantia.firebaseId)
            } else {
                firestore.collection(COLLECTION_GARANTIAS).document()
            }
            
            val garantiaMap = mapOf(
                "id" to garantia.id,
                "tipo" to garantia.tipo,
                "descripcion" to garantia.descripcion,
                "valorEstimado" to garantia.valorEstimado,
                "fotosUrls" to garantia.fotosUrls,
                "estado" to garantia.estado,
                "fechaRegistro" to garantia.fechaRegistro,
                "notas" to garantia.notas,
                "lastSyncTime" to System.currentTimeMillis()
            )
            
            docRef.set(garantiaMap, SetOptions.merge()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getGarantias(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_GARANTIAS).get().await()
            val garantias = snapshot.documents.mapNotNull { it.data }
            Result.success(garantias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== CONFIGURACIÓN ====================
    
    suspend fun syncConfiguracion(config: ConfiguracionEntity): Result<String> {
        return try {
            val docRef = firestore.collection(COLLECTION_CONFIGURACION).document("config")
            
            val configMap = mapOf(
                "tasaInteresBase" to config.tasaInteresBase,
                "tasaMoraBase" to config.tasaMoraBase,
                "nombreNegocio" to config.nombreNegocio,
                "telefonoNegocio" to config.telefonoNegocio,
                "direccionNegocio" to config.direccionNegocio,
                "logoUrl" to config.logoUrl,
                "mensajeRecibo" to config.mensajeRecibo,
                "notificacionesActivas" to config.notificacionesActivas,
                "envioWhatsApp" to config.envioWhatsApp,
                "envioSMS" to config.envioSMS,
                "lastSyncTime" to System.currentTimeMillis()
            )
            
            docRef.set(configMap, SetOptions.merge()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getConfiguracion(): Result<Map<String, Any>?> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CONFIGURACION)
                .document("config").get().await()
            Result.success(snapshot.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

