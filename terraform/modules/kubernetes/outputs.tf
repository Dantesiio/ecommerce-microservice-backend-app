output "cluster_endpoint" {
  description = "Kubernetes cluster endpoint"
  value       = "configured"
}

output "namespace_name" {
  description = "Namespace name"
  value       = kubernetes_namespace.main.metadata[0].name
}

