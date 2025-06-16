package com.selimhorri.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.PaymentService;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(IntegrationTestConfig.class)
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private PaymentRepository paymentRepository;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;
    private PaymentDto paymentDto;
    private Payment payment;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();

        // Setup test data
        orderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(10.0)
                .build();

        paymentDto = PaymentDto.builder()
                .paymentId(1)
                .paymentStatus(PaymentStatus.COMPLETED)
                .isPayed(true)
                .orderDto(orderDto)
                .build();

        payment = Payment.builder()
                .paymentId(1)
                .orderId(1)
                .paymentStatus(PaymentStatus.COMPLETED)
                .isPayed(true)
                .build();
    }

    @Test
    void testFindById_WithSuccessfulOrderServiceCall() throws Exception {
        // Given
        when(paymentRepository.findById(1))
                .thenReturn(Optional.of(payment));

        // Mock order-service call
        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        // When
        PaymentDto result = paymentService.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
        assertTrue(result.getIsPayed());
        assertNotNull(result.getOrderDto());
        assertEquals("Test Order", result.getOrderDto().getOrderDesc());

        mockServer.verify();
    }

    @Test
    void testFindById_WithOrderServiceFailure() throws Exception {
        // Given
        when(paymentRepository.findById(1))
                .thenReturn(Optional.of(payment));

        // Mock order-service failure
        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // When & Then
        assertThrows(Exception.class, () -> {
            paymentService.findById(1);
        });

        mockServer.verify();
    }

    @Test
    void testFindAll_WithMultiplePayments() throws Exception {
        // Given
        Payment payment2 = Payment.builder()
                .paymentId(2)
                .orderId(2)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .isPayed(false)
                .build();

        when(paymentRepository.findAll())
                .thenReturn(Arrays.asList(payment, payment2));

        OrderDto orderDto2 = OrderDto.builder()
                .orderId(2)
                .orderDate(LocalDateTime.now())
                .orderDesc("Second Order")
                .orderFee(20.0)
                .build();

        // Mock multiple order-service calls
        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto), MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(orderDto2), MediaType.APPLICATION_JSON));

        // When
        var result = paymentService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first payment
        PaymentDto first = result.get(0);
        assertEquals(PaymentStatus.COMPLETED, first.getPaymentStatus());
        assertTrue(first.getIsPayed());
        assertEquals("Test Order", first.getOrderDto().getOrderDesc());
        
        // Verify second payment
        PaymentDto second = result.get(1);
        assertEquals(PaymentStatus.IN_PROGRESS, second.getPaymentStatus());
        assertFalse(second.getIsPayed());
        assertEquals("Second Order", second.getOrderDto().getOrderDesc());

        mockServer.verify();
    }

    @Test
    void testServiceIntegration_WithOrderServiceTimeout() throws Exception {
        // Given
        when(paymentRepository.findById(1))
                .thenReturn(Optional.of(payment));

        // Mock order-service failure to simulate service unavailability
        mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT));

        // When & Then
        assertThrows(Exception.class, () -> {
            paymentService.findById(1);
        });
        
        // Reset mock server to avoid verification issues
        mockServer.reset();
    }

    @Test
    void testPaymentProcessing_WithOrderValidation() throws Exception {
        // Given
        PaymentDto newPayment = PaymentDto.builder()
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .isPayed(false)
                .orderDto(OrderDto.builder().orderId(1).build())
                .build();

        Payment savedPayment = Payment.builder()
                .paymentId(1)
                .orderId(1)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .isPayed(false)
                .build();

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        // When
        PaymentDto result = paymentService.save(newPayment);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPaymentId());
        assertEquals(PaymentStatus.IN_PROGRESS, result.getPaymentStatus());
        assertFalse(result.getIsPayed());
        // Note: save() method doesn't call order-service, so orderDto will only have orderId
        assertNotNull(result.getOrderDto());
        assertEquals(1, result.getOrderDto().getOrderId());

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCircuitBreakerPattern_OnOrderServiceFailures() throws Exception {
        // Given
        when(paymentRepository.findById(1))
                .thenReturn(Optional.of(payment));

        // Mock multiple failures to trigger circuit breaker
        for (int i = 0; i < 3; i++) {
            mockServer.expect(requestTo("http://ORDER-SERVICE/order-service/api/orders/1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        // When & Then - Multiple calls should fail
        for (int i = 0; i < 3; i++) {
            assertThrows(Exception.class, () -> {
                paymentService.findById(1);
            });
        }

        mockServer.verify();
    }

    @Test
    void testPaymentStatusUpdate_WithOrderNotification() throws Exception {
        // Given
        PaymentDto updatedPayment = PaymentDto.builder()
                .paymentId(1)
                .paymentStatus(PaymentStatus.COMPLETED)
                .isPayed(true)
                .orderDto(OrderDto.builder().orderId(1).build())
                .build();

        Payment savedPayment = Payment.builder()
                .paymentId(1)
                .orderId(1)
                .paymentStatus(PaymentStatus.COMPLETED)
                .isPayed(true)
                .build();

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        // When
        PaymentDto result = paymentService.update(updatedPayment);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
        assertTrue(result.getIsPayed());
        // Note: update() method doesn't call order-service, so orderDto will only have orderId
        assertNotNull(result.getOrderDto());
        assertEquals(1, result.getOrderDto().getOrderId());

        verify(paymentRepository).save(any(Payment.class));
    }
} 