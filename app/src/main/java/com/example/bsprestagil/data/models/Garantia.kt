package com.example.bsprestagil.data.models

data class Garantia(
    val id: String = "",
    val tipo: TipoGarantia = TipoGarantia.OTRO,
    val descripcion: String = "",
    val valorEstimado: Double = 0.0,
    val fotosUrls: List<String> = emptyList(),
    val estado: EstadoGarantia = EstadoGarantia.RETENIDA,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val notas: String = ""
)

enum class TipoGarantia {
    VEHICULO,
    ELECTRODOMESTICO,
    ELECTRONICO,
    JOYA,
    MUEBLE,
    OTRO
}

enum class EstadoGarantia {
    RETENIDA,
    DEVUELTA,
    EJECUTADA
}

