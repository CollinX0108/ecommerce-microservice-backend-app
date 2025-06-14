package com.selimhorri.app.service.impl;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderDto orderDto;
    private Order order;
    private Cart cart;
    private CartDto cartDto;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .cartId(1)
                .userId(1)
                .build();

        cartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .build();

        orderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(10.0)
                .cartDto(cartDto)
                .build();

        order = Order.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(10.0)
                .cart(cart)
                .build();
    }

    @Test
    void testFindAll_ShouldReturnOrderList() {
        // Given
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<OrderDto> result = orderService.findAll();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(orderRepository).findAll();
    }

    @Test
    void testFindById_ShouldReturnOrderDto_WhenOrderExists() {
        // Given
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // When
        OrderDto result = orderService.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        verify(orderRepository).findById(1);
    }

    @Test
    void testFindById_ShouldThrowException_WhenOrderNotExists() {
        // Given
        when(orderRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderService.findById(1));
        verify(orderRepository).findById(1);
    }

    @Test
    void testSave_ShouldReturnSavedOrder() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        OrderDto result = orderService.save(orderDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testUpdate_ShouldReturnUpdatedOrder() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        OrderDto result = orderService.update(orderDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testDeleteById_ShouldDeleteOrder() {
        // Given
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        // When
        orderService.deleteById(1);

        // Then
        verify(orderRepository).findById(1);
        verify(orderRepository).delete(any(Order.class));
    }
} 