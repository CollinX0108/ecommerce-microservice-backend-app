terraform {
  backend "azurerm" {
    resource_group_name  = "rg-terraform-state"
    storage_account_name = "stterraformstate1110285872"
    container_name      = "tfstate"
    key                 = "ecommerce-microservice/terraform.tfstate"
  }
} 