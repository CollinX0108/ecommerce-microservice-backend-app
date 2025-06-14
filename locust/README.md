# 🚀 Pruebas de Rendimiento con Locust

## 📋 Descripción

Este directorio contiene todas las pruebas de rendimiento y estrés para el sistema de microservicios de e-commerce utilizando **Locust**.

## 🎯 Tipos de Pruebas Disponibles

### 🌐 Pruebas del Sistema Completo
- **Carga Ligera**: 50 usuarios, 10 minutos
- **Carga Normal**: 100 usuarios, 15 minutos
- **Carga Pesada**: 200 usuarios, 20 minutos
- **Prueba de Estrés**: 300 usuarios, 15 minutos
- **Prueba de Picos**: 500 usuarios, 10 minutos
- **Prueba de Resistencia**: 150 usuarios, 60 minutos

### 🎯 Pruebas por Microservicio
- **Product Service**: Navegación intensiva de productos
- **User Service**: Operaciones CRUD de usuarios
- **Order Service**: Gestión de órdenes y carritos
- **Payment Service**: Procesamiento de pagos
- **Favourite Service**: Gestión de favoritos

## 🚀 Inicio Rápido

### 1. Instalación
```bash
# Instalar dependencias
pip install -r requirements.txt
```

### 2. Script de Rendimiento (NUEVO) 🎉
```bash
cd scripts

# Script único de pruebas de rendimiento
./run_performance_tests.sh
```

### 3. Prueba Rápida de Demostración
```bash
cd scripts
python quick_test.py
```

### 4. Pruebas Automatizadas Python
```bash
cd scripts

# Prueba normal del sistema
python run_load_tests.py --type normal

# Prueba específica de un servicio
python run_load_tests.py --service product-service

# Todas las pruebas de servicios
python run_load_tests.py --all-services
```

### 5. Ejecución Manual
```bash
# Prueba completa del sistema
locust -f test/ecommerce-complete/ecommerce_load_test.py \
       --headless -u 100 -r 10 --run-time 15m \
       --host http://localhost:9081 \
       --html reports/system_test.html

# Interfaz web interactiva
locust -f test/ecommerce-complete/ecommerce_load_test.py \
       --host http://localhost:9081
# Abrir http://localhost:8089
```

## 📁 Estructura de Archivos

```
locust/
├── test/
│   ├── ecommerce-complete/     # Pruebas completas del sistema
│   ├── product-service/        # Pruebas específicas de productos
│   ├── user-service/          # Pruebas específicas de usuarios
│   ├── order-service/         # Pruebas específicas de órdenes
│   ├── payment-service/       # Pruebas específicas de pagos
│   └── favourite-service/     # Pruebas específicas de favoritos
├── scripts/
│   ├── run_load_tests.py          # Script ejecutor principal Python
│   ├── quick_test.py              # Prueba rápida de demostración
│   └── run_performance_tests.sh   # Script único de pruebas de rendimiento
├── reports/                   # Reportes generados automáticamente
└── README.md                  # Este archivo
```

## 🎭 Clases de Usuario Disponibles

### Sistema Completo
- `EcommerceUser`: Operaciones generales de e-commerce
- `LightLoadUser`: Carga ligera (wait_time: 3-8s)
- `HeavyLoadUser`: Carga pesada (wait_time: 0.5-2s)
- `SpikeLoadUser`: Picos de carga (wait_time: 0.1-1s)
- `MobileAppUser`: Simulación de app móvil
- `HealthCheckUser`: Monitoreo de salud de servicios

### Especializadas por Servicio
- `ProductServiceUser`: Navegación intensiva de productos
- `UserServiceUser`: Operaciones de usuarios
- `OrderServiceUser`: Gestión de órdenes y carritos
- `PaymentServiceUser`: Procesamiento de pagos
- `FavouriteServiceUser`: Gestión de favoritos

## 📊 Reportes y Métricas

### Métricas Principales
- **Response Time**: Tiempo de respuesta (min, max, avg, 95%)
- **Throughput**: Requests por segundo (RPS)
- **Error Rate**: Porcentaje de errores
- **Concurrent Users**: Usuarios concurrentes activos

### Tipos de Reportes
- **HTML Report**: Reporte visual interactivo
- **CSV Files**: Datos raw para análisis
- **Real-time Dashboard**: Monitoreo en vivo

## ⚙️ Configuración

### Variables de Entorno
```bash
export API_GATEWAY_HOST=http://localhost:9081
export PRODUCT_SERVICE_HOST=http://localhost:8081
export USER_SERVICE_HOST=http://localhost:8080
```

### Servicios Requeridos
- API Gateway: http://localhost:9081
- Product Service: http://localhost:8081
- User Service: http://localhost:8080
- Order Service: http://localhost:8082
- Payment Service: http://localhost:8083
- Favourite Service: http://localhost:8085

## 🎯 Criterios de Éxito

### Carga Normal
- Response Time 95%: < 500ms
- Throughput: > 200 RPS
- Error Rate: < 2%

### Carga de Estrés
- Response Time 95%: < 2000ms
- Error Rate: < 10%
- Sistema se recupera tras la prueba

## 🚨 Troubleshooting

### Problemas Comunes

#### Connection Refused
```bash
# Verificar servicios
curl http://localhost:9081/actuator/health
```

#### High Error Rate
```bash
# Reducir carga
locust -f test.py --headless -u 50 -r 5 --run-time 10m
```

#### Memory Issues
```bash
# Monitorear recursos
docker stats
```

## 📚 Documentación Completa

Para documentación detallada, consultar:
- `docs/PRUEBAS_RENDIMIENTO.md`: Documentación completa
- Reportes HTML generados en `reports/`

## 🎉 Ejemplos de Uso

### Prueba Rápida (30 segundos)
```bash
cd scripts
python quick_test.py
```

### Prueba de Estrés Completa
```bash
python run_load_tests.py --type stress
```

### Prueba Personalizada
```bash
python run_load_tests.py --custom --users 150 --spawn-rate 15 --run-time 20m
```

### Interfaz Web Interactiva
```bash
locust -f test/ecommerce-complete/ecommerce_load_test.py --host http://localhost:9081
# Abrir http://localhost:8089 en el navegador
```

---

**¡Sistema de pruebas de rendimiento listo para validar la capacidad y rendimiento del sistema de microservicios!** 🚀 