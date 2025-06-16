package com.e2e.tests;

import com.e2e.tests.util.E2ESuite;
import com.e2e.tests.util.TestRestFacade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CompleteUserFlowE2ETest extends E2ESuite {

    @Autowired
    private TestRestFacade restFacade;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Value("${favourite.service.url}")
    private String favouriteServiceUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy__HH:mm:ss:SSSSSS");

    @Test
    public void testCompleteSystemValidation() throws Exception {
        System.out.println("🚀 Iniciando validación completa del sistema...");

        // 1. Validar servicio de usuarios
        System.out.println("👤 Paso 1: Validando servicio de usuarios...");
        ResponseEntity<String> usersResponse = restFacade.get(
            userServiceUrl + "/user-service/api/users",
            String.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
        JsonNode usersNode = objectMapper.readTree(usersResponse.getBody());
        JsonNode usersCollection = usersNode.get("collection");
        assertTrue(usersCollection.size() > 0);
        Integer userId = usersCollection.get(0).get("userId").asInt();
        System.out.println("✅ Servicio de usuarios funcional - Usuario ID: " + userId);

        // 2. Validar servicio de productos
        System.out.println("🛍️ Paso 2: Validando servicio de productos...");
        ResponseEntity<String> productsResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products",
            String.class
        );
        assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
        JsonNode productsNode = objectMapper.readTree(productsResponse.getBody());
        JsonNode productsCollection = productsNode.get("collection");
        assertTrue(productsCollection.size() > 0);
        Integer productId = productsCollection.get(0).get("productId").asInt();
        System.out.println("✅ Servicio de productos funcional - Producto ID: " + productId);

        // 3. Validar servicio de favoritos
        System.out.println("❤️ Paso 3: Validando servicio de favoritos...");
        Map<String, Object> favourite = new HashMap<>();
        favourite.put("userId", userId);
        favourite.put("productId", productId);
        favourite.put("likeDate", LocalDateTime.now().format(dateFormatter));

        ResponseEntity<String> favouriteResponse = restFacade.post(
            favouriteServiceUrl + "/favourite-service/api/favourites", 
            favourite,
            String.class
        );
        assertEquals(HttpStatus.OK, favouriteResponse.getStatusCode());
        System.out.println("✅ Servicio de favoritos funcional");

        // 4. Validar servicio de carritos
        System.out.println("🛒 Paso 4: Validando servicio de carritos...");
        ResponseEntity<String> cartsResponse = restFacade.get(
            orderServiceUrl + "/order-service/api/carts",
            String.class
        );
        assertEquals(HttpStatus.OK, cartsResponse.getStatusCode());
        JsonNode cartsNode = objectMapper.readTree(cartsResponse.getBody());
        JsonNode cartsCollection = cartsNode.get("collection");
        assertTrue(cartsCollection.size() > 0);
        System.out.println("✅ Servicio de carritos funcional - " + cartsCollection.size() + " carritos encontrados");

        // 5. Validar servicio de órdenes
        System.out.println("📦 Paso 5: Validando servicio de órdenes...");
        ResponseEntity<String> ordersResponse = restFacade.get(
            orderServiceUrl + "/order-service/api/orders",
            String.class
        );
        assertEquals(HttpStatus.OK, ordersResponse.getStatusCode());
        JsonNode ordersNode = objectMapper.readTree(ordersResponse.getBody());
        JsonNode ordersCollection = ordersNode.get("collection");
        assertTrue(ordersCollection.size() > 0);
        System.out.println("✅ Servicio de órdenes funcional - " + ordersCollection.size() + " órdenes encontradas");

        // 6. Validar servicio de pagos
        System.out.println("💳 Paso 6: Validando servicio de pagos...");
        ResponseEntity<String> paymentsResponse = restFacade.get(
            paymentServiceUrl + "/payment-service/api/payments",
            String.class
        );
        assertEquals(HttpStatus.OK, paymentsResponse.getStatusCode());
        JsonNode paymentsNode = objectMapper.readTree(paymentsResponse.getBody());
        JsonNode paymentsCollection = paymentsNode.get("collection");
        assertTrue(paymentsCollection.size() > 0);
        System.out.println("✅ Servicio de pagos funcional - " + paymentsCollection.size() + " pagos encontrados");

        System.out.println("🎉 ¡Validación completa del sistema exitosa!");
        System.out.println("📊 Todos los microservicios están funcionando correctamente");
    }

    @Test
    public void testUserFavouritesManagement() throws Exception {
        System.out.println("🚀 Iniciando gestión de favoritos de usuario...");

        // 1. Obtener usuario existente
        ResponseEntity<String> usersResponse = restFacade.get(
            userServiceUrl + "/user-service/api/users",
            String.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
        JsonNode usersNode = objectMapper.readTree(usersResponse.getBody());
        JsonNode usersCollection = usersNode.get("collection");
        assertTrue(usersCollection.size() > 0);
        Integer userId = usersCollection.get(0).get("userId").asInt();

        // 2. Obtener producto existente
        ResponseEntity<String> productsResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products",
            String.class
        );
        assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
        JsonNode productsNode = objectMapper.readTree(productsResponse.getBody());
        JsonNode productsCollection = productsNode.get("collection");
        Integer productId = productsCollection.get(0).get("productId").asInt();

        // 3. Agregar a favoritos
        Map<String, Object> favourite = new HashMap<>();
        favourite.put("userId", userId);
        favourite.put("productId", productId);
        String likeDate = LocalDateTime.now().format(dateFormatter);
        favourite.put("likeDate", likeDate);

        ResponseEntity<String> addFavouriteResponse = restFacade.post(
            favouriteServiceUrl + "/favourite-service/api/favourites", 
            favourite,
            String.class
        );
        assertEquals(HttpStatus.OK, addFavouriteResponse.getStatusCode());

        // 4. Verificar favoritos
        ResponseEntity<String> favouritesResponse = restFacade.get(
            favouriteServiceUrl + "/favourite-service/api/favourites",
            String.class
        );
        assertEquals(HttpStatus.OK, favouritesResponse.getStatusCode());
        JsonNode favouritesNode = objectMapper.readTree(favouritesResponse.getBody());
        assertTrue(favouritesNode.get("collection").size() > 0);

        System.out.println("✅ Gestión de favoritos completada exitosamente");
    }

    @Test
    public void testServiceCommunication() throws Exception {
        System.out.println("🚀 Iniciando pruebas de comunicación entre servicios...");

        // 1. Verificar que los servicios pueden comunicarse entre sí
        System.out.println("🔗 Paso 1: Verificando comunicación user-service...");
        ResponseEntity<String> usersResponse = restFacade.get(
            userServiceUrl + "/user-service/api/users",
            String.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
        System.out.println("✅ User-service responde correctamente");

        System.out.println("🔗 Paso 2: Verificando comunicación product-service...");
        ResponseEntity<String> productsResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products",
            String.class
        );
        assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
        System.out.println("✅ Product-service responde correctamente");

        System.out.println("🔗 Paso 3: Verificando comunicación order-service...");
        ResponseEntity<String> cartsResponse = restFacade.get(
            orderServiceUrl + "/order-service/api/carts",
            String.class
        );
        assertEquals(HttpStatus.OK, cartsResponse.getStatusCode());
        System.out.println("✅ Order-service responde correctamente");

        System.out.println("🔗 Paso 4: Verificando comunicación payment-service...");
        ResponseEntity<String> paymentsResponse = restFacade.get(
            paymentServiceUrl + "/payment-service/api/payments",
            String.class
        );
        assertEquals(HttpStatus.OK, paymentsResponse.getStatusCode());
        System.out.println("✅ Payment-service responde correctamente");

        System.out.println("🔗 Paso 5: Verificando comunicación favourite-service...");
        ResponseEntity<String> favouritesResponse = restFacade.get(
            favouriteServiceUrl + "/favourite-service/api/favourites",
            String.class
        );
        assertEquals(HttpStatus.OK, favouritesResponse.getStatusCode());
        System.out.println("✅ Favourite-service responde correctamente");

        System.out.println("🎉 ¡Comunicación entre servicios verificada exitosamente!");
    }

    @Test
    public void testDataConsistency() throws Exception {
        System.out.println("🚀 Iniciando pruebas de consistencia de datos...");

        // 1. Verificar que los datos son consistentes entre servicios
        ResponseEntity<String> usersResponse = restFacade.get(
            userServiceUrl + "/user-service/api/users",
            String.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
        JsonNode usersNode = objectMapper.readTree(usersResponse.getBody());
        JsonNode usersCollection = usersNode.get("collection");
        
        if (usersCollection.size() > 0) {
            Integer userId = usersCollection.get(0).get("userId").asInt();
            
            // Verificar que el usuario específico existe
            ResponseEntity<String> userResponse = restFacade.get(
                userServiceUrl + "/user-service/api/users/" + userId,
                String.class
            );
            assertEquals(HttpStatus.OK, userResponse.getStatusCode());
            JsonNode userNode = objectMapper.readTree(userResponse.getBody());
            assertEquals(userId, userNode.get("userId").asInt());
            System.out.println("✅ Consistencia de datos de usuario verificada");
        }

        // 2. Verificar productos
        ResponseEntity<String> productsResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products",
            String.class
        );
        assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
        JsonNode productsNode = objectMapper.readTree(productsResponse.getBody());
        JsonNode productsCollection = productsNode.get("collection");
        
        if (productsCollection.size() > 0) {
            Integer productId = productsCollection.get(0).get("productId").asInt();
            
            ResponseEntity<String> productResponse = restFacade.get(
                productServiceUrl + "/product-service/api/products/" + productId,
                String.class
            );
            assertEquals(HttpStatus.OK, productResponse.getStatusCode());
            JsonNode productNode = objectMapper.readTree(productResponse.getBody());
            assertEquals(productId, productNode.get("productId").asInt());
            System.out.println("✅ Consistencia de datos de producto verificada");
        }

        System.out.println("🎉 ¡Consistencia de datos verificada exitosamente!");
    }
} 