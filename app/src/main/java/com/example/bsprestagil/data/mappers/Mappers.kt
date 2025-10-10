package com.example.bsprestagil.data.mappers

import com.example.bsprestagil.data.database.entities.*
import com.example.bsprestagil.data.models.*

// Cliente Mappers
fun ClienteEntity.toCliente() = Cliente(
    id = id,
    nombre = nombre,
    telefono = telefono,
    direccion = direccion,
    email = email,
    fotoUrl = fotoUrl,
    referencias = referencias.map { it.toReferencia() },
    fechaRegistro = fechaRegistro,
    prestamosActivos = prestamosActivos,
    historialPagos = when (historialPagos) {
        "AL_DIA" -> EstadoPagos.AL_DIA
        "ATRASADO" -> EstadoPagos.ATRASADO
        "MOROSO" -> EstadoPagos.MOROSO
        else -> EstadoPagos.AL_DIA
    }
)

fun Cliente.toEntity() = ClienteEntity(
    id = id,
    nombre = nombre,
    telefono = telefono,
    direccion = direccion,
    email = email,
    fotoUrl = fotoUrl,
    referencias = referencias.map { it.toEntity() },
    fechaRegistro = fechaRegistro,
    prestamosActivos = prestamosActivos,
    historialPagos = historialPagos.name
)

fun ReferenciaEntity.toReferencia() = Referencia(
    nombre = nombre,
    telefono = telefono,
    relacion = relacion
)

fun Referencia.toEntity() = ReferenciaEntity(
    nombre = nombre,
    telefono = telefono,
    relacion = relacion
)

// Préstamo Mappers
fun PrestamoEntity.toPrestamo() = Prestamo(
    id = id,
    clienteId = clienteId,
    clienteNombre = clienteNombre,
    montoOriginal = montoOriginal,
    capitalPendiente = capitalPendiente,
    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
    frecuenciaPago = when (frecuenciaPago) {
        "DIARIO" -> FrecuenciaPago.DIARIO
        "SEMANAL" -> FrecuenciaPago.SEMANAL
        "QUINCENAL" -> FrecuenciaPago.QUINCENAL
        "MENSUAL" -> FrecuenciaPago.MENSUAL
        else -> FrecuenciaPago.MENSUAL
    },
    numeroCuotas = numeroCuotas,
    cuotasPagadas = cuotasPagadas,
    garantiaId = garantiaId,
    fechaInicio = fechaInicio,
    ultimaFechaPago = ultimaFechaPago,
    estado = when (estado) {
        "ACTIVO" -> EstadoPrestamo.ACTIVO
        "ATRASADO" -> EstadoPrestamo.ATRASADO
        "COMPLETADO" -> EstadoPrestamo.COMPLETADO
        "CANCELADO" -> EstadoPrestamo.CANCELADO
        else -> EstadoPrestamo.ACTIVO
    },
    totalInteresesPagados = totalInteresesPagados,
    totalCapitalPagado = totalCapitalPagado,
    totalMorasPagadas = totalMorasPagadas,
    notas = notas
)

fun Prestamo.toEntity() = PrestamoEntity(
    id = id,
    clienteId = clienteId,
    clienteNombre = clienteNombre,
    montoOriginal = montoOriginal,
    capitalPendiente = capitalPendiente,
    tasaInteresPorPeriodo = tasaInteresPorPeriodo,
    frecuenciaPago = frecuenciaPago.name,
    numeroCuotas = numeroCuotas,
    cuotasPagadas = cuotasPagadas,
    garantiaId = garantiaId,
    fechaInicio = fechaInicio,
    ultimaFechaPago = ultimaFechaPago,
    estado = estado.name,
    totalInteresesPagados = totalInteresesPagados,
    totalCapitalPagado = totalCapitalPagado,
    totalMorasPagadas = totalMorasPagadas,
    notas = notas
)

// Pago Mappers
fun PagoEntity.toPago() = Pago(
    id = id,
    prestamoId = prestamoId,
    cuotaId = cuotaId,
    numeroCuota = numeroCuota,
    clienteId = clienteId,
    clienteNombre = clienteNombre,
    montoPagado = montoPagado,
    montoAInteres = montoAInteres,
    montoACapital = montoACapital,
    montoMora = montoMora,
    fechaPago = fechaPago,
    diasTranscurridos = diasTranscurridos,
    interesCalculado = interesCalculado,
    capitalPendienteAntes = capitalPendienteAntes,
    capitalPendienteDespues = capitalPendienteDespues,
    metodoPago = when (metodoPago) {
        "EFECTIVO" -> MetodoPago.EFECTIVO
        "TRANSFERENCIA" -> MetodoPago.TRANSFERENCIA
        "TARJETA" -> MetodoPago.TARJETA
        "OTRO" -> MetodoPago.OTRO
        else -> MetodoPago.EFECTIVO
    },
    recibidoPor = recibidoPor,
    notas = notas,
    reciboUrl = reciboUrl
)

