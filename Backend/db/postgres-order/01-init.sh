#!/bin/bash
# =============================================================
# Init script: postgres-order
# Se ejecuta UNA SOLA VEZ cuando el volumen está vacío.
# Crea el schema 'orders' y el usuario dedicado.
# =============================================================
set -e

echo ">>> [postgres-order] Creando schema 'orders' y usuario 'order_user'..."

DB_NAME="${POSTGRES_DB:-${POSTGRES_USER:-donatech}}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB_NAME" <<-EOSQL
    -- Crear schema
    CREATE SCHEMA IF NOT EXISTS orders;

    -- Crear usuario si no existe
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'order_user') THEN
            CREATE USER order_user WITH PASSWORD '${ORDER_DB_PASS:-order_pass}';
        END IF;
    END
    \$\$;

    -- Dar permisos sobre el schema
    GRANT USAGE, CREATE ON SCHEMA orders TO order_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA orders TO order_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA orders TO order_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA orders GRANT ALL ON TABLES TO order_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA orders GRANT ALL ON SEQUENCES TO order_user;

    -- Setear search_path por defecto del usuario
    ALTER USER order_user SET search_path TO orders;
EOSQL

echo ">>> [postgres-order] Schema y usuario creados correctamente."
