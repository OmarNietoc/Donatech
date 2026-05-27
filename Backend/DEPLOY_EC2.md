# 🚀 Guía de Despliegue — Donatech Backend en EC2 (Ubuntu)

> **Estado actual asumido:**
> - ✅ EC2 Backend levantada (Ubuntu 22.04/24.04)
> - ✅ EC2 Frontend levantada (Ubuntu 22.04/24.04)
> - ✅ Repo del backend clonado en la EC2 backend
> - ✅ Repo del frontend clonado en la EC2 frontend
> - ✅ Docker instalado en ambas EC2
> - ❌ Docker Compose plugin **NO** instalado aún
> - ❌ Archivo `.env` **NO** creado aún
> - ❌ Contenedores **NO** levantados aún

---

## 📐 Arquitectura de las dos EC2

```
┌──────────────────────────────────────┐     ┌────────────────────────────┐
│         EC2 BACKEND                  │     │       EC2 FRONTEND          │
│  (esta guía)                         │     │                            │
│                                      │     │  React / Next.js           │
│  docker-compose-cloud.yml            │◄────│  apunta a IP_BACKEND:8080  │
│  ┌─────────────────────────────┐     │     │                            │
│  │  api-gateway        :8080   │◄────┼─────│  ← único puerto público    │
│  │  discovery-server   :8761   │     │     └────────────────────────────┘
│  │  auth-service       :8084   │     │
│  │  users-service      :8083   │     │
│  │  catalog-service    :8081   │     │
│  │  order-service      :8082   │     │
│  │  supports-service   :8085   │     │
│  │  notification-svc   :8086   │     │
│  │  shipping-service   :8087   │     │
│  │  rabbitmq           :5672   │     │
│  │  postgres-users     :5432   │     │
│  │  postgres-catalog   :5432   │     │
│  │  postgres-order     :5432   │     │
│  │  postgres-supports  :5432   │     │
│  │  postgres-shipping  :5432   │     │
│  └─────────────────────────────┘     │
│                                      │
│  ⚠️  Solo el puerto 8080 es público  │
│  Todo lo demás es red interna Docker │
└──────────────────────────────────────┘
```

> ⚠️ **Importante:** El frontend NO se conecta directamente a los microservicios.
> Solo se conecta a `http://IP_BACKEND:8080` (el API Gateway).

---

## 🔐 Paso 0 — Configurar Security Groups en AWS

Antes de seguir, verificá que los **Security Groups** de cada EC2 tengan las reglas correctas.

### EC2 Backend — Inbound Rules

| Puerto | Protocolo | Origen | Descripción |
|--------|-----------|--------|-------------|
| 22 | TCP | Tu IP | SSH para administración |
| 8080 | TCP | 0.0.0.0/0 | API Gateway → público para el frontend y usuarios |
| 8761 | TCP | Tu IP | Eureka Dashboard (solo para debugging) |
| 15672 | TCP | Tu IP | RabbitMQ Management UI (solo para debugging) |

> ❌ NO abrir los puertos 8081-8087 ni 5432 al público. Son red interna.

### EC2 Frontend — Inbound Rules

| Puerto | Protocolo | Origen | Descripción |
|--------|-----------|--------|-------------|
| 22 | TCP | Tu IP | SSH para administración |
| 80 | TCP | 0.0.0.0/0 | HTTP público |
| 443 | TCP | 0.0.0.0/0 | HTTPS público (si usás SSL) |
| 3000 | TCP | 0.0.0.0/0 | Puerto dev de Next.js/React (si no usás nginx) |

---

## 💻 EC2 BACKEND — Guía completa

### Paso 1 — Conectarse por SSH

```bash
# Desde tu máquina local (Windows: usar Git Bash o WSL)
ssh -i /ruta/a/tu-clave.pem ubuntu@<IP_PUBLICA_BACKEND>

# Ejemplo real:
ssh -i ~/.ssh/donatech-key.pem ubuntu@54.123.45.67
```

