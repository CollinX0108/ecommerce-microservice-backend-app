# Guía de Pruebas - Patrones de Diseño Implementados

## 🧪 Preparación del Entorno de Pruebas

### Requisitos Previos
1. **Servicios ejecutándose**:
   ```bash
   # Verificar que los servicios estén corriendo
   docker-compose up -d
   # O si usas Maven
   mvn spring-boot:run -pl proxy-client
   mvn spring-boot:run -pl payment-service
   ```

2. **Verificar conectividad**:
   ```bash
   curl http://localhost:8900/app/actuator/health
   curl http://localhost:8400/payment-service/actuator/health
   ```

---

## 🔧 1. Pruebas de Feature Toggle Pattern

### Objetivo: Verificar que las características se pueden habilitar/deshabilitar dinámicamente

### Prueba 1: Verificar Estado Actual de Features
```bash
curl -X GET http://localhost:8900/app/api/payments/features
```

**Resultado esperado**:
```
Active Features:
Enhanced Payment Processing: true
Product Recommendations: false
Advanced Order Tracking: false
Circuit Breaker Monitoring: true
Experimental User Dashboard: false
```

### Prueba 2: Cambiar Feature Toggle en Runtime
1. **Modificar application.yml**:
   ```yaml
   features:
     enhanced-payment-processing: false  # Cambiar a false
     product-recommendations: true       # Cambiar a true
   ```

2. **Reiniciar solo el proxy-client**:
   ```bash
   mvn spring-boot:run -pl proxy-client
   ```

3. **Verificar cambios**:
   ```bash
   curl -X GET http://localhost:8900/app/api/payments/features
   ```

4. **Hacer una llamada y verificar logs**:
   ```bash
   curl -X POST http://localhost:8900/app/api/payments \
        -H "Content-Type: application/json" \
        -d '{
          "paymentId": 1,
          "orderId": 1,
          "paymentStatus": "ACCEPTED",
          "amount": 100.0,
          "paymentMethod": "CREDIT_CARD"
        }'
   ```

**Resultado esperado en logs**:
```
INFO - Creating new payment
INFO - Using standard payment processing  # Porque enhanced-payment está en false
```

### ✅ **Criterio de Éxito**: Los logs cambian según el estado de la feature flag

---

## ⚡ 2. Pruebas de Circuit Breaker Pattern

### Objetivo: Verificar que el Circuit Breaker se abre cuando el servicio falla y ejecuta fallbacks

### Prueba 1: Estado Normal (Circuit Breaker CLOSED)
```bash
# Verificar estado inicial
curl http://localhost:8900/app/actuator/health | jq '.components.circuitBreakers'
```

**Resultado esperado**:
```json
{
  "status": "UP",
  "details": {
    "paymentService": {
      "status": "UP",
      "details": {
        "state": "CLOSED",
        "failureRate": "0.0%"
      }
    }
  }
}
```

### Prueba 2: Simular Fallo del Payment Service
1. **Detener el Payment Service**:
   ```bash
   # Si usas Docker Compose
   docker-compose stop payment-service-container
   
   # Si usas Maven, detener el proceso del payment-service
   ```

2. **Hacer múltiples llamadas para activar el Circuit Breaker**:
   ```bash
   # Hacer 6 llamadas (threshold es 5)
   for i in {1..6}; do
     echo "Llamada $i:"
     curl -X POST http://localhost:8900/app/api/payments \
          -H "Content-Type: application/json" \
          -d '{
            "paymentId": '$i',
            "orderId": 1,
            "paymentStatus": "ACCEPTED",
            "amount": 100.0,
            "paymentMethod": "CREDIT_CARD"
          }'
     echo -e "\n---"
     sleep 1
   done
   ```

3. **Verificar que el Circuit Breaker se abre**:
   ```bash
   curl http://localhost:8900/app/actuator/health | jq '.components.circuitBreakers'
   ```

**Resultado esperado**:
```json
{
  "status": "DOWN",
  "details": {
    "paymentService": {
      "status": "DOWN",
      "details": {
        "state": "OPEN",
        "failureRate": "100.0%"
      }
    }
  }
}
```

### Prueba 3: Verificar Fallback Method
```bash
curl -X POST http://localhost:8900/app/api/payments \
     -H "Content-Type: application/json" \
     -d '{
       "paymentId": 99,
       "orderId": 1,
       "paymentStatus": "ACCEPTED",
       "amount": 100.0,
       "paymentMethod": "CREDIT_CARD"
     }'
```

**Resultado esperado**:
- **HTTP Status**: `503 Service Unavailable`
- **Logs**: `ERROR - Payment service is unavailable. Using fallback for save`
- **Respuesta inmediata** (no timeout)

### Prueba 4: Recuperación Automática
1. **Reiniciar Payment Service**:
   ```bash
   docker-compose start payment-service-container
   ```

2. **Esperar 5 segundos** (wait-duration-in-open-state)

