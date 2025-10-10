package com.example.bsprestagil.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PhotoUtils {
    
    /**
     * Crea un archivo temporal para guardar la foto
     */
    fun crearArchivoTemporal(context: Context, garantiaId: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.filesDir, "garantias")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, "GARANTIA_${garantiaId}_${timeStamp}.jpg")
    }
    
    /**
     * Obtiene la URI del archivo usando FileProvider
     */
    fun obtenerUriParaFoto(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * Guarda la ruta de la foto
     */
    fun guardarRutaFoto(file: File): String {
        return file.absolutePath
    }
    
    /**
     * Obtiene la URI desde una ruta
     */
    fun obtenerUriDesdeRuta(ruta: String): Uri {
        return Uri.fromFile(File(ruta))
    }
    
    /**
     * Elimina una foto
     */
    fun eliminarFoto(ruta: String): Boolean {
        return try {
            val file = File(ruta)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtiene todas las fotos de una garantía
     */
    fun obtenerFotosGarantia(context: Context, garantiaId: String): List<String> {
        val storageDir = File(context.filesDir, "garantias")
        if (!storageDir.exists()) return emptyList()
        
        return storageDir.listFiles()
            ?.filter { it.name.startsWith("GARANTIA_$garantiaId") }
            ?.map { it.absolutePath }
            ?: emptyList()
    }
}

