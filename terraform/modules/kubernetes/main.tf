# Kubernetes namespace
resource "kubernetes_namespace" "main" {
  metadata {
    name = var.namespace
    labels = var.common_tags
  }
}

# Service Discovery (Eureka)
resource "kubernetes_deployment" "eureka" {
  metadata {
    name      = "eureka-service"
    namespace = kubernetes_namespace.main.metadata[0].name
    labels = merge(var.common_tags, {
      app = "eureka-service"
    })
  }
  
  spec {
    replicas = var.replica_count[var.environment]
    
    selector {
      match_labels = {
        app = "eureka-service"
      }
    }
    
    template {
      metadata {
        labels = {
          app = "eureka-service"
        }
      }
      
      spec {
        container {
          name  = "eureka-service"
          image = "${var.docker_registry}/${var.docker_repository}:eureka-service-latest"
          
          port {
            container_port = 8761
          }
          
          resources {
            requests = {
              cpu    = "200m"
              memory  = "256Mi"
            }
            limits = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }
          
          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = 8761
            }
            initial_delay_seconds = 60
            period_seconds       = 30
          }
          
          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8761
            }
            initial_delay_seconds = 30
            period_seconds       = 10
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "eureka" {
  metadata {
    name      = "eureka-service"
    namespace = kubernetes_namespace.main.metadata[0].name
  }
  
  spec {
    selector = {
      app = "eureka-service"
    }
    
    port {
      port        = 8761
      target_port = 8761
    }
    
    type = "ClusterIP"
  }
}

# Cloud Config
resource "kubernetes_deployment" "cloud_config" {
  metadata {
    name      = "cloud-config-service"
    namespace = kubernetes_namespace.main.metadata[0].name
    labels = merge(var.common_tags, {
      app = "cloud-config-service"
    })
  }
  
  spec {
    replicas = var.replica_count[var.environment]
    
    selector {
      match_labels = {
        app = "cloud-config-service"
      }
    }
    
    template {
      metadata {
        labels = {
          app = "cloud-config-service"
        }
      }
      
      spec {
        container {
          name  = "cloud-config-service"
          image = "${var.docker_registry}/${var.docker_repository}:cloud-config-latest"
          
          port {
            container_port = 9296
          }
          
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = var.environment
          }
          
          resources {
            requests = {
              cpu    = "200m"
              memory  = "256Mi"
            }
            limits = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "cloud_config" {
  metadata {
    name      = "cloud-config-service"
    namespace = kubernetes_namespace.main.metadata[0].name
  }
  
  spec {
    selector = {
      app = "cloud-config-service"
    }
    
    port {
      port        = 9296
      target_port = 9296
    }
    
    type = "ClusterIP"
  }
}

# Microservices deployments (template para todos los servicios)
locals {
  services = [
    "api-gateway",
    "proxy-client",
    "user-service",
    "product-service",
    "order-service",
    "payment-service",
    "shipping-service",
    "favourite-service"
  ]
  
  service_ports = {
    "api-gateway"      = 8080
    "proxy-client"     = 8900
    "user-service"     = 8700
    "product-service"  = 8200
    "order-service"    = 8300
    "payment-service"  = 8400
    "shipping-service" = 8500
    "favourite-service" = 8600
  }
}

resource "kubernetes_deployment" "microservices" {
  for_each = toset(local.services)
  
  metadata {
    name      = "${replace(each.value, "-", "-")}-deployment"
    namespace = kubernetes_namespace.main.metadata[0].name
    labels = merge(var.common_tags, {
      app = each.value
    })
  }
  
  spec {
    replicas = var.replica_count[var.environment]
    
    selector {
      match_labels = {
        app = each.value
      }
    }
    
    template {
      metadata {
        labels = {
          app = each.value
        }
      }
      
      spec {
        container {
          name  = each.value
          image = "${var.docker_registry}/${var.docker_repository}:${each.value}-latest"
          
          port {
            container_port = local.service_ports[each.value]
          }
          
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = var.environment
          }
          
          env {
            name  = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE"
            value = "http://${kubernetes_service.eureka.metadata[0].name}:8761/eureka/"
          }
          
          env {
            name  = "SPRING_CLOUD_CONFIG_URI"
            value = "http://${kubernetes_service.cloud_config.metadata[0].name}:9296"
          }
          
          resources {
            requests = {
              cpu    = "200m"
              memory  = "256Mi"
            }
            limits = {
              cpu    = var.resource_limits[var.environment].cpu
              memory = var.resource_limits[var.environment].memory
            }
          }
          
          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = local.service_ports[each.value]
            }
            initial_delay_seconds = 60
            period_seconds       = 30
          }
          
          readiness_probe {
            http_get {
              path = "/actuator/health/readiness"
              port = local.service_ports[each.value]
            }
            initial_delay_seconds = 30
            period_seconds       = 10
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "microservices" {
  for_each = toset(local.services)
  
  metadata {
    name      = "${each.value}-service"
    namespace = kubernetes_namespace.main.metadata[0].name
  }
  
  spec {
    selector = {
      app = each.value
    }
    
    port {
      port        = local.service_ports[each.value]
      target_port = local.service_ports[each.value]
    }
    
    type = "ClusterIP"
  }
}