3. **Hacer llamadas de prueba**:
   ```bash
   # El Circuit Breaker pasa a HALF_OPEN
   curl -X POST http://localhost:8900/app/api/payments \
        -H "Content-Type: application/json" \
        -d '{
          "paymentId": 100,
          "orderId": 1,
          "paymentStatus": "ACCEPTED",
          "amount": 100.0,
          "paymentMethod": "CREDIT_CARD"
        }'
   ```

4. **Verificar estado**:
   ```bash
   curl http://localhost:8900/app/actuator/health | jq '.components.circuitBreakers'
   ```

**Resultado esperado**: State cambia de `OPEN` → `HALF_OPEN` → `CLOSED`

### ✅ **Criterio de Éxito**: Circuit Breaker se abre, ejecuta fallbacks y se recupera automáticamente

---

## 🏗️ 3. Pruebas de Bulkhead Pattern

### Objetivo: Verificar que el aislamiento de recursos funciona correctamente

### Prueba 1: Verificar Configuración de Bulkhead
```bash
curl http://localhost:8900/app/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls | jq '.'
```

**Resultado esperado**:
```json
{
  "name": "resilience4j.bulkhead.available.concurrent.calls",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 10.0
    }
  ],
  "availableTags": [
    {
      "tag": "name",
      "values": ["paymentService"]
    }
  ]
}
```

### Prueba 2: Saturar el Bulkhead
1. **Crear script de carga paralela**:
   ```bash
   # Crear archivo test-bulkhead.sh
   #!/bin/bash
   
   echo "Iniciando prueba de Bulkhead - 15 llamadas paralelas"
   
   for i in {1..15}; do
     {
       echo "Iniciando llamada $i a $(date)"
       curl -X POST http://localhost:8900/app/api/payments \
            -H "Content-Type: application/json" \
            -d '{
              "paymentId": '$i',
              "orderId": 1,
              "paymentStatus": "ACCEPTED",
              "amount": 100.0,
              "paymentMethod": "CREDIT_CARD"
            }' \
            -w "Llamada $i completada en %{time_total}s\n"
     } &
   done
   
   wait
   echo "Todas las llamadas completadas"
   ```

2. **Ejecutar el script**:
   ```bash
   chmod +x test-bulkhead.sh
   ./test-bulkhead.sh
   ```

3. **Monitorear métricas durante la prueba**:
   ```bash
   # En otra terminal, ejecutar cada segundo
   watch -n 1 'curl -s http://localhost:8900/app/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls | jq ".measurements[0].value"'
   ```

**Resultado esperado**:
- Máximo 10 llamadas ejecutándose simultáneamente
- Llamadas 11-15 esperan o se rechazan según `max-wait-duration`
- Métricas muestran que `available.concurrent.calls` baja de 10 a 0 y vuelve a subir

### Prueba 3: Verificar Aislamiento entre Servicios
1. **Saturar Payment Service** (con el script anterior)

2. **En paralelo, probar otro servicio**:
   ```bash
   curl -X POST http://localhost:8900/app/api/orders \
        -H "Content-Type: application/json" \
        -d '{
          "orderId": 1,
          "userId": 1,
          "orderStatus": "PENDING"
        }'
   ```

**Resultado esperado**: Order Service responde normalmente aunque Payment esté saturado

### ✅ **Criterio de Éxito**: Solo 10 llamadas simultáneas a Payment, otros servicios no afectados

---

## 🔄 4. Pruebas de Retry Pattern

### Objetivo: Verificar que los reintentos funcionan con backoff exponencial

### Prueba 1: Simular Fallos Temporales
1. **Modificar Payment Service para simular inestabilidad**:
   - Detener y reiniciar cada 2 segundos:
   ```bash
   # Script para simular inestabilidad
   #!/bin/bash
   for i in {1..5}; do
     echo "Deteniendo servicio..."
     docker-compose stop payment-service-container
     sleep 2
     echo "Reiniciando servicio..."
     docker-compose start payment-service-container
     sleep 3
   done
   ```

2. **Hacer llamadas durante la inestabilidad**:
   ```bash
   curl -X POST http://localhost:8900/app/api/payments \
        -H "Content-Type: application/json" \
        -d '{
          "paymentId": 999,
          "orderId": 1,
          "paymentStatus": "ACCEPTED",
          "amount": 100.0,
          "paymentMethod": "CREDIT_CARD"
        }' \
        -w "Tiempo total: %{time_total}s\n"
   ```

### Prueba 2: Verificar Métricas de Retry
```bash
curl http://localhost:8900/app/actuator/metrics/resilience4j.retry.calls | jq '.'
```

**Resultado esperado**:
```json
{
  "name": "resilience4j.retry.calls",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 3.0
    }
  ],
  "availableTags": [
    {
      "tag": "name",
      "values": ["paymentService"]
    },
    {
      "tag": "kind",
      "values": ["successful_with_retry", "failed_with_retry"]
    }
  ]
}
```

### ✅ **Criterio de Éxito**: Llamadas se reintentan 3 veces con delays crecientes (1s, 2s, 4s)

