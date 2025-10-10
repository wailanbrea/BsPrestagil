# 📱 BsPrestagil - Módulos del Sistema

---

## 🔐 1. MÓDULO DE AUTENTICACIÓN

### Funcionalidades
- ✅ Inicio de sesión con email y contraseña
- ✅ Registro de nuevos usuarios
- ✅ Cerrar sesión
- ✅ Recuperación de contraseña

### Tecnología
- Firebase Authentication
- Validaciones en tiempo real
- Sesión persistente

---

## 👥 2. MÓDULO DE CLIENTES

### Funcionalidades
- ✅ Agregar nuevo cliente
- ✅ Editar información del cliente
- ✅ Eliminar cliente
- ✅ Búsqueda en tiempo real
- ✅ Ver detalle completo
- ✅ Gestión de referencias personales (hasta 2)

### Campos Principales
- Nombre completo
- Teléfono
- Dirección
- Email
- Referencias (nombre, teléfono, relación)
- Estado de pagos (Al día, Atrasado, Moroso)

---

## 💰 3. MÓDULO DE PRÉSTAMOS

### Funcionalidades
- ✅ Crear nuevo préstamo
- ✅ Selector de clientes integrado
- ✅ Cálculo automático de intereses
- ✅ Ver detalle del préstamo
- ✅ Filtrar por estado
- ✅ Cronograma de pagos
- ✅ Actualización automática de saldos
- ✅ Compartir resumen por WhatsApp

### Campos Principales
- Cliente seleccionado
- Monto del préstamo
- Tasa de interés (desde configuración)
- Plazo en meses
- Frecuencia de pago (Diario, Semanal, Quincenal, Mensual)
- Garantía asociada (opcional)
- Notas

### Cálculos Automáticos
- Intereses totales
- Total a pagar
- Número de cuotas
- Monto por cuota
- Saldo pendiente
- Progreso en porcentaje

---

## 💳 4. MÓDULO DE PAGOS

### Funcionalidades
- ✅ Registrar pago de cuota
- ✅ Cálculo automático de mora
- ✅ Mora opcional (switch activar/desactivar)
- ✅ Alerta de días de retraso
- ✅ Ver historial de pagos
- ✅ Estadísticas del día
- ✅ Generar recibo
- ✅ Compartir recibo por WhatsApp
- ✅ Actualización automática del préstamo

### Campos Principales
- Monto del pago
- Mora (opcional con switch)
- Método de pago (Efectivo, Transferencia, Tarjeta, Otro)
- Notas adicionales
- Usuario que recibe

### Automatización
- Reducir saldo pendiente
- Aumentar cuotas pagadas
- Marcar préstamo como COMPLETADO cuando saldo = 0
- Calcular mora por días × tasa

---

## 🔒 5. MÓDULO DE GARANTÍAS

### Funcionalidades
- ✅ Agregar nueva garantía
- ✅ Tomar fotos con cámara
- ✅ Seleccionar fotos de galería (múltiples)
- ✅ Ver galería de fotos
- ✅ Ampliar fotos
- ✅ Eliminar fotos individuales
- ✅ Generar código QR único
- ✅ Compartir QR por WhatsApp
- ✅ Escanear código QR
- ✅ Ver historial completo
- ✅ Filtrar por estado

### Tipos de Artículos
- Vehículo
- Electrodoméstico
- Electrónico
- Joya
- Mueble
- Otro

### Estados
- **Retenida** - En posesión del prestamista
- **Devuelta** - Regresada al cliente
- **Ejecutada** - Vendida por impago

### Sistema de Fotos
- Tomar con cámara integrada
- Seleccionar múltiples de galería
- Preview en scroll horizontal
- Alta calidad
- Sin límite de fotos
- Almacenamiento local

### Sistema de Códigos QR
- Generación automática con ZXing
- QR de 512x512 píxeles
- Información completa en el código
- Compartir por WhatsApp/Email
- Escáner integrado en la app
- Verificación instantánea

