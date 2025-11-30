# Outputs for Azure AKS deployment

output "aks_cluster_name" {
  description = "Name of the AKS cluster"
  value       = module.azure_aks.cluster_name
}

output "aks_resource_group" {
  description = "Resource group containing the AKS cluster"
  value       = module.azure_aks.resource_group_name
}

output "kubeconfig_raw" {
  description = "Raw kubeconfig for the AKS cluster"
  value       = module.azure_aks.kube_config
  sensitive   = true
}

output "kubeconfig_base64" {
  description = "Base64 encoded kubeconfig (ready for GitHub Secrets)"
  value       = module.azure_aks.kube_config_base64
  sensitive   = true
}

output "namespace" {
  description = "Kubernetes namespace created"
  value       = "${var.project_name}-${var.environment}"
}

output "acr_login_server" {
  description = "ACR login server URL (if created)"
  value       = module.azure_aks.acr_login_server
}

output "get_kubeconfig_command" {
  description = "Command to get kubeconfig from Azure"
  value       = "az aks get-credentials --resource-group ${module.azure_aks.resource_group_name} --name ${module.azure_aks.cluster_name}"
}

output "github_secret_command" {
  description = "Command to encode kubeconfig for GitHub Secrets"
  value       = "terraform output -raw kubeconfig_raw | base64 -w 0"
}
