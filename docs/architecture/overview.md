# Arquitectura del Sistema - Ecommerce Microservices

## Tabla de Contenidos
- [Resumen Ejecutivo](#resumen-ejecutivo)
- [Arquitectura de Alto Nivel](#arquitectura-de-alto-nivel)
- [Microservicios](#microservicios)
- [Infraestructura](#infraestructura)
- [Flujos de Comunicación](#flujos-de-comunicación)
- [Stack Tecnológico](#stack-tecnológico)
- [Observabilidad](#observabilidad)
- [Seguridad](#seguridad)

---

## Resumen Ejecutivo

Sistema de comercio electrónico implementado con arquitectura de microservicios siguiendo principios cloud-native, twelve-factor app y event-driven architecture.

**Características principales**:
- ✅ 10 microservicios independientes
- ✅ Kubernetes-native deployment
- ✅ Observabilidad completa (Prometheus, Grafana, ELK, Zipkin)
- ✅ CI/CD automatizado con GitHub Actions
- ✅ Infraestructura como código con Terraform
- ✅ Alta disponibilidad y resiliencia

---

## Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────────────┐
│                            CLIENTES                                  │
│                   (Web, Mobile, Third-party APIs)                   │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       API GATEWAY (8080)                             │
│                   Spring Cloud Gateway                               │
│           [Rate Limiting, Routing, Load Balancing]                  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     PROXY CLIENT (8900)                              │
│              [JWT Auth, Authorization, Unified API]                 │
│                   Context Path: /app/**                             │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
         ▼                     ▼                     ▼
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│  USER SERVICE   │  │ PRODUCT SERVICE  │  │  ORDER SERVICE  │
│    (8700)       │  │     (8600)       │  │     (8300)      │
│  [Users,        │  │  [Products,      │  │  [Carts,        │
│   Credentials]  │  │   Categories]    │  │   Orders]       │
└─────────────────┘  └──────────────────┘  └─────────────────┘
         │                     │                     │
         ▼                     ▼                     ▼
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ PAYMENT SERVICE │  │ SHIPPING SERVICE │  │FAVOURITE SERVICE│
│    (8400)       │  │     (8500)       │  │     (8200)      │
│  [Payments]     │  │  [Shipments]     │  │  [Favourites]   │
└─────────────────┘  └──────────────────┘  └─────────────────┘
         │                     │                     │
         └─────────────────────┴─────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE SERVICES                           │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │ SERVICE REGISTRY │  │  CONFIG SERVER   │  │  ZIPKIN TRACING │  │
│  │   (Eureka 8761)  │  │  (Cloud Config)  │  │     (9411)      │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        OBSERVABILITY STACK                           │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │   PROMETHEUS     │  │     GRAFANA      │  │   ELK STACK     │  │
│  │   (Metrics)      │  │   (Dashboards)   │  │ (Logs/Search)   │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Microservicios

### Capa de Infraestructura

#### 1. Service Discovery (Eureka)
- **Puerto**: 8761
- **Propósito**: Registro y descubrimiento de servicios
- **Tecnología**: Netflix Eureka Server
- **Dashboard**: http://localhost:8761
- **Código**: [service-discovery/](../../service-discovery/)

#### 2. Cloud Config Server
- **Puerto**: 9296
- **Propósito**: Configuración centralizada
- **Tecnología**: Spring Cloud Config
- **Endpoints**:
  - `GET /{service}/{profile}` - Obtener configuración
  - `POST /actuator/refresh` - Refrescar configuración
- **Código**: [cloud-config/](../../cloud-config/)

#### 3. API Gateway
- **Puerto**: 8080
- **Propósito**: Punto de entrada unificado
- **Tecnología**: Spring Cloud Gateway
- **Funcionalidades**:
  - Enrutamiento dinámico
  - Rate limiting
  - Balanceo de carga
- **Código**: [api-gateway/](../../api-gateway/)

---

### Capa de Aplicación

#### 4. Proxy Client
- **Puerto**: 8900
- **Context Path**: `/app`
- **Propósito**: Autenticación, autorización y orquestación
- **Tecnología**: Spring Boot + JWT + Feign Clients
- **Endpoints principales**:
  - `POST /app/authenticate` - Login y generación de JWT
  - `GET /app/users/**` - Delega a user-service
  - `GET /app/products/**` - Delega a product-service
  - `POST /app/orders/**` - Delega a order-service
- **Seguridad**: JWT-based authentication
- **Resiliencia**: Circuit Breaker con Resilience4j
- **Código**: [proxy-client/](../../proxy-client/)

---

### Servicios de Negocio

#### 5. User Service
- **Puerto**: 8700
- **Base de datos**: H2 (dev) / MySQL `userdb` (prod)
- **Propósito**: Gestión de usuarios y credenciales
- **Endpoints**:
  - `GET /api/users` - Listar usuarios
  - `POST /api/users` - Crear usuario
  - `GET /api/users/{userId}` - Obtener usuario
  - `PUT /api/users/{userId}` - Actualizar usuario
  - `DELETE /api/users/{userId}` - Eliminar usuario
  - `GET /api/credentials/{username}` - Obtener credenciales para autenticación
- **Entidades**: `User`, `Credential`, `Address`
- **Migraciones Flyway**: 12 migraciones (V1-V12)
- **Código**: [user-service/](../../user-service/)

#### 6. Product Service
- **Puerto**: 8600
- **Base de datos**: H2 (dev) / MySQL `productdb` (prod)
- **Propósito**: Gestión de productos y categorías
- **Endpoints**:
  - `GET /api/products` - Listar productos
  - `POST /api/products` - Crear producto
  - `GET /api/categories` - Listar categorías
- **Entidades**: `Product`, `Category`
- **Código**: [product-service/](../../product-service/)

#### 7. Order Service
- **Puerto**: 8300
- **Base de datos**: H2 (dev) / MySQL `orderdb` (prod)
- **Propósito**: Gestión de carritos y órdenes
- **Endpoints**:
  - `GET /api/carts` - Listar carritos
  - `POST /api/carts` - Crear carrito
  - `POST /api/orders` - Crear orden desde carrito
- **Entidades**: `Cart`, `Order`, `OrderItem`
- **Dependencias**: Llama a `user-service` para enriquecer datos de usuario
- **Código**: [order-service/](../../order-service/)

#### 8. Payment Service
- **Puerto**: 8400
- **Base de datos**: H2 (dev) / MySQL `paymentdb` (prod)
- **Propósito**: Procesamiento de pagos
- **Endpoints**:
  - `GET /api/payments` - Listar pagos
  - `POST /api/payments` - Procesar pago
- **Entidades**: `Payment`
- **Dependencias**: Llama a `order-service` para obtener detalles de orden
- **Código**: [payment-service/](../../payment-service/)

#### 9. Shipping Service
- **Puerto**: 8500
- **Base de datos**: H2 (dev) / MySQL `shippingdb` (prod)
- **Propósito**: Gestión de envíos
- **Endpoints**:
  - `GET /api/shipments` - Listar envíos
  - `POST /api/shipments` - Crear envío
- **Entidades**: `Shipment`
- **Dependencias**: Llama a `product-service` y `order-service`
- **Código**: [shipping-service/](../../shipping-service/)

#### 10. Favourite Service
- **Puerto**: 8200
- **Base de datos**: H2 (dev) / MySQL `favouritedb` (prod)
- **Propósito**: Gestión de productos favoritos de usuarios
- **Endpoints**:
  - `GET /api/favourites` - Listar favoritos
  - `POST /api/favourites` - Añadir favorito
- **Entidades**: `Favourite`
- **Código**: [favourite-service/](../../favourite-service/)

---

## Infraestructura

### Kubernetes

**Plataforma de orquestación**: Kubernetes (Minikube local, AKS para producción)

**Namespaces**:
- `default` - Microservicios de negocio
- `monitoring` - Prometheus, Grafana, Alertmanager
- `logging` - Elasticsearch, Kibana, Filebeat
- `kube-system` - Componentes de Kubernetes

**Recursos por servicio**:
- Deployment (replicas configurables)
- Service (ClusterIP o NodePort)
- ConfigMap (configuraciones)
- Secret (credenciales)

**Health Checks**:
- Liveness Probe: `/actuator/health/liveness`
- Readiness Probe: `/actuator/health/readiness`

**Manifiestos**: [k8s/](../../k8s/)

---

### Terraform (IaC)

**Módulos**:

1. **kubernetes/** - Deployments de microservicios
2. **monitoring/** - Prometheus, Grafana, ELK, Jaeger
3. **security/** - RBAC, Network Policies, Secrets

**Ambientes**:
- `dev` - Desarrollo (Minikube)
- `stage` - Staging (Azure AKS)
- `prod` - Producción (Azure AKS)

**Backend**: Azure Storage Account para estado de Terraform

**Archivos**: [terraform/](../../terraform/)

---

## Flujos de Comunicación

### 1. Flujo de Autenticación

```
Cliente → API Gateway → Proxy Client → User Service
                            │
                            ▼
                       JWT generado
                            │
                            ▼
                     Devuelto al cliente
```

**Secuencia detallada**:
1. Cliente envía `POST /app/authenticate` con `{username, password}`
2. Proxy Client recibe request
3. Proxy Client llama a `user-service` vía Feign: `GET /api/credentials/{username}`
4. User Service busca credenciales en BD
5. Proxy Client valida password
6. Si válido, genera JWT con roles de usuario
7. Devuelve JWT al cliente
8. Cliente incluye JWT en header `Authorization: Bearer <token>` en requests subsiguientes

**Código**: [proxy-client/src/main/java/com/selimhorri/app/security/](../../proxy-client/src/main/java/com/selimhorri/app/security/)

---

### 2. Flujo de Creación de Orden

```
Cliente → API Gateway → Proxy Client → Order Service
                                            │
                                            ├─→ User Service (enriquecer datos)
                                            │
                                            ▼
                                       Orden creada
                                            │
                                            ▼
                      Payment Service ← Order Service
                                            │
                                            ▼
                      Shipping Service ← Order Service
```

**Secuencia detallada**:
1. Cliente autenticado envía `POST /app/orders` con carrito ID
2. Proxy Client delega a Order Service
3. Order Service:
   - Busca carrito en BD
   - Llama a User Service para obtener datos completos de usuario
   - Crea entidad Order con OrderItems
   - Guarda en BD
4. Payment Service procesa pago de la orden
5. Shipping Service crea envío basado en orden

---

### 3. Flujo de Service Discovery

```
Microservicio inicia
       │
       ▼
Registra en Eureka
       │
       ▼
Heartbeats cada 30s
       │
       ├─→ Si no hay heartbeat: Eureka marca como DOWN
       │
       ▼
Otros servicios consultan Eureka para descubrir instancias
       │
       ▼
Feign Client usa nombre lógico (ej: "USER-SERVICE")
       │
       ▼
Spring Cloud LoadBalancer selecciona instancia
```

---

## Stack Tecnológico

### Backend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 11 | Lenguaje principal |
| Spring Boot | 2.5.7 | Framework de microservicios |
| Spring Cloud | 2020.0.4 | Componentes cloud-native |
| Spring Data JPA | 2.5.7 | Persistencia |
| Hibernate | 5.4.x | ORM |
| Flyway | 7.x | Migraciones de BD |
| Resilience4j | 1.7.1 | Circuit Breaker |
| Feign | 3.0.4 | HTTP Clients |
| JWT (jjwt) | 0.11.2 | Autenticación |
| Lombok | 1.18.22 | Reducir boilerplate |
| MapStruct | 1.4.2 | Mapeo de DTOs |

### Base de Datos

| Ambiente | Tecnología | Versión |
|----------|------------|---------|
| Desarrollo | H2 | In-memory |
| Stage/Prod | MySQL | 8.0 |

### Observabilidad

| Herramienta | Propósito | Puerto |
|-------------|-----------|--------|
| Prometheus | Métricas y alertas | 9090 |
| Grafana | Visualización de dashboards | 3000 |
| Elasticsearch | Almacenamiento de logs | 9200 |
| Kibana | Visualización de logs | 5601 |
| Filebeat | Recolección de logs | - |
| Zipkin | Distributed tracing | 9411 |
| Spring Boot Actuator | Health checks y métricas | /actuator |

### DevOps

| Herramienta | Propósito |
|-------------|-----------|
| Docker | Contenedorización |
| Kubernetes | Orquestación |
| Helm | Package manager para K8s |
| Terraform | Infraestructura como código |
| GitHub Actions | CI/CD |
| SonarQube | Análisis de calidad de código |
| Trivy | Escaneo de vulnerabilidades |
| OWASP ZAP | Security testing |
| Maven | Build automation |

---

## Observabilidad

### Métricas (Prometheus)

**Métricas expuestas** por cada servicio en `/actuator/prometheus`:

**Métricas de negocio (custom)**:
- `users_created_total` - Total de usuarios creados
- `users_login_success_total` - Logins exitosos
- `users_login_failed_total` - Logins fallidos
- `orders_created_total` - Órdenes creadas
- `orders_completed_total` - Órdenes completadas
- `payments_success_total` - Pagos exitosos
- `payments_failed_total` - Pagos fallidos

**Métricas técnicas** (Spring Boot Actuator):
- `http_server_requests_seconds_*` - Latencia de requests HTTP
- `jvm_memory_*` - Uso de memoria JVM
- `jvm_threads_*` - Threads activos
- `resilience4j_circuitbreaker_*` - Estado de circuit breakers

**Alertas configuradas** (8 alertas críticas):
- PodDown
- HighCPUUsage (>80%)
- HighMemoryUsage (>85%)
- ContainerRestarting
- ServiceUnavailable
- HighHTTPErrorRate (>5% errores 5xx)
- DiskSpaceLow (<15%)
- CircuitBreakerOpen

**Archivo**: [k8s/prometheus-alerts.yaml](../../k8s/prometheus-alerts.yaml)

---

### Logs (ELK Stack)

**Pipeline**:
1. **Filebeat** recolecta logs de todos los contenedores en `/var/log/containers/*.log`
2. **Elasticsearch** indexa y almacena logs
3. **Kibana** permite búsqueda y visualización

**Acceso**: http://localhost:5601 (elastic / wdb6mQPlDC3uLytd)

**Queries útiles**:
- Buscar errores: `log.level: ERROR`
- Buscar por servicio: `kubernetes.labels.app: "user-service"`
- Buscar por trace ID: `traceId: "a1b2c3d4"`

---

### Tracing (Zipkin)

**Correlación de requests** distribuidas:

Cada request recibe un **Trace ID** que se propaga por todos los servicios.

**Ejemplo de trace**:
```
Trace ID: a1b2c3d4e5f6g7h8
├─ Span 1: proxy-client (100ms)
│  └─ Span 2: order-service (80ms)
│     ├─ Span 3: user-service (30ms)
│     └─ Span 4: product-service (25ms)
```

**Acceso**: http://localhost:9411/zipkin/

---

### Dashboards (Grafana)

**Dashboards disponibles**:
1. **Kubernetes Cluster Monitoring** - CPU, memoria, pods por namespace
2. **Microservices Overview** - Latencia, error rate, throughput
3. **JVM Metrics** - Heap usage, GC activity, threads
4. **Business Metrics** - Usuarios creados, órdenes, pagos

**Acceso**: http://localhost:3000 (admin / <password>)

---

## Seguridad

### Autenticación y Autorización

**Mecanismo**: JWT (JSON Web Tokens)

**Flujo**:
1. Usuario se autentica con username/password
2. Proxy Client valida credenciales contra User Service
3. Si válido, genera JWT con roles: `ROLE_USER` o `ROLE_ADMIN`
4. JWT tiene expiración de 24 horas
5. Cliente incluye JWT en header `Authorization: Bearer <token>`
6. Proxy Client valida JWT en cada request
7. Spring Security permite/niega acceso basado en roles

**Configuración**: [proxy-client/src/main/java/com/selimhorri/app/security/SecurityConfig.java](../../proxy-client/src/main/java/com/selimhorri/app/security/SecurityConfig.java)

---

### Roles y Permisos

| Rol | Permisos |
|-----|----------|
| `ROLE_USER` | Leer productos, crear órdenes, ver su perfil |
| `ROLE_ADMIN` | CRUD completo de usuarios, productos, órdenes |

**Ejemplo de endpoint protegido**:
```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{userId}")
public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
    // ...
}
```

---

### Network Policies (Kubernetes)

**Principio de mínimo privilegio**: Cada servicio solo puede comunicarse con servicios necesarios.

**Ejemplo**: Order Service solo puede llamar a User Service, Product Service y Payment Service.

**Configuración**: [terraform/modules/security/network-policies.tf](../../terraform/modules/security/)

---

### Secrets Management

**Desarrollo**: Secrets en ConfigMaps (NO recomendado para prod)

**Producción**:
- Kubernetes Secrets (base64 encoded)
- Azure Key Vault (recomendado para stage/prod)

**Ejemplo de secret**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysql-credentials
type: Opaque
data:
  username: cm9vdA==  # base64("root")
  password: cm9vdA==  # base64("root")
```

---

### Escaneo de Vulnerabilidades

**Herramientas**:
- **SonarQube**: Análisis estático de código (OWASP Top 10)
- **Trivy**: Escaneo de imágenes Docker
- **OWASP ZAP**: Pentesting automatizado

**CI/CD**: Escaneo automático en cada push a dev/stage/prod

---

## Escalabilidad

### Horizontal Scaling

**Kubernetes HPA** (Horizontal Pod Autoscaler):

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: user-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: user-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

**Escala automática** cuando CPU >70%

---

### Vertical Scaling

**Resource Limits** en Kubernetes:

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

---

## Resiliencia

### Circuit Breaker

**Configuración** en Proxy Client para todas las llamadas Feign.

**Estados**:
- CLOSED: Todo funciona, requests pasan
- OPEN: Servicio caído, requests fallan inmediatamente
- HALF_OPEN: Probando si servicio se recuperó

**Beneficios**:
- Evita cascadas de fallos
- Respuestas rápidas ante errores
- Recuperación automática

---

### Retry Pattern

**Configuración**:
```yaml
resilience4j:
  retry:
    instances:
      userService:
        maxAttempts: 3
        waitDuration: 500ms
```

---

### Timeouts

**Feign Client Timeouts**:
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
```

---

## Despliegue

### Ambientes

| Ambiente | Infraestructura | Base de Datos | Propósito |
|----------|----------------|---------------|-----------|
| **dev** | Minikube local | H2 in-memory | Desarrollo local |
| **stage** | Azure AKS | MySQL en Azure | Pruebas pre-producción |
| **prod** | Azure AKS | MySQL en Azure (HA) | Producción |

### CI/CD Pipeline

**GitHub Actions** ejecuta:

1. **Build**: `mvn clean package`
2. **Test**: `mvn test` (unit + integration tests)
3. **Code Quality**: SonarQube analysis
4. **Security Scan**: Trivy image scan
5. **Docker Build**: Construir imágenes
6. **Docker Push**: Subir a Docker Hub
7. **Deploy**: kubectl apply (stage/prod)

**Workflows**: [.github/workflows/](../../.github/workflows/)

---

## Monitoreo de Costos

### Estimación de Costos (Azure AKS)

| Recurso | Tipo | Costo Mensual (USD) |
|---------|------|---------------------|
| AKS Cluster (2 nodes) | Standard_B2s | $30 |
| Azure Load Balancer | Basic | $5 |
| Managed Disks (100GB) | Standard SSD | $5 |
| **Total Estimado** | - | **$40/mes** |

**Estrategia de ahorro**:
- Desarrollo en Minikube (gratis)
- Deploy a Azure solo para demos finales
- Destruir recursos después de presentación

---

## Próximos Pasos

### Mejoras Recomendadas

1. **Service Mesh** (Istio/Linkerd)
   - mTLS automático entre servicios
   - Traffic management avanzado
   - Observabilidad mejorada

2. **GitOps con ArgoCD**
   - Despliegue declarativo
   - Sincronización automática
   - Rollbacks sencillos

3. **Event-Driven Architecture**
   - Apache Kafka para eventos
   - CQRS pattern
   - Event Sourcing

4. **API Versioning**
   - Versionado de endpoints
   - Backwards compatibility

5. **GraphQL Gateway**
   - Unified data fetching
   - Reduce over-fetching

---

## Referencias

- [Diagrama de arquitectura](../../app-architecture.drawio.png)
- [Diagrama ER de base de datos](../../ecommerce-ERD.drawio.png)
- [README principal](../../README.md)
- [Patrones de diseño implementados](../design-patterns/existing-patterns.md)

---

**Última actualización**: 24 Noviembre 2025
**Autores**: Santiago & David - Universidad Icesi
**Proyecto**: Ingeniería de Software V - Proyecto Final
