variable "environment" {
  description = "Environment name (dev, stage, prod)"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "ecommerce-microservice"
}

variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "kubeconfig_path" {
  description = "Path to kubeconfig file"
  type        = string
  default     = "~/.kube/config"
}

variable "kube_context" {
  description = "Kubernetes context to use"
  type        = string
  default     = ""
}

variable "docker_registry" {
  description = "Docker registry URL"
  type        = string
  default     = "docker.io"
}

variable "docker_repository" {
  description = "Docker repository name"
  type        = string
  default     = "dantesiio/ecommerce-backend"
}

variable "replica_count" {
  description = "Number of replicas per service"
  type        = map(number)
  default = {
    dev   = 1
    stage = 2
    prod  = 3
  }
}

variable "resource_limits" {
  description = "Resource limits per environment"
  type = map(object({
    cpu    = string
    memory = string
  }))
  default = {
    dev = {
      cpu    = "500m"
      memory = "512Mi"
    }
    stage = {
      cpu    = "1000m"
      memory = "1Gi"
    }
    prod = {
      cpu    = "2000m"
      memory = "2Gi"
    }
  }
}

variable "enable_monitoring" {
  description = "Enable monitoring stack"
  type        = bool
  default     = true
}

variable "enable_logging" {
  description = "Enable logging stack (ELK)"
  type        = bool
  default     = true
}

variable "enable_tracing" {
  description = "Enable distributed tracing"
  type        = bool
  default     = true
}

