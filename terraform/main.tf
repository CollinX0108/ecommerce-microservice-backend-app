# ============================================================================
# ECOMMERCE MICROSERVICES - TERRAFORM INFRASTRUCTURE
# ============================================================================

terraform {
  required_version = ">= 1.0"
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.24"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.12"
    }
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
  }

  # Backend configuration will be provided via backend config files
  backend "local" {
    path = "terraform.tfstate"
  }
}

# ============================================================================
# PROVIDER CONFIGURATIONS
# ============================================================================

provider "kubernetes" {
  config_path = var.kubeconfig_path
}

provider "helm" {
  kubernetes {
    config_path = var.kubeconfig_path
  }
}

provider "docker" {
  host = var.docker_host
}

# ============================================================================
# LOCAL VALUES
# ============================================================================

locals {
  project_name = "ecommerce-microservices"
  environment  = var.environment
  
  common_labels = {
    project     = local.project_name
    environment = local.environment
    managed_by  = "terraform"
  }

  microservices = [
    "api-gateway",
    "service-discovery", 
    "cloud-config",
    "user-service",
    "product-service",
    "order-service",
    "payment-service",
    "shipping-service",
    "favourite-service",
    "proxy-client"
  ]
}

# ============================================================================
# MODULE CALLS
# ============================================================================

module "networking" {
  source = "./modules/networking"
  
  environment    = var.environment
  common_labels  = local.common_labels
}

module "kubernetes_base" {
  source = "./modules/kubernetes"
  
  environment     = var.environment
  common_labels   = local.common_labels
  namespace_name  = "ecommerce-${var.environment}"
}

module "microservices" {
  source = "./modules/microservices"
  
  environment     = var.environment
  common_labels   = local.common_labels
  namespace       = module.kubernetes_base.namespace_name
  microservices   = local.microservices
  docker_registry = var.docker_registry
  image_tag       = var.image_tag
  
  depends_on = [module.kubernetes_base]
}

module "monitoring" {
  source = "./modules/monitoring"
  
  environment   = var.environment
  common_labels = local.common_labels
  namespace     = module.kubernetes_base.namespace_name
  
  depends_on = [module.kubernetes_base]
} 