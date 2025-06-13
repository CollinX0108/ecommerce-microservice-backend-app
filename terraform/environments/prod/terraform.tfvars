# ============================================================================
# PRODUCTION ENVIRONMENT CONFIGURATION
# ============================================================================

environment     = "prod"
docker_registry = "collinx0108"
image_tag      = "prod"

# Resource configuration for production (full resources and HA)
replica_count = {
  "api-gateway"        = 3
  "service-discovery"  = 2  # HA for critical service
  "cloud-config"      = 2  # HA for critical service
  "user-service"      = 3
  "product-service"   = 3
  "order-service"     = 3
  "payment-service"   = 3
  "shipping-service"  = 2
  "favourite-service" = 2
  "proxy-client"      = 3
}

# Production resource limits (full resources)
resource_limits = {
  "api-gateway" = {
    memory_request = "512Mi"
    memory_limit   = "1Gi"
    cpu_request    = "200m"
    cpu_limit      = "800m"
  }
  "service-discovery" = {
    memory_request = "512Mi"
    memory_limit   = "1Gi"
    cpu_request    = "200m"
    cpu_limit      = "600m"
  }
  "cloud-config" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "user-service" = {
    memory_request = "512Mi"
    memory_limit   = "1Gi"
    cpu_request    = "200m"
    cpu_limit      = "800m"
  }
  "product-service" = {
    memory_request = "512Mi"
    memory_limit   = "1Gi"
    cpu_request    = "200m"
    cpu_limit      = "800m"
  }
  "order-service" = {
    memory_request = "512Mi"
    memory_limit   = "1Gi"
    cpu_request    = "200m"
    cpu_limit      = "800m"
  }
  "payment-service" = {
    memory_request = "512Mi"
    memory_limit   = "1Gi"
    cpu_request    = "200m"
    cpu_limit      = "800m"
  }
  "shipping-service" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "favourite-service" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "proxy-client" = {
    memory_request = "512Mi"
    memory_limit   = "1Gi"
    cpu_request    = "200m"
    cpu_limit      = "800m"
  }
}

# All features enabled for production
enable_monitoring = true
enable_logging    = true
enable_tracing    = true

# Production domain
domain_name = "ecommerce.prod.local" 