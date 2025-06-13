package com.selimhorri.app.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feature Toggle pattern
 * Uses Spring properties for enabling/disabling features
 * Features can be controlled through application.yml or environment variables
 */
@Configuration
public class FeatureToggleConfiguration {
    
    // Feature toggles are handled through ApplicationFeatures component
    // and configured via application properties
    
} 