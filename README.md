# Ecommerce Microservice Backend

## Collin Gonzalez - A00382429
## Manuel Herrera - A00381987

## Descripción
Este proyecto implementa una arquitectura de microservicios para una plataforma de comercio electrónico utilizando Spring Boot, Docker y Kubernetes.

## Arquitectura
El sistema está compuesto por los siguientes microservicios:
- API Gateway
- Service Discovery (Eureka)
- Cloud Config
- User Service
- Product Service
- Order Service
- Payment Service
- Shipping Service
- Favourite Service

## Pipeline de CI/CD

### Desarrollo (develop)
- Compilación del código
- Pruebas unitarias
- Construcción de imágenes Docker
- Push de imágenes a Docker Hub

### Staging (stage)
- Compilación del código
- Pruebas unitarias
- Pruebas de integración
- Pruebas E2E
- Pruebas de rendimiento con Locust
- Construcción y push de imágenes Docker
- Despliegue en Kubernetes (staging)

### Producción (master)
- Compilación del código
- Pruebas unitarias
- Pruebas de integración
- Pruebas E2E
- Construcción y push de imágenes Docker
- Generación de Release Notes
- Despliegue en Kubernetes (producción)

## Pruebas Implementadas

### Pruebas Unitarias
- UserService: Validación de creación y actualización de usuarios
- ProductService: Validación de operaciones CRUD de productos
- OrderService: Validación de creación y procesamiento de órdenes
- PaymentService: Validación de procesamiento de pagos
- ShippingService: Validación de cálculo de costos de envío

### Pruebas de Integración
- User-Order: Validación de creación de órdenes por usuario
- Order-Payment: Validación de procesamiento de pagos para órdenes
- Payment-Shipping: Validación de envío después de pago exitoso
- Product-Favourite: Validación de gestión de productos favoritos
- Order-Notification: Validación de notificaciones de estado de orden

### Pruebas E2E
- Flujo de registro y login
- Flujo de compra de productos
- Flujo de gestión de favoritos
- Flujo de seguimiento de envío
- Flujo de historial de órdenes

### Pruebas de Rendimiento
- Pruebas de carga con Locust
- Simulación de usuarios concurrentes
- Métricas de tiempo de respuesta
- Análisis de throughput
- Monitoreo de tasa de errores

## Métricas de Rendimiento
- Tiempo de respuesta promedio: < 200ms
- Throughput: > 1000 requests/min
- Tasa de errores: < 1%

## Requisitos
- Java 11
- Maven 3.6+
- Docker
- Kubernetes
- Jenkins

## Configuración
1. Clonar el repositorio
2. Configurar variables de entorno
3. Ejecutar `mvn clean install`
4. Construir imágenes Docker
5. Desplegar en Kubernetes

## Contribución
1. Crear rama feature
2. Implementar cambios
3. Ejecutar pruebas
4. Crear Pull Request

## Licencia
MIT
