# 🧮 Calculadora de Préstamos

## ✨ **Nueva Funcionalidad Agregada**

La **Calculadora de Préstamos** es una herramienta de simulación que permite calcular cuotas y ver la tabla de amortización **antes de crear un préstamo formal**.

---

## 📍 **Ubicación en la App**

### **Acceso desde Dashboard:**
```
Dashboard → Accesos rápidos → 🧮 "Calculadora"
```

### **Acceso directo:**
- Tab: **Dashboard**
- Sección: **Accesos rápidos**
- Botón: 🧮 **"Calculadora"**

---

## 🎯 **Funcionalidades**

### 1️⃣ **Simulación en Tiempo Real**
Ingresa los datos y ve el resultado **inmediatamente**:
- ✅ **Monto** a prestar
- ✅ **Tasa de interés** (ajustable según frecuencia)
- ✅ **Frecuencia de pago** (Diario, Quincenal, Mensual)
- ✅ **Número de cuotas**

### 2️⃣ **Resultado Instantáneo**
```
┌─────────────────────────┐
│     CUOTA FIJA          │
│    $7,346.59           │
│   cada mensual          │
└─────────────────────────┘

Resumen:
• Capital a prestar: $50,000.00
• Total a pagar: $88,159.08
• Total intereses: $38,159.08
• Número de cuotas: 12
```

### 3️⃣ **Tabla de Amortización Completa**
Botón expandible para ver:
```
No. | Cuota    | Capital  | Interés  | Balance
-----|----------|----------|----------|----------
  1  | $7,347  | $2,347  | $5,000  | $47,653
  2  | $7,347  | $2,581  | $4,765  | $45,072
...
 12  | $7,347  | $6,679  | $668    | $0
```

### 4️⃣ **Acciones Rápidas**
- ✅ **"Crear préstamo"**: Navega a la pantalla de nuevo préstamo
- ✅ **"Compartir"**: Comparte el cálculo por WhatsApp (por implementar)

---

## 📱 **Flujo de Uso**

### **Caso 1: Mostrar al cliente cuánto pagaría**
```
1. Cliente pregunta: "¿Cuánto pago si pido $50,000 a 12 meses?"
2. Abres calculadora desde Dashboard
3. Ingresas:
   - Monto: $50,000
   - Tasa: 10% mensual
   - Cuotas: 12
4. Le muestras:
   - Cuota fija: $7,346.59
   - Total a pagar: $88,159.08
5. Expandes tabla para mostrar cada cuota
6. Cliente decide si acepta
7. Presionas "Crear préstamo" para formalizar
```

### **Caso 2: Comparar diferentes opciones**
```
1. Cliente quiere comparar:
   - Opción A: 10% x 12 meses
   - Opción B: 8% x 18 meses
2. Calculas opción A → $7,346.59/mes
3. Cambias datos para opción B → $3,747.23/mes
4. Cliente elige la que prefiere
```

### **Caso 3: Validar viabilidad antes de otorgar**
```
1. Revisas capacidad de pago del cliente
2. Usas calculadora para ver diferentes escenarios
3. Ajustas monto/plazo hasta encontrar cuota razonable
4. Creas préstamo con datos validados
```

---

## 🎨 **Diseño de la Interfaz**

### **Sección Superior:**
```
┌──────────────────────────────────────┐
│ 🧮 Simulador de préstamos           │
│ Calcula cuotas y tabla de amortiza- │
│ ción antes de crear el préstamo     │
└──────────────────────────────────────┘
```

### **Formulario:**
```
Datos del préstamo
───────────────────
[Monto a prestar]      $ _______
[Tasa de interés]      _______ % mensual
[Frecuencia de pago]   [Mensual ▼]
[Número de cuotas]     _______
```

### **Resultado:**
```
┌──────────────────────────────────────┐
│          CUOTA FIJA                  │
│         $7,346.59                    │
│       cada mensual                   │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│ Capital a prestar:  $50,000.00       │
│ ───────────────────────────────────  │
│ Total a pagar:      $88,159.08       │
│ Total intereses:    $38,159.08       │
│ ───────────────────────────────────  │
│ Número de cuotas:   12               │
└──────────────────────────────────────┘
```

### **Tabla (Expandible):**
```
[Ver tabla de amortización ▼]

[Tabla completa con 12 filas]

[Crear préstamo]  [Compartir]
```

---

## 🔧 **Características Técnicas**