---

## 📊 6. MÓDULO DE REPORTES

### Funcionalidades
- ✅ Selector de período
- ✅ Estadísticas de cobros
- ✅ Análisis de préstamos
- ✅ Estadísticas de clientes
- ✅ Tasa de morosidad
- ✅ Exportar reportes (en desarrollo)

### Períodos Disponibles
- Hoy
- Semana actual
- Mes actual
- Año actual

### Métricas Mostradas
- Total cobrado del período
- Intereses generados
- Préstamos activos
- Préstamos atrasados
- Préstamos completados
- Tasa de morosidad
- Total clientes
- Clientes al día
- Clientes atrasados
- Clientes morosos

---

## 🔔 7. MÓDULO DE NOTIFICACIONES

### Tipos de Notificaciones
- **Pago Vencido** (Rojo) - Alerta de atraso
- **Pago Próximo** (Amarillo) - Recordatorio
- **Pago Recibido** (Azul) - Confirmación
- **Nuevo Cliente** (Azul) - Aviso

### Funcionalidades
- Lista ordenada por fecha
- Indicador de no leídas
- Marcar como leída
- Marcar todas como leídas
- Click para ir a detalles

---

## ⚙️ 8. MÓDULO DE CONFIGURACIÓN

### Opciones Disponibles

**General:**
- Tasa de interés base (editable)
- Tasa de mora (configurable)
- Personalización de recibos

**Sincronización:**
- Estado de sincronización
- Elementos pendientes de subir
- Botón de sincronización manual
- Indicador visual

**Cobradores:**
- Gestionar usuarios
- Roles y permisos

**Notificaciones:**
- Activar/Desactivar notificaciones
- Activar/Desactivar WhatsApp
- Activar/Desactivar SMS

**Cuenta:**
- Ver perfil
- Cerrar sesión

---

## 📱 9. MÓDULO DE NAVEGACIÓN

### Barra Inferior (5 Pestañas)
1. **Dashboard** 🏠 - Vista general
2. **Clientes** 👥 - Gestión de clientes
3. **Préstamos** 💰 - Gestión de préstamos
4. **Pagos** 💳 - Registro de cobros
5. **Ajustes** ⚙️ - Configuración

### Accesos Rápidos en Dashboard
1. Nuevo Cliente
2. Nuevo Préstamo
3. Escanear QR
4. Historial de Garantías
5. Reportes
6. Garantías

---

## 💾 10. MÓDULO DE SINCRONIZACIÓN

### Características
- ✅ **Offline-First** - Funciona sin internet
- ✅ **Sincronización automática** cada 15 minutos
- ✅ **Sincronización manual** con botón
- ✅ **Indicador de estado** en Settings
- ✅ **Contador de pendientes**

### Base de Datos Dual
- **Room (Local)** - SQLite en el dispositivo
- **Firestore (Nube)** - Respaldo en Firebase

### Proceso
```
Crear/Editar → Guardar en Room → Marcar pendingSync 
→ WorkManager detecta internet → Sube a Firestore 
→ Marca sincronizado ✅
```

### Elementos Sincronizados
- Clientes
- Préstamos
- Pagos
- Garantías
- Configuración

---

## 🧮 11. MÓDULO DE CÁLCULOS

### Cálculos Automáticos

**Préstamos:**
- Interés = Monto × (Tasa / 100)
- Total a Pagar = Monto + Interés
- Número de Cuotas = Plazo × Frecuencia
- Monto por Cuota = Total / Cuotas

**Mora:**
- Días de Retraso = Fecha Actual - Fecha Vencimiento
- Mora por Día = Cuota × (Tasa Mora / 100)
- Mora Total = Mora por Día × Días Retraso

**Estadísticas:**
- Capital Prestado = SUM(préstamos activos)
- Cartera Vencida = SUM(saldos atrasados)
- Tasa Morosidad = (Atrasados / Activos) × 100
- Intereses = Total Cobrado - Moras

