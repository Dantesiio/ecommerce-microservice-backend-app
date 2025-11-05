# Taller 2: Pruebas y Lanzamiento - CI/CD

## Estudiante
- **Nombre:** Daniel Donneys
- **Usuario:** dantesiio
- **Fecha:** Noviembre 2024

## Estado del Proyecto

### ✅ Completado (80%)
- CI/CD con GitHub Actions
- 68 tests implementados
  - Unitarias: 27 tests (5 clases)
  - Integración: 16 tests (5 clases)
  - E2E: 25 tests (5 clases)
  - Performance: 5 escenarios Locust
- Railway eliminado
- Workflows funcionando
- Documentación actualizada

### 🔄 Pendiente (20%)
- Configuración Kubernetes
- Despliegue a ambientes

## Arquitectura
```
GitHub → GitHub Actions → Docker Hub
                           ↓
                    [Kubernetes - Pendiente]
```

## Workflows
- api-gateway-pipeline-dev-pr.yml
- api-gateway-pipeline-dev-push.yml
- api-gateway-pipeline-prod-pr.yml
- api-gateway-pipeline-prod-push.yml
- api-gateway-pipeline-stage-pr.yml
- api-gateway-pipeline-stage-push.yml
- cloud-config-pipeline-dev-pr.yml
- cloud-config-pipeline-dev-push.yml
- cloud-config-pipeline-prod-pr.yml
- cloud-config-pipeline-prod-push.yml
- cloud-config-pipeline-stage-pr.yml
- cloud-config-pipeline-stage-push.yml
- favourite-service-pipeline-dev-pr.yml
- favourite-service-pipeline-dev-push.yml
- favourite-service-pipeline-prod-pr.yml
- favourite-service-pipeline-prod-push.yml
- favourite-service-pipeline-stage-pr.yml
- favourite-service-pipeline-stage-push.yml
- order-service-pipeline-dev-pr.yml
- order-service-pipeline-dev-push.yml
- order-service-pipeline-prod-pr.yml
- order-service-pipeline-prod-push.yml
- order-service-pipeline-stage-pr.yml
- order-service-pipeline-stage-push.yml
- payment-service-pipeline-dev-pr.yml
- payment-service-pipeline-dev-push.yml
- payment-service-pipeline-prod-pr.yml
- payment-service-pipeline-prod-push.yml
- payment-service-pipeline-stage-pr.yml
- payment-service-pipeline-stage-push.yml
- product-service-pipeline-dev-pr.yml
- product-service-pipeline-dev-push.yml
- product-service-pipeline-prod-pr.yml
- product-service-pipeline-prod-push.yml
- product-service-pipeline-stage-pr.yml
- product-service-pipeline-stage-push.yml
- proxy-client-pipeline-dev-pr.yml
- proxy-client-pipeline-dev-push.yml
- proxy-client-pipeline-prod-pr.yml
- proxy-client-pipeline-prod-push.yml
- proxy-client-pipeline-stage-pr.yml
- proxy-client-pipeline-stage-push.yml
- service-discovery-pipeline-dev-pr.yml
- service-discovery-pipeline-dev-push.yml
- service-discovery-pipeline-prod-pr.yml
- service-discovery-pipeline-prod-push.yml
- service-discovery-pipeline-stage-pr.yml
- service-discovery-pipeline-stage-push.yml
- shipping-service-pipeline-dev-pr.yml
- shipping-service-pipeline-dev-push.yml
- shipping-service-pipeline-prod-pr.yml
- shipping-service-pipeline-prod-push.yml
- shipping-service-pipeline-stage-pr.yml
- shipping-service-pipeline-stage-push.yml
- stage-ci.yml
- user-service-pipeline-dev-pr.yml
- user-service-pipeline-dev-push.yml
- user-service-pipeline-prod-pr.yml
- user-service-pipeline-prod-push.yml
- user-service-pipeline-stage-pr.yml
- user-service-pipeline-stage-push.yml

## Pruebas Implementadas

### Unitarias (27 tests)
- CredentialServiceImplTest (5)
- CartServiceImplTest (7)
- PaymentServiceImplTest (5)
- ProductServiceImplTest (5)
- FavouriteServiceImplTest (5)

### Integración (16 tests)
- UserServiceIntegrationTest (3)
- PaymentOrderIntegrationTest (3)
- ProductCategoryIntegrationTest (3)
- ShippingPaymentIntegrationTest (3)
- FavouriteUserProductIntegrationTest (4)

### E2E (25 tests)
- UserRegistrationE2ETest (5)
- ProductBrowsingE2ETest (5)
- OrderCreationE2ETest (5)
- PaymentProcessingE2ETest (5)
- ShippingFulfillmentE2ETest (5)

### Performance
- Locust: 5 escenarios en locustfile.py

## Evidencias

Ver: docs/evidencias/ (mínimo 6 screenshots)

## Documentación

- INFORME_TALLER2.md (técnica completa)
- README_TALLER.md (este archivo)
- taller2-tests-package/ (código pruebas)

## Enlaces

- Repo: https://github.com/dantesiio/ecommerce-microservice-backend-app
- Actions: https://github.com/dantesiio/ecommerce-microservice-backend-app/actions

## Próximos Pasos

1. Setup Kubernetes
2. Desplegar a dev/staging/production
3. Evidencias finales
4. ZIP completo

---
Última actualización: 4 de noviembre de 2025
