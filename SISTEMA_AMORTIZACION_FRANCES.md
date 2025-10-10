# 📊 Sistema de Amortización Francés - BsPrestagil

## 🎯 ¿Qué es el Sistema Francés?

El **Sistema Francés de Amortización** (también llamado **Método de Anualidad**) es el sistema más utilizado profesionalmente en préstamos bancarios y financieros a nivel mundial.

### Características principales:
✅ **Cuota FIJA** durante todo el préstamo  
✅ **Distribución automática** entre capital e interés  
✅ **Interés decreciente** con el tiempo  
✅ **Capital creciente** con el tiempo  
✅ **Cronograma completo** desde el inicio  

---

## 📐 Fórmula de Cálculo

### Cuota Fija
```
Cuota = P × [i × (1 + i)^n] / [(1 + i)^n - 1]

Donde:
P = Capital prestado (Principal)
i = Tasa de interés por período (decimal, ej: 0.20 para 20%)
n = Número de cuotas
```

### Ejemplo Real
```
Capital:      $100,000
Tasa:         20% mensual
Plazo:        12 meses

Cálculo:
i = 0.20
n = 12
(1 + i)^n = (1.20)^12 = 8.9161

Cuota = 100,000 × [0.20 × 8.9161] / [8.9161 - 1]
Cuota = 100,000 × 1.7832 / 7.9161
Cuota = $22,526.50 (FIJA todos los meses)
```

---

## 📋 Tabla de Amortización

| No. | Cuota      | Capital   | Interés   | Balance    |
|-----|------------|-----------|-----------|------------|
| 1   | $22,526.50 | $2,526.50 | $20,000.00| $97,473.50 |
| 2   | $22,526.50 | $3,031.80 | $19,494.70| $94,441.71 |
| 3   | $22,526.50 | $3,638.15 | $18,888.34| $90,803.55 |
| ... | ...        | ...       | ...       | ...        |
| 12  | $22,526.50 | $18,772.08| $3,754.42 | $0.00      |

### Proceso de cada cuota:

**Cuota 1:**
- Balance inicial: $100,000
- Interés = $100,000 × 20% = $20,000
- Capital = $22,526.50 - $20,000 = $2,526.50
- Balance final = $100,000 - $2,526.50 = $97,473.50

**Cuota 2:**
- Balance inicial: $97,473.50
- Interés = $97,473.50 × 20% = $19,494.70
- Capital = $22,526.50 - $19,494.70 = $3,031.80
- Balance final = $97,473.50 - $3,031.80 = $94,441.70

**Y así sucesivamente...**

---

## 💻 Implementación en BsPrestagil

### 1. Clase `AmortizacionUtils.kt`
```kotlin
// Calcula la cuota fija
fun calcularCuotaFija(
    capital: Double,
    tasaInteresPorPeriodo: Double,
    numeroCuotas: Int
): Double

// Genera tabla completa de amortización
fun generarTablaAmortizacion(
    capitalInicial: Double,
    tasaInteresPorPeriodo: Double,
    numeroCuotas: Int
): List<FilaAmortizacion>

// Calcula totales
fun calcularTotalAPagar()
fun calcularTotalIntereses()
```

### 2. Modelos actualizados
```kotlin
data class Prestamo(
    val montoOriginal: Double,
    val capitalPendiente: Double,
    val tasaInteresPorPeriodo: Double,
    val numeroCuotas: Int,
    val montoCuotaFija: Double, // ⭐ NUEVO campo
    // ...
)

data class Cuota(
    val numeroCuota: Int,
    val fechaVencimiento: Long,
    val montoCuotaMinimo: Double, // = cuota fija
    val capitalPendienteAlInicio: Double,
    // Campos de pago real:
    val montoPagado: Double,
    val montoAInteres: Double,
    val montoACapital: Double,
    val estado: EstadoCuota
)
```

