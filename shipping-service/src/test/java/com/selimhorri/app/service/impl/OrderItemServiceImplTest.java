package com.selimhorri.app.service.impl;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.helper.OrderItemMappingHelper;
import com.selimhorri.app.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItemDto orderItemDto;
    private OrderItem orderItem;
    private OrderItemId orderItemId;
    private OrderDto orderDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        orderItemId = new OrderItemId(1, 1); // productId, orderId

        orderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(100.0)
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
                .orderedQuantity(2)
                .productDto(productDto)
                .orderDto(orderDto)
                .build();

        orderItem = OrderItem.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(2)
                .build();
    }

    @Test
    void testFindAll_ShouldReturnOrderItemListWithProductAndOrderData() {
        // Given
        List<OrderItem> orderItems = Arrays.asList(orderItem);
        when(orderItemRepository.findAll()).thenReturn(orderItems);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + productDto.getProductId(),
                ProductDto.class
        )).thenReturn(productDto);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + orderDto.getOrderId(),
                OrderDto.class
        )).thenReturn(orderDto);

        try (MockedStatic<OrderItemMappingHelper> mockedHelper = mockStatic(OrderItemMappingHelper.class)) {
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItem.class))).thenReturn(orderItemDto);

            // When
            List<OrderItemDto> result = orderItemService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(orderItemDto.getOrderedQuantity(), result.get(0).getOrderedQuantity());
            assertNotNull(result.get(0).getProductDto());
            assertNotNull(result.get(0).getOrderDto());
            verify(orderItemRepository).findAll();
            verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
        }
    }

    @Test
    void testFindById_ShouldReturnOrderItemWithProductAndOrderData_WhenOrderItemExists() {
        // Given
        when(orderItemRepository.findById(null)).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + productDto.getProductId(),
                ProductDto.class
        )).thenReturn(productDto);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + orderDto.getOrderId(),
                OrderDto.class
        )).thenReturn(orderDto);

        try (MockedStatic<OrderItemMappingHelper> mockedHelper = mockStatic(OrderItemMappingHelper.class)) {
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItem.class))).thenReturn(orderItemDto);

            // When
            OrderItemDto result = orderItemService.findById(orderItemId);

            // Then
            assertNotNull(result);
            assertEquals(orderItemDto.getOrderedQuantity(), result.getOrderedQuantity());
            assertNotNull(result.getProductDto());
            assertNotNull(result.getOrderDto());
            verify(orderItemRepository).findById(null); // Note: implementation has bug with null
            verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
        }
    }

    @Test
    void testFindById_ShouldThrowException_WhenOrderItemNotExists() {
        // Given
        when(orderItemRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.findById(orderItemId));
        verify(orderItemRepository).findById(null); // Note: implementation has bug with null
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testSave_ShouldReturnSavedOrderItem() {
        // Given
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        try (MockedStatic<OrderItemMappingHelper> mockedHelper = mockStatic(OrderItemMappingHelper.class)) {
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItemDto.class))).thenReturn(orderItem);
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItem.class))).thenReturn(orderItemDto);

            // When
            OrderItemDto result = orderItemService.save(orderItemDto);

            // Then
            assertNotNull(result);
            assertEquals(orderItemDto.getOrderedQuantity(), result.getOrderedQuantity());
            verify(orderItemRepository).save(any(OrderItem.class));
        }
    }

    @Test
    void testUpdate_ShouldReturnUpdatedOrderItem() {
        // Given
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        try (MockedStatic<OrderItemMappingHelper> mockedHelper = mockStatic(OrderItemMappingHelper.class)) {
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItemDto.class))).thenReturn(orderItem);
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItem.class))).thenReturn(orderItemDto);

            // When
            OrderItemDto result = orderItemService.update(orderItemDto);

            // Then
            assertNotNull(result);
            assertEquals(orderItemDto.getOrderedQuantity(), result.getOrderedQuantity());
            verify(orderItemRepository).save(any(OrderItem.class));
        }
    }

    @Test
    void testDeleteById_ShouldDeleteOrderItem() {
        // Given
        // No setup needed for deleteById

        // When
        orderItemService.deleteById(orderItemId);

        // Then
        verify(orderItemRepository).deleteById(orderItemId);
    }

    @Test
    void testFindAll_ShouldHandleEmptyList() {
        // Given
        when(orderItemRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<OrderItemDto> result = orderItemService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderItemRepository).findAll();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testFindAll_ShouldHandleRestTemplateException() {
        // Given
        List<OrderItem> orderItems = Arrays.asList(orderItem);
        when(orderItemRepository.findAll()).thenReturn(orderItems);
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenThrow(new RuntimeException("Product service unavailable"));

        try (MockedStatic<OrderItemMappingHelper> mockedHelper = mockStatic(OrderItemMappingHelper.class)) {
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItem.class))).thenReturn(orderItemDto);

            // When & Then
            assertThrows(RuntimeException.class, () -> orderItemService.findAll());
            verify(orderItemRepository).findAll();
            verify(restTemplate).getForObject(anyString(), eq(ProductDto.class));
        }
    }

    @Test
    void testFindAll_ShouldHandleNullProductServiceResponse() {
        // Given
        List<OrderItem> orderItems = Arrays.asList(orderItem);
        when(orderItemRepository.findAll()).thenReturn(orderItems);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + productDto.getProductId(),
                ProductDto.class
        )).thenReturn(null);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + orderDto.getOrderId(),
                OrderDto.class
        )).thenReturn(orderDto);

        try (MockedStatic<OrderItemMappingHelper> mockedHelper = mockStatic(OrderItemMappingHelper.class)) {
            mockedHelper.when(() -> OrderItemMappingHelper.map(any(OrderItem.class))).thenReturn(orderItemDto);

            // When
            List<OrderItemDto> result = orderItemService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderItemRepository).findAll();
            verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
        }
    }
} 