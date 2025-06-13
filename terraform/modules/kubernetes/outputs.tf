output "namespace_name" {
  description = "The name of the created namespace"
  value       = kubernetes_namespace.ecommerce.metadata[0].name
}

output "namespace_id" {
  description = "The ID of the created namespace"
  value       = kubernetes_namespace.ecommerce.id
}

output "config_map_name" {
  description = "The name of the common config map"
  value       = kubernetes_config_map.common_config.metadata[0].name
}

output "service_account_name" {
  description = "The name of the service account"
  value       = kubernetes_service_account.ecommerce_service_account.metadata[0].name
}

output "docker_registry_secret_name" {
  description = "The name of the Docker registry secret"
  value       = kubernetes_secret.docker_registry.metadata[0].name
}

output "app_secrets_name" {
  description = "The name of the application secrets"
  value       = kubernetes_secret.app_secrets.metadata[0].name
} 