variable "environment" {
  description = "Environment name"
  type        = string
}

variable "project_name" {
  description = "Project name"
  type        = string
}

variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
}

variable "common_tags" {
  description = "Common tags for resources"
  type        = map(string)
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
  description = "Number of replicas per environment"
  type        = map(number)
}

variable "resource_limits" {
  description = "Resource limits per environment"
  type = map(object({
    cpu    = string
    memory = string
  }))
}

