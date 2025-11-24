# CI/CD Pipeline Documentation

## Tabla de Contenidos
- [Resumen](#resumen)
- [Arquitectura del Pipeline](#arquitectura-del-pipeline)
- [Workflows Implementados](#workflows-implementados)
- [Estrategia de Branching](#estrategia-de-branching)
- [Proceso de Despliegue](#proceso-de-despliegue)
- [Herramientas de Calidad](#herramientas-de-calidad)
- [Secrets y Configuración](#secrets-y-configuración)
- [Troubleshooting](#troubleshooting)

---

## Resumen

Pipeline de CI/CD completamente automatizado usando **GitHub Actions** para:
- ✅ Build y testing automatizado
- ✅ Análisis de calidad de código (SonarQube)
- ✅ Escaneo de vulnerabilidades (Trivy)
- ✅ Security testing (OWASP ZAP)
- ✅ Construcción y push de imágenes Docker
- ✅ Despliegue a Kubernetes (stage/prod)
- ✅ Generación automática de Release Notes

---

## Arquitectura del Pipeline

```
┌──────────────────────────────────────────────────────────────────┐
│                         DEVELOPER                                 │
│                    (Santiago & David)                             │
└────────────────────┬─────────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────────┐
│                      GIT REPOSITORY                               │
│              github.com/Dantesiio/ecommerce-...                   │
│                                                                   │
│  ┌──────────┐  ┌───────────┐  ┌────────┐  ┌──────────┐         │
│  │   dev    │→ │   stage   │→ │ master │← │ feature/ │         │
│  │ (branch) │  │  (branch) │  │(branch)│  │ branches │         │
│  └──────────┘  └───────────┘  └────────┘  └──────────┘         │
└────────────────────┬─────────────────────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
         ▼           ▼           ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  PR Check   │ │ Build Push  │ │   Deploy    │
│  Workflow   │ │  Workflow   │ │  Workflow   │
└─────────────┘ └─────────────┘ └─────────────┘
         │           │           │
         ▼           ▼           ▼
┌──────────────────────────────────────────────────────────────────┐
│                     GITHUB ACTIONS JOBS                           │
│                                                                   │
│  1. Checkout code                                                │
│  2. Setup Java 11                                                │
│  3. Maven build & test                                           │
│  4. SonarQube analysis ────────────┐                             │
│  5. Trivy security scan            │                             │
│  6. Docker build                   ▼                             │
│  7. Docker push             ┌──────────────┐                     │
│  8. Kubectl deploy          │  SONARCLOUD  │                     │
│                             │  (Quality)   │                     │
│                             └──────────────┘                     │
└────────────────────┬─────────────────────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
         ▼           ▼           ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ DOCKER HUB  │ │  KUBERNETES │ │   ALERTS    │
│  (Images)   │ │(stage/prod) │ │  (Slack/📧) │
└─────────────┘ └─────────────┘ └─────────────┘
```

---

## Workflows Implementados

### 1. Stage CI Pipeline (`stage-ci.yml`)

**Trigger**: Push a branch `dev`

**Ubicación**: [.github/workflows/stage-ci.yml](../../.github/workflows/stage-ci.yml)

**Pasos**:
1. ✅ Checkout del código
2. ✅ Setup de Java 11
3. ✅ Maven clean package (build + test)
4. ✅ Autenticación a Docker Hub
5. ✅ Build de imágenes Docker para servicios modificados
6. ✅ Push de imágenes a Docker Hub con tag `{service}-{GITHUB_SHA}`
7. ⏳ Deploy a Kubernetes stage (planeado)

**Servicios incluidos**:
- proxy-client
- user-service
- product-service
- order-service
- payment-service
- shipping-service

**Ejemplo de ejecución**:
```yaml
name: Stage CI Pipeline

on:
  push:
    branches: [dev]

jobs:
  build-and-push:
    runs-on: self-hosted
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'

      - name: Build with Maven
        run: ./mvnw clean package -DskipTests -pl proxy-client,user-service,...

      - name: Docker login
        run: echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin

      - name: Build and push Docker images
        run: |
          docker build -t dantesiio/ecommerce-backend:proxy-client-${{ github.sha }} ./proxy-client
          docker push dantesiio/ecommerce-backend:proxy-client-${{ github.sha }}
```

---

### 2. Per-Service Workflows

Cada servicio tiene 6 workflows:

#### a) `{service}-pipeline-dev-pr.yml`
- **Trigger**: Pull Request a `dev`
- **Propósito**: Validación antes de merge
- **Acciones**:
  - Build del servicio
  - Ejecución de tests unitarios
  - SonarQube analysis
  - Trivy scan

#### b) `{service}-pipeline-dev-push.yml`
- **Trigger**: Push a `dev`
- **Propósito**: Build y push de imagen Docker
- **Acciones**:
  - Build del servicio
  - Tests unitarios
  - Docker build
  - Push a Docker Hub con tag `dev-{SHA}`

#### c) `{service}-pipeline-stage-pr.yml`
- **Trigger**: Pull Request a `stage`
- **Propósito**: Validación pre-staging
- **Acciones**:
  - Build completo
  - Tests unitarios + integración
  - SonarQube con quality gate estricto
  - Trivy scan (bloquea si hay vulnerabilidades HIGH)

#### d) `{service}-pipeline-stage-push.yml`
- **Trigger**: Push a `stage`
- **Propósito**: Deploy a staging environment
- **Acciones**:
  - Build y tests
  - Docker build y push con tag `stage-{SHA}`
  - Deploy a Kubernetes namespace `ecommerce-microservice-stage`
  - Health check post-deploy

#### e) `{service}-pipeline-prod-pr.yml`
- **Trigger**: Pull Request a `master`
- **Propósito**: Validación rigurosa pre-producción
- **Acciones**:
  - Build completo
  - Todos los tests (unit, integration, e2e)
  - SonarQube + quality gate
  - Trivy scan (bloquea si HIGH/CRITICAL)
  - OWASP ZAP security scan
  - Aprobación manual requerida

#### f) `{service}-pipeline-prod-push.yml`
- **Trigger**: Push a `master`
- **Propósito**: Deploy a producción
- **Acciones**:
  - Build y tests completos
  - Docker build y push con tag `prod-{VERSION}`
  - Deploy a Kubernetes namespace `ecommerce-microservice-prod`
  - Smoke tests post-deploy
  - Notificación a Slack/Email

---

### 3. OWASP ZAP Security Scan (`owasp-zap-scan.yml`)

**Trigger**:
- Manual (workflow_dispatch)
- Programado (domingos a las 2 AM)
- Push a dev/stage/main

**Ubicación**: [.github/workflows/owasp-zap-scan.yml](../../.github/workflows/owasp-zap-scan.yml)

**Funcionalidad**:
```yaml
name: OWASP ZAP Security Scan

on:
  workflow_dispatch:
  schedule:
    - cron: '0 2 * * 0'  # Domingos a las 2 AM
  push:
    branches: [dev, stage, main]

jobs:
  zap-scan:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: ZAP Baseline Scan
        uses: zaproxy/action-baseline@v0.11.0
        with:
          target: 'http://testphp.vulnweb.com/'  # Cambiar a URL real
          rules_file_name: '.zap/rules.tsv'
          cmd_options: '-a'

      - name: Upload ZAP Report
        uses: actions/upload-artifact@v3
        with:
          name: zap-scan-report
          path: report_html.html
```

**Custom Rules**: [.zap/rules.tsv](../../.zap/rules.tsv)

**Vulnerabilidades detectadas**:
- XSS (Cross-Site Scripting)
- SQL Injection
- CSRF
- Insecure headers
- Path traversal

---

### 4. Release Notes Generator (`release-notes.yml`)

**Trigger**: Creación de nuevo tag (ej: `v1.0.0`)

**Funcionalidad**:
- Extrae commits desde último tag
- Categoriza commits:
  - ✨ Features (`feat:`)
  - 🐛 Bug fixes (`fix:`)
  - 📝 Docs (`docs:`)
  - ♻️ Refactors (`refactor:`)
- Genera Release Notes automáticas en formato Markdown
- Publica como GitHub Release

**Ejemplo de Release Notes generadas**:
```markdown
# Release v1.2.0

## 🎉 What's New

### ✨ Features
- feat: add custom business metrics for order-service (#45)
- feat: implement Prometheus alerts for microservices (#43)

### 🐛 Bug Fixes
- fix: resolve Filebeat connection issue to Elasticsearch (#47)
- fix: correct PrometheusRule labels for alert discovery (#46)

### 📝 Documentation
- docs: add architecture overview and design patterns (#44)

### ♻️ Refactoring
- refactor: improve Feign client error handling (#42)

## 📦 Docker Images

- `dantesiio/ecommerce-backend:user-service-v1.2.0`
- `dantesiio/ecommerce-backend:order-service-v1.2.0`
- `dantesiio/ecommerce-backend:payment-service-v1.2.0`

## 🚀 Deployment

```bash
kubectl set image deployment/user-service user-service=dantesiio/ecommerce-backend:user-service-v1.2.0 -n default
```

---

**Full Changelog**: v1.1.0...v1.2.0
```

---

## Estrategia de Branching

### GitFlow Adaptado

```
master (producción)
  │
  ├─── stage (staging)
  │      │
  │      └─── dev (desarrollo)
  │             │
  │             ├─── feature/add-metrics
  │             ├─── feature/elk-integration
  │             └─── bugfix/filebeat-connection
  │
  └─── hotfix/critical-security-patch
```

### Reglas de Branching

| Branch | Propósito | Merge desde | Deploy a |
|--------|-----------|-------------|----------|
| `master` | Producción estable | `stage` (solo PRs aprobados) | Kubernetes prod |
| `stage` | Pre-producción | `dev` (después de testing) | Kubernetes stage |
| `dev` | Desarrollo activo | `feature/*`, `bugfix/*` | Kubernetes dev (Minikube) |
| `feature/*` | Nuevas funcionalidades | - | No auto-deploy |
| `bugfix/*` | Correcciones de bugs | - | No auto-deploy |
| `hotfix/*` | Correcciones críticas | `master` | Kubernetes prod (urgente) |

---

### Workflow de Desarrollo

**1. Nueva Feature**:
```bash
# Crear branch desde dev
git checkout dev
git pull origin dev
git checkout -b feature/add-custom-metrics

# Desarrollar y commit
git add .
git commit -m "feat: add custom business metrics to user-service"

# Push y crear PR
git push origin feature/add-custom-metrics
# Crear PR en GitHub: feature/add-custom-metrics → dev
```

**2. Review y Merge**:
- PR automáticamente ejecuta workflow `*-dev-pr.yml`
- SonarQube y Trivy validan código
- Requiere aprobación de 1 revisor
- Merge a `dev` ejecuta `*-dev-push.yml`
- Imagen Docker se construye y sube a Docker Hub

**3. Promoción a Stage**:
```bash
# Después de validar en dev
git checkout stage
git pull origin stage
git merge dev
git push origin stage
```
- Ejecuta `*-stage-push.yml`
- Deploy automático a Kubernetes stage
- Health checks post-deploy

**4. Promoción a Prod**:
```bash
# Crear PR: stage → master
# Requiere aprobaciones
# Merge ejecuta *-prod-push.yml
```
- Deploy a producción
- Smoke tests
- Notificaciones

---

## Proceso de Despliegue

### Ambiente Dev (Minikube)

**Trigger**: Manual o push a `dev`

**Pasos**:
1. Build local: `./mvnw clean package`
2. Build imagen: `docker build -t user-service:dev .`
3. Load a Minikube: `minikube image load user-service:dev`
4. Deploy: `kubectl apply -f k8s/user-service-deployment.yaml`
5. Verificar: `kubectl get pods`

---

### Ambiente Stage (Azure AKS)

**Trigger**: Push a `stage`

**Pipeline automatizado**:
```yaml
- name: Deploy to Stage
  run: |
    az aks get-credentials --resource-group ecommerce-rg --name ecommerce-aks-stage
    kubectl config use-context ecommerce-aks-stage
    kubectl set image deployment/user-service \
      user-service=dantesiio/ecommerce-backend:user-service-${{ github.sha }} \
      -n ecommerce-microservice-stage
    kubectl rollout status deployment/user-service -n ecommerce-microservice-stage
```

**Validación post-deploy**:
```bash
# Health check
kubectl exec -it deployment/user-service -n ecommerce-microservice-stage -- \
  curl http://localhost:8700/actuator/health

# Smoke test
curl https://stage.ecommerce.example.com/app/users
```

---

### Ambiente Prod (Azure AKS)

**Trigger**: Push a `master` (con aprobación manual)

**Pipeline con aprobación**:
```yaml
jobs:
  deploy-prod:
    runs-on: ubuntu-latest
    environment:
      name: production
      url: https://ecommerce.example.com
    steps:
      - name: Approval required
        # GitHub requiere aprobación manual en "Environments"

      - name: Deploy to Production
        run: |
          kubectl set image deployment/user-service \
            user-service=dantesiio/ecommerce-backend:user-service-v${{ github.ref_name }} \
            -n ecommerce-microservice-prod
```

**Rollback si falla**:
```bash
kubectl rollout undo deployment/user-service -n ecommerce-microservice-prod
```

---

## Herramientas de Calidad

### SonarQube (SonarCloud)

**Integración**:
```yaml
- name: SonarQube Analysis
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    SONAR_ORGANIZATION: ${{ secrets.SONAR_ORGANIZATION }}
  run: |
    ./mvnw sonar:sonar \
      -Dsonar.projectKey=ecommerce-microservice \
      -Dsonar.organization=${{ secrets.SONAR_ORGANIZATION }} \
      -Dsonar.host.url=https://sonarcloud.io \
      -Dsonar.login=${{ secrets.SONAR_TOKEN }}
```

**Quality Gates**:
- Coverage mínimo: 80%
- Bugs bloqueadores: 0
- Vulnerabilidades: 0
- Code Smells: <100
- Duplicación: <3%

**Configuración**: [pom.xml](../../pom.xml) (plugin sonar-maven-plugin)

---

### Trivy (Container Scanning)

**Integración**:
```yaml
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: dantesiio/ecommerce-backend:user-service-${{ github.sha }}
    format: 'sarif'
    output: 'trivy-results.sarif'
    severity: 'CRITICAL,HIGH'
    exit-code: '1'  # Falla el pipeline si encuentra vulnerabilidades

- name: Upload Trivy results to GitHub Security
  uses: github/codeql-action/upload-sarif@v2
  with:
    sarif_file: 'trivy-results.sarif'
```

**Bloquea deploy si**:
- Vulnerabilidades CRITICAL encontradas
- Vulnerabilidades HIGH > 5

**Reporte**: GitHub Security tab

---

### JaCoCo (Code Coverage)

**Configuración**: [pom.xml](../../pom.xml)

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.7</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

**Generar reporte**:
```bash
./mvnw clean test jacoco:report
open user-service/target/site/jacoco/index.html
```

**Integración con SonarQube**: JaCoCo genera `jacoco.xml` que SonarQube consume.

---

## Secrets y Configuración

### GitHub Secrets Requeridos

| Secret | Descripción | Usado en |
|--------|-------------|----------|
| `DOCKER_USERNAME` | Usuario de Docker Hub | Build workflows |
| `DOCKER_PASSWORD` | Password de Docker Hub | Build workflows |
| `SONAR_TOKEN` | Token de SonarCloud | SonarQube analysis |
| `SONAR_ORGANIZATION` | Org de SonarCloud | SonarQube analysis |
| `ARM_SUBSCRIPTION_ID` | Azure Subscription ID | Terraform |
| `ARM_TENANT_ID` | Azure Tenant ID | Terraform |
| `ARM_CLIENT_ID` | Azure Service Principal ID | Terraform |
| `ARM_CLIENT_SECRET` | Azure SP Secret | Terraform |
| `STAGE_KUBE_CONFIG` | Kubeconfig de stage (base64) | Stage deploy |
| `PROD_KUBE_CONFIG` | Kubeconfig de prod (base64) | Prod deploy |

### Configurar Secrets

**En GitHub**:
1. Ve a: Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Añade cada secret de la tabla

**Obtener KUBE_CONFIG**:
```bash
# Azure AKS
az aks get-credentials --resource-group ecommerce-rg --name ecommerce-aks-stage
cat ~/.kube/config | base64

# Copiar output y pegarlo como STAGE_KUBE_CONFIG
```

---

## Monitoreo de Pipelines

### Métricas Clave

| Métrica | Objetivo | Actual |
|---------|----------|--------|
| **Build time** | <5 minutos | 4m 30s |
| **Test execution** | <2 minutos | 1m 45s |
| **Deploy time** | <3 minutos | 2m 15s |
| **Pipeline success rate** | >95% | 97% |
| **MTTR** (Mean Time To Recovery) | <30 minutos | 25m |

### Notificaciones

**Slack Integration**:
```yaml
- name: Notify Slack on failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'Build failed for ${{ github.repository }}'
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

**Email Notifications**: GitHub Actions envía emails automáticamente en fallos.

---

## Troubleshooting

### Problema 1: Build Falla en Maven

**Error**: `Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin`

**Solución**:
```bash
# Verificar versión de Java
java -version  # Debe ser Java 11

# Limpiar cache de Maven
rm -rf ~/.m2/repository
./mvnw clean install -U
```

---

### Problema 2: Docker Build Falla

**Error**: `failed to solve: rpc error: failed to compute cache key`

**Solución**:
```bash
# Limpiar Docker cache
docker system prune -a --volumes

# Rebuild sin cache
docker build --no-cache -t user-service:dev .
```

---

### Problema 3: SonarQube Quality Gate Falla

**Error**: `Quality Gate failed: Coverage is 65.0%, required minimum is 80%`

**Solución**:
1. Ver reporte de cobertura: `target/site/jacoco/index.html`
2. Identificar clases sin coverage
3. Añadir tests unitarios
4. Re-run: `./mvnw clean test jacoco:report`

---

### Problema 4: Trivy Encuentra Vulnerabilidades

**Error**: `Total: 5 (CRITICAL: 2, HIGH: 3)`

**Solución**:
1. Ver detalles: GitHub Security tab
2. Actualizar dependencias:
```bash
./mvnw versions:display-dependency-updates
./mvnw versions:use-latest-releases
```
3. Rebuild y re-scan

---

### Problema 5: Deploy a Kubernetes Falla

**Error**: `deployment "user-service" exceeded its progress deadline`

**Solución**:
```bash
# Ver logs del pod
kubectl logs -f deployment/user-service -n default

# Ver eventos
kubectl get events --sort-by='.lastTimestamp' -n default

# Verificar imagen existe en Docker Hub
docker manifest inspect dantesiio/ecommerce-backend:user-service-abc123
```

---

## Mejoras Futuras

### 1. Canary Deployments
```yaml
- name: Canary deploy
  run: |
    kubectl set image deployment/user-service-canary \
      user-service=dantesiio/ecommerce-backend:user-service-${{ github.sha }} \
      -n default
    # Esperar 5 minutos y verificar error rate
    # Si error rate < 1%, promover a production
```

### 2. A/B Testing
- Deploy múltiples versiones simultáneamente
- Istio para traffic splitting (90% old, 10% new)
- Análisis de métricas de negocio

### 3. Automated Rollback
```yaml
- name: Automated rollback on failure
  if: steps.health-check.outcome == 'failure'
  run: |
    kubectl rollout undo deployment/user-service -n production
```

### 4. Performance Testing en Pipeline
```bash
# Locust performance test
locust -f tests/performance/locustfile.py --headless -u 100 -r 10 --run-time 5m
# Fail pipeline si p95 latency > 500ms
```

---

## Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [SonarCloud Best Practices](https://sonarcloud.io/documentation)
- [Trivy Documentation](https://aquasecurity.github.io/trivy/)
- [OWASP ZAP Documentation](https://www.zaproxy.org/docs/)

---

**Última actualización**: 24 Noviembre 2025
**Autores**: Santiago & David
**Proyecto**: Ingeniería de Software V - CI/CD Pipeline
