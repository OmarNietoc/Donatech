#!/bin/sh
# ============================================================================
# restore.sh — restaura un dump a una BD. Si el archivo no está local, lo baja
# de Spaces (prefijo S3_PREFIX). Uso desde el contenedor:
#   docker exec donatech-db-backup restore.sh <host> <archivo.sql.gz> [db]
# Ej. cloud:  restore.sh postgres-catalog catalog-2026-06-26_0300.sql.gz
# Ej. local:  restore.sh donatech-postgres donatech-2026-06-26_0300.sql.gz
# ============================================================================
set -e

HOST="${1:-}"
FILE="${2:-}"
DB="${3:-${DB_NAME:-donatech}}"

if [ -z "$HOST" ] || [ -z "$FILE" ]; then
  echo "Uso: restore.sh <host> <ruta-o-nombre.sql.gz> [db]" >&2
  exit 1
fi

LOCAL="$FILE"
if [ ! -f "$LOCAL" ]; then
  echo "[restore] no existe local; descargando de spaces:${SPACES_BUCKET}/${S3_PREFIX}/$(basename "$FILE")"
  rclone copy "spaces:${SPACES_BUCKET}/${S3_PREFIX}/$(basename "$FILE")" /tmp/
  LOCAL="/tmp/$(basename "$FILE")"
fi

echo "[restore] restaurando ${LOCAL} -> ${HOST}/${DB}"
gunzip -c "$LOCAL" | PGPASSWORD="$POSTGRES_PASSWORD" psql -h "$HOST" -U "$POSTGRES_USER" -d "$DB"
echo "[restore] OK"
