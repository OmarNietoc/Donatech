# Donatech — Hub Logístico de Donaciones

Plataforma de microservicios para centralizar la logística de donaciones ante catástrofes naturales en Chile. Permite a organizaciones crear campañas, a donantes seleccionar kits y adjuntar comprobantes de transferencia, y a administradores validar y coordinar la entrega a beneficiarios.

---

## Stack tecnológico

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Cloud | 2025.1.1 |
| PostgreSQL | 16 |
| RabbitMQ | 3.13 |
| springdoc-openapi | 2.3.0 |
| jjwt | 0.11.5 |
| MapStruct | 1.5.5.Final |
| Maven | 3.9+ |
| Docker | — |

---

## Arquitectura

```
┌──────────────────────────────────────────────┐
│            Frontend Vue.js :5173             │
└───────────────────────┬──────────────────────┘
                        │ HTTP
                        ▼
              ┌───────────────────┐
              │    api-gateway    │ :8080
              │  JWT auth · CORS  │
              └─────────┬─────────┘
                        │ Eureka service discovery
        ┌───────────────┼──────────────────────┐
        ▼               ▼                      ▼
  ┌──────────┐   ┌────────────┐   ┌──────────────────┐
  │   auth   │   │   users    │   │     catalog      │
  │  :8084   │   │   :8083    │   │      :8081       │
  └──────────┘   └────────────┘   └──────────────────┘
        ▼               ▼                      ▼
  ┌──────────┐   ┌────────────┐   ┌──────────────────┐
  │  order   │   │  supports  │   │   notification   │
  │  :8082   │   │   :8085    │   │      :8086       │
  └──────────┘   └────────────┘   └──────────────────┘
                        ▼
              ┌───────────────────┐
              │     shipping      │ :8087
              └───────────────────┘

Infraestructura:
  Eureka :8761 · PostgreSQL :5432 · RabbitMQ :5672 / UI :15672
```

Mensajería asíncrona entre ms via RabbitMQ exchange `donatech.events` (TopicExchange).  
Comunicación inter-ms sincrónica via OpenFeign + Resilience4j (circuit breaker + retry).

---

## Microservicios

**Swagger UI agregado (todos los servicios):** [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)

