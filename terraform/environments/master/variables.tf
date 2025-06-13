variable "environment" {
  description = "Environment name (develop, stage, master)"
  type        = string
}

variable "location" {
  description = "Azure region where resources will be created"
  type        = string
}

variable "node_count" {
  description = "Number of nodes in the AKS cluster"
  type        = number
  default     = 4
}

variable "node_size" {
  description = "Size of the nodes in the AKS cluster"
  type        = string
  default     = "Standard_DS4_v2"
} 