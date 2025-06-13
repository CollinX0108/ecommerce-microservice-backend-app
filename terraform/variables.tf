# ============================================================================
# TERRAFORM VARIABLES - ECOMMERCE MICROSERVICES
# ============================================================================

variable "environment" {
  description = "Environment name (dev, stage, prod)"
  type        = string
  
  validation {
    condition     = contains(["dev", "stage", "prod"], var.environment)
    error_message = "Environment must be one of: dev, stage, prod."
  }
}

variable "kubeconfig_path" {
  description = "Path to the kubeconfig file"
  type        = string
  default     = "~/.kube/config"
}

variable "docker_host" {
  description = "Docker host endpoint"
  type        = string
  default     = "unix:///var/run/docker.sock"
}

variable "docker_registry" {
  description = "Docker registry for microservice images"
  type        = string
  default     = "collinx0108"
}

variable "image_tag" {
  description = "Tag for microservice images"
  type        = string
  default     = "latest"
}

variable "replica_count" {
  description = "Number of replicas for each microservice"
  type        = map(number)
  default = {
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
}

variable "resource_limits" {
  description = "Resource limits for microservices"
  type = map(object({
    memory_request = string
    memory_limit   = string
    cpu_request    = string
    cpu_limit      = string
  }))
  default = {
    "api-gateway" = {
      memory_request = "256Mi"
      memory_limit   = "512Mi"
      cpu_request    = "100m"
      cpu_limit      = "500m"
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
}

variable "enable_monitoring" {
  description = "Enable monitoring stack (Prometheus, Grafana)"
  type        = bool
  default     = true
}

variable "enable_logging" {
  description = "Enable logging stack (ELK)"
  type        = bool
  default     = true
}

variable "enable_tracing" {
  description = "Enable distributed tracing (Jaeger)"
  type        = bool
  default     = true
}

variable "ingress_class" {
  description = "Ingress class to use"
  type        = string
  default     = "nginx"
}

variable "domain_name" {
  description = "Domain name for ingress"
  type        = string
  default     = "ecommerce.local"
} 