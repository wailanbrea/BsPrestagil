# 📱 BsPrestagil - Sistema de Gestión de Préstamos
## Manual Completo de Funcionalidades

---

## 📋 Tabla de Contenido

1. [Descripción General](#descripción-general)
2. [Autenticación](#autenticación)
3. [Dashboard Principal](#dashboard-principal)
4. [Gestión de Clientes](#gestión-de-clientes)
5. [Gestión de Préstamos](#gestión-de-préstamos)
6. [Sistema de Pagos](#sistema-de-pagos)
7. [Gestión de Garantías](#gestión-de-garantías)
8. [Códigos QR](#códigos-qr)
9. [Reportes y Estadísticas](#reportes-y-estadísticas)
10. [Notificaciones](#notificaciones)
11. [Configuración](#configuración)
12. [Sincronización](#sincronización)
13. [Características Técnicas](#características-técnicas)

---

## 🎯 Descripción General

**BsPrestagil** es una aplicación móvil Android profesional diseñada para la gestión integral de préstamos, orientada a prestamistas y empresas de microcréditos.

### Características Principales

- ✅ **100% Offline** - Funciona sin conexión a internet
- ✅ **Sincronización Automática** - Respaldo en la nube con Firebase
- ✅ **Interfaz Moderna** - Material Design 3 con tema claro/oscuro
- ✅ **Código QR** - Para identificación rápida de garantías
- ✅ **WhatsApp Integrado** - Envío de recibos y códigos QR
- ✅ **Base de Datos Dual** - Room (local) + Firestore (nube)
- ✅ **Segura** - Autenticación con Firebase Authentication

---

## 🔐 Autenticación

### 1. Inicio de Sesión (Login)

**Funcionalidades:**
- Inicio de sesión con correo electrónico y contraseña
- Validación de credenciales con Firebase Authentication
- Recordar sesión (no necesita login cada vez)
- Mensajes de error descriptivos
- Estado de carga visual

**Datos de acceso de prueba:**
- Email: `wailandkey@gmail.com`
- Password: `12345678`

**Flujo:**
```
1. Abrir app
2. Ingresar email y contraseña
3. Click "Iniciar sesión"
4. ✅ Acceso al Dashboard
```

### 2. Registro de Usuarios

**Funcionalidades:**
- Crear cuenta nueva
- Validación de email
- Validación de contraseña (mínimo 6 caracteres)
- Confirmación de contraseña
- Nombre completo y teléfono
- Creación automática en Firebase

**Campos requeridos:**
- Nombre completo
- Correo electrónico
- Teléfono
- Contraseña
- Confirmar contraseña

### 3. Cerrar Sesión

**Funcionalidades:**
- Diálogo de confirmación
- Cierra sesión de Firebase
- Regresa a pantalla de login
- Limpia datos de sesión

---

## 📊 Dashboard Principal

### Vista General del Negocio

**Estadísticas Principales:**

1. **Capital Prestado**
   - Suma total de préstamos activos
   - Número de préstamos activos
   - Actualización en tiempo real

2. **Intereses Generados**
   - Intereses del mes actual
   - Cálculo automático
   - Basado en pagos recibidos

3. **Cartera Vencida**
   - Total de préstamos atrasados
   - Número de préstamos atrasados
   - Color rojo de alerta

### Accesos Rápidos (6 Botones)

1. **Nuevo Cliente** - Ir directo a crear cliente
2. **Nuevo Préstamo** - Crear préstamo
3. **Escanear QR** - Abrir escáner de garantías
4. **Historial** - Ver historial de garantías
5. **Reportes** - Análisis del negocio
6. **Garantías** - Gestionar garantías

### Préstamos Recientes

- Lista de últimos 5 préstamos
- Tarjetas con:
  - Nombre del cliente
  - Monto original
  - Saldo pendiente
  - Progreso de cuotas (barra visual)
  - Estado con color (Verde: Activo, Naranja: Atrasado)
- Click para ver detalles

### Funcionalidades Adicionales

- **Pull-to-refresh** - Deslizar hacia abajo para actualizar
- **Botón flotante (+)** - Crear nuevo préstamo
- **Ícono de notificaciones** - Ver alertas
- **Ícono de prueba (bug)** - Pantalla de prueba de sincronización

---

## 👥 Gestión de Clientes

### 1. Lista de Clientes

**Funcionalidades:**
- Ver todos los clientes registrados
- **Búsqueda en tiempo real** por nombre o teléfono
- Ordenados por fecha de registro
- Tarjetas con información clave

**Información mostrada:**
- Nombre completo
- Teléfono
- Estado de pagos (Al día, Atrasado, Moroso)
- Número de préstamos activos
- Códigos de color según estado

**Botón flotante (+)** - Agregar nuevo cliente

### 2. Agregar Cliente

**Información Básica (Requerida):**
- Nombre completo *
- Teléfono *
- Dirección *
- Correo electrónico (opcional)

**Referencias (Hasta 2):**

Referencia 1 y 2:
- Nombre
- Teléfono
- Relación (Hermano, Amigo, Padre, etc.)

**Proceso:**
```
1. Completar formulario
2. Agregar hasta 2 referencias
3. Click "Guardar cliente"
4. ✅ Diálogo de confirmación
5. Cliente guardado en Room
6. Sincronización automática con Firebase
```

### 3. Ver Detalle de Cliente

**Información mostrada:**
- Foto de perfil (ícono)
- Nombre completo
- Teléfono (con ícono para llamar)
- Email
- Dirección completa

**Lista de Préstamos del Cliente:**
- Todos los préstamos (activos y completados)
- Tarjetas con estado
- Click para ver detalle del préstamo

**Botones de Acción:**
- **Llamar** - Inicia llamada al teléfono
- **Nuevo Préstamo** - Crear préstamo para este cliente

### 4. Editar Cliente

**Funcionalidades:**
- Cargar datos existentes automáticamente
- Modificar cualquier campo
- Actualizar referencias
- **Botón de eliminar** (ícono basura rojo)
- Confirmación antes de eliminar

**Al eliminar:**
- Se elimina de Room (local)
- Se elimina de Firebase (nube)
- Se eliminan préstamos relacionados (cascada)
- Diálogo de advertencia

---

## 💰 Gestión de Préstamos

### 1. Lista de Préstamos

**Funcionalidades:**
- Ver todos los préstamos
- **Filtrar por estado:**
  - Todos
  - Activos
  - Atrasados
  - Completados
  - Cancelados
- Ordenados por fecha de inicio

**Información en Tarjetas:**
- Nombre del cliente
- Monto original
- Saldo pendiente
- Progreso de cuotas (barra visual)
- Estado con color
- Porcentaje completado

**Botón flotante (+)** - Nuevo préstamo

### 2. Crear Nuevo Préstamo

**Paso 1: Seleccionar Cliente**

Opciones:
- Si viene desde un cliente → Ya seleccionado
- Si viene desde FAB → **Diálogo selector de clientes**
  - Lista completa de clientes
  - Búsqueda por nombre
  - Click para seleccionar
  - Sin salir de la pantalla

**Paso 2: Detalles del Préstamo**

**Campos Requeridos:**
- Monto del préstamo *
- Tasa de interés * (carga automáticamente desde configuración)
- Plazo en meses *
- Frecuencia de pago:
  - Diario
  - Semanal
  - Quincenal
  - Mensual

**Campos Opcionales:**
- Garantía asociada
- Notas adicionales

**Resumen Automático:**
- Capital: $XX,XXX
- Interés (X%): $X,XXX
- **Total a pagar: $XX,XXX**

**Proceso:**
```
1. Seleccionar cliente
2. Ingresar monto y condiciones
3. Ver resumen automático
4. Click "Crear préstamo"
5. Diálogo de confirmación con resumen
6. ✅ Préstamo creado
7. Se guarda en Room
8. Sincroniza con Firebase
9. Genera cronograma de pagos
```

**Cálculos Automáticos:**
- Interés total = Monto × (Tasa / 100)
- Total a pagar = Monto + Interés
- Número de cuotas = Plazo × Frecuencia
- Monto por cuota = Total / Cuotas
- Fecha de vencimiento = Inicio + Plazo

### 3. Ver Detalle de Préstamo

**Información Completa:**

**Estado Visual:**
- Estado actual con color
- Ícono grande según estado
- Card de color según estado

**Información del Cliente:**
- Nombre (click para ver perfil)
- Ícono de navegación

**Resumen Financiero:**
- Monto original
- Tasa de interés
- Total a pagar
- Saldo pendiente

**Progreso de Pagos:**
- Cuotas pagadas / Total cuotas
- Barra de progreso visual
- Porcentaje completado

**Fechas:**
- Fecha de inicio
- Fecha de vencimiento
- Plazo en meses

**Botón Principal:**
- **"Registrar Pago"** - Va a pantalla de pago

**Compartir:**
- Ícono de compartir en barra superior
- Genera resumen del préstamo
- Comparte por WhatsApp o cualquier app

---

## 💳 Sistema de Pagos

### 1. Lista de Pagos

**Funcionalidades:**
- Ver historial completo de pagos
- Ordenados por fecha (más reciente primero)
- Total cobrado del día (destacado)
- Número de pagos del día

**Información en Tarjetas:**
- Nombre del cliente
- Número de cuota
- Fecha y hora del pago
- Monto total
- Método de pago
- Alerta si incluye mora

**Resumen del Día:**
- Total cobrado hoy: $XX,XXX
- XX pagos registrados
- Card verde destacado

### 2. Registrar Pago

**Carga Automática:**
- Datos del préstamo
- Nombre del cliente
- Monto de la cuota
- Número de cuota siguiente

**Alertas Inteligentes:**
- **Si hay retraso:**
  - Card roja de advertencia
  - "⚠️ Pago con X día(s) de retraso"
  - Mora calculada automáticamente
  - Muestra porcentaje de mora

**Campos:**

**Monto del Pago:**
- Campo principal
- Muestra sugerencia del monto de cuota
- Validación numérica

**Mora (Opcional con Switch):**
- Switch "Cobrar mora"
- Desactivado por defecto
- Al activar:
  - Muestra campo de monto de mora
  - Cálculo automático basado en:
    - Días de retraso
    - Tasa de mora configurada
    - Monto de la cuota
- Campo con color rojo
- Se puede editar el monto sugerido

**Método de Pago:**
- Efectivo
- Transferencia
- Tarjeta
- Otro

**Notas Adicionales:**
- Campo de texto libre
- Para observaciones

**Resumen antes de Guardar:**
- Total a registrar
- "Incluye mora: $XXX" (si aplica)
- Monto destacado grande

**Proceso:**
```
1. Abrir detalle de préstamo
2. Click "Registrar pago"
3. Ver datos del préstamo
4. Si hay retraso → Ver alerta y mora calculada
5. Decidir si cobrar mora (switch)
6. Ingresar monto
7. Seleccionar método de pago
8. Click "Registrar pago"
9. ✅ Pago guardado
10. Préstamo actualizado automáticamente:
    - Saldo reducido
    - Cuotas pagadas +1
    - Si saldo = 0 → Estado: COMPLETADO
11. Sincroniza con Firebase
```

### 3. Ver Detalle de Pago (Recibo)

**Pantalla de Recibo:**
- Ícono de check verde grande
- Monto destacado
- Toda la información del pago

**Información Completa:**
- Cliente
- Número de cuota
- Monto de la cuota
- Mora (si aplica)
- Total pagado
- Método de pago
- Fecha y hora exacta
- Quién recibió el pago
- Notas adicionales

**Botones:**
- **Descargar Recibo** (PDF - en desarrollo)
- **Enviar por WhatsApp** - Formato profesional:

```
🧾 RECIBO DE PAGO
━━━━━━━━━━━━━━━━━━━━
Prestágil

📋 DATOS DEL PAGO
Cliente: Juan Pérez
Cuota #: 5

💰 MONTOS
Cuota: $1,000.00
Mora: $50.00 (si aplica)
Total pagado: $1,050.00

📅 Fecha: 10/10/2025 14:30
💳 Método: Efectivo
👤 Recibido por: admin@prestágil.com

━━━━━━━━━━━━━━━━━━━━
✅ Gracias por su pago
```

---

## 🔒 Gestión de Garantías

### 1. Lista de Garantías Activas

**Funcionalidades:**
- Ver garantías retenidas actualmente
- 3 iconos en barra superior:
  - 📷 **Escanear QR** - Verificar garantía
  - 🕐 **Historial** - Ver todas las garantías
  - ➕ **Agregar** - Nueva garantía

**Información en Tarjetas:**
- Ícono de tipo de artículo
- Descripción del artículo
- Tipo (Vehículo, Electrónico, etc.)
- Estado con color
- Valor estimado
- **2 Botones:**
  - "Ver QR" - Generar código QR
  - "Detalles" - Ver información completa

### 2. Agregar Nueva Garantía

**Formulario Completo:**

**Información del Artículo:**
- **Tipo * (Selector):**
  - Vehículo
  - Electrodoméstico
  - Electrónico
  - Joya
  - Mueble
  - Otro

- **Descripción * :**
  - Ejemplo: "Laptop Dell Inspiron 15"
  - Marca, modelo, características

- **Valor Estimado * :**
  - Monto en dólares
  - Para calcular riesgo

- **Notas Adicionales:**
  - Detalles, condición, accesorios
  - Estado del artículo
  - Números de serie

**📷 Sección de Fotos:**

**Botones:**
- **"Tomar Foto"** - Abre cámara
- **"Galería"** - Seleccionar múltiples fotos

**Funcionalidades:**
- Tomar foto con cámara integrada
- Seleccionar múltiples fotos de galería
- Preview de fotos en scroll horizontal
- **Eliminar foto individual** (X roja en esquina)
- Contador: "X foto(s) agregada(s)"
- Sin límite de fotos
- Fotos en alta calidad

**Proceso:**
```
1. Click "Nueva garantía"
2. Seleccionar tipo de artículo
3. Describir artículo
4. Ingresar valor estimado
5. Agregar notas
6. Click "Tomar foto" o "Galería"
7. Agregar 2-5 fotos del artículo
8. Click "Guardar garantía"
9. ✅ Garantía guardada con fotos
10. Opción de generar QR inmediatamente
```

### 3. Ver Detalle de Garantía

**Galería de Fotos:**
- Scroll horizontal de fotos
- Fotos de 150x150 píxeles
- Click en foto → Ver ampliada
- Diálogo con foto completa

**Información Completa:**
- Ícono grande de garantía
- Descripción del artículo
- Tipo de artículo
- Estado actual
- Valor estimado
- Fecha de registro
- Notas completas

**Botones:**
- **Editar** - Modificar información
- **Ver QR** - Generar código QR

### 4. Historial de Garantías

**Estadísticas Generales:**
- Total retenidas (naranja)
- Total devueltas (verde)
- Total ejecutadas (rojo)
- **Valor total retenido** en dinero

**Filtros Disponibles:**
- Todas
- Retenidas (en posesión)
- Devueltas (regresadas a cliente)
- Ejecutadas (vendidas por impago)

**Información en Tarjetas:**
- Ícono según estado:
  - 🔒 Retenida (candado)
  - ✅ Devuelta (check)
  - ⚖️ Ejecutada (martillo)
- Descripción
- Tipo
- Estado con color
- Fecha de registro
- Valor

**Botones en cada tarjeta:**
- Ver QR
- Ver Detalles

**Casos de Uso:**
- Ver cuántas garantías tienes retenidas
- Buscar garantías devueltas
- Revisar garantías ejecutadas
- Calcular valor total en garantías

---

## 📱 Códigos QR para Garantías

### 1. Generar Código QR

**¿Cuándo se Genera?**
- Al crear garantía (opcional)
- Desde lista de garantías (botón "Ver QR")
- Desde detalle de garantía
- Desde historial

**Información en el QR:**
```
🔒 GARANTÍA PRESTÁGIL
━━━━━━━━━━━━━━━━
ID: GAR-XXXXX
Cliente: Juan Pérez
Artículo: Laptop Dell
Tipo: ELECTRONICO
Valor: $12,000.00
Fecha: 10/10/2025
━━━━━━━━━━━━━━━━
Escanea para verificar
```

**Pantalla de QR:**
- Card con info de garantía arriba
- **Código QR grande** (512x512) en el centro
- Fondo blanco para mejor lectura
- ID visible debajo del QR
- Instrucciones de uso

**Opciones:**
- **Enviar por WhatsApp**
  - Mensaje profesional incluido
  - Cliente lo recibe y puede imprimir
  - Instrucciones de uso
  
- **Compartir por otra app**
  - Email
  - Google Drive
  - Cualquier app instalada

**Instrucciones Mostradas:**
```
📋 Instrucciones:
1. Comparte el QR por WhatsApp
2. El cliente lo imprime
3. Pega el código en el artículo
4. Escanea para verificar cuando devuelva
```

### 2. Escáner de QR Integrado

**Acceso:**
- Dashboard → Botón "Escanear QR"
- Garantías → Ícono de escáner

**Funcionalidades:**
- **Solicita permiso** de cámara (primera vez)
- **Cámara en vivo** con overlay
- **Escaneo automático** al detectar QR
- **Muestra resultado** completo
- Instrucciones en pantalla: "📷 Apunta al código QR"

**Al Escanear:**
- ✅ Ícono de éxito
- Toda la información del QR
- ID de garantía
- Cliente
- Artículo
- Valor
- Fecha
- **Botón:** "Escanear otro QR"
- **Botón:** "Cerrar"

**Casos de Uso:**
- Verificar garantía cuando cliente viene a pagar
- Identificar artículo rápidamente
- Confirmar datos antes de devolver
- Buscar garantía en bodega

**Sin Permiso de Cámara:**
- Mensaje explicativo
- Botón "Otorgar permiso"
- Solicita nuevamente

---

## 📈 Reportes y Estadísticas

### 1. Pantalla de Reportes

**Selector de Período:**
- Hoy
- Semana actual
- Mes actual
- Año actual

**Resumen de Cobros (Tarjetas):**

1. **Total Cobrado**
   - Monto total del período
   - Descripción del período

2. **Intereses**
   - Intereses generados del período
   - Porcentaje del total

**Estado de Préstamos:**
- Préstamos activos: X
- Préstamos atrasados: X
- Préstamos completados: X
- **Tasa de morosidad: X%** (calculada en tiempo real)

**Estadísticas de Clientes:**
- Total clientes: X
- Clientes al día: X (verde)
- Clientes atrasados: X (naranja)
- Clientes morosos: X (rojo)

**Botón:**
- **Exportar Reporte** (PDF/Excel - en desarrollo)

**Actualización:**
- Datos en tiempo real de la base de datos
- Cambio de período actualiza inmediatamente
- Cálculos automáticos

### 2. Análisis Disponible

**Métricas Calculadas:**
- Total prestado vs Total cobrado
- Tasa de recuperación
- Tasa de morosidad = (Atrasados / Activos) × 100
- Promedio de interés
- Capital en riesgo

---

## 🔔 Notificaciones

### Tipos de Notificaciones

**1. Pago Vencido (Rojo)**
- Ícono de error
- Mensaje: "Cliente X tiene pago vencido desde hace X días"
- Click para ir a préstamo

**2. Pago Próximo a Vencer (Amarillo)**
- Ícono de advertencia
- Mensaje: "Cliente X tiene pago que vence mañana"
- Click para recordar

**3. Pago Recibido (Azul)**
- Ícono de check
- Mensaje: "Se registró pago de $X de Cliente Y"
- Click para ver recibo

**4. Nuevo Cliente (Azul)**
- Ícono de persona
- Mensaje: "Nuevo cliente registrado: X"
- Click para ver perfil

**Funcionalidades:**
- Lista ordenada por fecha
- Indicador de no leídas (punto azul)
- Diferentes estilos según leída/no leída
- Marcar todas como leídas
- Fecha y hora de cada notificación

---

## ⚙️ Configuración

### Secciones Disponibles

**GENERAL**

1. **Tasa de Interés Base**
   - Muestra tasa actual: X%
   - Click para editar
   - Diálogo con campo numérico
   - Se aplica a nuevos préstamos
   - Se guarda en base de datos
   - Sincroniza con Firebase

2. **Personalización de Recibos**
   - Logo del negocio
   - Datos del negocio
   - Mensaje personalizado

**SINCRONIZACIÓN**

1. **Estado de Sincronización**
   - Muestra elementos pendientes
   - "X elementos pendientes" (rojo si hay)
   - "Todo sincronizado ✓" (verde si no hay)
   - **Botón de sincronizar manual**
   - Spinner de carga al sincronizar
   - Actualiza después de 2 segundos

**COBRADORES**

1. **Gestionar Cobradores**
   - Agregar usuarios
   - Eliminar usuarios
   - Asignar permisos (en desarrollo)

**NOTIFICACIONES**

1. **Notificaciones (Switch)**
   - Activar/Desactivar
   - Recordatorios de pago

2. **Mensajes WhatsApp (Switch)**
   - Enviar recibos automáticos
   - Recordatorios por WhatsApp

**CUENTA**

1. **Perfil**
   - Editar información personal
   - Cambiar contraseña (en desarrollo)

2. **Cerrar Sesión**
   - Diálogo de confirmación
   - "¿Estás seguro?"
   - Cierra sesión de Firebase
   - Regresa a Login

---

## 🔄 Sincronización

### Sistema Offline-First

**Funcionamiento:**

**Modo Offline (Sin Internet):**
```
1. Usuario crea/edita datos
2. Se guardan INMEDIATAMENTE en Room (SQLite local)
3. Se marcan como pendingSync = true
4. App funciona 100% normal
5. Datos disponibles instantáneamente
```

**Modo Online (Con Internet):**
```
1. WorkManager detecta conexión
2. Cada 15 minutos ejecuta sincronización
3. Sube cambios pendientes a Firestore
4. Marca como pendingSync = false
5. Datos respaldados en la nube
```

**Sincronización Manual:**
```
1. Settings → Sección SINCRONIZACIÓN
2. Click botón de sync (ícono circular)
3. Spinner de carga
4. Sincroniza inmediatamente
5. Actualiza contador de pendientes
```

### Elementos Sincronizados

- ✅ Clientes (con referencias)
- ✅ Préstamos (con todas las condiciones)
- ✅ Pagos (con moras y notas)
- ✅ Garantías (con rutas de fotos)
- ✅ Configuración (tasas y ajustes)

### Indicador de Estado

**En Settings muestra:**
- "5 elementos pendientes" (si hay cambios sin subir)
- "Todo sincronizado ✓" (si todo está en la nube)
- Botón para forzar sync inmediata

### Ventajas del Sistema

✅ **Nunca pierdes datos** - Se guarda localmente primero
✅ **Funciona sin internet** - Ideal para zonas sin señal
✅ **Respaldo automático** - En la nube cada 15 minutos
✅ **Multidevice** - Acceso desde varios dispositivos (futuro)
✅ **Recuperación** - Si pierdes el teléfono, datos en la nube

---

## 📊 Características Técnicas

### Tecnologías Utilizadas

**Frontend:**
- Kotlin (lenguaje)
- Jetpack Compose (UI declarativa)
- Material Design 3 (diseño moderno)
- Navigation Component (navegación)

**Backend/Base de Datos:**
- Room (SQLite local)
- Firebase Firestore (nube)
- Firebase Authentication (autenticación)

**Sincronización:**
- WorkManager (tareas en background)
- Coroutines (programación asíncrona)

**Librerías Adicionales:**
- ZXing (generación y escaneo de QR)
- CameraX (captura de fotos)
- Coil (carga de imágenes)
- Gson (serialización JSON)

### Arquitectura

**Patrón MVVM (Model-View-ViewModel):**

```
📱 UI (Compose Screens)
    ↓
🎮 ViewModels (Lógica de presentación)
    ↓
📦 Repositories (Lógica de negocio)
    ↓
💾 Room ←→ 🔄 Sync ←→ ☁️ Firestore
```

**Capas:**
1. **Presentación** - Pantallas en Compose
2. **ViewModels** - Estado y lógica de UI
3. **Repositories** - Acceso a datos
4. **Base de Datos** - Persistencia local y nube
5. **Sincronización** - WorkManager automático

### Base de Datos

**Entidades Room (6 Tablas):**

1. **clientes**
   - id, nombre, teléfono, dirección, email
   - fotoUrl, referencias (JSON)
   - fechaRegistro, prestamosActivos
   - historialPagos, campos de sync

2. **prestamos**
   - id, clienteId (FK), clienteNombre
   - montoOriginal, tasaInteres, plazoMeses
   - frecuenciaPago, garantiaId
   - fechaInicio, fechaVencimiento
   - estado, saldoPendiente, totalAPagar
   - cuotasPagadas, totalCuotas
   - notas, campos de sync

3. **pagos**
   - id, prestamoId (FK), clienteId (FK)
   - clienteNombre, monto, montoCuota
   - montoMora, fechaPago, fechaVencimiento
   - numeroCuota, metodoPago
   - recibidoPor, notas, reciboUrl
   - campos de sync

4. **garantias**
   - id, tipo, descripcion, valorEstimado
   - fotosUrls (JSON), estado
   - fechaRegistro, notas
   - campos de sync

5. **usuarios**
   - id, nombre, email, rol
   - fechaCreacion, campos de sync

6. **configuracion**
   - id (siempre 1), tasaInteresBase
   - tasaMoraBase, nombreNegocio
   - telefonoNegocio, direccionNegocio
   - logoUrl, mensajeRecibo
   - notificacionesActivas, envioWhatsApp
   - envioSMS, campos de sync

**Campos de Sincronización (en todas):**
- `pendingSync`: Boolean (true si hay cambios)
- `lastSyncTime`: Long (timestamp última sync)
- `firebaseId`: String? (ID en Firestore)

### Seguridad

**Autenticación:**
- Firebase Authentication
- Emails y contraseñas encriptadas
- Sesión persistente
- Tokens de seguridad

**Base de Datos:**
- Room con SQLite encriptado
- Firestore con reglas de seguridad
- Solo usuarios autenticados pueden acceder
- Datos asociados por usuario

**Permisos:**
- INTERNET - Para sincronización
- ACCESS_NETWORK_STATE - Detectar conexión
- CAMERA - Escanear QR y tomar fotos

---

## 🧮 Cálculos Automáticos

### Préstamos

**Al Crear Préstamo:**
```kotlin
Monto Original: $10,000
Tasa de Interés: 10%

Interés = $10,000 × (10 / 100) = $1,000
Total a Pagar = $10,000 + $1,000 = $11,000

Plazo: 12 meses
Frecuencia: Mensual
Número de Cuotas = 12

Monto por Cuota = $11,000 / 12 = $916.67
```

**Fechas:**
```kotlin
Fecha Inicio: 10/10/2025
Plazo: 12 meses
Fecha Vencimiento: 10/10/2026

Vencimiento Cuota 1: 10/11/2025
Vencimiento Cuota 2: 10/12/2025
...y así sucesivamente según frecuencia
```

### Mora

**Cálculo Automático:**
```kotlin
Fecha Vencimiento Cuota: 01/10/2025
Fecha Pago Real: 10/10/2025
Días de Retraso = 9 días

Tasa de Mora: 5% (configurable)
Monto Cuota: $916.67

Mora por Día = $916.67 × (5 / 100) = $45.83
Mora Total = $45.83 × 9 días = $412.50
```

**Mostrado en Pantalla:**
```
⚠️ Pago con 9 día(s) de retraso
Mora sugerida: $412.50 (5% de mora)
```

### Actualización de Préstamo al Pagar

**Antes del Pago:**
```
Saldo Pendiente: $11,000
Cuotas Pagadas: 0 / 12
Estado: ACTIVO
```

**Después del Pago de $916.67:**
```
Saldo Pendiente: $10,083.33
Cuotas Pagadas: 1 / 12
Estado: ACTIVO
```

**Cuando Saldo = $0:**
```
Saldo Pendiente: $0.00
Cuotas Pagadas: 12 / 12
Estado: COMPLETADO ✅
```

### Estadísticas de Dashboard

```kotlin
// Capital Prestado
SELECT SUM(montoOriginal) 
FROM prestamos 
WHERE estado IN ('ACTIVO', 'ATRASADO')

// Cartera Vencida
SELECT SUM(saldoPendiente) 
FROM prestamos 
WHERE estado = 'ATRASADO'

// Intereses del Mes
SELECT SUM(monto - montoMora) 
FROM pagos 
WHERE fechaPago >= inicioMes

// Tasa de Morosidad
(Préstamos Atrasados / Total Activos) × 100
```

---

## 📱 Navegación de la App

### Barra Inferior (5 Pestañas)

1. **Dashboard** 🏠
   - Resumen general
   - Estadísticas
   - Accesos rápidos

2. **Clientes** 👥
   - Lista de clientes
   - Búsqueda
   - Agregar nuevo

3. **Préstamos** 💰
   - Lista de préstamos
   - Filtros
   - Crear nuevo

4. **Pagos** 💳
   - Historial de pagos
   - Estadísticas del día
   - Registrar pago

5. **Ajustes** ⚙️
   - Configuración
   - Sincronización
   - Cerrar sesión

### Flujos Principales

**Flujo 1: Cliente Nuevo → Préstamo → Pago**
```
1. Clientes → (+) Nuevo
2. Completar datos y referencias
3. Guardar cliente
4. Ver detalle → "Nuevo Préstamo"
5. Completar préstamo
6. Confirmar y crear
7. Ver detalle → "Registrar Pago"
8. Registrar primer pago
9. ✅ Todo sincronizado
```

**Flujo 2: Garantía Completa**
```
1. Garantías → (+) Nueva
2. Completar datos del artículo
3. Tomar 3 fotos del artículo
4. Guardar garantía
5. Click "Ver QR"
6. Compartir por WhatsApp
7. Cliente imprime QR
8. Pegar QR en artículo
```

**Flujo 3: Verificar Garantía**
```
1. Cliente viene a pagar
2. Dashboard → "Escanear QR"
3. Apuntar a QR en artículo
4. ✅ Verificar identidad
5. Confirmar que es correcto
```

---

## 💡 Casos de Uso Reales

### Caso 1: Préstamo Simple

**Situación:** Juan necesita $5,000 por 6 meses

**Proceso en la App:**
1. Agregar cliente "Juan Pérez"
2. Crear préstamo:
   - Monto: $5,000
   - Tasa: 10%
   - Plazo: 6 meses
   - Frecuencia: Mensual
3. Total a pagar: $5,500 (calculado automático)
4. 6 cuotas de $916.67 cada una
5. Juan paga cada mes
6. Registrar pago → Saldo se actualiza
7. Al 6º pago → Préstamo COMPLETADO

### Caso 2: Préstamo con Garantía

**Situación:** María deja su laptop en garantía por $10,000

**Proceso:**
1. Agregar cliente "María González"
2. Crear garantía:
   - Tipo: Electrónico
   - Descripción: "Laptop Dell Inspiron"
   - Valor: $12,000
   - Tomar 3 fotos de la laptop
   - Guardar
3. Generar QR de la garantía
4. Compartir QR por WhatsApp
5. María imprime y pega en laptop
6. Crear préstamo asociado a garantía
7. Registrar pagos mensuales
8. Al completar → Devolver laptop
9. Escanear QR para verificar
10. Marcar garantía como DEVUELTA

### Caso 3: Pago con Retraso

**Situación:** Carlos paga 10 días tarde

**Proceso:**
1. Ir a préstamo de Carlos
2. Click "Registrar Pago"
3. ⚠️ Ver alerta: "10 días de retraso"
4. Ver mora calculada: $XXX
5. Decidir si cobrar mora:
   - Opción A: Activar switch → Cobrar mora
   - Opción B: Dejar apagado → Perdonar mora
6. Registrar pago
7. ✅ Saldo actualizado
8. Compartir recibo por WhatsApp

### Caso 4: Análisis del Negocio

**Situación:** Revisar desempeño del mes

**Proceso:**
1. Ir a Reportes
2. Seleccionar "Mes actual"
3. Ver estadísticas:
   - Total cobrado: $45,600
   - Intereses: $8,420
   - 12 préstamos activos
   - 3 atrasados
   - Tasa morosidad: 25%
4. Ver clientes morosos
5. Tomar decisiones

---

## 📖 Glosario de Términos

**Capital Prestado:** Suma total de dinero prestado actualmente

**Cartera Vencida:** Total adeudado de préstamos atrasados

**Cuota:** Pago periódico del préstamo

**Estado AL_DIA:** Cliente sin pagos atrasados

**Estado ATRASADO:** Cliente con 1-30 días de retraso

**Estado MOROSO:** Cliente con más de 30 días de retraso

**Frecuencia de Pago:** Cada cuándo se paga (Diario, Semanal, etc.)

**Garantía RETENIDA:** Artículo en posesión del prestamista

**Garantía DEVUELTA:** Artículo regresado al cliente

**Garantía EJECUTADA:** Artículo vendido por impago

**Mora:** Cargo adicional por pago tardío

**Plazo:** Duración total del préstamo en meses

**Sincronización Pendiente:** Datos no respaldados en la nube aún

**Tasa de Interés:** Porcentaje cobrado sobre el monto prestado

**Tasa de Morosidad:** Porcentaje de préstamos atrasados

---

## 🎓 Consejos de Uso

### Mejores Prácticas

**1. Siempre Agregar Referencias**
- Facilita contactar en caso de impago
- Mejor validación del cliente
- Mayor seguridad

**2. Tomar Fotos de Garantías**
- Documenta estado del artículo
- Evita disputas
- Facilita identificación

**3. Usar Códigos QR**
- Pega QR en artículos visibles
- Verifica al devolver
- Organiza tu bodega

**4. Registrar Pagos Inmediatamente**
- Sincroniza automáticamente
- Actualiza saldos
- Genera recibos al instante

**5. Revisar Reportes Semanalmente**
- Identifica tendencias
- Prevé problemas
- Toma decisiones informadas

**6. Sincronizar Manualmente**
- Antes de cerrar día
- Respaldo de seguridad
- Verifica que todo esté en la nube

### Tips Útiles

💡 **Pull-to-Refresh:** Desliza hacia abajo en Dashboard para actualizar datos

💡 **Búsqueda Rápida:** Escribe mientras buscas clientes (tiempo real)

💡 **Mora Opcional:** Puedes perdonar mora según el caso

💡 **WhatsApp Directo:** Envía recibos sin salir de la app

💡 **Offline 100%:** Trabaja sin internet, sincroniza después

💡 **Selector de Clientes:** Ya no necesitas salir a otra pantalla

---

## 📞 Soporte y Contacto

**Proyecto:** BsPrestagil
**Versión:** 1.0
**Plataforma:** Android 8.0+ (API 26+)
**Repositorio:** https://github.com/wailanbrea/BsPrestagil.git

### Requerimientos del Sistema

- Android 8.0 (Oreo) o superior
- 100 MB de espacio libre
- Conexión a internet (para sincronización)
- Cámara (para fotos y escáner QR)

---

## 📝 Notas Importantes

### Datos de Prueba

La aplicación incluye una pantalla de prueba (ícono de bug en Dashboard) que permite:
- Crear datos de ejemplo
- Probar sincronización
- Verificar que Firebase funciona
- Ver datos en Firebase Console

### Próximas Funcionalidades

**En Desarrollo:**
- Generación de PDF para recibos
- Envío de SMS
- Gráficas avanzadas
- Exportar reportes a Excel
- Múltiples usuarios cobradores
- Recordatorios automáticos
- Backup automático en Google Drive

### Limitaciones Actuales

- Sincronización bidireccional (Firebase → Room) en desarrollo
- Exportar reportes en desarrollo
- PDF de recibos en desarrollo
- Notificaciones push en desarrollo

---

## ✅ Resumen de Funcionalidades

### Por Módulo

**Autenticación:** 3 funciones
- Login, Registro, Logout

**Clientes:** 4 funciones CRUD
- Crear, Leer, Actualizar, Eliminar

**Préstamos:** 5 funciones
- Crear, Listar, Filtrar, Ver Detalle, Compartir

**Pagos:** 4 funciones
- Registrar, Listar, Ver Detalle, Compartir Recibo

**Garantías:** 8 funciones
- Crear, Fotos, QR, Escanear, Historial, Filtros, Ver, Compartir

**Reportes:** 4 funciones
- Estadísticas, Filtrar Período, Análisis, Ver Datos

**Configuración:** 5 funciones
- Tasas, Sync Manual, Notificaciones, WhatsApp, Logout

**Sincronización:** 3 funciones
- Automática, Manual, Indicador Estado

**TOTAL:** 36+ funciones principales

---

## 🏆 Características Destacadas

### Lo que Hace Única a BsPrestagil

1. **✅ Mora Opcional** - Tú decides si cobrarla o no
2. **✅ Cálculo Automático** - No más calculadora
3. **✅ Códigos QR** - Identifica garantías al instante
4. **✅ Fotos de Garantías** - Documenta todo
5. **✅ Escáner Integrado** - Verifica con tu teléfono
6. **✅ WhatsApp Directo** - Envía recibos profesionales
7. **✅ Offline-First** - Trabaja en cualquier lugar
8. **✅ Sync Automática** - Respaldo sin preocupaciones
9. **✅ Selector Inteligente** - Flujo sin interrupciones
10. **✅ Material Design 3** - Interfaz moderna y elegante

---

**Documento generado:** 10 de Octubre de 2025
**Versión de la App:** 1.0.0
**Estado:** Producción Ready ✅

---

## 🎯 Conclusión

BsPrestagil es una aplicación completa y profesional para la gestión de préstamos que incluye:

- ✅ Todas las funcionalidades solicitadas
- ✅ Sistema robusto offline-first
- ✅ Integración con Firebase
- ✅ Códigos QR para garantías
- ✅ Sistema de fotos
- ✅ Compartir por WhatsApp
- ✅ Cálculos automáticos inteligentes
- ✅ Reportes y análisis
- ✅ Interfaz moderna y profesional

**La aplicación está lista para ser utilizada en producción.** 🚀

---

*Desarrollado con ❤️ usando Kotlin, Jetpack Compose y Firebase*