> Si dice "Permission denied (publickey)", verificá que el archivo `.pem` tenga permisos correctos:
> ```bash
> chmod 400 ~/.ssh/donatech-key.pem
> ```

### Paso 2 — Instalar Docker Compose plugin

Ya tenés Docker instalado. Solo falta el plugin de Compose:

```bash
# Actualizar paquetes
sudo apt-get update

# Instalar el plugin oficial de Docker Compose
sudo apt-get install -y docker-compose-plugin

# Verificar que funcionó (debe mostrar "Docker Compose version v2.x.x")
docker compose version
```

> Si el comando anterior no encuentra el paquete:
> ```bash
> # Método alternativo: descargar binario directamente
> sudo mkdir -p /usr/local/lib/docker/cli-plugins
> sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
>   -o /usr/local/lib/docker/cli-plugins/docker-compose
> sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
> docker compose version
> ```

### Paso 3 — Verificar que tu usuario puede usar Docker sin sudo

```bash
# Intentar correr docker sin sudo
docker ps
```

Si ves un error como `permission denied`, correr:

```bash
sudo usermod -aG docker $USER
# Luego cerrar sesión SSH y volver a entrar para que tome efecto
exit
# Reconectarse:
ssh -i ~/.ssh/donatech-key.pem ubuntu@<IP_PUBLICA_BACKEND>
# Verificar:
docker ps
```

### Paso 4 — Ir al directorio del Backend

```bash
# Ver dónde quedó clonado el repo
ls ~

# El repo del backend debería estar algo así:
cd ~/donatech/Backend
# o
cd ~/Donatech-main/Backend
# o como se llame la carpeta

# Verificar que estás en el lugar correcto (debe mostrar docker-compose-cloud.yml)
ls
```

Deberías ver algo como:
```
api-gateway/   auth/   catalog/   db/   discovery-server/
docker-compose-cloud.yml   docker-compose-local.yml
notification/   order/   shipping/   supports/   users/
.env.example   DEPLOY_EC2.md
```

### Paso 5 — Crear el archivo `.env`

Este archivo contiene todas las contraseñas y credenciales. **Nunca se sube al repositorio.**

```bash
# Copiar la plantilla
cp .env.example .env

# Abrirlo para editarlo
nano .env
```

Dentro del editor `nano`, vas a ver la plantilla. Modificá cada valor:

```env
# ── JWT ──────────────────────────────────────
# Clave secreta para firmar los tokens JWT (mínimo 64 caracteres)
# Generarla con: openssl rand -base64 64
JWT_SECRET=PEGAR_AQUI_LA_CLAVE_GENERADA
JWT_EXPIRATION_MS=86400000

# ── PostgreSQL (superusuario) ─────────────────
POSTGRES_USER=donatech
POSTGRES_PASSWORD=UnaContraseñaFuerte2024!
POSTGRES_DB=donatech

# ── Contraseñas por microservicio ─────────────
# Cada MS tiene su propio usuario de BD
USERS_DB_PASS=UsersPass2024!
CATALOG_DB_PASS=CatalogPass2024!
ORDER_DB_PASS=OrderPass2024!
SUPPORTS_DB_PASS=SupportsPass2024!
SHIPPING_DB_PASS=ShippingPass2024!

# ── RabbitMQ ──────────────────────────────────
RABBITMQ_DEFAULT_USER=donatech
RABBITMQ_DEFAULT_PASS=RabbitPass2024!
RABBITMQ_USER=donatech
RABBITMQ_PASS=RabbitPass2024!

# ── SMTP (para emails de notificación) ────────
# Para pruebas: crear cuenta gratis en https://mailtrap.io
SMTP_HOST=sandbox.smtp.mailtrap.io
SMTP_PORT=587
SMTP_USER=tu_usuario_mailtrap
SMTP_PASS=tu_password_mailtrap
MAIL_FROM=noreply@donatech.cl
```