---

## 📤 12. MÓDULO DE COMPARTIR

### Integración WhatsApp

**Recibos de Pago:**
- Formato profesional
- Toda la información del pago
- Logo y datos del negocio
- Mensaje de agradecimiento

**Códigos QR:**
- Imagen del QR
- Información de la garantía
- Instrucciones de uso

**Resumen de Préstamo:**
- Datos financieros
- Condiciones del préstamo
- Fechas importantes

### Compartir Genérico
- Email
- Google Drive
- Cualquier app instalada
- Copiar al portapapeles

---

## 🎯 RESUMEN EJECUTIVO

### Total de Funcionalidades por Módulo

| Módulo | Funcionalidades |
|--------|----------------|
| Autenticación | 4 |
| Clientes | 6 |
| Préstamos | 8 |
| Pagos | 10 |
| Garantías | 11 |
| Códigos QR | 4 |
| Reportes | 6 |
| Notificaciones | 5 |
| Configuración | 8 |
| Sincronización | 5 |
| Cálculos | 10 |
| Compartir | 4 |
| **TOTAL** | **81 funcionalidades** |

### Pantallas Principales

1. Login / Registro
2. Dashboard
3. Lista de Clientes
4. Detalle de Cliente
5. Agregar/Editar Cliente
6. Lista de Préstamos
7. Detalle de Préstamo
8. Crear Préstamo
9. Lista de Pagos
10. Registrar Pago
11. Detalle de Pago (Recibo)
12. Lista de Garantías
13. Agregar Garantía (con fotos)
14. Detalle de Garantía
15. Generar Código QR
16. Escanear Código QR
17. Historial de Garantías
18. Reportes
19. Notificaciones
20. Configuración

**Total: 20+ pantallas completas**

---

## 🏗️ Arquitectura del Sistema

### Componentes Principales

**Frontend:**
- Jetpack Compose (UI)
- Material Design 3 (diseño)
- Navigation Component

**Backend:**
- Room Database (local)
- Firebase Firestore (nube)
- Firebase Auth (seguridad)

**Utilidades:**
- WorkManager (sync automática)
- ZXing (códigos QR)
- CameraX (fotos)
- Coil (carga de imágenes)

### Flujo de Datos

```
UI → ViewModel → Repository → Room ←→ Sync Worker ←→ Firebase
```

---

## 📊 Estadísticas del Proyecto

- **Líneas de código:** ~10,000+
- **Archivos creados:** 80+
- **Commits realizados:** 30+
- **Modelos de datos:** 6
- **ViewModels:** 8
- **Repositories:** 5
- **Pantallas:** 20+
- **Componentes reutilizables:** 10+

---

## ✅ Estado Actual

**Completado al 100%:**
- ✅ Autenticación
- ✅ CRUD Clientes
- ✅ CRUD Préstamos
- ✅ Sistema de Pagos
- ✅ Gestión de Garantías
- ✅ Códigos QR
- ✅ Escáner QR
- ✅ Fotos de artículos
- ✅ Reportes
- ✅ Sincronización
- ✅ WhatsApp Integration

**En Desarrollo:**
- 🔄 PDF de recibos
- 🔄 Exportar reportes
- 🔄 Notificaciones push
- 🔄 SMS automáticos

---

## 🚀 Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose
- **Base de Datos Local:** Room (SQLite)
- **Base de Datos Nube:** Firebase Firestore
- **Autenticación:** Firebase Auth
- **Sincronización:** WorkManager
- **QR Codes:** ZXing
- **Fotos:** CameraX
- **Imágenes:** Coil
- **Diseño:** Material Design 3

---

## 📞 Información del Proyecto

**Nombre:** BsPrestagil
**Versión:** 1.0.0
**Plataforma:** Android 8.0+
**Repositorio:** https://github.com/wailanbrea/BsPrestagil.git

---

*Aplicación lista para producción* ✅

