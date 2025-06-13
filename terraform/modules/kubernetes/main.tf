# ============================================================================
# KUBERNETES BASE MODULE
# ============================================================================

terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.24"
    }
  }
}

# ============================================================================
# NAMESPACE
# ============================================================================

resource "kubernetes_namespace" "ecommerce" {
  metadata {
    name = var.namespace_name
    labels = merge(var.common_labels, {
      name = var.namespace_name
    })
    annotations = {
      "created-by" = "terraform"
      "purpose"    = "ecommerce-microservices"
    }
  }
}

# ============================================================================
# COMMON CONFIG MAP
# ============================================================================

resource "kubernetes_config_map" "common_config" {
  metadata {
    name      = "common-config"
    namespace = kubernetes_namespace.ecommerce.metadata[0].name
    labels    = var.common_labels
  }

  data = {
    # Configuración común para todos los microservicios
    "common-services.properties" = file("${path.module}/config/common-services.properties")
    
    # Configuración específica para servicios de infraestructura
    "infrastructure-services.properties" = file("${path.module}/config/infrastructure-services.properties")
    
    # Configuraciones específicas por servicio
    "api-gateway.properties"        = file("${path.module}/config/api-gateway.properties")
    "order-service.properties"      = file("${path.module}/config/order-service.properties")
    "favourite-service.properties"  = file("${path.module}/config/favourite-service.properties")
    "payment-service.properties"    = file("${path.module}/config/payment-service.properties")
    "product-service.properties"    = file("${path.module}/config/product-service.properties")
    "proxy-client.properties"       = file("${path.module}/config/proxy-client.properties")
    "shipping-service.properties"   = file("${path.module}/config/shipping-service.properties")
    "user-service.properties"       = file("${path.module}/config/user-service.properties")
    "service-discovery.properties"  = file("${path.module}/config/service-discovery.properties")
    "cloud-config.properties"       = file("${path.module}/config/cloud-config.properties")
  }
}

# ============================================================================
# SECRETS
# ============================================================================

resource "kubernetes_secret" "docker_registry" {
  metadata {
    name      = "docker-registry-secret"
    namespace = kubernetes_namespace.ecommerce.metadata[0].name
    labels    = var.common_labels
  }

  type = "kubernetes.io/dockerconfigjson"

  data = {
    ".dockerconfigjson" = jsonencode({
      auths = {
        "https://index.docker.io/v1/" = {
          username = var.docker_username
          password = var.docker_password
          auth     = base64encode("${var.docker_username}:${var.docker_password}")
        }
      }
    })
  }
}

resource "kubernetes_secret" "app_secrets" {
  metadata {
    name      = "app-secrets"
    namespace = kubernetes_namespace.ecommerce.metadata[0].name
    labels    = var.common_labels
  }

  type = "Opaque"

  data = {
    database_url      = base64encode(var.database_url)
    database_username = base64encode(var.database_username)
    database_password = base64encode(var.database_password)
    jwt_secret        = base64encode(var.jwt_secret)
  }
}

# ============================================================================
# RBAC
# ============================================================================

resource "kubernetes_service_account" "ecommerce_service_account" {
  metadata {
    name      = "ecommerce-service-account"
    namespace = kubernetes_namespace.ecommerce.metadata[0].name
    labels    = var.common_labels
  }

  automount_service_account_token = true
}

resource "kubernetes_role" "ecommerce_role" {
  metadata {
    name      = "ecommerce-role"
    namespace = kubernetes_namespace.ecommerce.metadata[0].name
    labels    = var.common_labels
  }

  rule {
    api_groups = [""]
    resources  = ["pods", "services", "endpoints", "configmaps"]
    verbs      = ["get", "list", "watch"]
  }
  
  rule {
    api_groups = ["apps"]
    resources  = ["deployments", "replicasets"]
    verbs      = ["get", "list", "watch"]
  }
}

resource "kubernetes_role_binding" "ecommerce_role_binding" {
  metadata {
    name      = "ecommerce-role-binding"
    namespace = kubernetes_namespace.ecommerce.metadata[0].name
    labels    = var.common_labels
  }

  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "Role"
    name      = kubernetes_role.ecommerce_role.metadata[0].name
  }

  subject {
    kind      = "ServiceAccount"
    name      = kubernetes_service_account.ecommerce_service_account.metadata[0].name
    namespace = kubernetes_namespace.ecommerce.metadata[0].name
  }
} 