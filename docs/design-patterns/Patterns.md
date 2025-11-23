# Patrones de Diseño Existentes

## 1. Circuit Breaker Pattern ⚡

### Ubicación
- Servicio: `proxy-client`
- Implementación: Resilience4j
- Archivo: `proxy-client/src/main/resources/application.yml`

### Propósito
Prevenir fallos en cascada cuando un servicio downstream falla.

### Configuración
```yaml
resilience4j:
  circuitbreaker:
    instances:
      proxyService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
```

### Beneficios
- ✅ Resiliencia del sistema
- ✅ Fallo rápido en lugar de timeout
- ✅ Recuperación automática
- ✅ Métricas en `/actuator/health`

### Diagrama
```
Request → Circuit Breaker
           ├─ CLOSED: Llamada normal → Service
           ├─ OPEN: Fallo rápido → Fallback
           └─ HALF_OPEN: Prueba → Service
```

---

## 2. Service Discovery Pattern 🔍

### Ubicación
- Servicio: `service-discovery`
- Implementación: Netflix Eureka
- Puerto: 8761

### Propósito
Registro y descubrimiento dinámico de servicios sin hardcodear URLs.

### Configuración
Cada servicio se registra en startup:
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://service-discovery:8761/eureka
```

### Beneficios
- ✅ Escalabilidad horizontal
- ✅ No hardcoded URLs
- ✅ Health checking automático
- ✅ Load balancing client-side

### Servicios Registrados
- PROXY-CLIENT
- USER-SERVICE
- PRODUCT-SERVICE
- ORDER-SERVICE
- PAYMENT-SERVICE
- SHIPPING-SERVICE
- FAVOURITE-SERVICE

---

## 3. API Gateway Pattern 🚪

### Ubicación
- Servicios: `api-gateway` (puerto 8080) y `proxy-client` (puerto 8900)
- Implementación: Spring Cloud Gateway

### Propósito
Punto de entrada único para todas las peticiones del cliente.

### Responsabilidades
- Routing a servicios internos
- Autenticación JWT (en proxy-client)
- Rate limiting
- Request/Response transformation

### Beneficios
- ✅ Seguridad centralizada
- ✅ Simplifica cliente
- ✅ Cross-cutting concerns
- ✅ Versionado de API

---

## 4. Database per Service Pattern 💾

### Implementación
Cada microservicio tiene su propia base de datos:
- user-service → user_db
- product-service → product_db
- order-service → order_db
- payment-service → payment_db
- shipping-service → shipping_db

### Tecnología
- Desarrollo: H2 in-memory
- Producción: MySQL
- Migraciones: Flyway

### Beneficios
- ✅ Independencia de servicios
- ✅ Escalabilidad independiente
- ✅ Tecnología adaptada por servicio
- ✅ Fallo aislado

### Desventíos
- ❌ Transacciones distribuidas complejas
- ❌ Queries cross-service requieren composición

---

## 5. Externalized Configuration Pattern ⚙️

### Ubicación
- Servicio: `cloud-config`
- Implementación: Spring Cloud Config Server
- Puerto: 9296

### Propósito
Configuración centralizada para todos los servicios.

### Estructura
```
cloud-config/src/main/resources/config/
├── application.yml (común)
├── application-dev.yml
├── application-stage.yml
├── application-prod.yml
├── user-service.yml
├── product-service.yml
└── ...
```

### Beneficios
- ✅ Configuración centralizada
- ✅ Cambios sin rebuild
- ✅ Configuración por ambiente
- ✅ Versionado en Git

---

## 6. Saga Pattern (Implícito) 🔄

### Ubicación
Implementación distribuida entre:
- order-service
- payment-service
- shipping-service

### Flujo
```
1. Order Created (order-service)
2. Payment Processed (payment-service)
3. Shipping Initiated (shipping-service)
```

### Nota
Actualmente básico, sin compensating transactions explícitas.

---

## 7. Bulkhead Pattern (Próximo a Implementar) 🚧

### Objetivo
Aislar recursos para diferentes tipos de requests.

### Plan de Implementación
- Usar Resilience4j Bulkhead
- Separar thread pools por tipo de operación
- Configurar en proxy-client

---

## Resumen de Patrones

| Patrón | Implementado | Ubicación | Herramienta |
|--------|--------------|-----------|-------------|
| Circuit Breaker | ✅ | proxy-client | Resilience4j |
| Service Discovery | ✅ | service-discovery | Eureka |
| API Gateway | ✅ | api-gateway, proxy-client | Spring Cloud Gateway |
| Database per Service | ✅ | Todos los servicios | H2/MySQL + Flyway |
| External Configuration | ✅ | cloud-config | Spring Cloud Config |
| Saga | ⚠️ Básico | order/payment/shipping | Manual |
| Bulkhead | ❌ Pendiente | - | - |
| Retry | ❌ Pendiente | - | - |
| Health Check | ✅ | Todos | Spring Actuator |
```