output "cluster_name" {
  description = "AKS cluster name"
  value       = azurerm_kubernetes_cluster.aks.name
}

output "cluster_id" {
  description = "AKS cluster ID"
  value       = azurerm_kubernetes_cluster.aks.id
}

output "kube_config" {
  description = "Kubernetes config for the cluster"
  value       = azurerm_kubernetes_cluster.aks.kube_config_raw
  sensitive   = true
}

output "kube_config_base64" {
  description = "Base64 encoded kubeconfig"
  value       = base64encode(azurerm_kubernetes_cluster.aks.kube_config_raw)
  sensitive   = true
}

output "resource_group_name" {
  description = "Resource group name"
  value       = azurerm_resource_group.aks.name
}

output "acr_login_server" {
  description = "ACR login server URL"
  value       = var.create_acr ? azurerm_container_registry.acr[0].login_server : null
}

output "acr_admin_username" {
  description = "ACR admin username"
  value       = var.create_acr ? azurerm_container_registry.acr[0].admin_username : null
  sensitive   = true
}

output "acr_admin_password" {
  description = "ACR admin password"
  value       = var.create_acr ? azurerm_container_registry.acr[0].admin_password : null
  sensitive   = true
}
