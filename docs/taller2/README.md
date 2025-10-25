# Taller 2 – Pruebas y Releases

## Alcance
- Migrar seis microservicios clave (`proxy-client`, `user-service`, `product-service`, `order-service`, `payment-service`, `shipping-service`) a un ciclo CI/CD completo.
- Incorporar pruebas unitarias, integración, extremo a extremo y rendimiento que cubran flujos reales entre los servicios seleccionados.
- Desplegar a Kubernetes en tres ambientes: `dev`, `stage` y `master`, con publicación automática de Release Notes.

## Preparación de Herramientas
### Jenkins
- Versión mínima recomendada: 2.452 LTS (incluye JDK 11).
- Plugins requeridos: `Docker Pipeline`, `Kubernetes`, `Git`, `JUnit`, `Jacoco`, `HTML Publisher`, `Pipeline Utility Steps`, `AnsiColor`.
- Configurar agentes:
  - `docker` label con acceso al daemon Docker.
  - `k8s` label para ejecutar `kubectl`/`helm`.
- Credenciales:
  - `git-credentials` (con acceso lectura/escritura al repo).
  - `docker-registry` (usuario/token al registry elegido).
  - `kubeconfig-stage` y `kubeconfig-master` (secret text con el contenido del kubeconfig correspondiente).

### Docker
- Docker Engine ≥ 25.0 en el nodo Jenkins `docker`.
- Construcción multi-stage apoyada en `mvn -Pci` (se agregará en commits posteriores).
- Repository naming: `registry.example.com/ecommerce/<service-name>:<git-sha>`.

### Kubernetes
- Clúster administrado (AKS/EKS/GKE) o local (kind/minikube) con soporte para:
  - Ingress Controller (NGINX o Traefik) para exponer `proxy-client`.
  - Namespace dedicados por ambiente: `ecommerce-dev`, `ecommerce-stage`, `ecommerce-master`.
  - Secrets precreados para base de datos y credenciales externas (`orders-db`, `payments-db`).
- Herramientas en agente `k8s`: `kubectl`, `helm`, `kustomize`.

## Entregables Planeados
1. Jenkinsfile `dev`: build y pruebas básicas para cada microservicio (`jenkins/Jenkinsfile.dev`).
2. Jenkinsfile `stage`: build → empaquetado Docker → despliegue en Kubernetes → smoke tests (`jenkins/Jenkinsfile.stage`).
3. Jenkinsfile `master`: pipeline completo con pruebas funcionales, estrés y generación de Release Notes.
4. Suites de pruebas automatizadas:
   - Unitarias adicionales (≥5).
   - Integración entre servicios (≥5).
   - End-to-End a través de `proxy-client` (≥5).
   - Pruebas de rendimiento con Locust + métricas (tiempo respuesta, throughput, errores).
5. Documentación: evidencias de configuración/ejecución de pipelines, análisis de resultados y liberaciones.
6. Paquete `.zip` con las nuevas pruebas.

## Proceso de Trabajo
- Cada entregable tendrá su commit dedicado en la rama `dev`.
- Se actualizará este documento con enlaces a configuraciones, scripts y reportes consolidados.
- El merge a `master` se realizará solo cuando todos los pipelines estén verdes en Jenkins.

### Jenkinsfile.dev
- Ubicación: `jenkins/Jenkinsfile.dev`.
- Pipeline declarativo que ejecuta en agente con label `docker`.
- Etapas principales:
  - `Checkout`: checkout del repositorio y submódulos.
  - `Resolve Maven Wrapper`: verificación de wrapper y versión de Maven.
  - `Build Services (Dev)`: compilación paralela de los seis microservicios con perfil `dev`.
  - `Unit Tests Summary`: ejecución de `mvn test` sobre los módulos seleccionados.
- Publica reportes JUnit de `surefire` y archiva los artefactos `.jar` generados.

### Jenkinsfile.stage
- Ubicación: `jenkins/Jenkinsfile.stage`.
- Pipeline sin agente global con etapas diferenciadas para construcción y despliegue.
- Etapas principales:
  - `Checkout`: clona la rama `dev` mediante el credential `git-credentials`.
  - `Build & Unit Tests (Stage)`: ejecuta `./mvnw ... -Pdev clean verify` sobre los seis servicios núcleo.
  - `Build Container Images`: construye imágenes Docker etiquetadas con el `GIT_COMMIT` para cada servicio.
  - `Push Images`: inicia sesión en el registro (`docker-registry`) y publica las imágenes.
  - `Deploy to Kubernetes (Stage)`: aplica manifests de `k8s/overlays/stage` utilizando el kubeconfig almacenado en `kubeconfig-stage`.
  - `Smoke Tests`: verifica el rollout y ejecuta una comprobación de salud HTTP contra `proxy-client`.
- Post-condición: siempre publica reportes JUnit y limpia el workspace.

### Pruebas Unitarias Añadidas
- `user-service`: `UserServiceImplTest` cubre búsqueda exitosa y ausencia de usuarios.
- `product-service`: `ProductServiceImplTest` valida lista inmutable, eliminación y error de inexistente.
- `order-service`: `OrderServiceImplTest` comprueba inmutabilidad, actualización y errores.
- `payment-service`: `PaymentServiceImplTest` verifica enriquecimiento con órdenes y eliminación.
- `shipping-service`: `OrderItemServiceImplTest` asegura llamadas a servicios externos y manejo de faltantes.

### Pruebas de Integración Añadidas
- `order-service`: `CartResourceIT` valida que los carritos enriquecen la información del usuario al consultar `USER-SERVICE` (dos escenarios).
- `payment-service`: `PaymentResourceIT` comprueba que los pagos consultan detalles de la orden mediante `ORDER-SERVICE`.
- `shipping-service`: `OrderItemResourceIT` verifica que cada envío incorpora datos de producto y orden consumiendo `PRODUCT-SERVICE` y `ORDER-SERVICE`.
- `proxy-client`: `OrderControllerIT` confirma la delegación de solicitudes `GET` y `POST` hacia `ORDER-SERVICE` vía Feign.
