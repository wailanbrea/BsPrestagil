package com.example.bsprestagil.firebase

import com.example.bsprestagil.data.database.entities.*
import com.example.bsprestagil.data.repository.*
import com.example.bsprestagil.utils.AuthUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Sincroniza datos desde Firebase hacia Room
 * NUEVO: Ahora filtra por adminId para multi-tenancy
 */
class FirebaseToRoomSync(
    private val clienteRepository: ClienteRepository,
    private val prestamoRepository: PrestamoRepository,
    private val pagoRepository: PagoRepository,
    private val cuotaRepository: CuotaRepository,
    private val garantiaRepository: GarantiaRepository,
    private val configuracionRepository: ConfiguracionRepository,
    private val usuarioRepository: UsuarioRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    
    // Acceso directo a los DAOs para evitar la lógica de los repositorios que marca pendingSync = true
    private val clienteDao = clienteRepository.dao
    private val prestamoDao = prestamoRepository.dao
    private val pagoDao = pagoRepository.dao
    private val cuotaDao = cuotaRepository.dao
    private val usuarioDao = usuarioRepository.usuarioDao
    
    /**
     * Descarga clientes de Firebase y actualiza Room
     * NUEVO: Solo descarga clientes de la empresa actual (filtrado por adminId)
     */
    suspend fun syncClientesFromFirebase(): Result<Unit> {
        return try {
            // NUEVO: Obtener adminId del usuario actual
            val adminId = AuthUtils.getCurrentAdminId()
            
            // NUEVO: Filtrar por adminId en la query de Firestore
            val snapshot = firestore.collection("clientes")
                .whereEqualTo("adminId", adminId)
                .get()
                .await()
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
                        adminId = data["adminId"] as? String ?: adminId, // NUEVO: Multi-tenant
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            // Insertar o actualizar en Room directamente usando el DAO
            // para evitar la lógica del repositorio que marca pendingSync = true
            clientesFirebase.forEach { cliente ->
                clienteDao.insertCliente(cliente)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga préstamos de Firebase y actualiza Room
     * NUEVO: Solo descarga préstamos de la empresa actual
     */
    suspend fun syncPrestamosFromFirebase(): Result<Unit> {
        return try {
            val adminId = AuthUtils.getCurrentAdminId()
            
            val snapshot = firestore.collection("prestamos")
                .whereEqualTo("adminId", adminId)
                .get()
                .await()
            val prestamosFirebase = snapshot.documents.map { doc ->
                doc.data?.let { data ->
                    PrestamoEntity(
                        id = data["id"] as? String ?: doc.id,
                        clienteId = data["clienteId"] as? String ?: "",
                        clienteNombre = data["clienteNombre"] as? String ?: "",
                        cobradorId = data["cobradorId"] as? String,
                        cobradorNombre = data["cobradorNombre"] as? String,
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
                        adminId = data["adminId"] as? String ?: adminId, // NUEVO: Multi-tenant
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            // Insertar o actualizar en Room directamente usando el DAO
            prestamosFirebase.forEach { prestamo ->
                prestamoDao.insertPrestamo(prestamo)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga pagos de Firebase y actualiza Room
     * NUEVO: Solo descarga pagos de la empresa actual
     */
    suspend fun syncPagosFromFirebase(): Result<Unit> {
        return try {
            val adminId = AuthUtils.getCurrentAdminId()
            
            val snapshot = firestore.collection("pagos")
                .whereEqualTo("adminId", adminId)
                .get()
                .await()
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
                        adminId = data["adminId"] as? String ?: adminId, // NUEVO: Multi-tenant
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            // Insertar o actualizar en Room directamente usando el DAO
            pagosFirebase.forEach { pago ->
                pagoDao.insertPago(pago)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga cuotas de Firebase y actualiza Room
     * NUEVO: Solo descarga cuotas de la empresa actual
     */
    suspend fun syncCuotasFromFirebase(): Result<Unit> {
        return try {
            val adminId = AuthUtils.getCurrentAdminId()
            
            val snapshot = firestore.collection("cuotas")
                .whereEqualTo("adminId", adminId)
                .get()
                .await()
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
                        adminId = data["adminId"] as? String ?: adminId, // NUEVO: Multi-tenant
                        pendingSync = false,
                        lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            // Insertar o actualizar en Room directamente usando el DAO
            cuotasFirebase.forEach { cuota ->
                cuotaDao.insertCuota(cuota)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga usuarios de Firebase y actualiza Room
     * NUEVO: Solo descarga usuarios de la empresa actual
     */
    suspend fun syncUsuariosFromFirebase(): Result<Unit> {
        return try {
            val adminId = AuthUtils.getCurrentAdminId()
            
            val snapshot = firestore.collection("usuarios")
                .whereEqualTo("adminId", adminId)
                .get()
                .await()
            val usuariosFirebase = snapshot.documents.map { doc ->
                doc.data?.let { data ->
                    UsuarioEntity(
                        id = doc.id,
                        nombre = data["nombre"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        telefono = data["telefono"] as? String ?: "",
                        rol = data["rol"] as? String ?: "COBRADOR",
                        activo = data["activo"] as? Boolean ?: true,
                        fechaCreacion = data["fechaCreacion"] as? Long ?: System.currentTimeMillis(),
                        adminId = data["adminId"] as? String ?: adminId, // NUEVO: Multi-tenant
                        porcentajeComision = (data["porcentajeComision"] as? Number)?.toFloat() ?: 3.0f,
                        totalComisionesGeneradas = (data["totalComisionesGeneradas"] as? Number)?.toDouble() ?: 0.0,
                        totalComisionesPagadas = (data["totalComisionesPagadas"] as? Number)?.toDouble() ?: 0.0,
                        ultimoPagoComision = (data["ultimoPagoComision"] as? Long) ?: 0L,
                        pendingSync = false,
                        lastSyncTime = System.currentTimeMillis(),
                        firebaseId = doc.id
                    )
                }
            }.filterNotNull()
            
            // Insertar o actualizar en Room directamente usando el DAO
            usuariosFirebase.forEach { usuario ->
                usuarioDao.insertUsuario(usuario)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Descarga configuración de Firebase y actualiza Room
     */
    suspend fun syncConfiguracionFromFirebase(): Result<Unit> {
        return try {
            val snapshot = firestore.collection("configuracion")
                .document("config")
                .get()
                .await()
            
            snapshot.data?.let { data ->
                val configuracionEntity = ConfiguracionEntity(
                    id = 1,
                    // Configuración general
                    tasaInteresBase = (data["tasaInteresBase"] as? Number)?.toDouble() ?: 10.0,
                    tasaMoraBase = (data["tasaMoraBase"] as? Number)?.toDouble() ?: 5.0,
                    nombreNegocio = data["nombreNegocio"] as? String ?: "Prestágil",
                    telefonoNegocio = data["telefonoNegocio"] as? String ?: "",
                    direccionNegocio = data["direccionNegocio"] as? String ?: "",
                    logoUrl = data["logoUrl"] as? String ?: "",
                    mensajeRecibo = data["mensajeRecibo"] as? String ?: "Gracias por su pago",
                    notificacionesActivas = data["notificacionesActivas"] as? Boolean ?: true,
                    envioWhatsApp = data["envioWhatsApp"] as? Boolean ?: true,
                    envioSMS = data["envioSMS"] as? Boolean ?: false,
                    // Configuración de factura/contrato
                    rncEmpresa = data["rncEmpresa"] as? String ?: "",
                    emailEmpresa = data["emailEmpresa"] as? String ?: "",
                    sitioWebEmpresa = data["sitioWebEmpresa"] as? String ?: "",
                    tituloContrato = data["tituloContrato"] as? String ?: "CONTRATO DE PRÉSTAMO PERSONAL",
                    encabezadoContrato = data["encabezadoContrato"] as? String ?: "Entre las partes aquí identificadas, se establece el siguiente contrato de préstamo bajo los términos y condiciones que se detallan:",
                    terminosCondiciones = data["terminosCondiciones"] as? String ?: "1. El PRESTATARIO se compromete a pagar el préstamo en las fechas acordadas.\n2. Los intereses se calculan según el sistema de amortización elegido.\n3. El no pago genera intereses moratorios según la tasa establecida.\n4. La garantía quedará retenida hasta el pago total del préstamo.",
                    clausulasPenalizacion = data["clausulasPenalizacion"] as? String ?: "En caso de incumplimiento, se aplicará la tasa de mora establecida sobre el saldo pendiente.",
                    clausulasGarantia = data["clausulasGarantia"] as? String ?: "El PRESTATARIO entrega como garantía los bienes descritos, los cuales quedarán bajo custodia del PRESTAMISTA hasta la liquidación total del préstamo.",
                    clausulasLegales = data["clausulasLegales"] as? String ?: "Este contrato se rige por las leyes vigentes. Cualquier disputa será resuelta en los tribunales competentes.",
                    pieContrato = data["pieContrato"] as? String ?: "Gracias por confiar en nosotros. Para consultas, contáctenos a los números indicados.",
                    mensajeAdicionalContrato = data["mensajeAdicionalContrato"] as? String ?: "",
                    mostrarTablaAmortizacion = data["mostrarTablaAmortizacion"] as? Boolean ?: true,
                    mostrarDesglosePago = data["mostrarDesglosePago"] as? Boolean ?: true,
                    incluirEspacioFirmas = data["incluirEspacioFirmas"] as? Boolean ?: true,
                    numeroCopiasContrato = (data["numeroCopiasContrato"] as? Long)?.toInt() ?: 2,
                    // Sincronización
                    pendingSync = false,
                    lastSyncTime = data["lastSyncTime"] as? Long ?: System.currentTimeMillis(),
                    firebaseId = "config"
                )
                
                configuracionRepository.dao.insertConfiguracion(configuracionEntity)
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
            syncConfiguracionFromFirebase()
            syncUsuariosFromFirebase()
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

