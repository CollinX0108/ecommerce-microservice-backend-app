# ============================================================================
# DEVELOPMENT ENVIRONMENT CONFIGURATION
# ============================================================================

environment     = "dev"
docker_registry = "collinx0108"
image_tag      = "dev"

# Resource configuration for development (smaller resources)
replica_count = {
  "api-gateway"        = 1
  "service-discovery"  = 1
  "cloud-config"      = 1
  "user-service"      = 1
  "product-service"   = 1
  "order-service"     = 1
  "payment-service"   = 1
  "shipping-service"  = 1
  "favourite-service" = 1
  "proxy-client"      = 1
}

# Development resource limits (smaller)
resource_limits = {
  "api-gateway" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "service-discovery" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "cloud-config" = {
    memory_request = "64Mi"
    memory_limit   = "128Mi"
    cpu_request    = "25m"
    cpu_limit      = "100m"
  }
  "user-service" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "product-service" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "order-service" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "payment-service" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "shipping-service" = {
    memory_request = "64Mi"
    memory_limit   = "128Mi"
    cpu_request    = "25m"
    cpu_limit      = "100m"
  }
  "favourite-service" = {
    memory_request = "64Mi"
    memory_limit   = "128Mi"
    cpu_request    = "25m"
    cpu_limit      = "100m"
  }
  "proxy-client" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
}

# Feature flags for development
enable_monitoring = true
enable_logging    = false  # Disabled in dev to save resources
enable_tracing    = true

# Development domain
domain_name = "ecommerce-dev.local" 