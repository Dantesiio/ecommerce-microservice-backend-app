# Análisis de Costos de Infraestructura

## Resumen Ejecutivo

Análisis completo de costos para desplegar la arquitectura de microservicios en diferentes ambientes, con estrategias de optimización y recomendaciones específicas para el proyecto académico.

**TL;DR**:
- ✅ **Desarrollo**: $0/mes (Minikube local)
- ⚠️ **Staging**: ~$40-50/mes (Azure AKS mínimo)
- ❌ **Producción completa**: ~$135/mes (excede presupuesto)
- 🎯 **Recomendación**: Usar Minikube local, deploy a Azure solo para demo final

---

## Tabla de Contenidos
- [Costos por Ambiente](#costos-por-ambiente)
- [Desglose Detallado](#desglose-detallado)
- [Estrategias de Optimización](#estrategias-de-optimización)
- [Presupuesto Azure for Students](#presupuesto-azure-for-students)
- [Recomendaciones para el Proyecto](#recomendaciones-para-el-proyecto)

---

## Costos por Ambiente

### Ambiente 1: Desarrollo Local (Minikube)

**Costo Total: $0/mes** ✅

| Recurso | Especificaciones | Costo |
|---------|------------------|-------|
| Minikube | 4 CPU, 8GB RAM, 20GB disk (local) | $0 |
| Observability Stack | Prometheus, Grafana, ELK, Zipkin | $0 |
| Microservicios | 10 servicios | $0 |

**Ventajas**:
- ✅ Completamente gratis
- ✅ Desarrollo y testing sin límites
- ✅ No consume créditos de Azure
- ✅ Suficiente para desarrollo y demostración del proyecto

**Desventajas**:
- ❌ Solo accesible localmente
- ❌ No simula ambiente cloud real
- ❌ Requiere máquina potente (mínimo 8GB RAM)

**Requisitos del Sistema**:
```
CPU: 4 cores (recomendado)
RAM: 8GB mínimo, 16GB recomendado
Disk: 20GB libres
OS: Windows 10/11 con WSL2 o Linux
```

---

### Ambiente 2: Azure AKS - Configuración Mínima

**Costo Total: ~$40-50/mes** ⚠️

| Recurso | Tipo/SKU | Cantidad | Precio Unitario | Costo Mensual |
|---------|----------|----------|-----------------|---------------|
| **AKS Cluster** | Gratis | 1 | $0 | $0 |
| **VM Nodes** | Standard_B2s (2 vCPU, 4GB RAM) | 2 | $15/mes | $30 |
| **Load Balancer** | Basic | 1 | $5/mes | $5 |
| **Managed Disks** | Standard SSD 32GB | 2 | $2.50/mes | $5 |
| **Public IP** | Static | 1 | $3/mes | $3 |
| **Bandwidth** | Primeros 5GB gratis | - | Variable | $2-5 |
| **Azure Storage** | Terraform state | 1GB | $0.02/GB | $0.02 |
| **Total** | - | - | - | **~$43-48/mes** |

**Consumo de crédito Azure for Students**:
- Crédito disponible: $100 USD
- Duración: ~2 meses
- **Estrategia**: Destruir recursos después de cada demo para ahorrar

---

### Ambiente 3: Azure AKS - Configuración Recomendada (Producción)

**Costo Total: ~$135-160/mes** ❌ (Excede presupuesto de estudiante)

| Recurso | Tipo/SKU | Cantidad | Precio Unitario | Costo Mensual |
|---------|----------|----------|----------|---------------|
| **AKS Cluster** | Gratis | 1 | $0 | $0 |
| **VM Nodes** | Standard_D2s_v3 (2 vCPU, 8GB RAM) | 3 | $35/mes | $105 |
| **Load Balancer** | Standard | 1 | $18/mes | $18 |
| **Managed Disks** | Premium SSD 64GB | 3 | $5/mes | $15 |
| **Public IPs** | Static | 2 | $3/mes | $6 |
| **Azure Database for MySQL** | Basic B1ms (1 vCore, 1GB RAM) | 1 | $15/mes | $15 |
| **Application Gateway** | WAF_v2 (opcional) | 1 | $125/mes | $0 (skip) |
| **Bandwidth** | ~10GB | - | Variable | $5-10 |
| **Backup Storage** | GRS 10GB | - | $0.05/GB | $0.50 |
| **Total** | - | - | - | **~$159/mes** |

**No recomendado para proyecto académico** debido a alto costo.

---

## Desglose Detallado

### Azure Kubernetes Service (AKS)

**Control Plane**: GRATIS ✅
- Microsoft gestiona el control plane sin costo
- Solo pagas por los nodos (VMs)

**Nodos de Trabajo** (donde corren los pods):

| Tipo de VM | vCPUs | RAM | Almacenamiento | Precio/mes | Uso Recomendado |
|------------|-------|-----|----------------|------------|-----------------|
| **Standard_B1s** | 1 | 1GB | 4GB | $7.50 | ❌ Muy limitado |
| **Standard_B2s** | 2 | 4GB | 8GB | $15 | ✅ Dev/Stage mínimo |
| **Standard_B2ms** | 2 | 8GB | 16GB | $30 | ✅ Stage/Prod básico |
| **Standard_D2s_v3** | 2 | 8GB | 16GB | $35 | 🎯 Producción |
| **Standard_D4s_v3** | 4 | 16GB | 32GB | $70 | 💰 Producción alta carga |

**Cálculo para proyecto**:
```
2 nodos x Standard_B2s = 2 x $15 = $30/mes
```

**Capacidad**:
- 2 nodos x 2 vCPU = 4 vCPUs totales
- 2 nodos x 4GB RAM = 8GB RAM total
- Suficiente para ~10-15 pods (microservicios)

---

### Load Balancer

| Tipo | Reglas incluidas | Precio/mes | Uso |
|------|------------------|------------|-----|
| **Basic** | 1 IP pública | $5 | ✅ Stage mínimo |
| **Standard** | 5 reglas, HA | $18 | 🎯 Producción |

Para el proyecto: **Basic** es suficiente.

---

### Almacenamiento (Managed Disks)

**Discos persistentes** para Prometheus, Grafana, Elasticsearch:

| Tipo | IOPS | Throughput | Precio/mes (32GB) | Uso |
|------|------|------------|-------------------|-----|
| **Standard HDD** | 500 | 60 MB/s | $1.50 | ❌ Muy lento |
| **Standard SSD** | 500 | 60 MB/s | $2.50 | ✅ Dev/Stage |
| **Premium SSD** | 120 | 25 MB/s | $5 | 🎯 Producción |

**Cálculo para proyecto**:
```
2 discos x 32GB Standard SSD = 2 x $2.50 = $5/mes
```

**Uso**:
- 1 disco para Prometheus + Grafana (PVC 20GB)
- 1 disco para Elasticsearch (PVC 30GB)

---

### Networking

**Public IP**:
- Basic: $3/mes
- Standard: $4/mes

**Bandwidth** (transferencia de datos):
- Primeros 5GB: GRATIS
- 5GB - 10TB: $0.087/GB (~$0.09/GB)
- Inbound: GRATIS

**Cálculo conservador**:
```
5GB gratis + 5GB adicional x $0.09 = $0.45/mes
```

Estimado realista para stage: **$2-5/mes** (dependiendo de uso)

---

### Azure Storage Account (Terraform Backend)

**Almacenamiento de estado de Terraform**:

| Recurso | Tamaño | Precio/GB/mes | Costo |
|---------|--------|---------------|-------|
| tfstate files | ~1MB | $0.02 | $0.02 |
| Transacciones (read/write) | ~1000/mes | $0.0004 per 10k | $0.0004 |
| **Total** | - | - | **< $0.10/mes** |

Prácticamente **gratis** ✅

---

### Azure Database for MySQL (Opcional)

**Si usas MySQL en Azure** en lugar de en pods:

| SKU | vCores | RAM | Storage | Precio/mes | Recomendado |
|-----|--------|-----|---------|------------|-------------|
| Basic B1ms | 1 | 1GB | 32GB | $15 | Para pruebas |
| General Purpose GP_Gen5_2 | 2 | 10GB | 100GB | $105 | Producción |

**Recomendación para proyecto**:
- ❌ NO usar Azure Database for MySQL (muy caro)
- ✅ Usar MySQL en pods de Kubernetes (gratis, incluido en nodos)

---

## Estrategias de Optimización

### 1. Usar Spot Instances (Ahorro: ~70-80%)

**Spot VMs** son instancias con descuento, pueden ser interrumpidas por Azure.

| VM Type | Precio Regular | Precio Spot | Ahorro |
|---------|----------------|-------------|--------|
| Standard_B2s | $15/mes | $3-5/mes | 70-80% |
| Standard_D2s_v3 | $35/mes | $7-10/mes | 75% |

**Configuración en Terraform**:
```hcl
resource "azurerm_kubernetes_cluster_node_pool" "spot" {
  priority        = "Spot"
  eviction_policy = "Delete"
  spot_max_price  = -1  # Pay up to regular price
  node_labels = {
    "kubernetes.azure.com/scalesetpriority" = "spot"
  }
}
```

**Ventajas**:
- ✅ Ahorro significativo de costos
- ✅ Bueno para dev/stage (no crítico)

**Desventajas**:
- ❌ Puede ser interrumpido en cualquier momento
- ❌ NO recomendado para producción

---

### 2. Auto-Shutdown de Recursos

**AKS Stop/Start**:
```bash
# Detener cluster (ahorra costo de VMs)
az aks stop --name ecommerce-aks-stage --resource-group ecommerce-rg

# Iniciar cluster
az aks start --name ecommerce-aks-stage --resource-group ecommerce-rg
```

**Ahorro**: ~$30/mes si detienes el cluster cuando no lo usas (noches, fines de semana)

**Automatización con Azure Automation**:
```
Lunes-Viernes: Start a las 8 AM, Stop a las 6 PM
Sábado-Domingo: Stopped todo el día
Ahorro: ~70% del tiempo = ~$21/mes ahorrados
```

---

### 3. Usar Namespaces Compartidos

**En lugar de clusters separados** para dev/stage/prod:

❌ **Opción Cara**: 3 clusters AKS
```
Dev cluster: 2 nodos x $15 = $30
Stage cluster: 2 nodos x $15 = $30
Prod cluster: 3 nodos x $35 = $105
Total: $165/mes
```

✅ **Opción Económica**: 1 cluster con 3 namespaces
```
1 cluster: 3 nodos x $15 = $45
- Namespace: ecommerce-dev
- Namespace: ecommerce-stage
- Namespace: ecommerce-prod
Total: $45/mes
```

**Ahorro**: $120/mes (73%)

---

### 4. Reservar Capacidad (Reservations)

**Azure Reservations**: Commit de 1 o 3 años con descuento.

| Compromiso | Descuento |
|------------|-----------|
| 1 año | 30-40% |
| 3 años | 60-65% |

**Ejemplo**:
- Standard_B2s regular: $15/mes
- 1 año reservation: $10/mes (ahorro $5/mes)

**NO recomendado para proyecto académico** (no sabes si lo usarás >1 año).

---

### 5. Rightsizing de Recursos

**Kubernetes Resource Limits optimizados**:

```yaml
# Configuración actual (sobredimensionada)
resources:
  requests:
    memory: "1Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "2000m"

# Configuración optimizada (para stage)
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

**Resultado**: Puedes correr más pods en los mismos nodos.

---

## Presupuesto Azure for Students

### Detalles de la Suscripción

```
Subscription ID: 729a7552-62e6-428f-8621-7000f7d187d3
Tenant ID: e994072b-523e-4bfe-86e2-442c5e10b244
Crédito Total: $100 USD
Duración: 12 meses (renovable con email .edu)
```

### Consumo Proyectado

**Opción 1: Solo Terraform Backend (RECOMENDADO)**
```
Azure Storage Account: $0.10/mes
Duración del crédito: ~1000 meses (prácticamente infinito)
```

**Opción 2: AKS Mínimo para Demos**
```
Costo: $45/mes
Duración del crédito: 2.2 meses
Estrategia: Detener cluster cuando no se usa
```

**Opción 3: AKS Recomendado**
```
Costo: $135/mes
Duración del crédito: 0.74 meses (22 días)
❌ NO viable
```

### Estrategia de Uso Recomendada

**Semana 1-3** (Noviembre 4-24):
- ✅ Desarrollo completo en Minikube local ($0)
- ✅ Terraform state en Azure Storage ($0.10/mes)
- ✅ Testing local de toda la infraestructura

**Semana 4** (Noviembre 25):
- 🚀 Deploy a Azure AKS 2 días antes de presentación
- 📸 Tomar screenshots y grabar video
- 🎯 Presentación final
- 🗑️ Destruir cluster después de presentación

**Costo total**: ~$3-5 USD (solo 3-5 días de uso de AKS)

**Crédito restante**: ~$95 USD (para futuros proyectos)

---

## Comparativa: Azure vs AWS vs GCP

### Para 2 nodos (2 vCPU, 4GB RAM cada uno)

| Proveedor | Tipo de Instancia | Precio/mes | Notas |
|-----------|-------------------|------------|-------|
| **Azure** | Standard_B2s x2 | $30 | ✅ Tenemos crédito de $100 |
| **AWS** | t3.medium x2 | $30 | Free Tier solo 12 meses |
| **GCP** | e2-medium x2 | $25 | $300 crédito inicial |

**Conclusión**: Precios similares, Azure es la mejor opción porque ya tenemos la suscripción configurada.

---

## Comparativa: Cloud vs Local

| Aspecto | Minikube Local | Azure AKS |
|---------|----------------|-----------|
| **Costo** | $0 | $45/mes |
| **Acceso** | Solo local | Internet |
| **Escalabilidad** | Limitado (8GB RAM) | Hasta 100 nodos |
| **Alta disponibilidad** | No | Sí (multi-zona) |
| **Velocidad de deploy** | Rápido | Medio |
| **Realismo** | 70% | 100% |
| **Adecuado para proyecto** | ✅ SÍ | ✅ Solo para demo |

---

## Recomendaciones para el Proyecto

### ✅ Estrategia Recomendada: Híbrida

**Fase 1: Desarrollo (3 semanas)**
- Usar **Minikube local** exclusivamente
- Terraform state en **Azure Storage Account** ($0.10/mes)
- Desarrollar, testear y documentar todo localmente
- **Costo**: $0.10/mes

**Fase 2: Demo Final (3-5 días)**
- Deploy a **Azure AKS** (2 nodos Standard_B2s)
- Grabar video demostrativo
- Tomar screenshots de monitoreo en cloud real
- Presentación final
- **Costo**: $3-5 USD (solo 3-5 días)

**Fase 3: Post-Presentación**
- **Destruir cluster AKS inmediatamente**
- Conservar Storage Account para el estado de Terraform
- **Costo**: $0.10/mes

**Costo total del proyecto**: **~$5 USD** ✅

**Crédito Azure restante**: **~$95 USD** para futuros proyectos ✅

---

### ❌ Estrategias NO Recomendadas

**Opción 1: Cluster AKS permanente**
- Costo: $45/mes x 2 meses = $90
- Consume casi todo el crédito
- No necesario para desarrollo

**Opción 2: Configuración de producción completa**
- Costo: $135/mes
- Excede presupuesto en 3 semanas
- Overkill para proyecto académico

**Opción 3: Multi-cloud**
- Complejidad adicional
- Costos duplicados
- No aporta valor al proyecto

---

## Monitoreo de Costos

### Azure Cost Management

**Dashboard de costos**:
```
Portal Azure → Cost Management + Billing → Cost Analysis
```

**Configurar alertas**:
```
1. Ve a: Budgets
2. Create Budget
3. Nombre: "Azure for Students - Alert"
4. Amount: $50
5. Alert at: 80% ($40)
6. Email: tu-email@correo.univalle.edu.co
```

**CLI para ver costos actuales**:
```bash
# Ver costo acumulado del mes actual
az consumption usage list --subscription 729a7552-62e6-428f-8621-7000f7d187d3 \
  --start-date 2025-11-01 --end-date 2025-11-30 \
  --query "[].{Service:name.value, Cost:pretaxCost}" \
  --output table
```

---

### Terraform Cost Estimation

**Infracost**: Herramienta para estimar costos de Terraform antes de apply.

**Instalación**:
```bash
# Windows (PowerShell)
choco install infracost

# WSL/Linux
curl -fsSL https://raw.githubusercontent.com/infracost/infracost/master/scripts/install.sh | sh
```

**Uso**:
```bash
cd terraform
infracost breakdown --path . --terraform-var-file=environments/stage/terraform.tfvars

# Output:
# ┌──────────────────────────────────────────────────────────────────┐
# │ Project: terraform                                                │
# ├──────────────────────────────────────────────────────────────────┤
# │ Name                                    Monthly Qty  Unit  Cost   │
# ├──────────────────────────────────────────────────────────────────┤
# │ azurerm_kubernetes_cluster.aks                                    │
# │ ├─ Control plane                                       0  -       │
# │ └─ Nodes (Standard_B2s x 2)                           2  $15  $30 │
# │ azurerm_lb.main                                        1  $5   $5  │
# │ azurerm_managed_disk.prometheus                        1  $2.5 $2.5│
# │                                                                    │
# │ TOTAL                                                        $37.50│
# └──────────────────────────────────────────────────────────────────┘
```

---

## Cálculo de ROI (Return on Investment)

### Inversión

| Concepto | Costo |
|----------|-------|
| Tiempo de desarrollo (80 horas x $0) | $0 |
| Infraestructura Azure | $5 |
| Herramientas (GitHub, Docker Hub Free) | $0 |
| **Total Invertido** | **$5** |

### Aprendizaje Obtenido

| Habilidad | Valor de Mercado (Colombia) |
|-----------|---------------------------|
| Kubernetes | $3,500,000-6,000,000 COP/mes |
| Terraform | $4,000,000-7,000,000 COP/mes |
| CI/CD con GitHub Actions | $3,000,000-5,000,000 COP/mes |
| Observabilidad (Prometheus, Grafana) | $3,500,000-6,000,000 COP/mes |
| Arquitectura de Microservicios | $4,500,000-8,000,000 COP/mes |

**Salario promedio DevOps Jr** con estas habilidades: **$4,000,000-6,000,000 COP/mes**

**ROI del proyecto**: **Invaluable** 🚀

---

## Preguntas Frecuentes

### ¿Puedo usar el Free Tier de Azure?

Azure Free Tier incluye:
- ✅ 750 horas/mes de B1s VM (1 vCPU, 1GB RAM) por 12 meses
- ❌ NO incluye suficiente para AKS (necesitas al menos 2 nodos con 4GB RAM cada uno)

**Conclusión**: Azure for Students ($100 crédito) es mejor para este proyecto.

---

### ¿Qué pasa si me quedo sin crédito?

**Opciones**:
1. ✅ Usar solo Minikube local (gratis)
2. ✅ Solicitar nuevo crédito con otro email .edu
3. ✅ Usar GCP Free Trial ($300 crédito por 90 días)
4. ❌ Pagar con tarjeta de crédito (no recomendado)

---

### ¿Puedo usar Terraform con Minikube?

✅ **SÍ**, Terraform tiene un provider para Kubernetes que funciona con Minikube:

```hcl
provider "kubernetes" {
  config_path = "~/.kube/config"
  config_context = "minikube"
}
```

**Ventaja**: Practicas IaC sin gastar dinero.

---

## Referencias

- [Azure Pricing Calculator](https://azure.microsoft.com/en-us/pricing/calculator/)
- [Azure for Students](https://azure.microsoft.com/en-us/free/students/)
- [Infracost Documentation](https://www.infracost.io/docs/)
- [FinOps Foundation](https://www.finops.org/)

---

**Última actualización**: 24 Noviembre 2025
**Autores**: Santiago & David
**Proyecto**: Ingeniería de Software V - Análisis de Costos
