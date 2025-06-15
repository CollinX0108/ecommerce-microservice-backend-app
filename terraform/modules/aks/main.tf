resource "azurerm_kubernetes_cluster" "aks" {
  name                = var.cluster_name
  location            = var.location
  resource_group_name = var.resource_group_name
  dns_prefix         = var.dns_prefix

  default_node_pool {
    name                          = "default"
    node_count                    = var.node_count
    vm_size                       = var.node_size
    temporary_name_for_rotation   = "defaulttemp"
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin = "kubenet"
    network_policy = "calico"
  }

  tags = {
    Environment = var.environment
    Project     = "ecommerce-microservice"
    Org         = "1110285872"
  }
}

resource "azurerm_kubernetes_cluster_node_pool" "arm64" {
  name                  = "arm64"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.aks.id
  node_count           = var.node_count
  vm_size              = "Standard_B2ps_v2"
  node_taints          = ["arch=arm64:NoSchedule"]

  tags = {
    Environment = var.environment
    Project     = "ecommerce-microservice"
    Org         = "1110285872"
  }
} 