fun Pago.toEntity() = PagoEntity(
    id = id,
    prestamoId = prestamoId,
    cuotaId = cuotaId,
    numeroCuota = numeroCuota,
    clienteId = clienteId,
    clienteNombre = clienteNombre,
    montoPagado = montoPagado,
    montoAInteres = montoAInteres,
    montoACapital = montoACapital,
    montoMora = montoMora,
    fechaPago = fechaPago,
    diasTranscurridos = diasTranscurridos,
    interesCalculado = interesCalculado,
    capitalPendienteAntes = capitalPendienteAntes,
    capitalPendienteDespues = capitalPendienteDespues,
    metodoPago = metodoPago.name,
    recibidoPor = recibidoPor,
    notas = notas,
    reciboUrl = reciboUrl
)

// Cuota Mappers
fun CuotaEntity.toCuota() = Cuota(
    id = id,
    prestamoId = prestamoId,
    numeroCuota = numeroCuota,
    fechaVencimiento = fechaVencimiento,
    montoCuotaMinimo = montoCuotaMinimo,
    capitalPendienteAlInicio = capitalPendienteAlInicio,
    montoPagado = montoPagado,
    montoAInteres = montoAInteres,
    montoACapital = montoACapital,
    montoMora = montoMora,
    fechaPago = fechaPago,
    estado = when (estado) {
        "PENDIENTE" -> EstadoCuota.PENDIENTE
        "PAGADA" -> EstadoCuota.PAGADA
        "PARCIAL" -> EstadoCuota.PARCIAL
        "VENCIDA" -> EstadoCuota.VENCIDA
        else -> EstadoCuota.PENDIENTE
    },
    notas = notas
)

fun Cuota.toEntity() = CuotaEntity(
    id = id,
    prestamoId = prestamoId,
    numeroCuota = numeroCuota,
    fechaVencimiento = fechaVencimiento,
    montoCuotaMinimo = montoCuotaMinimo,
    capitalPendienteAlInicio = capitalPendienteAlInicio,
    montoPagado = montoPagado,
    montoAInteres = montoAInteres,
    montoACapital = montoACapital,
    montoMora = montoMora,
    fechaPago = fechaPago,
    estado = estado.name,
    notas = notas
)

// Garantía Mappers
fun GarantiaEntity.toGarantia() = Garantia(
    id = id,
    tipo = when (tipo) {
        "VEHICULO" -> TipoGarantia.VEHICULO
        "ELECTRODOMESTICO" -> TipoGarantia.ELECTRODOMESTICO
        "ELECTRONICO" -> TipoGarantia.ELECTRONICO
        "JOYA" -> TipoGarantia.JOYA
        "MUEBLE" -> TipoGarantia.MUEBLE
        "OTRO" -> TipoGarantia.OTRO
        else -> TipoGarantia.OTRO
    },
    descripcion = descripcion,
    valorEstimado = valorEstimado,
    fotosUrls = fotosUrls,
    estado = when (estado) {
        "RETENIDA" -> EstadoGarantia.RETENIDA
        "DEVUELTA" -> EstadoGarantia.DEVUELTA
        "EJECUTADA" -> EstadoGarantia.EJECUTADA
        else -> EstadoGarantia.RETENIDA
    },
    fechaRegistro = fechaRegistro,
    notas = notas
)

fun Garantia.toEntity() = GarantiaEntity(
    id = id,
    tipo = tipo.name,
    descripcion = descripcion,
    valorEstimado = valorEstimado,
    fotosUrls = fotosUrls,
    estado = estado.name,
    fechaRegistro = fechaRegistro,
    notas = notas
)

// Usuario Mappers
fun UsuarioEntity.toUsuario() = Usuario(
    id = id,
    nombre = nombre,
    email = email,
    rol = when (rol) {
        "PRESTAMISTA" -> RolUsuario.PRESTAMISTA
        "COBRADOR" -> RolUsuario.COBRADOR
        else -> RolUsuario.PRESTAMISTA
    },
    fechaCreacion = fechaCreacion
)

fun Usuario.toEntity() = UsuarioEntity(
    id = id,
    nombre = nombre,
    email = email,
    rol = rol.name,
    fechaCreacion = fechaCreacion
)

// Configuración Mappers
fun ConfiguracionEntity.toConfiguracion() = Configuracion(
    tasaInteresBase = tasaInteresBase,
    tasaMoraBase = tasaMoraBase,
    nombreNegocio = nombreNegocio,
    telefonoNegocio = telefonoNegocio,
    direccionNegocio = direccionNegocio,
    logoUrl = logoUrl,
    mensajeRecibo = mensajeRecibo,
    notificacionesActivas = notificacionesActivas,
    envioWhatsApp = envioWhatsApp,
    envioSMS = envioSMS
)

fun Configuracion.toEntity() = ConfiguracionEntity(
    id = 1,
    tasaInteresBase = tasaInteresBase,
    tasaMoraBase = tasaMoraBase,
    nombreNegocio = nombreNegocio,
    telefonoNegocio = telefonoNegocio,
    direccionNegocio = direccionNegocio,
    logoUrl = logoUrl,
    mensajeRecibo = mensajeRecibo,
    notificacionesActivas = notificacionesActivas,
    envioWhatsApp = envioWhatsApp,
    envioSMS = envioSMS
)

