package com.example.bsprestagil.data.models

data class Configuracion(
    val tasaInteresBase: Double = 10.0,
    val tasaMoraBase: Double = 5.0,
    val nombreNegocio: String = "Prestágil",
    val telefonoNegocio: String = "",
    val direccionNegocio: String = "",
    val logoUrl: String = "",
    val mensajeRecibo: String = "Gracias por su pago",
    val notificacionesActivas: Boolean = true,
    val envioWhatsApp: Boolean = true,
    val envioSMS: Boolean = false
)

