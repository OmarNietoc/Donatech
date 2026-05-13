# Donatech — Hub Logístico de Donaciones

Plataforma de microservicios para centralizar logística de donaciones ante catástrofes en Chile.
Migrada desde e-commerce "Shoppy" — paquetes `com.donatech.*`, BD `donatech`.

## Stack

**Backend:** Java 21 · Spring Boot 4.0.6 · Spring Cloud 2025.1.1 · PostgreSQL · RabbitMQ · JWT (jjwt 0.12) · Maven · Docker

**Frontend:** React 18 · TypeScript 5 · Vite 6 · pnpm · TanStack Query v5 · Zustand 4 · React Router v6 · Axios · Tailwind CSS v3 · React Hook Form 7 · Zod 3

## Infraestructura
- **Service Discovery**: Netflix Eureka (`discovery-server` :8761)
- **API Gateway**: Spring Cloud Gateway (`api-gateway` :8080) — Swagger agregado en `/swagger-ui`
- **Mensajería**: RabbitMQ exchange `donatech.events` (TopicExchange)
- **Resiliencia**: Resilience4j — circuit breaker + retry en OpenFeign inter-ms

## Microservicios

| MS | Puerto | Responsabilidad |
|---|---|---|
| `discovery-server` | 8761 | Eureka Server |
| `api-gateway` | 8080 | Enrutamiento, JWT, CORS, Swagger CDN agregado |
| `auth` | 8084 | Autenticación, registro, JWT |
| `users` | 8083 | Usuarios, beneficiarios, zonas catástrofe, CompanyDetails |
| `catalog` | 8081 | Kits, productos, Campaña de donación |
| `order` | 8082 | Órdenes, historial estados, comprobantes, tracking |
| `supports` | 8085 | Tickets soporte, validación campañas/transferencias |
| `notification` | 8086 | Emails HTML Thymeleaf (stateless, sin BD) |
| `shipping` | 8087 | **NO TOCAR** — en desarrollo paralelo |

## Flujo de negocio

```
ORG/BENEFICIARIO → POST /api/campaigns → EN_VALIDACION
  → campaign.created → supports auto-ticket VALIDACION_CAMPAÑA
  → ADMIN: PATCH /api/supports/{id}/validate-campaign?approved=true
  → campaign.activated → catalog activa campaña → notification email org

DONANTE → POST /api/orders (campaignId + kitIds) → DRAFT→INGRESADA
  → POST /api/orders/{id}/transfer-proof (multipart) → EN_VALIDACION_TRANSFERENCIA
  → transfer.submitted → supports auto-ticket VALIDACION_TRANSFERENCIA
  → ADMIN: PATCH /api/supports/{id}/validate-transfer?approved=true
  → transfer.validated → order EN_PREPARACION + donation.confirmed → catalog descuenta stock

TRANSPORTISTA → POST /api/orders/{id}/delivery-proof (foto+doc) → PENDIENTE_CONFIRMACION
ADMIN → PATCH /api/orders/{id}/confirm-delivery → ENTREGADA
```

## Estados de Orden (DonationStatus)
`DRAFT` · `INGRESADA` · `EN_VALIDACION_TRANSFERENCIA` · `EN_PREPARACION` · `ASIGNADA_ENVIO` · `EN_CAMINO` · `PENDIENTE_CONFIRMACION` · `ENTREGADA` · `CANCELADA` · `RECHAZADA`

## Estado de Campaña (CampaignStatus)
`EN_VALIDACION` · `ACTIVA` · `INACTIVA` · `FINALIZADA`

## Eventos RabbitMQ (`donatech.events`)

| Routing Key | Publicado por | Consumido por |
|---|---|---|
| `campaign.created` | catalog | supports → auto-ticket |
| `campaign.activated` | supports | notification · catalog |
| `campaign.rejected` | supports | notification · catalog |
| `transfer.submitted` | order | supports → auto-ticket |
| `transfer.validated` | supports | order → EN_PREPARACION + donation.confirmed |
| `transfer.rejected` | supports | notification |
| `donation.confirmed` | order | catalog → descontar stock |
| `stock.low` | catalog | supports → auto-ticket |
| `beneficiary.verified` | users | order |
| `order.shipped` | shipping (futuro) | notification |

## Roles
`ROLE_ADMIN` · `ROLE_DONANTE` · `ROLE_VOLUNTARIO` · `ROLE_BENEFICIARIO` · `ROLE_ORGANIZACION`

## Frontend — Páginas planificadas

| Ruta | Roles | Descripción |
|---|---|---|
| `/` | público | Landing + campañas activas |
| `/campaigns` | público | Listado campañas activas |
| `/campaigns/:id` | público | Detalle campaña + kits |
| `/login` | público | JWT login |
| `/register` | público | Registro (donante/org/beneficiario) |
| `/orders/new` | DONANTE | Crear orden con kits |
| `/orders/:id` | DONANTE | Tracking + historial + polling |
| `/orders/:id/proof` | DONANTE | Subir comprobante transferencia |
| `/admin/tickets` | ADMIN/VOLUNTARIO | Lista tickets soporte |
| `/admin/tickets/:id/validate` | ADMIN/VOLUNTARIO | Validar campaña o transferencia |
| `/admin/dashboard` | ADMIN | Resumen órdenes por estado |
| `/org/campaigns/new` | ORG/BENEFICIARIO | Crear campaña |
| `/org/campaigns` | ORG/BENEFICIARIO | Mis campañas + estado |

## URLs importantes

| Recurso | URL |
|---|---|
| Swagger agregado | http://localhost:8080/swagger-ui |
| Gateway | http://localhost:8080 |
| Eureka | http://localhost:8761 |
| RabbitMQ UI | http://localhost:15672 |
| Frontend dev | http://localhost:5173 |

## Reglas

### Backend
1. NO crear/modificar ms `shipping`
2. Java 21 + Spring Boot 4.0.6 + Spring Cloud 2025.1.1 obligatorio
3. Todo ms registrado en Eureka como eureka client
4. Comunicación inter-ms con Resilience4j (circuit breaker + retry)
5. Eventos de negocio vía RabbitMQ (no HTTP para operaciones asíncronas)
6. Toda entidad con validaciones Jakarta, todo endpoint con OpenAPI `@Operation`
7. Verificar compilación: `./mvnw compile`
8. BD: `donatech` (no `shoppy`)

### Frontend
9. pnpm obligatorio (no npm/yarn)
10. TypeScript strict, sin `any`
11. TanStack Query para async state + polling de órdenes (`refetchInterval`)
12. Zustand solo para auth state (token, user, roles, logout)
13. Axios con interceptor JWT en `src/api/client.ts`
14. Router guards por rol en `src/router/index.tsx`
15. Vite proxy a `http://localhost:8080` — sin CORS en dev
16. `pnpm install` desde `Frontend/` antes de `pnpm dev`

## Comandos

```bash
# Backend
cd {ms} && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd {ms} && ./mvnw compile
cd {ms} && ./mvnw test
docker compose -f docker-compose-local.yml up -d

# Frontend
cd Frontend && pnpm install
cd Frontend && pnpm dev
cd Frontend && pnpm build
```