| MS | Puerto | Responsabilidad | Swagger UI |
|---|---|---|---|
| discovery-server | 8761 | Eureka service registry | — |
| api-gateway | 8080 | Routing, JWT auth, CORS | [/swagger-ui](http://localhost:8080/swagger-ui) (agregado) |
| auth | 8084 | Autenticación, registro, generación JWT | [via gateway](http://localhost:8080/auth/swagger-ui/index.html) |
| users | 8083 | Usuarios, beneficiarios, zonas catástrofe, CompanyDetails | [via gateway](http://localhost:8080/users/swagger-ui/index.html) |
| catalog | 8081 | Productos, kits, categorías, campañas de donación | [via gateway](http://localhost:8080/catalog/swagger-ui/index.html) |
| order | 8082 | Órdenes de donación, historial de estados, comprobantes | [via gateway](http://localhost:8080/order/swagger-ui/index.html) |
| supports | 8085 | Tickets de soporte, validación campañas/transferencias | [via gateway](http://localhost:8080/supports/swagger-ui/index.html) |
| notification | 8086 | Envío de emails (stateless, sin BD) | — sin endpoints REST |
| shipping | 8087 | Envíos, rutas optimizadas (OSRM), seguimiento | [via gateway](http://localhost:8080/shipping/swagger-ui/index.html) |

---

## Rutas del API Gateway

Todas las rutas pasan por `:8080`. Las rutas de Swagger no requieren autenticación.

| Prefijo | MS destino | Auth requerida |
|---|---|---|
| `/api/auth/**` | auth | No |
| `/api/users/**`, `/api/regions/**`, `/api/comunas/**`, `/api/beneficiaries/**`, `/api/zonas-catastrofe/**` | users | Sí |
| `/api/products/**`, `/api/categories/**`, `/api/units/**`, `/api/kits/**`, `/api/campaigns/**`, `/api/necesidades/**` | catalog | Sí |
| `/api/orders/**`, `/api/donations/**` | order | Sí |
| `/api/supports/**` | supports | Sí |
| `/api/shipments/**`, `/api/routes/**` | shipping | Sí |
| `/swagger-ui` | gateway (HTML inline) | No |
| `/{service}/swagger-ui/**`, `/{service}/v3/api-docs/**` | cada MS | No |

CORS configurado para `http://localhost:5173` (frontend Vue.js).

> **Nota springdoc:** `springdoc-openapi 2.3.0` no es compatible con Spring Framework 7.x fuera de la caja. Todos los MS tienen `springdoc.override-with-generic-response: false` en `application.yml` para evitar el `NoSuchMethodError` de `ControllerAdviceBean`. El gateway no usa springdoc — sirve el Swagger UI agregado vía un controller propio con CDN.

---

## Flujo de negocio

```
ORGANIZACIÓN / BENEFICIARIO
  → crea Campaña (catalog)
  → evento: campaign.created
  → supports: auto-ticket VALIDACION_CAMPAÑA
  → admin valida
      → APROBADA: campaign.activated → notification email + catalog activa kits
      → RECHAZADA: campaign.rejected → notification email

DONANTE
  → selecciona kits de campaña activa
  → adjunta comprobante de transferencia (order)
  → Order: INGRESADA + evento: transfer.submitted
  → supports: auto-ticket VALIDACION_TRANSFERENCIA
  → admin valida
      → APROBADA: transfer.validated → order: EN_PREPARACION + donation.confirmed
          → catalog descuenta stock de kits
      → RECHAZADA: transfer.rejected → notification email

TRANSPORTISTA
  → shipping asigna ruta optimizada (OSRM)
  → sube foto de entrega → Order: PENDIENTE_CONFIRMACION

ADMIN
  → confirma entrega → Order: ENTREGADA
  → evento: order.shipped → notification email
```

---

## Eventos RabbitMQ

Exchange: `donatech.events` (TopicExchange)

| Routing Key | Publicado por | Consumido por | Propósito |
|---|---|---|---|
| `campaign.created` | catalog | supports | Auto-ticket de validación campaña |
| `campaign.activated` | supports | notification, catalog | Notificar aprobación, activar kits |
| `campaign.rejected` | supports | notification | Notificar rechazo |
| `transfer.submitted` | order | supports | Auto-ticket validación transferencia |
| `transfer.validated` | supports | order | Mover a EN_PREPARACION + confirmar donación |
| `transfer.rejected` | supports | notification | Notificar rechazo |
| `donation.confirmed` | order | catalog | Descontar stock de kits |
| `stock.low` | catalog | supports | Auto-ticket stock bajo |
| `beneficiary.verified` | users | order | Habilitar procesamiento de orden |
| `order.ready_for_shipping` | order | shipping | Crear envío automáticamente |
| `order.shipped` | shipping | notification | Notificar envío |

---

## Estados

### DonationStatus (Orden)

```
DRAFT → INGRESADA → EN_VALIDACION_TRANSFERENCIA → EN_PREPARACION
  → ASIGNADA_ENVIO → EN_CAMINO → PENDIENTE_CONFIRMACION → ENTREGADA
                                                         ↘ CANCELADA
                                                         ↘ RECHAZADA
```

### CampaignStatus

```
EN_VALIDACION → ACTIVA → INACTIVA
                       ↘ FINALIZADA
```

### DeliveryStatus (Shipping)

```
PENDING → ASSIGNED → DISPATCHED → DELIVERED
       ↘ FAILED / CANCELLED (desde cualquier estado)
```

---

## Roles

| Rol | Descripción |
|---|---|
| `ROLE_ADMIN` | Administrador general — valida campañas y transferencias |
| `ROLE_DONANTE` | Realiza donaciones y sube comprobantes |
| `ROLE_VOLUNTARIO` | Atiende tickets de soporte |
| `ROLE_BENEFICIARIO` | Recibe donaciones, requiere verificación |
| `ROLE_ORGANIZACION` | Crea campañas de donación |

---

## Prerequisitos

- Java 21
- Maven 3.9+
- Docker + Docker Compose

---

## Setup y ejecución

### Con Docker Compose (recomendado)

```bash
# 1. Copiar variables de entorno
cp .env.example .env
# Editar .env con tus valores

# 2. Iniciar toda la plataforma
docker-compose -f docker-compose-local.yml up -d

# 3. Verificar que todos los servicios estén corriendo
docker-compose -f docker-compose-local.yml ps

# 4. Ver logs de un MS específico
docker-compose -f docker-compose-local.yml logs -f auth
```

Los servicios se inician en orden: PostgreSQL → RabbitMQ → Eureka → API Gateway → MS de negocio.

### Desarrollo local (sin Docker)

Requiere PostgreSQL y RabbitMQ corriendo localmente.

```bash
# Iniciar un MS individual
cd auth
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Iniciar todos los MS (PowerShell)
foreach ($ms in @("discovery-server","api-gateway","auth","users","catalog","order","supports","notification","shipping")) {
    Start-Process powershell -ArgumentList "-NoExit","-Command","cd $ms; ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
}
```

### Compilar sin ejecutar

```bash
cd auth && ./mvnw compile
```

---

## Tests

```bash
# Tests de un MS
cd users && ./mvnw test

# Test de una clase específica
./mvnw test -Dtest=ShipmentServiceTest

# Test de un método específico
./mvnw test -Dtest=UserServiceTest#getAllUsers_ShouldReturnAllUsers

# Reporte HTML detallado
# Después de ./mvnw test, abrir en browser:
# target/surefire-reports/index.html

# Todos los MS (PowerShell)
foreach ($ms in @("auth","users","catalog","order","supports","shipping","notification")) {
    Write-Host "=== $ms ===" -ForegroundColor Cyan
    Push-Location $ms; ./mvnw test -q; Pop-Location
}
```

> Los `*ApplicationTests` requieren infraestructura (PostgreSQL + RabbitMQ). Los tests unitarios (Mockito) se ejecutan sin infraestructura.

---

## Variables de entorno

Copiar `.env.example` a `.env` y configurar:

| Variable | Descripción | Ejemplo |
|---|---|---|
| `POSTGRES_USER` | Usuario de PostgreSQL | `donatech` |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL | `secret` |
| `JWT_SECRET` | Clave secreta para firmar JWT (min. 32 chars) | `donatechSecretKey...` |
| `RABBITMQ_HOST` | Host de RabbitMQ | `rabbitmq` |
| `RABBITMQ_USERNAME` | Usuario RabbitMQ | `guest` |
| `RABBITMQ_PASSWORD` | Contraseña RabbitMQ | `guest` |
| `MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Puerto SMTP | `587` |
| `MAIL_USERNAME` | Cuenta de correo | `notificaciones@donatech.cl` |
| `MAIL_PASSWORD` | Contraseña / app password | `...` |

---

## Estructura del proyecto

```
Donatech/
├── api-gateway/          # Spring Cloud Gateway — routing, JWT, CORS
├── auth/                 # Autenticación y registro de usuarios
├── catalog/              # Kits, productos, categorías, campañas
├── discovery-server/     # Netflix Eureka service registry
├── notification/         # Envío de emails vía SMTP (stateless)
├── order/                # Órdenes de donación e historial
├── shipping/             # Envíos y rutas de entrega (OSRM)
├── supports/             # Tickets de soporte y validaciones
├── users/                # Perfiles, beneficiarios, zonas catástrofe
├── docker-compose-local.yml
├── docker-compose-cloud.yml
├── init-dbs.sql          # Script de inicialización de bases de datos
├── .env.example          # Plantilla de variables de entorno
```

Cada microservicio sigue la estructura estándar de Spring Boot:

```
{ms}/
├── src/main/java/com/donatech/{ms}/
│   ├── config/       # Configuración (RabbitMQ, OpenAPI, Security)
│   ├── controller/   # REST controllers con OpenAPI annotations
│   ├── dto/          # Data Transfer Objects con @Schema
│   ├── event/        # Publishers y consumers de RabbitMQ
│   ├── exception/    # Excepciones personalizadas + GlobalExceptionHandler
│   ├── model/        # Entidades JPA
│   ├── repository/   # Spring Data JPA repositories
│   └── service/      # Lógica de negocio
├── src/test/         # Tests unitarios (Mockito)
├── Dockerfile
└── pom.xml
```
