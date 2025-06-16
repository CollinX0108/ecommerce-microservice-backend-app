package com.selimhorri.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Feature Toggle implementation using Spring properties
 * This allows us to enable/disable features through configuration
 */
@Component
public class ApplicationFeatures {

    @Value("${features.enhanced-payment-processing:true}")
    private boolean enhancedPaymentProcessing;

    @Value("${features.product-recommendations:false}")
    private boolean productRecommendations;

    @Value("${features.advanced-order-tracking:false}")
    private boolean advancedOrderTracking;

    @Value("${features.circuit-breaker-monitoring:true}")
    private boolean circuitBreakerMonitoring;

    @Value("${features.experimental-user-dashboard:false}")
    private boolean experimentalUserDashboard;

    public boolean isEnhancedPaymentProcessingEnabled() {
        return enhancedPaymentProcessing;
    }

    public boolean isProductRecommendationsEnabled() {
        return productRecommendations;
    }

    public boolean isAdvancedOrderTrackingEnabled() {
        return advancedOrderTracking;
    }

    public boolean isCircuitBreakerMonitoringEnabled() {
        return circuitBreakerMonitoring;
    }

    public boolean isExperimentalUserDashboardEnabled() {
        return experimentalUserDashboard;
    }
} 