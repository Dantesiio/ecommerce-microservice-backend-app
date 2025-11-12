output "grafana_url" {
  description = "Grafana dashboard URL"
  value       = var.enable_monitoring ? "http://${kubernetes_service.grafana[0].metadata[0].name}.${var.namespace}.svc.cluster.local:3000" : null
}

output "kibana_url" {
  description = "Kibana dashboard URL"
  value       = var.enable_logging ? "http://${kubernetes_service.kibana[0].metadata[0].name}.${var.namespace}.svc.cluster.local:5601" : null
}

output "tracing_url" {
  description = "Jaeger tracing URL"
  value       = var.enable_tracing ? "http://${kubernetes_service.jaeger[0].metadata[0].name}.${var.namespace}.svc.cluster.local:16686" : null
}

output "prometheus_url" {
  description = "Prometheus URL"
  value       = var.enable_monitoring ? "http://${kubernetes_service.prometheus[0].metadata[0].name}.${var.namespace}.svc.cluster.local:9090" : null
}