**Para generar el JWT_SECRET:**
```bash
# Abrí otra terminal o ejecutá esto antes de abrir nano:
openssl rand -base64 64
# Copiá el resultado y pegalo en JWT_SECRET
```

**Guardar en nano:** `Ctrl+O` → Enter → `Ctrl+X`

**Verificar que se guardó bien:**
```bash
cat .env
```

### Paso 6 — Dar permisos de ejecución a los scripts de la BD

Los scripts que crean los schemas de PostgreSQL necesitan permisos de ejecución:

```bash
# Darles permisos a todos de una vez
chmod +x db/postgres-users/01-init.sh \
         db/postgres-catalog/01-init.sh \
         db/postgres-order/01-init.sh \
         db/postgres-supports/01-init.sh \
         db/postgres-shipping/01-init.sh

# Verificar que quedaron con permiso de ejecución (debe aparecer la 'x')
ls -la db/postgres-users/
# Esperado: -rwxr-xr-x ... 01-init.sh
```

### Paso 7 — Levantar todos los contenedores

```bash
# Levantar todo en segundo plano con --build para compilar los JARs
docker compose -f docker-compose-cloud.yml up -d --build
```

**¿Qué hace esto?**
1. Descarga las imágenes base (PostgreSQL, RabbitMQ, Java)
2. Compila cada microservicio con Maven dentro de un contenedor
3. Crea los contenedores y los inicia en orden

⏳ **La primera vez tarda entre 8 y 15 minutos** porque Maven descarga dependencias y compila el código Java. Sé paciente.

Vas a ver output como:
```
[+] Building 45.3s (12/12) FINISHED
[+] Running 15/15
 ✔ Container donatech-postgres-users    Started
 ✔ Container donatech-postgres-catalog  Started
 ✔ Container donatech-rabbitmq          Started
 ✔ Container donatech-discovery         Started
 ✔ Container donatech-auth              Started
 ✔ Container donatech-users             Started
 ...
```

### Paso 8 — Verificar que todo levantó correctamente

```bash
# Ver estado de TODOS los contenedores
docker compose -f docker-compose-cloud.yml ps
```

La salida debe verse así (todos en estado `running` o `healthy`):

```
NAME                        STATUS          PORTS
donatech-postgres-users     healthy         5432/tcp
donatech-postgres-catalog   healthy         5432/tcp
donatech-postgres-order     healthy         5432/tcp
donatech-postgres-supports  healthy         5432/tcp
donatech-postgres-shipping  healthy         5432/tcp
donatech-rabbitmq           healthy         5672/tcp, 15672/tcp
donatech-discovery          healthy         0.0.0.0:8761->8761/tcp
donatech-auth               running         0.0.0.0:8084->8084/tcp
donatech-users              running         0.0.0.0:8083->8083/tcp
donatech-catalog            running         0.0.0.0:8081->8081/tcp
donatech-order              running         0.0.0.0:8082->8082/tcp
donatech-supports           running         0.0.0.0:8085->8085/tcp
donatech-notification       running         0.0.0.0:8086->8086/tcp
donatech-shipping           running         0.0.0.0:8087->8087/tcp
donatech-gateway            running         0.0.0.0:8080->8080/tcp
```

> ⚠️ Si alguno aparece como `exited` o `restarting`, ir a la sección **Troubleshooting** más abajo.

### Paso 9 — Verificar que el API Gateway responde

```bash
# Desde adentro de la EC2
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{"status":"UP"}
```

```bash
# Desde tu navegador o desde tu máquina local
curl http://<IP_PUBLICA_BACKEND>:8080/actuator/health
```

Si responde `{"status":"UP"}`, **el backend está funcionando correctamente** 🎉

---

## 💻 EC2 FRONTEND — Guía completa

### Paso 1 — Conectarse por SSH

```bash
ssh -i ~/.ssh/donatech-key.pem ubuntu@<IP_PUBLICA_FRONTEND>
```

