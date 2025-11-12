# Prometheus deployment
resource "kubernetes_deployment" "prometheus" {
  count = var.enable_monitoring ? 1 : 0
  
  metadata {
    name      = "prometheus"
    namespace = var.namespace
    labels = merge(var.common_tags, {
      app = "prometheus"
    })
  }
  
  spec {
    replicas = 1
    
    selector {
      match_labels = {
        app = "prometheus"
      }
    }
    
    template {
      metadata {
        labels = {
          app = "prometheus"
        }
      }
      
      spec {
        container {
          name  = "prometheus"
          image = "prom/prometheus:latest"
          
          port {
            container_port = 9090
          }
          
          volume_mount {
            name       = "prometheus-config"
            mount_path = "/etc/prometheus"
          }
          
          resources {
            requests = {
              cpu    = "500m"
              memory = "1Gi"
            }
            limits = {
              cpu    = "2000m"
              memory = "2Gi"
            }
          }
        }
        
        volume {
          name = "prometheus-config"
          config_map {
            name = kubernetes_config_map.prometheus[0].metadata[0].name
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "prometheus" {
  count = var.enable_monitoring ? 1 : 0
  
  metadata {
    name      = "prometheus-service"
    namespace = var.namespace
  }
  
  spec {
    selector = {
      app = "prometheus"
    }
    
    port {
      port        = 9090
      target_port = 9090
    }
    
    type = "ClusterIP"
  }
}

resource "kubernetes_config_map" "prometheus" {
  count = var.enable_monitoring ? 1 : 0
  
  metadata {
    name      = "prometheus-config"
    namespace = var.namespace
  }
  
  data = {
    "prometheus.yml" = <<-EOT
      global:
        scrape_interval: 15s
        evaluation_interval: 15s
      
      scrape_configs:
        - job_name: 'kubernetes-pods'
          kubernetes_sd_configs:
            - role: pod
          relabel_configs:
            - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
              action: keep
              regex: true
            - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
              action: replace
              target_label: __metrics_path__
              regex: (.+)
            - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
              action: replace
              regex: ([^:]+)(?::\d+)?;(\d+)
              replacement: $1:$2
              target_label: __address__
    EOT
  }
}

# Grafana deployment
resource "kubernetes_deployment" "grafana" {
  count = var.enable_monitoring ? 1 : 0
  
  metadata {
    name      = "grafana"
    namespace = var.namespace
    labels = merge(var.common_tags, {
      app = "grafana"
    })
  }
  
  spec {
    replicas = 1
    
    selector {
      match_labels = {
        app = "grafana"
      }
    }
    
    template {
      metadata {
        labels = {
          app = "grafana"
        }
      }
      
      spec {
        container {
          name  = "grafana"
          image = "grafana/grafana:latest"
          
          port {
            container_port = 3000
          }
          
          env {
            name  = "GF_SECURITY_ADMIN_PASSWORD"
            value = "admin"
          }
          
          env {
            name  = "GF_SERVER_ROOT_URL"
            value = "http://localhost:3000"
          }
          
          resources {
            requests = {
              cpu    = "200m"
              memory = "512Mi"
            }
            limits = {
              cpu    = "1000m"
              memory = "1Gi"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "grafana" {
  count = var.enable_monitoring ? 1 : 0
  
  metadata {
    name      = "grafana-service"
    namespace = var.namespace
  }
  
  spec {
    selector = {
      app = "grafana"
    }
    
    port {
      port        = 3000
      target_port = 3000
    }
    
    type = "ClusterIP"
  }
}

# ELK Stack - Elasticsearch
resource "kubernetes_deployment" "elasticsearch" {
  count = var.enable_logging ? 1 : 0
  
  metadata {
    name      = "elasticsearch"
    namespace = var.namespace
    labels = merge(var.common_tags, {
      app = "elasticsearch"
    })
  }
  
  spec {
    replicas = 1
    
    selector {
      match_labels = {
        app = "elasticsearch"
      }
    }
    
    template {
      metadata {
        labels = {
          app = "elasticsearch"
        }
      }
      
      spec {
        container {
          name  = "elasticsearch"
          image = "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
          
          port {
            container_port = 9200
          }
          
          env {
            name  = "discovery.type"
            value = "single-node"
          }
          
          env {
            name  = "xpack.security.enabled"
            value = "false"
          }
          
          resources {
            requests = {
              cpu    = "1000m"
              memory = "2Gi"
            }
            limits = {
              cpu    = "2000m"
              memory = "4Gi"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "elasticsearch" {
  count = var.enable_logging ? 1 : 0
  
  metadata {
    name      = "elasticsearch-service"
    namespace = var.namespace
  }
  
  spec {
    selector = {
      app = "elasticsearch"
    }
    
    port {
      port        = 9200
      target_port = 9200
    }
    
    type = "ClusterIP"
  }
}

# Kibana
resource "kubernetes_deployment" "kibana" {
  count = var.enable_logging ? 1 : 0
  
  metadata {
    name      = "kibana"
    namespace = var.namespace
    labels = merge(var.common_tags, {
      app = "kibana"
    })
  }
  
  spec {
    replicas = 1
    
    selector {
      match_labels = {
        app = "kibana"
      }
    }
    
    template {
      metadata {
        labels = {
          app = "kibana"
        }
      }
      
      spec {
        container {
          name  = "kibana"
          image = "docker.elastic.co/kibana/kibana:8.11.0"
          
          port {
            container_port = 5601
          }
          
          env {
            name  = "ELASTICSEARCH_HOSTS"
            value = "http://${kubernetes_service.elasticsearch[0].metadata[0].name}:9200"
          }
          
          resources {
            requests = {
              cpu    = "500m"
              memory = "1Gi"
            }
            limits = {
              cpu    = "1000m"
              memory = "2Gi"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "kibana" {
  count = var.enable_logging ? 1 : 0
  
  metadata {
    name      = "kibana-service"
    namespace = var.namespace
  }
  
  spec {
    selector = {
      app = "kibana"
    }
    
    port {
      port        = 5601
      target_port = 5601
    }
    
    type = "ClusterIP"
  }
}

# Jaeger for distributed tracing
resource "kubernetes_deployment" "jaeger" {
  count = var.enable_tracing ? 1 : 0
  
  metadata {
    name      = "jaeger"
    namespace = var.namespace
    labels = merge(var.common_tags, {
      app = "jaeger"
    })
  }
  
  spec {
    replicas = 1
    
    selector {
      match_labels = {
        app = "jaeger"
      }
    }
    
    template {
      metadata {
        labels = {
          app = "jaeger"
        }
      }
      
      spec {
        container {
          name  = "jaeger"
          image = "jaegertracing/all-in-one:latest"
          
          port {
            container_port = 16686
          }
          
          port {
            container_port = 14268
          }
          
          resources {
            requests = {
              cpu    = "200m"
              memory = "512Mi"
            }
            limits = {
              cpu    = "500m"
              memory = "1Gi"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "jaeger" {
  count = var.enable_tracing ? 1 : 0
  
  metadata {
    name      = "jaeger-service"
    namespace = var.namespace
  }
  
  spec {
    selector = {
      app = "jaeger"
    }
    
    port {
      name       = "ui"
      port        = 16686
      target_port = 16686
    }
    
    port {
      name       = "http"
      port        = 14268
      target_port = 14268
    }
    
    type = "ClusterIP"
  }
}

