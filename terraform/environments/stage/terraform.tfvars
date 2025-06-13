# ============================================================================
# STAGING ENVIRONMENT CONFIGURATION
# ============================================================================

environment     = "stage"
docker_registry = "collinx0108"
image_tag      = "stage"

# Resource configuration for staging (medium resources)
replica_count = {
  "api-gateway"        = 2
  "service-discovery"  = 1
  "cloud-config"      = 1
  "user-service"      = 2
  "product-service"   = 2
  "order-service"     = 2
  "payment-service"   = 2
  "shipping-service"  = 1
  "favourite-service" = 1
  "proxy-client"      = 2
}

# Staging resource limits (production-like but smaller)
resource_limits = {
  "api-gateway" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "service-discovery" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "300m"
  }
  "cloud-config" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "user-service" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "product-service" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "order-service" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "payment-service" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
  "shipping-service" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "favourite-service" = {
    memory_request = "128Mi"
    memory_limit   = "256Mi"
    cpu_request    = "50m"
    cpu_limit      = "200m"
  }
  "proxy-client" = {
    memory_request = "256Mi"
    memory_limit   = "512Mi"
    cpu_request    = "100m"
    cpu_limit      = "400m"
  }
}

# All features enabled for staging
enable_monitoring = true
enable_logging    = true
enable_tracing    = true

# Staging domain
domain_name = "ecommerce-stage.local" 