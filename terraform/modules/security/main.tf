# Kubernetes Service Account
resource "kubernetes_service_account" "microservices" {
  metadata {
    name      = "microservices-sa"
    namespace = var.namespace
    labels    = var.common_tags
  }
}

# RBAC - Role
resource "kubernetes_role" "microservices" {
  metadata {
    name      = "microservices-role"
    namespace = var.namespace
    labels    = var.common_tags
  }
  
  rule {
    api_groups = [""]
    resources  = ["pods", "services", "configmaps", "secrets"]
    verbs      = ["get", "list", "watch"]
  }
  
  rule {
    api_groups = ["apps"]
    resources  = ["deployments"]
    verbs      = ["get", "list", "watch"]
  }
}

# RBAC - RoleBinding
resource "kubernetes_role_binding" "microservices" {
  metadata {
    name      = "microservices-rolebinding"
    namespace = var.namespace
    labels    = var.common_tags
  }
  
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "Role"
    name      = kubernetes_role.microservices.metadata[0].name
  }
  
  subject {
    kind      = "ServiceAccount"
    name      = kubernetes_service_account.microservices.metadata[0].name
    namespace = var.namespace
  }
}

# Network Policy - Deny all by default
resource "kubernetes_network_policy" "default_deny" {
  metadata {
    name      = "default-deny-all"
    namespace = var.namespace
    labels    = var.common_tags
  }
  
  spec {
    pod_selector {}
    policy_types = ["Ingress", "Egress"]
  }
}

# Network Policy - Allow internal communication
resource "kubernetes_network_policy" "allow_internal" {
  metadata {
    name      = "allow-internal"
    namespace = var.namespace
    labels    = var.common_tags
  }
  
  spec {
    pod_selector {
      match_labels = var.common_tags
    }
    
    ingress {
      from {
        pod_selector {
          match_labels = var.common_tags
        }
      }
    }
    
    egress {
      to {
        pod_selector {
          match_labels = var.common_tags
        }
      }
    }
    
    policy_types = ["Ingress", "Egress"]
  }
}

# Secret for database credentials (example)
resource "kubernetes_secret" "db_credentials" {
  metadata {
    name      = "db-credentials"
    namespace = var.namespace
    labels    = var.common_tags
  }
  
  type = "Opaque"
  
  data = {
    username = base64encode("dbuser")
    password = base64encode("dbpassword")
  }
}

# Pod Security Policy (DEPRECATED in Kubernetes 1.25+)
# Commented out as Minikube v1.34 no longer supports PSP
# Use Pod Security Standards (PSS) instead in production
# resource "kubernetes_pod_security_policy" "restricted" {
#   metadata {
#     name   = "restricted-psp"
#     labels = var.common_tags
#   }
#
#   spec {
#     privileged                 = false
#     allow_privilege_escalation  = false
#     required_drop_capabilities = ["ALL"]
#
#     volumes = [
#       "configMap",
#       "emptyDir",
#       "projected",
#       "secret",
#       "downwardAPI",
#       "persistentVolumeClaim"
#     ]
#
#     run_as_user {
#       rule = "MustRunAsNonRoot"
#     }
#
#     se_linux {
#       rule = "RunAsAny"
#     }
#
#     fs_group {
#       rule = "RunAsAny"
#     }
#
#     read_only_root_filesystem = false
#   }
# }

