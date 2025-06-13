package com.selimhorri.app.resource;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.selimhorri.app.business.payment.model.PaymentDto;
import com.selimhorri.app.business.payment.model.response.PaymentPaymentServiceDtoCollectionResponse;
import com.selimhorri.app.business.payment.service.PaymentClientService;
import com.selimhorri.app.config.ApplicationFeatures;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentResource {

    private final PaymentClientService paymentClientService;
    private final ApplicationFeatures applicationFeatures;

    @GetMapping
    public ResponseEntity<PaymentPaymentServiceDtoCollectionResponse> findAll() {
        log.info("Fetching all payments");
        
        if (applicationFeatures.isCircuitBreakerMonitoringEnabled()) {
            log.info("Circuit breaker monitoring is enabled");
        }
        
        return paymentClientService.findAll();
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> findById(
            @PathVariable("paymentId") 
            @NotBlank(message = "Input must not be blank!") 
            @Valid final String paymentId) {
        
        log.info("Fetching payment with ID: {}", paymentId);
        return paymentClientService.findById(paymentId);
    }

    @PostMapping
    public ResponseEntity<PaymentDto> save(
            @RequestBody 
            @NotNull(message = "Input must not be NULL!") 
            @Valid final PaymentDto paymentDto) {
        
        log.info("Creating new payment");
        
        // Feature toggle for enhanced payment processing
        if (applicationFeatures.isEnhancedPaymentProcessingEnabled()) {
            log.info("Using enhanced payment processing");
            // Aquí iría la lógica mejorada de procesamiento de pagos
        } else {
            log.info("Using standard payment processing");
        }
        
        return paymentClientService.save(paymentDto);
    }

    @PutMapping
    public ResponseEntity<PaymentDto> update(
            @RequestBody 
            @NotNull(message = "Input must not be NULL!") 
            @Valid final PaymentDto paymentDto) {
        
        log.info("Updating payment");
        return paymentClientService.update(paymentDto);
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Boolean> deleteById(@PathVariable("paymentId") final String paymentId) {
        log.info("Deleting payment with ID: {}", paymentId);
        return paymentClientService.deleteById(paymentId);
    }

    @GetMapping("/features")
    public ResponseEntity<String> getActiveFeatures() {
        StringBuilder features = new StringBuilder();
        features.append("Active Features:\n");
        features.append("Enhanced Payment Processing: ").append(applicationFeatures.isEnhancedPaymentProcessingEnabled()).append("\n");
        features.append("Product Recommendations: ").append(applicationFeatures.isProductRecommendationsEnabled()).append("\n");
        features.append("Advanced Order Tracking: ").append(applicationFeatures.isAdvancedOrderTrackingEnabled()).append("\n");
        features.append("Circuit Breaker Monitoring: ").append(applicationFeatures.isCircuitBreakerMonitoringEnabled()).append("\n");
        features.append("Experimental User Dashboard: ").append(applicationFeatures.isExperimentalUserDashboardEnabled()).append("\n");
        
        return ResponseEntity.ok(features.toString());
    }
} 