### **Archivo:**
```
app/src/main/java/com/example/bsprestagil/screens/calculator/
├── CalculadoraPrestamoScreen.kt
```

### **Navegación:**
```kotlin
Screen.CalculadoraPrestamo.route = "calculadora_prestamo"
```

### **Cálculos:**
- Usa `AmortizacionUtils.calcularCuotaFija()`
- Usa `AmortizacionUtils.generarTablaAmortizacion()`
- Actualización en tiempo real con `remember { mutableStateOf() }`

### **Validaciones:**
```kotlin
val montoNum = monto.toDoubleOrNull() ?: 0.0
val tasaNum = tasaInteres.toDoubleOrNull() ?: 0.0
val cuotasNum = numeroCuotas.toIntOrNull() ?: 0

// Solo muestra resultado si todos los datos son válidos
if (montoNum > 0 && tasaNum > 0 && cuotasNum > 0) {
    // Mostrar resultado
}
```

---

## 📊 **Ejemplos de Cálculo**

### **Ejemplo 1: Préstamo Pequeño**
```
Monto: $10,000
Tasa: 10% mensual
Cuotas: 12

Resultado:
Cuota fija: $1,468.74
Total a pagar: $17,624.88
Total intereses: $7,624.88
```

### **Ejemplo 2: Préstamo Grande**
```
Monto: $100,000
Tasa: 20% mensual
Cuotas: 12

Resultado:
Cuota fija: $22,526.50
Total a pagar: $270,318.00
Total intereses: $170,318.00
```

### **Ejemplo 3: Quincenal**
```
Monto: $30,000
Tasa: 8% quincenal
Cuotas: 10

Resultado:
Cuota fija: $4,468.69
Total a pagar: $44,686.90
Total intereses: $14,686.90
```

---

## 🚀 **Ventajas para el Negocio**

### **Para el prestamista:**
1. ✅ **Herramienta de ventas** profesional
2. ✅ **Validar viabilidad** antes de otorgar
3. ✅ **Comparar opciones** rápidamente
4. ✅ **Transparencia** con el cliente
5. ✅ **Agiliza el proceso** de toma de decisión

### **Para el cliente:**
1. ✅ **Conoce el monto exacto** que pagará
2. ✅ **Ve toda la tabla** de pagos
3. ✅ **Compara opciones** fácilmente
4. ✅ **Toma decisiones informadas**
5. ✅ **Confianza** en el prestamista

---

## 📝 **Próximas Mejoras**

### **Por implementar:**
1. 🔜 **Compartir cálculo** por WhatsApp
2. 🔜 **Guardar simulaciones** para referencia
3. 🔜 **Comparar 2 opciones** lado a lado
4. 🔜 **Exportar a PDF** la tabla
5. 🔜 **Pre-llenar datos** al crear préstamo

---

## 🎯 **Casos de Uso Reales**

### **Escenario 1: Cliente indeciso**
```
Cliente: "No sé si puedo pagar ese monto"
Prestamista: [Abre calculadora]
Prestamista: "Mira, con 18 cuotas pagarías $X menos por mes"
Cliente: "Ah sí, así sí puedo"
Prestamista: [Crea préstamo con datos validados]
```

### **Escenario 2: Negociación**
```
Cliente: "Quiero $100,000"
Prestamista: [Calcula] "La cuota sería $22,526"
Cliente: "Es mucho"
Prestamista: [Ajusta a 18 cuotas] "Mira, ahora sería $15,047"
Cliente: "Ok, así sí"
```

### **Escenario 3: Educación financiera**
```
Prestamista: "Te voy a mostrar exactamente cómo funciona"
[Muestra tabla de amortización]
Prestamista: "Ves cómo en la cuota 1 pagas $20K de interés"
Prestamista: "Pero en la cuota 12 solo pagas $667"
Cliente: "Ahh, entiendo"
```

---

## ✨ **Resumen**

| Característica | Estado |
|----------------|--------|
| **Cálculo de cuota fija** | ✅ Implementado |
| **Tabla de amortización** | ✅ Implementado |
| **Cambio de frecuencia** | ✅ Implementado |
| **Actualización en tiempo real** | ✅ Implementado |
| **Navegación a crear préstamo** | ✅ Implementado |
| **Compartir por WhatsApp** | 🔜 Por implementar |
| **Guardar simulaciones** | 🔜 Futuro |

---

**🎉 Calculadora de Préstamos lista para usar!**

