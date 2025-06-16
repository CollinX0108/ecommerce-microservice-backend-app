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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProductManagementFlowE2ETest extends E2ESuite {

    @Autowired
    private TestRestFacade restFacade;

    @Value("${product.service.url}")
    private String productServiceUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testProductCatalogBrowsing() throws Exception {
        System.out.println("🚀 Iniciando navegación del catálogo de productos...");

        // 1. Obtener todas las categorías
        System.out.println("📂 Paso 1: Obteniendo categorías...");
        ResponseEntity<String> categoriesResponse = restFacade.get(
            productServiceUrl + "/product-service/api/categories",
            String.class
        );
        assertEquals(HttpStatus.OK, categoriesResponse.getStatusCode());
        JsonNode categoriesNode = objectMapper.readTree(categoriesResponse.getBody());
        JsonNode categoriesCollection = categoriesNode.get("collection");
        assertTrue(categoriesCollection.size() > 0);
        System.out.println("✅ Categorías encontradas: " + categoriesCollection.size());

        // 2. Obtener todos los productos
        System.out.println("🛍️ Paso 2: Obteniendo productos...");
        ResponseEntity<String> productsResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products",
            String.class
        );
        assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
        JsonNode productsNode = objectMapper.readTree(productsResponse.getBody());
        JsonNode productsCollection = productsNode.get("collection");
        assertTrue(productsCollection.size() > 0);
        System.out.println("✅ Productos encontrados: " + productsCollection.size());

        // 3. Obtener detalles de un producto específico
        System.out.println("🔍 Paso 3: Obteniendo detalles de producto...");
        Integer productId = productsCollection.get(0).get("productId").asInt();
        ResponseEntity<String> productResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products/" + productId,
            String.class
        );
        assertEquals(HttpStatus.OK, productResponse.getStatusCode());
        JsonNode productNode = objectMapper.readTree(productResponse.getBody());
        assertEquals(productId, productNode.get("productId").asInt());
        System.out.println("✅ Detalles de producto obtenidos para ID: " + productId);

        System.out.println("🎉 ¡Navegación del catálogo completada exitosamente!");
    }

    @Test
    public void testCategoryManagement() throws Exception {
        System.out.println("🚀 Iniciando gestión de categorías...");

        // 1. Listar categorías existentes
        System.out.println("📋 Paso 1: Listando categorías existentes...");
        ResponseEntity<String> categoriesResponse = restFacade.get(
            productServiceUrl + "/product-service/api/categories",
            String.class
        );
        assertEquals(HttpStatus.OK, categoriesResponse.getStatusCode());
        JsonNode categoriesNode = objectMapper.readTree(categoriesResponse.getBody());
        JsonNode categoriesCollection = categoriesNode.get("collection");
        System.out.println("✅ Categorías listadas: " + categoriesCollection.size());

        // 2. Obtener detalles de una categoría específica
        if (categoriesCollection.size() > 0) {
            System.out.println("🔍 Paso 2: Obteniendo detalles de categoría...");
            Integer categoryId = categoriesCollection.get(0).get("categoryId").asInt();
            ResponseEntity<String> categoryResponse = restFacade.get(
                productServiceUrl + "/product-service/api/categories/" + categoryId,
                String.class
            );
            assertEquals(HttpStatus.OK, categoryResponse.getStatusCode());
            JsonNode categoryNode = objectMapper.readTree(categoryResponse.getBody());
            assertEquals(categoryId, categoryNode.get("categoryId").asInt());
            System.out.println("✅ Detalles de categoría obtenidos para ID: " + categoryId);
        }

        System.out.println("🎉 ¡Gestión de categorías completada exitosamente!");
    }

    @Test
    public void testProductSearch() throws Exception {
        System.out.println("🚀 Iniciando búsqueda de productos...");

        // 1. Buscar productos por categoría
        System.out.println("🔍 Paso 1: Buscando productos por categoría...");
        ResponseEntity<String> categoriesResponse = restFacade.get(
            productServiceUrl + "/product-service/api/categories",
            String.class
        );
        assertEquals(HttpStatus.OK, categoriesResponse.getStatusCode());
        JsonNode categoriesNode = objectMapper.readTree(categoriesResponse.getBody());
        JsonNode categoriesCollection = categoriesNode.get("collection");
        
        if (categoriesCollection.size() > 0) {
            Integer categoryId = categoriesCollection.get(0).get("categoryId").asInt();
            String categoryTitle = categoriesCollection.get(0).get("categoryTitle").asText();
            System.out.println("🏷️ Buscando productos en categoría: " + categoryTitle);
            
            // Verificar que los productos pertenecen a la categoría
            ResponseEntity<String> productsResponse = restFacade.get(
                productServiceUrl + "/product-service/api/products",
                String.class
            );
            assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
            JsonNode productsNode = objectMapper.readTree(productsResponse.getBody());
            JsonNode productsCollection = productsNode.get("collection");
            
            int productsInCategory = 0;
            for (JsonNode product : productsCollection) {
                if (product.has("category") && 
                    product.get("category").get("categoryId").asInt() == categoryId) {
                    productsInCategory++;
                }
            }
            System.out.println("✅ Productos encontrados en categoría " + categoryTitle + ": " + productsInCategory);
        }

        System.out.println("🎉 ¡Búsqueda de productos completada exitosamente!");
    }

    @Test
    public void testProductDataIntegrity() throws Exception {
        System.out.println("🚀 Iniciando verificación de integridad de datos...");

        // 1. Verificar que todos los productos tienen datos válidos
        System.out.println("✅ Paso 1: Verificando integridad de productos...");
        ResponseEntity<String> productsResponse = restFacade.get(
            productServiceUrl + "/product-service/api/products",
            String.class
        );
        assertEquals(HttpStatus.OK, productsResponse.getStatusCode());
        JsonNode productsNode = objectMapper.readTree(productsResponse.getBody());
        JsonNode productsCollection = productsNode.get("collection");

        for (JsonNode product : productsCollection) {
            // Verificar campos obligatorios
            assertTrue(product.has("productId"));
            assertTrue(product.has("productTitle"));
            assertTrue(product.has("sku"));
            assertTrue(product.has("category"));
            
            // Verificar que el ID es válido
            assertTrue(product.get("productId").asInt() > 0);
            
            // Verificar que el título no está vacío
            assertFalse(product.get("productTitle").asText().isEmpty());
            
            System.out.println("✅ Producto válido: " + product.get("productTitle").asText());
        }

        // 2. Verificar que todas las categorías tienen datos válidos
        System.out.println("✅ Paso 2: Verificando integridad de categorías...");
        ResponseEntity<String> categoriesResponse = restFacade.get(
            productServiceUrl + "/product-service/api/categories",
            String.class
        );
        assertEquals(HttpStatus.OK, categoriesResponse.getStatusCode());
        JsonNode categoriesNode = objectMapper.readTree(categoriesResponse.getBody());
        JsonNode categoriesCollection = categoriesNode.get("collection");

        for (JsonNode category : categoriesCollection) {
            // Verificar campos obligatorios
            assertTrue(category.has("categoryId"));
            assertTrue(category.has("categoryTitle"));
            
            // Verificar que el ID es válido
            assertTrue(category.get("categoryId").asInt() > 0);
            
            // Verificar que el título no está vacío
            assertFalse(category.get("categoryTitle").asText().isEmpty());
            
            System.out.println("✅ Categoría válida: " + category.get("categoryTitle").asText());
        }

        System.out.println("🎉 ¡Verificación de integridad completada exitosamente!");
    }
} 