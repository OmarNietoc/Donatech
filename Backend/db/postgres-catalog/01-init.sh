#!/bin/bash
# =============================================================
# Init script: postgres-catalog
# Se ejecuta UNA SOLA VEZ cuando el volumen está vacío.
# Crea el schema 'catalog' y el usuario dedicado.
# =============================================================
set -e

echo ">>> [postgres-catalog] Creando schema 'catalog' y usuario 'catalog_user'..."

DB_NAME="${POSTGRES_DB:-${POSTGRES_USER:-donatech}}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB_NAME" <<-EOSQL
    -- Crear schema
    CREATE SCHEMA IF NOT EXISTS catalog;

    -- Crear usuario si no existe
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'catalog_user') THEN
            CREATE USER catalog_user WITH PASSWORD '${CATALOG_DB_PASS:-catalog_pass}';
        END IF;
    END
    \$\$;

    -- Dar permisos sobre el schema
    GRANT USAGE, CREATE ON SCHEMA catalog TO catalog_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA catalog TO catalog_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA catalog TO catalog_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA catalog GRANT ALL ON TABLES TO catalog_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA catalog GRANT ALL ON SEQUENCES TO catalog_user;

    -- Setear search_path por defecto del usuario
    ALTER USER catalog_user SET search_path TO catalog;
EOSQL

echo ">>> [postgres-catalog] Schema y usuario creados correctamente."