### Paso 2 — Instalar Docker Compose plugin (igual que el backend)

```bash
sudo apt-get update
sudo apt-get install -y docker-compose-plugin
docker compose version
```

### Paso 3 — Verificar que tu usuario puede usar Docker sin sudo

```bash
docker ps
# Si da error de permisos:
sudo usermod -aG docker $USER
exit
# Reconectarse por SSH y verificar de nuevo
```

### Paso 4 — Ir al directorio del frontend

```bash
ls ~
cd ~/donatech-frontend   # o como se llame el repo
ls
```

### Paso 5 — Configurar la URL del backend en el frontend

El frontend necesita saber la IP pública de la EC2 Backend para hacer las llamadas a la API.

Buscá en el código del frontend el archivo de configuración de la URL base. Puede estar en:
- `.env` o `.env.production`
- `src/config.js` o `src/api/index.js`

Debés setear algo como:
```env
VITE_API_URL=http://<IP_PUBLICA_BACKEND>:8080
# o
NEXT_PUBLIC_API_URL=http://<IP_PUBLICA_BACKEND>:8080
# o
REACT_APP_API_URL=http://<IP_PUBLICA_BACKEND>:8080
```

### Paso 6 — Levantar el frontend

Si tiene Dockerfile:
```bash
docker compose up -d --build
# o
docker build -t donatech-frontend .
docker run -d -p 3000:3000 donatech-frontend
```

Si es con npm directamente:
```bash
npm install
npm run build
npm start  # o npm run preview
```

---

## 🔍 Cómo verificar paso a paso que todo funciona

### Verificar los PostgreSQL (las 5 bases de datos)

```bash
# Entrar al postgres de users y ver las tablas
docker exec -it donatech-postgres-users psql -U donatech -d donatech

# Dentro de psql, ejecutar:
SET search_path TO users;
\dt
```

Debés ver las tablas: `app_users`, `role`, `regions`, `comunas`, `beneficiaries`, `company_details`, `zonas_catastrofe`

```sql
-- Verificar que los roles semilla existen
SELECT * FROM role;
```

Debe mostrar:
```
 id |       name
----+-------------------
  1 | ROLE_ADMIN
  2 | ROLE_DONANTE
  3 | ROLE_VOLUNTARIO
  4 | ROLE_BENEFICIARIO
  5 | ROLE_ORGANIZACION
```

Para salir de psql: `\q`

```bash
# Verificar catalog
docker exec -it donatech-postgres-catalog psql -U donatech -d donatech
# Dentro:
SET search_path TO catalog;
\dt
SELECT name FROM categories;
\q

# Verificar order
docker exec -it donatech-postgres-order psql -U donatech -d donatech
SET search_path TO orders;
\dt
SELECT * FROM coupons;
\q
```

### Verificar Eureka (Service Discovery)

Abrí en el navegador:
```
http://<IP_PUBLICA_BACKEND>:8761
```

Debés ver el panel de Eureka con todos los microservicios registrados:
- `AUTH-SERVICE`
- `USERS-SERVICE`
- `CATALOG-SERVICE`
- `ORDER-SERVICE`
- `SUPPORTS-SERVICE`
- `NOTIFICATION-SERVICE`
- `SHIPPING-SERVICE`

> ⚠️ Los servicios tardan ~30-60 segundos en aparecer en Eureka después de arrancar.

### Verificar RabbitMQ

Abrí en el navegador:
```
http://<IP_PUBLICA_BACKEND>:15672
```

Usuario y contraseña: los que pusiste en `RABBITMQ_DEFAULT_USER` y `RABBITMQ_DEFAULT_PASS` del `.env`.

Debés ver el dashboard de RabbitMQ.

### Verificar el API Gateway + un endpoint real

```bash
# Intentar login (debe devolver 401 si las credenciales son incorrectas, no 502 ni 404)
curl -X POST http://<IP_PUBLICA_BACKEND>:8080/auth/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test"}'
```

