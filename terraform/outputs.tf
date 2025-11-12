output "namespace" {
  description = "Kubernetes namespace name"
  value       = local.namespace
}

output "environment" {
  description = "Current environment"
  value       = var.environment
}

output "kubernetes_endpoint" {
  description = "Kubernetes cluster endpoint"
  value       = module.kubernetes_cluster.cluster_endpoint
  sensitive   = true
}

output "monitoring_dashboard_url" {
  description = "Grafana dashboard URL"
  value       = module.monitoring.grafana_url
}

output "logging_dashboard_url" {
  description = "Kibana dashboard URL"
  value       = module.monitoring.kibana_url
}

output "tracing_url" {
  description = "Jaeger/Zipkin tracing URL"
  value       = module.monitoring.tracing_url
}

