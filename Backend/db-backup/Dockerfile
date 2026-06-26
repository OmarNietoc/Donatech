# Sidecar de backup: pg_dump (16) + rclone (offsite a DO Spaces) + cron (busybox).
# pg_dump 16 coincide con la versión de los servidores Postgres del stack.
FROM postgres:16-alpine

RUN apk add --no-cache rclone tzdata

COPY backup.sh restore.sh entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/backup.sh /usr/local/bin/restore.sh /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