Si devuelve `{"message":"Credenciales incorrectas"}` o similar → el gateway está ruteando bien.
Si devuelve `502 Bad Gateway` → el microservicio de auth no está up todavía, esperar 30s y reintentar.

---

## 🔧 Troubleshooting — Problemas comunes

### ❌ Un contenedor está en estado `Restarting` o `Exited`

```bash
# Ver los logs del contenedor que falla (reemplazar por el nombre real)
docker compose -f docker-compose-cloud.yml logs users-service --tail=50

# También podés ver los logs en tiempo real
docker compose -f docker-compose-cloud.yml logs -f users-service
```

**Causa más común:** El `.env` tiene un valor incorrecto o falta una variable.

### ❌ "connection refused" o "could not connect to database"

El microservicio arrancó antes que el PostgreSQL estuviera listo.

```bash
# Ver si el postgres está healthy
docker compose -f docker-compose-cloud.yml ps postgres-users
# Si no dice "healthy", ver sus logs:
docker compose -f docker-compose-cloud.yml logs postgres-users

# Solución: reiniciar el servicio que falló
docker compose -f docker-compose-cloud.yml restart users-service
```

### ❌ "Schema 'users' does not exist" o "permission denied for schema"

El script `01-init.sh` no se ejecutó. Puede pasar si el volumen ya existía sin el schema.

```bash
# Verificar que los scripts tienen permisos de ejecución
ls -la db/postgres-users/

# Si no tienen la 'x':
chmod +x db/postgres-*/01-init.sh

# Solución: bajar el postgres problemático CON su volumen y volver a subirlo
docker compose -f docker-compose-cloud.yml stop postgres-users
docker compose -f docker-compose-cloud.yml rm -f postgres-users
docker volume rm donatech-backend-cloud_postgres-users-data   # ⚠️ esto borra los datos
docker compose -f docker-compose-cloud.yml up -d postgres-users
# Esperar que esté healthy y luego levantar el servicio
docker compose -f docker-compose-cloud.yml up -d users-service
```

> ⚠️ `docker volume rm` borra los datos de esa BD. Solo hacerlo si estás en primera configuración.

### ❌ El build tarda mucho o se queda colgado

En instancias t2.micro o con poca RAM, Maven puede quedarse sin memoria.

```bash
# Ver si hay procesos consumiendo RAM
free -h
htop  # (instalarlo con: sudo apt-get install -y htop)

# Agregar swap si la RAM es escasa (solo si tenés menos de 2GB RAM)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### ❌ El API Gateway devuelve 502 en todos los endpoints

El gateway está vivo pero los microservicios todavía no se registraron en Eureka.

```bash
# Esperar 2-3 minutos y verificar Eureka:
# http://<IP>:8761

# Si después de 5 minutos siguen sin aparecer, ver logs del servicio:
docker compose -f docker-compose-cloud.yml logs catalog-service --tail=30
```

### ❌ "No space left on device"

El disco de la EC2 está lleno (las imágenes Docker pesan bastante).

```bash
# Ver espacio en disco
df -h

# Limpiar imágenes no usadas
docker system prune -f

# Si sigue sin espacio, ampliar el EBS en AWS Console
```

---

## 📋 Comandos de referencia rápida

```bash
# ── Ver estado de todos los contenedores ──────────────────────
docker compose -f docker-compose-cloud.yml ps

# ── Ver logs de un servicio ───────────────────────────────────
docker compose -f docker-compose-cloud.yml logs -f <nombre-servicio> --tail=100
# Nombres: users-service, catalog-service, order-service,
#          supports-service, shipping-service, notification-service,
#          auth-service, api-gateway, discovery-server, rabbitmq
#          postgres-users, postgres-catalog, etc.

# ── Reiniciar un servicio (sin perder datos) ──────────────────
docker compose -f docker-compose-cloud.yml restart <nombre-servicio>

