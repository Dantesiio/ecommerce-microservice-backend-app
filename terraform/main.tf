terraform {
  required_version = ">= 1.0"
  
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  # Backend remoto para estado (configurar según ambiente)
  backend "s3" {
    # Configurar en terraform.tfvars o variables de entorno
    # bucket = "ecommerce-terraform-state"
    # key    = "terraform.tfstate"
    # region = "us-east-1"
    # encrypt = true
  }
}

# Variables de ambiente
variable "environment" {
  description = "Environment name (dev, stage, prod)"
  type        = string
  default     = "dev"
  
  validation {
    condition     = contains(["dev", "stage", "prod"], var.environment)
    error_message = "Environment must be dev, stage, or prod."
  }
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

# Provider configuration
provider "kubernetes" {
  config_path    = var.kubeconfig_path
  config_context = var.kube_context
}

provider "docker" {
  host = "unix:///var/run/docker.sock"
}

provider "aws" {
  region = var.region
}

# Local values
locals {
  common_tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
  
  namespace = "${var.project_name}-${var.environment}"
}

# Módulos
module "kubernetes_cluster" {
  source = "./modules/kubernetes"
  
  environment      = var.environment
  project_name     = var.project_name
  namespace        = local.namespace
  common_tags      = local.common_tags
  docker_registry  = var.docker_registry
  docker_repository = var.docker_repository
  replica_count    = var.replica_count
  resource_limits  = var.resource_limits
}

module "monitoring" {
  source = "./modules/monitoring"
  
  environment     = var.environment
  project_name    = var.project_name
  namespace        = local.namespace
  common_tags      = local.common_tags
  enable_monitoring = var.enable_monitoring
  enable_logging   = var.enable_logging
  enable_tracing   = var.enable_tracing
}

module "security" {
  source = "./modules/security"
  
  environment   = var.environment
  project_name  = var.project_name
  namespace     = local.namespace
  common_tags   = local.common_tags
}

# Outputs
output "namespace" {
  value = local.namespace
}

output "environment" {
  value = var.environment
}

