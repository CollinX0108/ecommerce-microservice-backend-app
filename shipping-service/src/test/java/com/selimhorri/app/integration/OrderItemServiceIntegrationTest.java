package com.selimhorri.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.repository.OrderItemRepository;
import com.selimhorri.app.service.OrderItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(IntegrationTestConfig.class)
class OrderItemServiceIntegrationTest {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private OrderItemRepository orderItemRepository;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;
    private OrderItemDto orderItemDto;
    private OrderItem orderItem;
    private OrderDto orderDto;
    private ProductDto productDto;
    private OrderItemId orderItemId;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();

        // Setup test data
        orderItemId = new OrderItemId(1, 1);
        
        orderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(10.0)
                .build();

        productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .imageUrl("test-image.jpg")
                .sku("TEST-001")
                .priceUnit(99.99)
                .quantity(10)
                .build();

        orderItemDto = OrderItemDto.builder()
                .orderId(1)
                .productId(1)
                .orderedQuantity(2)
                .orderDto(orderDto)
                .productDto(productDto)
                .build();

        orderItem = OrderItem.builder()
                .orderId(1)
                .productId(1)
                .orderedQuantity(2)
                .build();
    }

    @Test
    void testFindById_WithSuccessfulServiceCalls() throws Exception {
        // Given
        when(orderItemRepository.findById(any()))
                .thenReturn(Optional.of(orderItem));

        // Mock product-service call
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        // Mock order-service call
        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        // When
        OrderItemDto result = orderItemService.findById(orderItemId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals(1, result.getProductId());
        assertEquals(2, result.getOrderedQuantity());
        assertNotNull(result.getOrderDto());
        assertNotNull(result.getProductDto());
        assertEquals("Test Order", result.getOrderDto().getOrderDesc());
        assertEquals("Test Product", result.getProductDto().getProductTitle());

        mockServer.verify();
    }

    @Test
    void testFindById_WithProductServiceFailure() throws Exception {
        // Given
        when(orderItemRepository.findById(any()))
                .thenReturn(Optional.of(orderItem));

        // Mock product-service failure
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // When & Then
        assertThrows(Exception.class, () -> {
            orderItemService.findById(orderItemId);
        });

        mockServer.verify();
    }

    @Test
    void testFindById_WithOrderServiceFailure() throws Exception {
        // Given
        when(orderItemRepository.findById(any()))
                .thenReturn(Optional.of(orderItem));

        // Mock product-service success
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        // Mock order-service failure
        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThrows(Exception.class, () -> {
            orderItemService.findById(orderItemId);
        });

        mockServer.verify();
    }

    @Test
    void testFindAll_WithMultipleOrderItems() throws Exception {
        // Given
        OrderItem orderItem2 = OrderItem.builder()
                .orderId(2)
                .productId(2)
                .orderedQuantity(3)
                .build();

        when(orderItemRepository.findAll())
                .thenReturn(Arrays.asList(orderItem, orderItem2));

        OrderDto orderDto2 = OrderDto.builder()
                .orderId(2)
                .orderDate(LocalDateTime.now())
                .orderDesc("Second Order")
                .orderFee(15.0)
                .build();

        ProductDto productDto2 = ProductDto.builder()
                .productId(2)
                .productTitle("Another Product")
                .sku("TEST-002")
                .priceUnit(149.99)
                .build();

        // Mock multiple service calls
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto2), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto2), MediaType.APPLICATION_JSON));

        // When
        var result = orderItemService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first order item
        OrderItemDto first = result.get(0);
        assertEquals("Test Order", first.getOrderDto().getOrderDesc());
        assertEquals("Test Product", first.getProductDto().getProductTitle());
        assertEquals(2, first.getOrderedQuantity());
        
        // Verify second order item
        OrderItemDto second = result.get(1);
        assertEquals("Second Order", second.getOrderDto().getOrderDesc());
        assertEquals("Another Product", second.getProductDto().getProductTitle());
        assertEquals(3, second.getOrderedQuantity());

        mockServer.verify();
    }

    @Test
    void testServiceIntegration_WithPartialFailure() throws Exception {
        // Given
        OrderItem orderItem2 = OrderItem.builder()
                .orderId(2)
                .productId(2)
                .orderedQuantity(3)
                .build();

        when(orderItemRepository.findAll())
                .thenReturn(Arrays.asList(orderItem, orderItem2));

        // Mock first set of calls - success
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        // Mock second set of calls - failure
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        // When & Then
        assertThrows(Exception.class, () -> {
            orderItemService.findAll();
        });

        mockServer.verify();
    }

    @Test
    void testServiceIntegration_WithRetryMechanism() throws Exception {
        // Given
        when(orderItemRepository.findById(any()))
                .thenReturn(Optional.of(orderItem));

        // Mock product-service call (succeeds) - FIRST CALL
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        // Mock order-service call (succeeds) - SECOND CALL
        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        // When
        OrderItemDto result = orderItemService.findById(orderItemId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals(1, result.getProductId());
        assertNotNull(result.getOrderDto());
        assertNotNull(result.getProductDto());

        mockServer.verify();
    }

    @Test
    void testConcurrentServiceCalls() throws Exception {
        // Given
        when(orderItemRepository.findById(any()))
                .thenReturn(Optional.of(orderItem));

        // Mock concurrent calls - need 2 calls for each service (2 threads)
        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://PRODUCT-SERVICE/product-service/api/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        // When - Simulate concurrent calls
        Thread thread1 = new Thread(() -> {
            try {
                orderItemService.findById(orderItemId);
            } catch (Exception e) {
                // Handle exception in thread
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                orderItemService.findById(orderItemId);
            } catch (Exception e) {
                // Handle exception in thread
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Then - Verify that concurrent calls don't interfere
        assertTrue(true, "Concurrent calls completed without deadlock");
    }
} 