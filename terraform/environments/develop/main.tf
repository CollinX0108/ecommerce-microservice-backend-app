provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "rg-${var.environment}-ecommerce"
  location = var.location

  tags = {
    Environment = var.environment
    Project     = "ecommerce-microservice"
    Org         = "1110285872"
  }
}

module "aks" {
  source = "../../modules/aks"

  cluster_name       = "aks-${var.environment}-ecommerce"
  location           = var.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix         = "${var.environment}-ecommerce"
  node_count         = var.node_count
  node_size          = var.node_size
  environment        = var.environment
} 