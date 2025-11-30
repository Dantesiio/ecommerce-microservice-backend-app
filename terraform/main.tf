# Terraform configuration for Azure AKS deployment
# Use this file to create the AKS cluster infrastructure

terraform {
  required_version = ">= 1.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
  }

  # Backend remoto Azure para estado
  backend "azurerm" {
    resource_group_name  = "terraform-state-rg"
    storage_account_name = "tfstate85754"
    container_name       = "tfstate"
    key                  = "azure-aks.tfstate"
  }
}

provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

# Local values
locals {
  common_tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

# Create AKS cluster
module "azure_aks" {
  source = "./modules/azure-aks"

  project_name       = var.project_name
  environment        = var.environment
  location           = var.azure_location
  kubernetes_version = var.kubernetes_version
  node_count         = var.aks_node_count
  min_node_count     = var.aks_min_node_count
  max_node_count     = var.aks_max_node_count
  vm_size            = var.aks_vm_size
  create_acr         = var.create_acr
  common_tags        = local.common_tags
}
