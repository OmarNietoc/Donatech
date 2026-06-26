#!/bin/sh
# ============================================================================
# entrypoint.sh — configura rclone (Spaces) por env, programa el cron y corre
# un backup inicial. busybox crond NO hereda el env del contenedor, así que se
# persiste a /etc/backup.env y el cron lo sourcea antes de cada ejecución.
# ============================================================================
set -e

# rclone remote "spaces" derivado de las SPACES_* (sin archivo de config).
export RCLONE_CONFIG_SPACES_TYPE=s3
export RCLONE_CONFIG_SPACES_PROVIDER=DigitalOcean
export RCLONE_CONFIG_SPACES_ACCESS_KEY_ID="${SPACES_KEY:-}"
export RCLONE_CONFIG_SPACES_SECRET_ACCESS_KEY="${SPACES_SECRET:-}"
export RCLONE_CONFIG_SPACES_ENDPOINT="${SPACES_ENDPOINT:-}"
export RCLONE_CONFIG_SPACES_ACL=private

# Persistir env relevante para el cron (single-quote para valores con espacios).
printenv | grep -E '^(POSTGRES_|SPACES_|S3_PREFIX|DB_NAME|BACKUP_TARGETS|RETENTION_DAYS|RCLONE_|TZ)=' \
  | while IFS='=' read -r k v; do printf "export %s='%s'\n" "$k" "$v"; done > /etc/backup.env

CRON="${BACKUP_CRON:-0 3 * * *}"
echo "${CRON} . /etc/backup.env; /usr/local/bin/backup.sh >> /proc/1/fd/1 2>&1" > /etc/crontabs/root

echo "[entrypoint] crontab programado:"; crontab -l
echo "[entrypoint] ejecutando backup inicial (valida config)..."
/usr/local/bin/backup.sh || echo "[entrypoint] backup inicial falló; el cron seguirá intentando"

echo "[entrypoint] arrancando crond (logs a stdout del contenedor)..."
exec crond -f -l 8
