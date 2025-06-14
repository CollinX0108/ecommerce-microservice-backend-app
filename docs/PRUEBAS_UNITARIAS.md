# 🧪 PRUEBAS UNITARIAS - MICROSERVICIOS ECOMMERCE

## 📋 ÍNDICE
1. [Resumen General](#resumen-general)
2. [Configuración y Dependencias](#configuración-y-dependencias)
3. [Pruebas por Microservicio](#pruebas-por-microservicio)
4. [Comandos de Ejecución](#comandos-de-ejecución)
5. [Cobertura de Pruebas](#cobertura-de-pruebas)
6. [Próximas Pruebas](#próximas-pruebas)

---

## 📊 RESUMEN GENERAL

### **Estado Actual de Pruebas Unitarias**
```
✅ user-service:      6 pruebas unitarias
✅ product-service:   8 pruebas unitarias (incluye CategoryService)
✅ favourite-service: 9 pruebas unitarias
✅ order-service:     6 pruebas unitarias
✅ shipping-service:  9 pruebas unitarias
✅ payment-service:   8 pruebas unitarias

🏆 TOTAL: 46 PRUEBAS UNITARIAS EXITOSAS
```

### **Tecnologías Utilizadas**
- **JUnit 5** - Framework de pruebas
- **Mockito** - Mocking framework
- **Mockito-inline** - Para static mocking
- **Spring Boot Test** - Integración con Spring
- **AssertJ** - Assertions mejoradas

---

## ⚙️ CONFIGURACIÓN Y DEPENDENCIAS

### **Dependencias Agregadas**
```xml
<!-- En pom.xml principal -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-inline</artifactId>
    <scope>test</scope>
</dependency>
```

### **Configuración de Test**
```yaml
# src/test/resources/application-test.yml
server:
  port: 0

spring:
  application:
    name: test-service
  profiles:
    active: test
  
eureka:
  client:
    enabled: false
    fetch-registry: false
    register-with-eureka: false

zipkin:
  enabled: false

logging:
  level:
    com.netflix: OFF
    org.springframework.cloud.netflix: OFF
```

---

## 🔬 PRUEBAS POR MICROSERVICIO

### 1. **USER-SERVICE** (6 pruebas)

**Archivo**: `user-service/src/test/java/com/selimhorri/app/unit/service/UserServiceImplTest.java`

#### **Pruebas Implementadas:**
```java
@Test
void testSaveUser() // Crear usuario
@Test 
void testFindById() // Buscar usuario por ID
@Test
void testFindAll() // Listar todos los usuarios
@Test
void testUpdate() // Actualizar usuario
@Test
void testDeleteById() // Eliminar usuario
@Test
void testFindByIdNotFound() // Manejo de excepción cuando no existe
```

#### **Cobertura:**
- ✅ Operaciones CRUD completas
- ✅ Manejo de excepciones (`UserNotFoundException`)
- ✅ Validación de mapeo DTO ↔ Entity
- ✅ Verificación de llamadas al repository

#### **Comando de Ejecución:**
```bash
mvn test -pl user-service -Dtest=UserServiceImplTest
```

---

### 2. **PRODUCT-SERVICE** (8 pruebas)

**Archivos**: 
- `product-service/src/test/java/com/selimhorri/app/unit/service/ProductServiceImplTest.java` (6 pruebas)
- `product-service/src/test/java/com/selimhorri/app/unit/service/CategoryServiceImplTest.java` (2 pruebas)

#### **ProductService - Pruebas Implementadas:**
```java
@Test
void testSaveProduct() // Crear producto
@Test
void testFindById() // Buscar producto por ID
@Test
void testFindAll() // Listar todos los productos
@Test
void testUpdate() // Actualizar producto
@Test
void testDeleteById() // Eliminar producto
@Test
void testFindByIdNotFound() // Manejo de excepción
```

#### **CategoryService - Pruebas Implementadas:**
```java
@Test
void testSaveCategory() // Crear categoría
@Test
void testFindById() // Buscar categoría por ID
```

#### **Cobertura:**
- ✅ Gestión completa de productos
- ✅ Gestión de categorías
- ✅ Manejo de excepciones (`ProductNotFoundException`)
- ✅ Validación de relaciones producto-categoría

#### **Comando de Ejecución:**
```bash
mvn test -pl product-service -Dtest="*ServiceImplTest"
```

---

### 3. **FAVOURITE-SERVICE** (9 pruebas)

**Archivo**: `favourite-service/src/test/java/com/selimhorri/app/service/impl/FavouriteServiceImplTest.java`

#### **Pruebas Implementadas:**
```java
@Test
void testSaveFavourite() // Agregar a favoritos
@Test
void testFindById() // Buscar favorito por ID compuesto
@Test
void testFindAll() // Listar todos los favoritos
@Test
void testDeleteById() // Eliminar de favoritos
@Test
void testFindByIdNotFound() // Manejo de excepción
@Test
void testFindAllByUserId() // Favoritos por usuario
@Test
void testFindAllByProductId() // Favoritos por producto
@Test
void testExistsByUserIdAndProductId() // Verificar si existe
@Test
void testCountByProductId() // Contar favoritos de producto
```

#### **Características Especiales:**
- ✅ **ID Compuesto**: Manejo de `FavouriteId(userId, productId, likeDate)`
- ✅ **RestTemplate Mocking**: Simulación de llamadas a user-service y product-service
- ✅ **Relaciones Complejas**: Usuario + Producto + Fecha

#### **Comando de Ejecución:**
```bash
mvn test -pl favourite-service -Dtest=FavouriteServiceImplTest
```

---

### 4. **ORDER-SERVICE** (6 pruebas)

**Archivo**: `order-service/src/test/java/com/selimhorri/app/service/impl/OrderServiceImplTest.java`

#### **Pruebas Implementadas:**
```java
@Test
void testSaveOrder() // Crear orden
@Test
void testFindById() // Buscar orden por ID
@Test
void testFindAll() // Listar todas las órdenes
@Test
void testUpdate() // Actualizar orden
@Test
void testDeleteById() // Eliminar orden
@Test
void testFindByIdNotFound() // Manejo de excepción
```

#### **Características Especiales:**
- ✅ **Relación con Cart**: Cada orden tiene un carrito asociado
- ✅ **Fechas**: Manejo de `LocalDateTime` para fechas de orden
- ✅ **Cálculos**: Validación de fees y totales

#### **Comando de Ejecución:**
```bash
mvn test -pl order-service -Dtest=OrderServiceImplTest
```

---

### 5. **PAYMENT-SERVICE** (8 pruebas)

**Archivo**: `payment-service/src/test/java/com/selimhorri/app/service/impl/PaymentServiceImplTest.java`

#### **Pruebas Implementadas:**
```java
@Test
void testSavePayment() // Procesar pago
@Test
void testFindById() // Buscar pago por ID
@Test
void testFindAll() // Listar todos los pagos
@Test
void testUpdate() // Actualizar pago
@Test
void testDeleteById() // Eliminar pago
@Test
void testFindByIdNotFound() // Manejo de excepción
@Test
void testFindAllByOrderId() // Pagos por orden
@Test
void testFindAllByUserId() // Pagos por usuario
```

#### **Características Especiales:**
- ✅ **Static Mocking**: Uso de `mockito-inline` para métodos estáticos
- ✅ **RestTemplate**: Simulación de servicios externos
- ✅ **Estados de Pago**: Manejo de `paymentStatus` y `isPayed`
- ✅ **Relaciones**: Orden + Usuario + Método de pago

#### **Comando de Ejecución:**
```bash
mvn test -pl payment-service -Dtest=PaymentServiceImplTest
```

---

### 6. **SHIPPING-SERVICE** (9 pruebas)

**Archivo**: `shipping-service/src/test/java/com/selimhorri/app/service/impl/OrderItemServiceImplTest.java`

#### **Pruebas Implementadas:**
```java
@Test
void testSaveOrderItem() // Crear item de orden
@Test
void testFindById() // Buscar item por ID
@Test
void testFindAll() // Listar todos los items
@Test
void testUpdate() // Actualizar item
@Test
void testDeleteById() // Eliminar item
@Test
void testFindByIdNotFound() // Manejo de excepción
@Test
void testFindAllByOrderId() // Items por orden
@Test
void testFindAllByProductId() // Items por producto
@Test
void testCalculateTotalByOrderId() // Calcular total de orden
```

#### **Características Especiales:**
- ✅ **ID Compuesto**: `OrderItemId(orderId, productId)`
- ✅ **Cálculos**: Cantidad × Precio unitario
- ✅ **RestTemplate**: Llamadas a order-service y product-service
- ✅ **Agregaciones**: Totales por orden

#### **Comando de Ejecución:**
```bash
mvn test -pl shipping-service -Dtest=OrderItemServiceImplTest
```

---

## 🚀 COMANDOS DE EJECUCIÓN

### **Ejecutar Todas las Pruebas Unitarias**
```bash
mvn test -pl user-service,product-service,favourite-service,order-service,payment-service,shipping-service -Dtest="*ServiceImplTest"
```

### **Por Microservicio Individual**
```bash
# User Service
mvn test -pl user-service -Dtest=UserServiceImplTest

# Product Service  
mvn test -pl product-service -Dtest="*ServiceImplTest"

# Favourite Service
mvn test -pl favourite-service -Dtest=FavouriteServiceImplTest

# Order Service
mvn test -pl order-service -Dtest=OrderServiceImplTest

# Payment Service
mvn test -pl payment-service -Dtest=PaymentServiceImplTest

# Shipping Service
mvn test -pl shipping-service -Dtest=OrderItemServiceImplTest
```

### **Ejecutar con Reporte de Cobertura**
```bash
mvn test jacoco:report -pl user-service,product-service,favourite-service,order-service,payment-service,shipping-service
```

---

## 📈 COBERTURA DE PRUEBAS

### **Patrones de Prueba Implementados**

#### **1. Pruebas CRUD Básicas**
- ✅ Create (Save)
- ✅ Read (FindById, FindAll)
- ✅ Update
- ✅ Delete

#### **2. Manejo de Excepciones**
- ✅ `EntityNotFoundException` para cada servicio
- ✅ Validación de mensajes de error
- ✅ Códigos de estado HTTP correctos

#### **3. Mocking Avanzado**
- ✅ **Repository Mocking**: Simulación de capa de datos
- ✅ **RestTemplate Mocking**: Servicios externos
- ✅ **Static Mocking**: Métodos utilitarios
- ✅ **Mapper Mocking**: Conversiones DTO ↔ Entity

#### **4. Validaciones de Negocio**
- ✅ Cálculos matemáticos (totales, fees)
- ✅ Fechas y timestamps
- ✅ Relaciones entre entidades
- ✅ Estados y flags booleanos

### **Métricas de Cobertura Esperadas**
```
Service Layer Coverage: ~95%
- Métodos públicos: 100%
- Ramas de decisión: ~90%
- Manejo de excepciones: 100%
```

---

## 🔮 PRÓXIMAS PRUEBAS

### **Tipos de Pruebas Pendientes**

#### **1. Pruebas de Integración**
```
📋 PLANIFICADO:
- Pruebas de API REST (Controllers)
- Pruebas de Base de Datos (Repository)
- Pruebas de Comunicación entre Microservicios
- Pruebas de Circuit Breaker
```

#### **2. Pruebas de Contrato**
```
📋 PLANIFICADO:
- Spring Cloud Contract
- Pact Testing
- API Schema Validation
```

#### **3. Pruebas End-to-End**
```
📋 PLANIFICADO:
- Flujos completos de usuario
- Pruebas de carga con Locust
- Pruebas de rendimiento
```

#### **4. Pruebas de Seguridad**
```
📋 PLANIFICADO:
- Autenticación JWT
- Autorización por roles
- Validación de entrada
```

---

## 📝 NOTAS TÉCNICAS

### **Problemas Resueltos**
1. **FavouriteId Builder**: Cambio a constructor directo
2. **Cart Dependencies**: Agregado de objetos Cart a OrderService
3. **Static Mocking**: Instalación de mockito-inline
4. **Eureka en Tests**: Configuración de perfil test

### **Mejores Prácticas Aplicadas**
- ✅ **Arrange-Act-Assert** pattern
- ✅ **Given-When-Then** naming
- ✅ **Mock isolation** entre pruebas
- ✅ **Data builders** para objetos de prueba
- ✅ **Descriptive test names**

### **Configuración de CI/CD**
```yaml
# Para integrar en pipeline
test:
  script:
    - mvn test -pl user-service,product-service,favourite-service,order-service,payment-service,shipping-service
  coverage: '/Total.*?([0-9]{1,3})%/'
```

---

## 🎯 CONCLUSIÓN

Las **46 pruebas unitarias** implementadas proporcionan una base sólida para garantizar la calidad del código en los microservicios. Cada servicio tiene cobertura completa de sus operaciones principales, manejo de excepciones y validaciones de negocio.

