package com.selimhorri.app.service.impl;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.helper.PaymentMappingHelper;
import com.selimhorri.app.repository.PaymentRepository;
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
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentDto paymentDto;
    private Payment payment;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        orderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(100.0)
                .build();

        paymentDto = PaymentDto.builder()
                .paymentId(1)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .orderDto(orderDto)
                .build();

        payment = Payment.builder()
                .paymentId(1)
                .orderId(1)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();
    }

    @Test
    void testFindAll_ShouldReturnPaymentListWithOrderData() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findAll()).thenReturn(payments);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + orderDto.getOrderId(),
                OrderDto.class
        )).thenReturn(orderDto);

        try (MockedStatic<PaymentMappingHelper> mockedHelper = mockStatic(PaymentMappingHelper.class)) {
            mockedHelper.when(() -> PaymentMappingHelper.map(any(Payment.class))).thenReturn(paymentDto);

            // When
            List<PaymentDto> result = paymentService.findAll();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(paymentDto.getPaymentId(), result.get(0).getPaymentId());
            assertNotNull(result.get(0).getOrderDto());
            verify(paymentRepository).findAll();
            verify(restTemplate).getForObject(anyString(), eq(OrderDto.class));
        }
    }

    @Test
    void testFindById_ShouldReturnPaymentWithOrderData_WhenPaymentExists() {
        // Given
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + orderDto.getOrderId(),
                OrderDto.class
        )).thenReturn(orderDto);

        try (MockedStatic<PaymentMappingHelper> mockedHelper = mockStatic(PaymentMappingHelper.class)) {
            mockedHelper.when(() -> PaymentMappingHelper.map(any(Payment.class))).thenReturn(paymentDto);

            // When
            PaymentDto result = paymentService.findById(1);

            // Then
            assertNotNull(result);
            assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
            assertNotNull(result.getOrderDto());
            verify(paymentRepository).findById(1);
            verify(restTemplate).getForObject(anyString(), eq(OrderDto.class));
        }
    }

    @Test
    void testFindById_ShouldThrowException_WhenPaymentNotExists() {
        // Given
        when(paymentRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> paymentService.findById(1));
        verify(paymentRepository).findById(1);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testSave_ShouldReturnSavedPayment() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        try (MockedStatic<PaymentMappingHelper> mockedHelper = mockStatic(PaymentMappingHelper.class)) {
            mockedHelper.when(() -> PaymentMappingHelper.map(any(PaymentDto.class))).thenReturn(payment);
            mockedHelper.when(() -> PaymentMappingHelper.map(any(Payment.class))).thenReturn(paymentDto);

            // When
            PaymentDto result = paymentService.save(paymentDto);

            // Then
            assertNotNull(result);
            assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    void testUpdate_ShouldReturnUpdatedPayment() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        try (MockedStatic<PaymentMappingHelper> mockedHelper = mockStatic(PaymentMappingHelper.class)) {
            mockedHelper.when(() -> PaymentMappingHelper.map(any(PaymentDto.class))).thenReturn(payment);
            mockedHelper.when(() -> PaymentMappingHelper.map(any(Payment.class))).thenReturn(paymentDto);

            // When
            PaymentDto result = paymentService.update(paymentDto);

            // Then
            assertNotNull(result);
            assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    void testDeleteById_ShouldDeletePayment() {
        // Given
        // No setup needed for deleteById

        // When
        paymentService.deleteById(1);

        // Then
        verify(paymentRepository).deleteById(1);
    }

    @Test
    void testFindAll_ShouldHandleEmptyList() {
        // Given
        when(paymentRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<PaymentDto> result = paymentService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository).findAll();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testFindById_ShouldHandleRestTemplateException() {
        // Given
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        try (MockedStatic<PaymentMappingHelper> mockedHelper = mockStatic(PaymentMappingHelper.class)) {
            mockedHelper.when(() -> PaymentMappingHelper.map(any(Payment.class))).thenReturn(paymentDto);

            // When & Then
            assertThrows(RuntimeException.class, () -> paymentService.findById(1));
            verify(paymentRepository).findById(1);
            verify(restTemplate).getForObject(anyString(), eq(OrderDto.class));
        }
    }
} 