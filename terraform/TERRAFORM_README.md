# Guía Rápida de Terraform

## 📁 Estructura de Archivos

```
terraform/
├── main.tf                          # Configuración principal (Kubernetes genérico)
├── azure-main.tf                    # Configuración específica para Azure AKS
├── variables.tf                     # Variables globales
├── outputs.tf                       # Outputs generales
├── azure-variables.tf              # Variables específicas de Azure
├── azure-outputs.tf                # Outputs específicos de Azure
├── modules/
│   ├── azure-aks/                  # Módulo para crear cluster AKS
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── kubernetes/                 # Módulo para deployments de K8s
│   ├── monitoring/                 # Módulo de monitoreo
│   └── security/                   # Módulo de seguridad
└── environments/
    ├── dev/
    │   ├── terraform.tfvars        # Variables para dev (genérico)
    │   └── azure.tfvars            # Variables para dev en Azure
    ├── stage/
    └── prod/
```

## 🎯 ¿Qué archivo usar?

### Para crear infraestructura en Azure AKS

Usa `azure-main.tf`:

```bash
# Inicializar
terraform init

# Ver plan
terraform plan \
  -var-file=environments/dev/azure.tfvars

# Aplicar
terraform apply \
  -var-file=environments/dev/azure.tfvars

# Obtener kubeconfig
terraform output -raw kubeconfig_base64
```

### Para trabajar con un cluster existente

Usa `main.tf`:

```bash
terraform init

terraform plan \
  -var-file=environments/dev/terraform.tfvars

terraform apply \
  -var-file=environments/dev/terraform.tfvars
```

## 🚀 Comandos Útiles

### Inicializar Terraform
```bash
cd terraform
terraform init
```

### Ver qué cambios se aplicarán (sin aplicar)
```bash
terraform plan -var-file=environments/dev/azure.tfvars
```

### Aplicar cambios
```bash
terraform apply -var-file=environments/dev/azure.tfvars
```

### Ver outputs
```bash
# Ver todos los outputs
terraform output

# Ver un output específico
terraform output kubeconfig_base64

# Ver output sin formato (útil para copiar)
terraform output -raw kubeconfig_base64
```

### Destruir infraestructura
```bash
terraform destroy -var-file=environments/dev/azure.tfvars
```

### Ver estado actual
```bash
terraform show
```

### Listar recursos
```bash
terraform state list
```

## 🔧 Configuración del Backend Remoto

El backend remoto almacena el estado de Terraform en Azure Storage Account.

### Configurar el backend por primera vez

1. Crear el Storage Account (solo una vez):
```bash
az group create --name terraform-state-rg --location eastus

az storage account create \
  --resource-group terraform-state-rg \
  --name tfstate85754 \
  --sku Standard_LRS

ACCOUNT_KEY=$(az storage account keys list \
  --resource-group terraform-state-rg \
  --account-name tfstate85754 \
  --query '[0].value' -o tsv)

az storage container create \
  --name tfstate \
  --account-name tfstate85754 \
  --account-key $ACCOUNT_KEY
```

2. El backend ya está configurado en `azure-main.tf`:
```hcl
backend "azurerm" {
  resource_group_name  = "terraform-state-rg"
  storage_account_name = "tfstate85754"
  container_name       = "tfstate"
  key                  = "azure-aks.tfstate"
}
```

### Usar backend local (para pruebas)

Si prefieres no usar Azure Storage, comenta el bloque `backend` en `azure-main.tf`:

```hcl
# backend "azurerm" {
#   resource_group_name  = "terraform-state-rg"
#   storage_account_name = "tfstate85754"
#   container_name       = "tfstate"
#   key                  = "azure-aks.tfstate"
# }
```

El estado se guardará localmente en `terraform.tfstate`.

## 📝 Personalizar Variables

Edita `environments/dev/azure.tfvars`:

```hcl
# Cambiar región de Azure
azure_location = "West Europe"

# Cambiar tamaño de VM para ahorrar costos
aks_vm_size = "Standard_B2s"  # Más barato para dev

# Reducir número de nodos
aks_node_count = 1

# Deshabilitar monitoreo en dev para ahorrar recursos
enable_monitoring = false
```

## 🔍 Troubleshooting

### Error: "backend configuration has changed"
```bash
terraform init -reconfigure
```

### Error: "resource already exists"
```bash
# Importar el recurso existente
terraform import azurerm_resource_group.aks /subscriptions/{sub-id}/resourceGroups/{rg-name}
```

### Ver logs detallados
```bash
export TF_LOG=DEBUG
terraform plan -var-file=environments/dev/azure.tfvars
```

### Validar configuración
```bash
terraform validate
terraform fmt -check
```

## 💡 Tips

1. **Siempre revisa el plan antes de aplicar**: `terraform plan` es tu amigo
2. **Usa workspaces para múltiples ambientes**:
   ```bash
   terraform workspace new dev
   terraform workspace select dev
   ```
3. **Bloquea versiones importantes**:
   ```hcl
   required_version = "~> 1.6.0"
   ```
4. **Usa `-auto-approve` solo en CI/CD**:
   ```bash
   terraform apply -var-file=environments/dev/azure.tfvars -auto-approve
   ```

## 🎓 Próximos Pasos

1. ✅ Crear cluster AKS con Terraform
2. ✅ Obtener kubeconfig
3. ✅ Configurar secreto en GitHub
4. ⬜ Configurar Ingress Controller
5. ⬜ Configurar cert-manager para HTTPS
6. ⬜ Configurar monitoring stack
7. ⬜ Configurar auto-scaling
