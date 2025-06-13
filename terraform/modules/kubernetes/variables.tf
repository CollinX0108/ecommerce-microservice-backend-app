variable "namespace_name" {
  description = "Name of the Kubernetes namespace"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "common_labels" {
  description = "Common labels to apply to all resources"
  type        = map(string)
}

variable "docker_username" {
  description = "Docker registry username"
  type        = string
  sensitive   = true
  default     = ""
}

variable "docker_password" {
  description = "Docker registry password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "database_url" {
  description = "Database connection URL"
  type        = string
  sensitive   = true
  default     = "jdbc:h2:mem:ecommerce_db;DB_CLOSE_ON_EXIT=FALSE"
}

variable "database_username" {
  description = "Database username"
  type        = string
  sensitive   = true
  default     = "sa"
}

variable "database_password" {
  description = "Database password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "jwt_secret" {
  description = "JWT secret key"
  type        = string
  sensitive   = true
  default     = "ecommerce-jwt-secret-key-2024"
} 