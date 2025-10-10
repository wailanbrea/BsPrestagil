# 🧪 Instrucciones para Probar el Nuevo Sistema de Amortización

## 📱 Compilar y Ejecutar

### Opción 1: Desde Android Studio (Recomendado)
1. Abre el proyecto en **Android Studio**
2. Espera a que sincronice las dependencias
3. Conecta un dispositivo Android o inicia un emulador
4. Click en **Run** ▶️ (o presiona `Shift + F10`)

### Opción 2: Desde línea de comandos
```bash
# Si tienes Android Studio instalado, configura JAVA_HOME:
# Windows:
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
.\gradlew.bat assembleDebug

# Instalar en dispositivo conectado:
.\gradlew.bat installDebug
```

---

## 🎯 Pasos para Probar el Sistema

### 1️⃣ **Pantalla de Pruebas (TestSync)**

La app incluye una pantalla especial para probar rápidamente:

**Acceso:**
- Desde Dashboard → Botón de menú (⋮) → **NO aparece en menú principal**
- La pantalla `TestSyncScreen` fue **removida del Dashboard** por tu solicitud anterior
- Para acceder, necesitas volver a agregar el botón en `DashboardScreen.kt`

**O puedes usar el flujo normal:**

---

### 2️⃣ **Flujo Normal de Prueba**

#### **A) Crear un Cliente**
1. Abre la app
2. Ve a **"Clientes"** en el menú inferior
3. Presiona el botón **"+"** (agregar)
4. Llena el formulario:
   ```
   Nombre: María Rodríguez López
   Teléfono: 809-555-1234
   Dirección: Av. Independencia #456, Santo Domingo
   Email: maria.rodriguez@email.com
   ```
5. Presiona **"Guardar"**

#### **B) Crear un Préstamo con Sistema Francés**
1. Desde la lista de clientes, selecciona **María Rodríguez**
2. Presiona **"Nuevo préstamo"** o usa el botón **"+"** en Préstamos
3. Llena los datos del préstamo:
   ```
   Cliente: María Rodríguez López
   Monto: 50000
   Tasa de interés: 10% (mensual o quincenal)
   Frecuencia: Mensual
   Número de cuotas: 12
   ```
4. Verás el **resumen antes de confirmar**:
   ```
   Capital: $50,000.00
   Tasa: 10% mensual
   Número de cuotas: 12
   
   CUOTA FIJA: $7,346.59
   
   Total a pagar: $88,159.08
   Total intereses: $38,159.08
   ```
5. Presiona **"Confirmar"**

#### **C) Ver el Cronograma Completo**
1. Ve a **"Préstamos"** en el menú inferior
2. Selecciona el préstamo que acabas de crear
3. En **"Detalles del préstamo"** verás:
   - Capital original: $50,000
   - Capital pendiente: $50,000
   - Cuota fija: $7,346.59
   - Total intereses pagados: $0
   
4. Desplázate hacia abajo hasta **"Cronograma de cuotas"**
5. Verás las **12 cuotas** con:
   ```
   Cuota 1 - Vencimiento: 10/11/2025
   Cuota fija: $7,346.59
   Estado: PENDIENTE
   Notas: Interés proyectado: $5,000.00, Capital: $2,346.59
   
   Cuota 2 - Vencimiento: 10/12/2025
   Cuota fija: $7,346.59
   Estado: PENDIENTE
   Notas: Interés proyectado: $4,765.34, Capital: $2,581.25
   
   ... y así hasta la cuota 12
   ```

#### **D) Registrar un Pago**
1. Desde los detalles del préstamo, presiona **"Registrar pago"**
2. Verás automáticamente:
   ```
   Cuota 1 de 12
   Monto sugerido: $7,346.59 (la cuota fija)
   ```
3. Puedes:
   - **Pagar la cuota exacta:** $7,346.59
   - **Pagar más (abono extraordinario):** $10,000 → $2,653.41 irán al capital
   - **Pagar menos:** Sistema te alertará (en versión futura)
   
4. Selecciona método de pago: **Efectivo**
5. Presiona **"Registrar pago"**

#### **E) Ver Distribución del Pago**
1. Ve a **"Pagos"** en el menú inferior
2. Selecciona el pago que registraste
3. Verás el **detalle completo**:
   ```
   Total pagado: $7,346.59
   → A interés: $5,000.00
   → A capital: $2,346.59
   → Mora: $0.00
   
   Días transcurridos: 30 días
   Capital pendiente: $47,653.41
   ```

#### **F) Verificar Actualización del Préstamo**
1. Regresa a **"Préstamos"** → Selecciona el préstamo
2. Verás:
   ```
   Capital pendiente: $47,653.41 (antes: $50,000)
   Total capital pagado: $2,346.59
   Total intereses pagados: $5,000.00
   Cuotas pagadas: 1 de 12
   
   Progreso: 5% (barra verde)
   ```
3. En el cronograma:
   ```
   Cuota 1: ✅ PAGADA
   Cuota 2: PENDIENTE (próxima)
   ```

---

## 📊 Casos de Prueba Específicos

### Caso 1: Préstamo Quincenal
```
Monto: $30,000
Tasa: 8% quincenal
Cuotas: 10
Cuota fija: $4,468.69
```

### Caso 2: Préstamo Mensual Largo Plazo
```
Monto: $100,000
Tasa: 20% mensual
Cuotas: 12
Cuota fija: $22,526.50
(Exacto al ejemplo de ProUsuario)
```

### Caso 3: Préstamo con Abono Extraordinario
```
1. Crea préstamo de $20,000 al 15% x 6 meses
2. Cuota fija: $4,764.21
3. En cuota 1, paga $7,000
   → $3,000 interés + $1,764.21 capital normal + $2,235.79 abono extra
4. Capital pendiente queda en $15,999.79 (en vez de $17,235.79)
```

---

## ✅ Verificaciones Importantes

### Verifica que:
1. ✅ La **cuota fija** se calcula correctamente
2. ✅ El **cronograma completo** se genera al crear el préstamo
3. ✅ Cada cuota muestra la **distribución proyectada** de capital/interés
4. ✅ Al registrar un pago, se **actualiza la cuota correspondiente**
5. ✅ El **saldo del préstamo** se reduce correctamente
6. ✅ Los **reportes** muestran intereses y capital por separado
7. ✅ La **sincronización con Firebase** incluye el nuevo campo `montoCuotaFija`

---

## 🐛 Reportar Problemas

Si encuentras algún error:
1. Anota el **mensaje de error exacto**
2. Los **pasos** que seguiste
3. Los **datos** que ingresaste
4. **Capturas de pantalla** si es posible

---

## 📚 Documentación Adicional

- **Sistema de Amortización:** `SISTEMA_AMORTIZACION_FRANCES.md`
- **Módulos de la app:** `MODULOS_BSPRESTAGIL.md`
- **Documentación completa:** `DOCUMENTACION_BSPRESTAGIL.md`

---

**¡Listo para probar el sistema profesional de préstamos! 🚀**