### 3. Generación de cronograma
```kotlin
// Al crear préstamo:
1. Calcular cuota fija con AmortizacionUtils
2. Generar tabla de amortización completa
3. Crear entidades CuotaEntity para cada fila
4. Guardar préstamo + todas las cuotas
```

### 4. Registro de pagos
```kotlin
// Al recibir pago:
1. Validar que monto >= cuota fija
2. Aplicar distribución automática:
   - Interés = capital × tasa
   - Capital = cuota - interés
3. Actualizar cuota correspondiente
4. Actualizar saldo del préstamo
5. Si pago > cuota: abono extraordinario al capital
```

---

## 🔄 Diferencias con el sistema anterior

### ❌ Sistema Anterior (Interés sobre balance)
```
✗ Cuota VARIABLE (mínimo = interés)
✗ Cliente paga "lo que pueda"
✗ No hay cronograma fijo
✗ Difícil predecir duración
✗ Poco profesional
```

### ✅ Sistema Nuevo (Francés)
```
✓ Cuota FIJA profesional
✓ Cliente sabe exactamente cuánto pagar
✓ Cronograma completo desde inicio
✓ Duración exacta (12 meses = 12 cuotas)
✓ Sistema bancario estándar
```

---

## 📊 Ejemplo Práctico

### Préstamo de $50,000 al 10% mensual x 6 meses

```
Cuota fija: $11,443.44

Mes 1: Paga $11,443.44
  → Interés: $5,000.00 (10% de $50,000)
  → Capital: $6,443.44
  → Saldo: $43,556.56

Mes 2: Paga $11,443.44
  → Interés: $4,355.66 (10% de $43,556.56)
  → Capital: $7,087.78
  → Saldo: $36,468.78

Mes 3: Paga $11,443.44
  → Interés: $3,646.88 (10% de $36,468.78)
  → Capital: $7,796.56
  → Saldo: $28,672.22

Mes 4: Paga $11,443.44
  → Interés: $2,867.22 (10% de $28,672.22)
  → Capital: $8,576.22
  → Saldo: $20,096.00

Mes 5: Paga $11,443.44
  → Interés: $2,009.60 (10% de $20,096.00)
  → Capital: $9,433.84
  → Saldo: $10,662.16

Mes 6: Paga $11,443.44
  → Interés: $1,066.22 (10% de $10,662.16)
  → Capital: $10,377.22
  → Saldo: $0.00

Total pagado: $68,660.64
Total intereses: $18,660.64
```

---

## 🎓 Ventajas del Sistema Francés

### Para el cliente:
✅ Sabe exactamente cuánto pagar cada mes  
✅ Puede planificar su presupuesto  
✅ Certeza de la fecha de finalización  
✅ Transparencia total  

### Para el prestamista:
✅ Sistema profesional y confiable  
✅ Fácil seguimiento de mora  
✅ Estándar bancario internacional  
✅ Protección legal  
✅ Reportes precisos  

---

## 🚀 Frecuencias de Pago Disponibles

| Frecuencia | Período  | Ejemplo de tasa |
|------------|----------|-----------------|
| DIARIO     | 1 día    | 1% diario       |
| QUINCENAL  | 15 días  | 10% quincenal   |
| MENSUAL    | 30 días  | 20% mensual     |

---

## ⚠️ Notas Importantes

1. **La cuota es FIJA** y no puede cambiar durante el préstamo
2. **El interés se calcula** sobre el saldo pendiente
3. **Si el cliente paga más** de la cuota, el excedente reduce el capital
4. **Si el cliente paga menos** de la cuota, la cuota queda pendiente
5. **El cronograma se genera** al crear el préstamo
6. **Cada cuota tiene** una fecha de vencimiento específica

---

## 📞 Referencias

- Superintendencia de Bancos - República Dominicana (ProUsuario)
- Sistema de Amortización Francés estándar internacional
- Calculadora de cuotas: [ProUsuario - Cuota de préstamo](https://www.prousuario.gob.do/calculadoras/cuota-prestamo)

---

**✨ BsPrestagil ahora utiliza el sistema profesional de amortización bancaria ✨**

