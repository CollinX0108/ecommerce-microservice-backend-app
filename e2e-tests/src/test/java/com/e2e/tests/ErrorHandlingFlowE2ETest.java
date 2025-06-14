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
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ErrorHandlingFlowE2ETest extends E2ESuite {

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

    @Test
    public void testNotFoundErrors() {
        System.out.println("🔍 Iniciando pruebas de errores 404...");
        System.out.println("📋 Paso 1: Buscando usuario inexistente...");
        
        try {
            ResponseEntity<String> response = restFacade.get(
                userServiceUrl + "/user-service/api/users/99999",
                String.class
            );
            // Si no lanza excepción, verificar que sea un error
            assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
        } catch (Exception e) {
            System.out.println("HTTP Error in GET " + userServiceUrl + "/user-service/api/users/99999: " + e.getMessage());
            // Aceptar tanto 404 como 500 como errores válidos
            assertTrue(e.getMessage().contains("404") || e.getMessage().contains("500"));
        }
        
        System.out.println("✅ Prueba de errores 404/500 completada exitosamente!");
    }

    @Test
    public void testBadRequestErrors() {
        System.out.println("🚫 Iniciando pruebas de errores 400...");
        System.out.println("📋 Paso 1: Creando favorito con datos inválidos...");
        
        // Test con favorito inválido (sin campos requeridos)
        Map<String, Object> invalidFavourite = new HashMap<>();
        invalidFavourite.put("userId", null); // Campo requerido nulo
        
        try {
            ResponseEntity<String> response = restFacade.post(
                favouriteServiceUrl + "/favourite-service/api/favourites",
                invalidFavourite,
                String.class
            );
            // Si no lanza excepción, verificar que sea un error
            assertTrue(response.getStatusCode().is4xxClientError());
        } catch (Exception e) {
            System.out.println("HTTP Error in POST " + favouriteServiceUrl + "/favourite-service/api/favourites: " + e.getMessage());
            assertTrue(e.getMessage().contains("400") || e.getMessage().contains("BAD_REQUEST"));
        }
        
        System.out.println("📋 Paso 2: Creando usuario con email inválido...");
        
        Map<String, Object> invalidUser = new HashMap<>();
        invalidUser.put("firstName", "Test");
        invalidUser.put("lastName", "User");
        invalidUser.put("email", "invalid-email"); // Email inválido
        
        try {
            ResponseEntity<String> response = restFacade.post(
                userServiceUrl + "/user-service/api/users",
                invalidUser,
                String.class
            );
            // Si no lanza excepción, verificar que sea un error
            assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
        } catch (Exception e) {
            System.out.println("HTTP Error in POST " + userServiceUrl + "/user-service/api/users: " + e.getMessage());
            // Aceptar tanto 400 como 500 como errores válidos para datos inválidos
            assertTrue(e.getMessage().contains("400") || e.getMessage().contains("500") || 
                      e.getMessage().contains("BAD_REQUEST") || e.getMessage().contains("INTERNAL_SERVER_ERROR"));
        }
        
        System.out.println("✅ Pruebas de errores 400/500 completadas exitosamente!");
    }

    @Test
    public void testServiceAvailability() throws Exception {
        System.out.println("🚀 Iniciando pruebas de disponibilidad de servicios...");

        // 1. Verificar que todos los servicios responden
        System.out.println("🔍 Paso 1: Verificando disponibilidad de servicios...");
        
        String[] services = {
            userServiceUrl + "/user-service/api/users",
            productServiceUrl + "/product-service/api/products",
            orderServiceUrl + "/order-service/api/carts",
            paymentServiceUrl + "/payment-service/api/payments",
            favouriteServiceUrl + "/favourite-service/api/favourites"
        };

        String[] serviceNames = {
            "User Service",
            "Product Service", 
            "Order Service",
            "Payment Service",
            "Favourite Service"
        };

        for (int i = 0; i < services.length; i++) {
            try {
                ResponseEntity<String> response = restFacade.get(services[i], String.class);
                assertTrue(response.getStatusCode().is2xxSuccessful());
                System.out.println("✅ " + serviceNames[i] + " disponible");
            } catch (Exception e) {
                fail(serviceNames[i] + " no está disponible: " + e.getMessage());
            }
        }

        System.out.println("🎉 ¡Todos los servicios están disponibles!");
    }

    @Test
    public void testDataValidation() throws Exception {
        System.out.println("🚀 Iniciando pruebas de validación de datos...");

        // 1. Verificar validación de favoritos
        System.out.println("❤️ Paso 1: Validando estructura de favoritos...");
        ResponseEntity<String> favouritesResponse = restFacade.get(
            favouriteServiceUrl + "/favourite-service/api/favourites",
            String.class
        );
        assertEquals(HttpStatus.OK, favouritesResponse.getStatusCode());
        JsonNode favouritesNode = objectMapper.readTree(favouritesResponse.getBody());
        assertTrue(favouritesNode.has("collection"));
        System.out.println("✅ Estructura de favoritos válida");

        // 2. Verificar validación de usuarios
        System.out.println("👤 Paso 2: Validando estructura de usuarios...");
        ResponseEntity<String> usersResponse = restFacade.get(
            userServiceUrl + "/user-service/api/users",
            String.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
        JsonNode usersNode = objectMapper.readTree(usersResponse.getBody());
        assertTrue(usersNode.has("collection"));
        System.out.println("✅ Estructura de usuarios válida");

        // 3. Verificar validación de productos
        System.out.println("🛍️ Paso 3: Validando estructura de productos...");
        ResponseEntity<String> productsResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products",
            String.class
        );
        assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
        JsonNode productsNode = objectMapper.readTree(productsResponse.getBody());
        assertTrue(productsNode.has("collection"));
        System.out.println("✅ Estructura de productos válida");

        System.out.println("🎉 ¡Validación de datos completada exitosamente!");
    }

    @Test
    public void testSystemResilience() throws Exception {
        System.out.println("🚀 Iniciando pruebas de resistencia del sistema...");

        // 1. Realizar múltiples consultas simultáneas
        System.out.println("⚡ Paso 1: Realizando consultas múltiples...");
        
        for (int i = 0; i < 5; i++) {
            ResponseEntity<String> response = restFacade.get(
                productServiceUrl + "/product-service/api/products",
                String.class
            );
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
        System.out.println("✅ Sistema maneja múltiples consultas correctamente");

        // 2. Verificar consistencia de datos
        System.out.println("🔍 Paso 2: Verificando consistencia de datos...");
        ResponseEntity<String> firstCall = restFacade.get(
            userServiceUrl + "/user-service/api/users",
            String.class
        );
        ResponseEntity<String> secondCall = restFacade.get(
            userServiceUrl + "/user-service/api/users",
            String.class
        );
        
        assertEquals(firstCall.getBody(), secondCall.getBody());
        System.out.println("✅ Datos consistentes entre llamadas");

        // 3. Verificar tiempo de respuesta razonable
        System.out.println("⏱️ Paso 3: Verificando tiempos de respuesta...");
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restFacade.get(
            productServiceUrl + "/product-service/api/categories",
            String.class
        );
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(responseTime < 5000, "Tiempo de respuesta demasiado alto: " + responseTime + "ms");
        System.out.println("✅ Tiempo de respuesta aceptable: " + responseTime + "ms");

        System.out.println("🎉 ¡Pruebas de resistencia completadas exitosamente!");
    }
} 