---

## 📊 5. Pruebas de Integración de Todos los Patrones

### Objetivo: Verificar que todos los patrones trabajen juntos correctamente

### Escenario Completo: "Fallo y Recuperación del Sistema"

1. **Estado inicial - Todo funcionando**:
   ```bash
   # Verificar Feature Toggles
   curl http://localhost:8900/app/api/payments/features
   
   # Verificar Circuit Breaker cerrado
   curl http://localhost:8900/app/actuator/health | jq '.components.circuitBreakers'
   
   # Verificar Bulkhead disponible
   curl http://localhost:8900/app/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls
   ```

2. **Simular carga alta y fallo**:
   ```bash
   # Saturar el sistema y luego detener payment service
   ./test-bulkhead.sh &
   sleep 2
   docker-compose stop payment-service-container
   ```

3. **Verificar comportamiento durante fallo**:
   ```bash
   # Debería usar fallback inmediatamente
   curl -X POST http://localhost:8900/app/api/payments \
        -H "Content-Type: application/json" \
        -d '{"paymentId": 777, "amount": 50.0}'
   ```

4. **Recuperar servicio y verificar**:
   ```bash
   docker-compose start payment-service-container
   sleep 6  # Esperar recuperación
   
   # Verificar que vuelve a funcionar
   curl -X POST http://localhost:8900/app/api/payments \
        -H "Content-Type: application/json" \
        -d '{"paymentId": 888, "amount": 75.0}'
   ```

### ✅ **Criterio de Éxito**: Sistema responde en todos los escenarios sin colgarse

---

## 📈 6. Dashboard de Monitoreo

### Métricas Clave para Monitorear
```bash
# Circuit Breaker
curl http://localhost:8900/app/actuator/metrics/resilience4j.circuitbreaker.state
curl http://localhost:8900/app/actuator/metrics/resilience4j.circuitbreaker.failure.rate

# Bulkhead
curl http://localhost:8900/app/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls
curl http://localhost:8900/app/actuator/metrics/resilience4j.bulkhead.max.allowed.concurrent.calls

# Retry
curl http://localhost:8900/app/actuator/metrics/resilience4j.retry.calls

# Feature Toggles
curl http://localhost:8900/app/api/payments/features

# Health General
curl http://localhost:8900/app/actuator/health
```

---

## 🎯 Checklist Final de Validación

### ✅ Feature Toggle Pattern
- [ ] Features se leen correctamente de application.yml
- [ ] Endpoint `/features` muestra estado actual
- [ ] Logs cambian según feature flags
- [ ] Cambios en configuración se reflejan tras reinicio

### ✅ Circuit Breaker Pattern
- [ ] Estado CLOSED en condiciones normales
- [ ] Se abre tras 5 fallos (50% threshold)
- [ ] Ejecuta fallback methods cuando está abierto
- [ ] Se recupera automáticamente tras 5 segundos
- [ ] Transición CLOSED → OPEN → HALF_OPEN → CLOSED

### ✅ Bulkhead Pattern
- [ ] Máximo 10 llamadas concurrentes a Payment Service
- [ ] Otras servicios no afectados por saturación
- [ ] Métricas muestran threads disponibles/usados
- [ ] Llamadas adicionales esperan o se rechazan

### ✅ Retry Pattern
- [ ] Reintenta 3 veces en fallos temporales
- [ ] Backoff exponencial: 1s, 2s, 4s
- [ ] Solo reintenta excepciones específicas
- [ ] Métricas muestran intentos exitosos/fallidos

### ✅ Integración
- [ ] Todos los patrones funcionan juntos
- [ ] Sistema resiliente en escenarios complejos
- [ ] Monitoreo completo disponible
- [ ] Performance aceptable bajo carga

---

## 🚀 Comandos Rápidos para Demo

```bash
# Demo completo en 5 minutos
echo "=== INICIANDO DEMO DE PATRONES ==="

echo "1. Feature Toggles:"
curl -s http://localhost:8900/app/api/payments/features

echo -e "\n2. Circuit Breaker (estado normal):"
curl -s http://localhost:8900/app/actuator/health | jq '.components.circuitBreakers.details.paymentService.details.state'

echo -e "\n3. Simulando fallo..."
docker-compose stop payment-service-container

echo -e "\n4. Verificando fallback (después de 6 llamadas):"
for i in {1..6}; do curl -s -X POST http://localhost:8900/app/api/payments -H "Content-Type: application/json" -d '{"paymentId":'$i'}' | head -c 50; echo; done

echo -e "\n5. Circuit Breaker abierto:"
curl -s http://localhost:8900/app/actuator/health | jq '.components.circuitBreakers.details.paymentService.details.state'

echo -e "\n6. Recuperando servicio..."
docker-compose start payment-service-container

echo -e "\n=== DEMO COMPLETADO ==="
```

¡Con estas pruebas puedes validar completamente que todos los patrones funcionen correctamente! 🎯 