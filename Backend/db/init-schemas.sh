#!/bin/bash
# Crea schemas y usuarios PostgreSQL por microservicio.
# Local: requiere Backend/.env con las passwords (ver .env.example)
# Docker: las vars se inyectan desde Backend/.env via env_file en compose
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

# En Docker el .env no existe junto al script — vars ya inyectadas por compose
if [ -f "$ENV_FILE" ]; then
  set -a; source "$ENV_FILE"; set +a
fi

# SQL junto al script (local) o en /docker-schemas/ (Docker)
SQL_FILE="$SCRIPT_DIR/init-schemas.sql"
[ ! -f "$SQL_FILE" ] && SQL_FILE="/docker-schemas/init-schemas.sql"

psql -U "${POSTGRES_USER:-postgres}" -d "${POSTGRES_DB:-donatech}" \
  -v catalog_pass="$CATALOG_DB_PASS" \
  -v order_pass="$ORDER_DB_PASS" \
  -v users_pass="$USERS_DB_PASS" \
  -v supports_pass="$SUPPORTS_DB_PASS" \
  -v shipping_pass="$SHIPPING_DB_PASS" \
  -f "$SQL_FILE"

echo "Schemas y usuarios creados correctamente."
