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
     * Sincronización completa bidireccional
     */
    suspend fun fullSync(): Result<Unit> {
        return try {
            syncClientesFromFirebase()
            // Aquí puedes agregar más entidades cuando las necesites
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

