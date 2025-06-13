variable "cluster_name" {
  description = "Name of the AKS cluster"
  type        = string
}

variable "location" {
  description = "Azure region where resources will be created"
  type        = string
}

variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
}

variable "dns_prefix" {
  description = "DNS prefix for the AKS cluster"
  type        = string
}

variable "node_count" {
  description = "Number of nodes in the AKS cluster"
  type        = number
  default     = 2
}

variable "node_size" {
  description = "Size of the nodes in the AKS cluster"
  type        = string
  default     = "Standard_DS2_v2"
}

variable "environment" {
  description = "Environment name (develop, stage, master)"
  type        = string
} 