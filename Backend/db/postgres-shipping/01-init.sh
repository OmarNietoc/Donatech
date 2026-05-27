#!/bin/bash
# =============================================================
# Init script: postgres-shipping
# Se ejecuta UNA SOLA VEZ cuando el volumen está vacío.
# Crea el schema 'shipping' y el usuario dedicado.
# =============================================================
set -e

echo ">>> [postgres-shipping] Creando schema 'shipping' y usuario 'shipping_user'..."

DB_NAME="${POSTGRES_DB:-${POSTGRES_USER:-donatech}}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB_NAME" <<-EOSQL
    -- Crear schema
    CREATE SCHEMA IF NOT EXISTS shipping;

    -- Crear usuario si no existe
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'shipping_user') THEN
            CREATE USER shipping_user WITH PASSWORD '${SHIPPING_DB_PASS:-shipping_pass}';
        END IF;
    END
    \$\$;

    -- Dar permisos sobre el schema
    GRANT USAGE, CREATE ON SCHEMA shipping TO shipping_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA shipping TO shipping_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA shipping TO shipping_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA shipping GRANT ALL ON TABLES TO shipping_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA shipping GRANT ALL ON SEQUENCES TO shipping_user;

    -- Setear search_path por defecto del usuario
    ALTER USER shipping_user SET search_path TO shipping;
EOSQL

echo ">>> [postgres-shipping] Schema y usuario creados correctamente."
