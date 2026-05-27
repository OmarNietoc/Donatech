#!/bin/bash
# =============================================================
# Init script: postgres-users
# Se ejecuta UNA SOLA VEZ cuando el volumen está vacío.
# Crea el schema 'users' y el usuario dedicado.
# =============================================================
set -e

echo ">>> [postgres-users] Creando schema 'users' y usuario 'users_user'..."

DB_NAME="${POSTGRES_DB:-${POSTGRES_USER:-donatech}}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB_NAME" <<-EOSQL
    -- Crear schema
    CREATE SCHEMA IF NOT EXISTS users;

    -- Crear usuario si no existe
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'users_user') THEN
            CREATE USER users_user WITH PASSWORD '${USERS_DB_PASS:-users_pass}';
        END IF;
    END
    \$\$;

    -- Dar permisos sobre el schema
    GRANT USAGE, CREATE ON SCHEMA users TO users_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA users TO users_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA users TO users_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA users GRANT ALL ON TABLES TO users_user;
    ALTER DEFAULT PRIVILEGES IN SCHEMA users GRANT ALL ON SEQUENCES TO users_user;

    -- Setear search_path por defecto del usuario
    ALTER USER users_user SET search_path TO users;
EOSQL

echo ">>> [postgres-users] Schema e usuario creados correctamente."
