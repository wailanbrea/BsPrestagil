package com.example.bsprestagil.data.models

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: RolUsuario = RolUsuario.ADMIN,
    val fechaCreacion: Long = System.currentTimeMillis()
)

enum class RolUsuario {
    ADMIN,
    COBRADOR
}

