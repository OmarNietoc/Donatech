# Actualización del Proyecto Donatech

## Fecha: 30 de Abril 2026

---

## 1. Corrección de Healthcheck en Docker Compose

**Problema:** El healthcheck del `discovery-server` usaba `curl -f http://localhost:8761/actuator/health`, pero `curl` no está disponible en la imagen `eclipse-temurin:21-jre-alpine`. Esto impedía que el healthcheck se completara, dejando el servicio en estado `health: starting` indefinidamente. Como consecuencia, los microservicios dependientes nunca se levantaban porque `depends_on: condition: service_healthy` nunca se cumplía.

**Solución:** Se reemplazó el healthcheck por `wget` (que sí viene en Alpine):

```yaml
# Antes
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
  interval: 15s
  timeout: 5s
  retries: 10

# Después
healthcheck:
  test: ["CMD-SHELL", "wget -q --spider http://localhost:8761/actuator/health || exit 1"]
  interval: 15s
  timeout: 5s
  retries: 10
  start_period: 30s
```

**Archivos modificados:**
- `docker-compose-local.yml`
- `docker-compose-cloud.yml`

---

## 2. Corrección de Versión de springdoc-openapi

**Problema:** Todos los microservicios (auth, users, catalog, order, supports) tenían `springdoc-openapi-starter-webmvc-ui` en versión `2.8.9`, que requiere Spring Boot 3.4+. El proyecto usa Spring Boot `3.2.4`, lo que causaba un error de clase no encontrada al arrancar:

```
java.lang.ClassNotFoundException: org.springframework.web.servlet.resource.LiteWebJarsResourceResolver
```

Todos los 5 microservicios fallaban con exit code 1 inmediatamente después del arranque.

**Solución:** Se bajó la versión de springdoc-openapi a `2.3.0`, que es compatible con Spring Boot 3.2.x.

**Archivos modificados:**
- `auth/pom.xml` — `<version>2.8.9</version>` → `<version>2.3.0</version>`
- `users/pom.xml` — `<version>2.8.9</version>` → `<version>2.3.0</version>`
- `catalog/pom.xml` — `<version>2.8.9</version>` → `<version>2.3.0</version>`
- `order/pom.xml` — `<version>2.8.9</version>` → `<version>2.3.0</version>`
- `supports/pom.xml` — `<version>2.8.9</version>` → `<version>2.3.0</version>`

---

## 3. Corrección de Query en ProductRepository (Catalog Service)

**Problema:** El método `findByStockLessThanEqualStockMinimo()` en `ProductRepository` usaba una convención de nombres de Spring Data JPA inválida. Spring interpretaba `LessThanEqualStockMinimo` como una propiedad anidada dentro del campo `stock`, lanzando:

```
PropertyReferenceException: No property 'lessThanEqualStockMinimo' found for type 'Integer'; Traversed path: Product.stock
```

**Solución:** Se reemplazó el método derivado por una query explícita con `@Query`:

```java
// Antes
java.util.List<Product> findByStockLessThanEqualStockMinimo();

// Después
@Query("SELECT p FROM Product p WHERE p.stock <= p.stockMinimo")
List<Product> findLowStockProducts();
```

Se actualizó la llamada correspondiente en `ProductService.java`.

**Archivos modificados:**
- `catalog/src/main/java/com/donatech/catalog/repository/ProductRepository.java`
- `catalog/src/main/java/com/donatech/catalog/service/ProductService.java`

---

## Resultado

Después de estas correcciones, todos los 9 contenedores se levantan correctamente:

| Servicio | Estado | Puerto |
|---|---|---|
| PostgreSQL | ✅ healthy | 5432 |
| RabbitMQ | ✅ healthy | 5672 / 15672 |
| Discovery Server | ✅ healthy | 8761 |
| Auth Service | ✅ UP | 8084 |
| Users Service | ✅ UP | 8083 |
| Catalog Service | ✅ UP | 8081 |
| Order Service | ✅ UP | 8082 |
| Supports Service | ✅ UP | 8085 |
| API Gateway | ✅ UP | 8080 |

Todos los microservicios se registran correctamente en Eureka y el gateway enruta las peticiones como se espera.
