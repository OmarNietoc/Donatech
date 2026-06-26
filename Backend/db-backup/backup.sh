#!/bin/sh
# ============================================================================
# backup.sh — pg_dump de cada target + offsite a DO Spaces (rclone).
#
# Env requeridas:
#   BACKUP_TARGETS   "label:host,label:host,..." (host = nombre de servicio docker)
#   POSTGRES_USER / POSTGRES_PASSWORD   superusuario (lee TODOS los schemas)
#   S3_PREFIX        "cloud" | "local" (carpeta en el bucket)
# Env opcionales:
#   DB_NAME (def. donatech) · RETENTION_DAYS (def. 7) · SPACES_BUCKET (si falta -> solo local)
#   RCLONE_CONFIG_SPACES_* (las exporta entrypoint.sh desde SPACES_*)
# ============================================================================
set -u

PREFIX="${S3_PREFIX:-default}"
DB_NAME="${DB_NAME:-donatech}"
RET="${RETENTION_DAYS:-7}"
DEST="/backups/${PREFIX}"
TS=$(date +%F_%H%M)
mkdir -p "$DEST"

ts() { date '+%Y-%m-%d %H:%M:%S'; }
rc=0

# --- Dump por target -------------------------------------------------------
OLD_IFS=$IFS
IFS=','
for target in $BACKUP_TARGETS; do
  IFS=$OLD_IFS
  label=${target%%:*}
  host=${target#*:}
  file="${DEST}/${label}-${TS}.sql.gz"
  echo "$(ts) [backup] dump ${label} @ ${host} -> ${file}"
  if PGPASSWORD="$POSTGRES_PASSWORD" pg_dump -h "$host" -U "$POSTGRES_USER" -d "$DB_NAME" \
       | gzip > "$file"; then
    echo "$(ts) [backup] OK ${label} ($(du -h "$file" | cut -f1))"
  else
    echo "$(ts) [backup] FALLO ${label}" >&2
    rm -f "$file"
    rc=1
  fi
  IFS=','
done
IFS=$OLD_IFS

# --- Poda local ------------------------------------------------------------
find "$DEST" -name '*.sql.gz' -mtime +"$RET" -delete 2>/dev/null || true

# --- Offsite a Spaces ------------------------------------------------------
if [ -n "${SPACES_BUCKET:-}" ]; then
  echo "$(ts) [backup] subiendo a spaces:${SPACES_BUCKET}/${PREFIX}"
  if rclone copy "$DEST" "spaces:${SPACES_BUCKET}/${PREFIX}" --s3-no-check-bucket; then
    rclone delete "spaces:${SPACES_BUCKET}/${PREFIX}" --min-age "${RET}d" 2>/dev/null || true
    echo "$(ts) [backup] offsite OK"
  else
    echo "$(ts) [backup] offsite FALLO (revisa SPACES_*/red)" >&2
    rc=1
  fi
else
  echo "$(ts) [backup] SPACES_BUCKET no configurado -> solo copia local" >&2
fi

echo "$(ts) [backup] fin (rc=$rc)"
exit $rc
