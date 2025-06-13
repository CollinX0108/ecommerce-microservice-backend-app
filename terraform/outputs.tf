# ============================================================================
# TERRAFORM OUTPUTS - ECOMMERCE MICROSERVICES
# ============================================================================

output "namespace_name" {
  description = "Kubernetes namespace name"
  value       = module.kubernetes_base.namespace_name
}

output "api_gateway_url" {
  description = "API Gateway external URL"
  value       = module.microservices.api_gateway_url
}

output "microservice_endpoints" {
  description = "Internal endpoints for all microservices"
  value       = module.microservices.service_endpoints
}

output "monitoring_endpoints" {
  description = "Monitoring stack endpoints"
  value       = var.enable_monitoring ? module.monitoring.endpoints : {}
}

output "service_discovery_url" {
  description = "Eureka service discovery URL"
  value       = module.microservices.service_discovery_url
}

output "config_server_url" {
  description = "Spring Cloud Config server URL"
  value       = module.microservices.config_server_url
}

output "zipkin_url" {
  description = "Zipkin tracing URL"
  value       = module.monitoring.zipkin_url
}

output "environment_info" {
  description = "Environment information"
  value = {
    environment     = var.environment
    namespace       = module.kubernetes_base.namespace_name
    replica_counts  = var.replica_count
    image_tag      = var.image_tag
    docker_registry = var.docker_registry
  }
}

output "health_check_urls" {
  description = "Health check URLs for all services"
  value = {
    for service in local.microservices :
    service => "http://${service}.${module.kubernetes_base.namespace_name}.svc.cluster.local:${module.microservices.service_ports[service]}/actuator/health"
  }
} 