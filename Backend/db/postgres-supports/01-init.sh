#!/bin/bash
# =============================================================
# Init script: postgres-supports
# Se ejecuta UNA SOLA VEZ cuando el volumen está vacío.
# Crea el schema 'supports' y el usuario dedicado.
# =============================================================
set -e

echo ">>> [postgres-supports] Creando schema 'supports' y usuario 'supports_user'..."

DB_NAME="${POSTGRES_DB:-${POSTGRES_USER:-donatech}}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB_NAME" <<-EOSQL
    -- Crear schema
    CREATE SCHEMA IF NOT EXISTS supports;

    -- Crear usuario si no existe
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'supports_user') THEN
            CREATE USER supports_user WITH PASSWORD '${SUPPORTS_DB_PASS:-supports_pass}';
        END IF;
    END
    \$\$;

    -- Dar permisos sobre el schema
    GRANT USAGE, CREATE ON SCHEMA supports TO supports_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA supports TO supports_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA supports TO supports_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA supports GRANT ALL ON TABLES TO supports_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA supports GRANT ALL ON SEQUENCES TO supports_user;

    -- Setear search_path por defecto del usuario
    ALTER USER supports_user SET search_path TO supports;
EOSQL

echo ">>> [postgres-supports] Schema y usuario creados correctamente."
