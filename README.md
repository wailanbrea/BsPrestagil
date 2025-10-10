# BsPrestagil - Sistema de Gestión de Préstamos

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

BsPrestagil es una aplicación móvil Android moderna para la gestión integral de préstamos, diseñada específicamente para prestamistas y empresas de microcréditos.

## 📱 Características Principales

### 🔐 Autenticación
- Login y registro de usuarios
- Roles: Prestamista y Cobrador
- Recuperación de contraseña

### 📊 Dashboard
- Resumen ejecutivo del negocio
- Indicadores clave: capital prestado, intereses generados, cartera vencida
- Vista rápida de préstamos activos y atrasados
- Accesos rápidos a funciones principales

### 👥 Gestión de Clientes
- Lista completa de clientes con búsqueda
- Perfil detallado con información de contacto
- Historial completo de préstamos
- Referencias personales
- Estado de pagos (Al día, Atrasado, Moroso)

### 💰 Gestión de Préstamos
- Crear nuevos préstamos con tasa de interés personalizable
- Configurar plazo y frecuencia de pago (Diario, Semanal, Quincenal, Mensual)
- Seguimiento del estado del préstamo
- Cronograma de pagos
- Cálculo automático de intereses
- Filtrado por estado (Activo, Atrasado, Completado, Cancelado)

### 🔒 Gestión de Garantías
- Registro de bienes como respaldo
- Tipos: Vehículos, Electrodomésticos, Electrónicos, Joyas, Muebles
- Almacenamiento de fotos y descripciones
- Estados: Retenida, Devuelta, Ejecutada
- Valor estimado de la garantía

### 💳 Registro de Pagos
- Captura rápida de pagos
- Múltiples métodos: Efectivo, Transferencia, Tarjeta
- Cálculo automático de moras
- Generación de recibos
- Historial completo de transacciones
- Envío de recibos por WhatsApp

### 📈 Reportes
- Resumen de cobros por período
- Análisis de cartera
- Estadísticas de clientes
- Tasa de morosidad
- Exportación a Excel/PDF

### 🔔 Notificaciones
- Recordatorios de pagos próximos
- Alertas de pagos vencidos
- Notificaciones de nuevos clientes
- Confirmación de pagos recibidos

### ⚙️ Configuración
- Tasa de interés base personalizable
- Configuración de tasa de mora
- Personalización de recibos con logo
- Gestión de usuarios cobradores
- Activación/desactivación de notificaciones
- Integración con WhatsApp

## 🎨 Diseño

La aplicación cuenta con un diseño moderno basado en Material Design 3 con:
- Tema claro y oscuro automático
- Paleta de colores personalizada (#1173D4 como color principal)
- Interfaz intuitiva y fácil de usar
- Navegación inferior para acceso rápido
- Animaciones fluidas

## 🏗️ Arquitectura

### Stack Tecnológico
- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navegación**: Navigation Compose
- **Arquitectura**: MVVM (preparado para implementación)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36

### Estructura del Proyecto
```
app/src/main/java/com/example/bsprestagil/
├── components/           # Componentes reutilizables
│   ├── BottomNavigationBar.kt
│   ├── TopAppBarComponent.kt
│   └── Cards.kt
├── data/
│   └── models/          # Modelos de datos
│       ├── Cliente.kt
│       ├── Prestamo.kt
│       ├── Pago.kt
│       ├── Garantia.kt
│       ├── Usuario.kt
│       └── Configuracion.kt
├── navigation/          # Sistema de navegación
│   ├── Screen.kt
│   └── NavGraph.kt
├── screens/            # Pantallas de la aplicación
│   ├── auth/          # Autenticación
│   ├── dashboard/     # Panel principal
│   ├── clients/       # Gestión de clientes
│   ├── loans/         # Gestión de préstamos
│   ├── payments/      # Pagos y cobros
│   ├── collaterals/   # Garantías
│   ├── reports/       # Reportes
│   ├── notifications/ # Notificaciones
│   └── settings/      # Configuración
├── ui/theme/          # Tema y estilos
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── MainActivity.kt
```

## 🚀 Instalación

1. Clona el repositorio:
```bash
git clone https://github.com/wailanbrea/BsPrestagil.git
```

2. Abre el proyecto en Android Studio

3. Sincroniza el proyecto con Gradle

4. Ejecuta la aplicación en un emulador o dispositivo físico

## 📋 Requisitos

- Android Studio Hedgehog o superior
- JDK 11 o superior
- Android SDK 26 o superior
- Dispositivo o emulador con Android 8.0 (API 26) o superior

## 🔧 Configuración

La aplicación permite configurar:
- Tasa de interés base predeterminada
- Información del negocio
- Logo y marca personalizada
- Métodos de pago disponibles
- Configuración de notificaciones

## 📱 Pantallas Principales

1. **Login/Registro** - Autenticación de usuarios
2. **Dashboard** - Vista general del negocio
3. **Clientes** - Lista y gestión de clientes
4. **Préstamos** - Creación y seguimiento de préstamos
5. **Pagos** - Registro de cobros
6. **Garantías** - Gestión de bienes en garantía
7. **Reportes** - Análisis y estadísticas
8. **Notificaciones** - Alertas y recordatorios
9. **Configuración** - Ajustes del sistema

## 🎯 Próximas Funcionalidades

- [ ] Integración con backend/base de datos
- [ ] Sincronización en la nube
- [ ] Generación de PDF para recibos
- [ ] Integración real con WhatsApp API
- [ ] Envío de SMS
- [ ] Gráficas y estadísticas avanzadas
- [ ] Exportación de reportes
- [ ] Copia de seguridad automática
- [ ] Modo offline
- [ ] Biométrica para autenticación

## 👨‍💻 Desarrollo

Este proyecto fue desarrollado con:
- **Jetpack Compose** para UI declarativa
- **Material Design 3** para un diseño moderno
- **Navigation Component** para navegación type-safe
- **Kotlin** como lenguaje principal

## 📄 Licencia

Este proyecto es privado y propiedad de BsPrestagil.

## 📞 Contacto

Para más información o soporte, contacta a través del repositorio de GitHub.

---

**Nota**: Esta es la versión inicial de la aplicación. Los datos mostrados son de ejemplo para demostración. La integración con una base de datos real está pendiente de implementación.