# ── Rebuild y redeploy de un servicio (nueva imagen) ──────────
docker compose -f docker-compose-cloud.yml up -d --build <nombre-servicio>

# ── Detener todo (datos se conservan en volúmenes) ────────────
docker compose -f docker-compose-cloud.yml down

# ── Levantar todo lo que está apagado (sin rebuild) ───────────
docker compose -f docker-compose-cloud.yml up -d

# ── Ver uso de recursos (CPU y RAM por contenedor) ────────────
docker stats

# ── Entrar a la shell de un contenedor ───────────────────────
docker exec -it donatech-users bash
# o
docker exec -it donatech-postgres-users psql -U donatech -d donatech
```

---

## 🗄️ Cómo funciona la persistencia de datos

```
┌─────────────────────────────────────────────────────────────┐
│  PRIMERA VEZ (volumen vacío)                                │
│                                                             │
│  1. PostgreSQL inicia                                       │
│     └─→ /docker-entrypoint-initdb.d/01-init.sh  ← SE EJECUTA│
│          └─→ CREATE SCHEMA users;                           │
│          └─→ CREATE USER users_user WITH PASSWORD '...';    │
│          └─→ GRANT USAGE ON SCHEMA users TO users_user;     │
│                                                             │
│  2. Spring Boot (users-service) inicia                      │
│     └─→ Hibernate lee las entidades @Entity                 │
│          └─→ CREATE TABLE users.app_users (...)  ← crea tablas│
│     └─→ Spring ejecuta data.sql                             │
│          └─→ INSERT INTO regions ... ON CONFLICT DO NOTHING │
│          └─→ INSERT INTO role ...   ON CONFLICT DO NOTHING  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  REINICIO / REBUILD (volumen CON datos)                     │
│                                                             │
│  1. PostgreSQL inicia                                       │
│     └─→ Volumen ya tiene datos → 01-init.sh NO SE EJECUTA  │
│                                                             │
│  2. Spring Boot inicia                                      │
│     └─→ Hibernate ve tablas existentes → solo hace UPDATE   │
│     └─→ data.sql se ejecuta                                 │
│          └─→ ON CONFLICT DO NOTHING → no inserta duplicados │
│                                                             │
│  ✅ DATOS INTACTOS                                          │
└─────────────────────────────────────────────────────────────┘
```

### ¿Cuándo se pierden los datos?

Solo en estos casos:
1. `docker compose down -v` (flag `-v` borra volúmenes explícitamente)
2. `docker volume rm <nombre-volumen>` (eliminación manual)
3. Se borra la instancia EC2 sin hacer snapshot del EBS

---

## 📦 Seed data incluido

Al primer inicio se insertan automáticamente:

| MS | Tabla | Registros |
|-----|-------|-----------|
| users | `regions` | 16 regiones de Chile |
| users | `comunas` | 62 comunas |
| users | `role` | 5 roles del sistema |
| catalog | `categories` | 7 categorías humanitarias |
| catalog | `units` | 7 unidades de medida |
| catalog | `products` | ~20 insumos humanitarios |
| order | `coupons` | 5 cupones de descuento |
| supports | `soportes` | 5 tickets de soporte |
| shipping | `routes` | 5 rutas de envío |

---

## 🔄 Workflow para actualizar el código (futuros deploys)

Cuando haya cambios en el código y necesites actualizar la EC2:

```bash
# 1. En la EC2 backend, ir al directorio
cd ~/donatech/Backend

# 2. Traer los cambios del repo
git pull origin main

# 3. Reconstruir y redeployar SOLO los servicios modificados
#    (los datos en los volúmenes no se tocan)
docker compose -f docker-compose-cloud.yml up -d --build users-service
docker compose -f docker-compose-cloud.yml up -d --build catalog-service
# ... o todos si cambiaron varios:
docker compose -f docker-compose-cloud.yml up -d --build
```
