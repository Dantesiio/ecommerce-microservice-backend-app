# Terraform Infrastructure as Code

Este directorio contiene la configuración de Terraform para desplegar la infraestructura del proyecto ecommerce-microservice en Kubernetes.

## Estructura

```
terraform/
├── main.tf                    # Configuración principal
├── variables.tf               # Variables globales
├── outputs.tf                 # Outputs del módulo principal
├── modules/
│   ├── kubernetes/           # Módulo para recursos Kubernetes
│   ├── monitoring/           # Módulo para stack de monitoreo
│   └── security/             # Módulo para seguridad y RBAC
└── environments/
    ├── dev/                  # Configuración para desarrollo
    ├── stage/                # Configuración para staging
    └── prod/                 # Configuración para producción
```

## Requisitos Previos

1. Terraform >= 1.0
2. kubectl configurado con acceso al clúster Kubernetes
3. Credenciales de Docker Hub (o registry configurado)
4. Backend remoto para estado de Terraform (S3, Azure Storage, etc.)

## Configuración del Backend Remoto

Para usar un backend remoto (recomendado para producción), configura el backend en `main.tf`:

```hcl
backend "s3" {
  bucket = "ecommerce-terraform-state"
  key    = "terraform.tfstate"
  region = "us-east-1"
  encrypt = true
}
```

O para Azure:

```hcl
backend "azurerm" {
  resource_group_name  = "terraform-state"
  storage_account_name = "terraformstate"
  container_name       = "tfstate"
  key                  = "terraform.tfstate"
}
```

## Uso

### Inicializar Terraform

```bash
cd terraform
terraform init
```

### Planear cambios

Para desarrollo:
```bash
terraform plan -var-file=environments/dev/terraform.tfvars
```

Para staging:
```bash
terraform plan -var-file=environments/stage/terraform.tfvars
```

Para producción:
```bash
terraform plan -var-file=environments/prod/terraform.tfvars
```

### Aplicar cambios

```bash
terraform apply -var-file=environments/dev/terraform.tfvars
```

### Destruir infraestructura

```bash
terraform destroy -var-file=environments/dev/terraform.tfvars
```

## Módulos

### Kubernetes

Despliega todos los microservicios y servicios de infraestructura (Eureka, Cloud Config) en Kubernetes.

### Monitoring

Despliega el stack de monitoreo:
- Prometheus para métricas
- Grafana para dashboards
- Elasticsearch y Kibana para logs
- Jaeger para distributed tracing

### Security

Configura seguridad:
- Service Accounts y RBAC
- Network Policies
- Secrets management
- Pod Security Policies

## Variables Principales

- `environment`: Ambiente (dev, stage, prod)
- `project_name`: Nombre del proyecto
- `region`: Región de AWS
- `docker_registry`: Registry de Docker
- `docker_repository`: Repositorio de Docker
- `replica_count`: Número de réplicas por ambiente
- `resource_limits`: Límites de recursos por ambiente

## Outputs

- `namespace`: Namespace de Kubernetes creado
- `environment`: Ambiente actual
- `monitoring_dashboard_url`: URL de Grafana
- `logging_dashboard_url`: URL de Kibana
- `tracing_url`: URL de Jaeger

## Arquitectura

La infraestructura se despliega en namespaces separados por ambiente:
- `ecommerce-microservice-dev`
- `ecommerce-microservice-stage`
- `ecommerce-microservice-prod`

Cada ambiente tiene sus propios recursos con configuraciones específicas de recursos y réplicas.

## Documentación Adicional

Ver `docs/terraform/` para documentación detallada de la arquitectura.

