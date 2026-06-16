# Donatech · Kit IA Service

Microservicio de IA (FastAPI) que genera **kits de ayuda personalizados** de uso
único para afectados por catástrofes, mediante un chat conversacional asistido por
LLM y filtrado semántico de productos.

## Rol en el ecosistema

- **Lee** el catálogo de productos directamente de PostgreSQL (schema `catalog`, solo `SELECT`).
- **Escribe** kits llamando por HTTP al ms `catalog` (Spring Boot): crea el kit con
  `tipo: USO_UNICO` y lo vincula a la campaña (`campaign_kits`).
- Se registra en **Eureka** (`kit-ia-service`); el **API Gateway** lo expone en
  `/api/kit-ia/**` con JWT.

## Stack

Python 3.11 · FastAPI · DeepSeek/Ollama (cliente OpenAI) · sentence-transformers ·
scikit-learn · psycopg2 · httpx · Tenacity · py-eureka-client.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/kit-ia/sesion/iniciar` | Inicia chat, infiere contexto, primer mensaje |
| POST | `/api/kit-ia/sesion/mensaje` | Procesa mensaje del afectado |
| POST | `/api/kit-ia/kit/generar` | Genera kit editable desde el contexto |
| POST | `/api/kit-ia/kit/confirmar` | Crea el kit en catalog (saga + compensación) |
| DELETE | `/api/kit-ia/sesion/{id}` | Limpia la sesión |
| GET | `/api/kit-ia/health` | Estado de BD, embeddings y proveedor LLM |

## Desarrollo local

```bash
cp .env.example .env   # completar DB_PASSWORD, API_KEY, etc.
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
pytest
```

## Docker

Integrado en `Backend/docker-compose-local.yml` (servicios `kit-ia-service` + `ollama`).
Todas las variables provienen de `Backend/.env`. Levantar:

```bash
docker compose -f Backend/docker-compose-local.yml up -d --build kit-ia-service ollama
```

## Resiliencia

- Reintentos (Tenacity) en LLM y en llamadas a catalog.
- Fallback automático DeepSeek → Ollama.
- Saga compensatoria al confirmar: si falla el vínculo a campaña, se borra el kit.
- Sesiones efímeras en RAM con job de limpieza (>30 min).
- Acceso a BD de solo lectura.
