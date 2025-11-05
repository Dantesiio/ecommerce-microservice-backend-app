# Taller 2 – Pruebas y Releases

## Alcance
- Migrar seis microservicios clave (`proxy-client`, `user-service`, `product-service`, `order-service`, `payment-service`, `shipping-service`) a un ciclo CI/CD completo.
- Incorporar pruebas unitarias, integración, extremo a extremo y rendimiento que cubran flujos reales entre los servicios seleccionados.
- Desplegar a Kubernetes en tres ambientes: `dev`, `stage` y `master`, con publicación automática de Release Notes.

## Preparación de Herramientas
### GitHub Actions
- Repositorio configurado con workflows en `.github/workflows` segmentados por servicio y ambiente.
- Runners utilizados:
  - `ubuntu-latest` hospedado por GitHub para compilaciones en ramas `dev`/`develop`.
  - `self-hosted` con Docker y acceso al registro para el flujo `stage-ci.yml`.
- Secretos requeridos:
  - `DOCKER_USERNAME` y `DOCKER_PASSWORD` para autenticación en Docker Hub.
  - `REGISTRY_HOST` y `REGISTRY_REPO` (variables de entorno compartidas en `stage-ci.yml`).
  - `STAGE_KUBE_CONFIG` (pendiente de creación) para configurar despliegues a Kubernetes.
- Estrategia de ramas:
  - `dev` activa ejecuciones de integración continua y despliegues a `stage`.
  - `master` consolidará despliegues a producción cuando el pipeline final esté completado.

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
1. Workflows `*-pipeline-dev-*.yml`: automatizan build y pruebas unitarias por microservicio en ramas `dev`/`develop`.
2. Workflow `stage-ci.yml`: build → empaquetado Docker → push a Docker Hub → despliegue en Kubernetes `stage` → smoke tests.
3. Workflow de release (pendiente): orquestará pruebas funcionales, estrés y publicación de Release Notes antes de promover a producción.
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
- El merge a `master` se realizará solo cuando todos los workflows de GitHub Actions estén verdes.

### Workflows Dev
- Ubicación: `.github/workflows/*-pipeline-dev-*.yml` (un archivo por microservicio).
- Ejecutan en `ubuntu-latest` con Java 11 y Maven Wrapper.
- Etapas principales:
  - `Checkout`: clona el repositorio.
  - `Build with Maven`: compila y ejecuta pruebas (`mvn -B package --file pom.xml`).
  - `Docker Login`: autentica en Docker Hub usando `DOCKER_USERNAME`/`DOCKER_PASSWORD`.
  - `Build & Push`: construye la imagen Docker del servicio y la etiqueta con `${{ secrets.PROJECT_VERSION }}dev`.
- Próxima iteración: publicar reportes JUnit y consolidar nomenclatura de imágenes.

### Workflow Stage
- Ubicación: `.github/workflows/stage-ci.yml`.
- Corre en runner `self-hosted` con Docker, acceso al registro y credenciales Kubernetes.
- Etapas principales:
  - `Checkout`: clona la rama `dev`.
  - `Build & Unit Tests`: ejecuta `./mvnw ... clean verify` para los seis servicios núcleo.
  - `Authenticate`: inicia sesión en Docker Hub con los secretos `DOCKER_USERNAME`/`DOCKER_PASSWORD`.
  - `Build service images`: construye imágenes etiquetadas como `$REGISTRY_HOST/$REGISTRY_REPO:<service>-<GITHUB_SHA>`.
  - `Push service images`: publica cada imagen en el registro.
  - En construcción: despliegue automático a Kubernetes `stage` mediante kubeconfig almacenado como secreto.
- Post-condición esperada: smoke tests sobre `proxy-client` una vez integrado el despliegue.

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
