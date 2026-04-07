#!/bin/bash
# =====================================================================
# Backup diario do PostgreSQL — VM GCP e2-micro
# =====================================================================
# Cron (configurado por setup-db-vm.sh):
#   0 3 * * * /opt/backups/backup-postgres.sh >> /opt/backups/backup.log 2>&1
# =====================================================================
set -euo pipefail

BACKUP_DIR="/opt/backups"
RETENTION_DAYS=7
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

# Carrega variaveis de ambiente (DB_USERNAME, POSTGRES_DB)
# shellcheck source=/dev/null
source /opt/db/.env

# Executa pg_dump dentro do container e comprime
docker exec rpg-postgres pg_dump -U "$DB_USERNAME" "$POSTGRES_DB" \
  | gzip > "$BACKUP_DIR/rpg_fichas_${TIMESTAMP}.sql.gz"

# Remove backups mais antigos que RETENTION_DAYS
find "$BACKUP_DIR" -name "rpg_fichas_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "[$(date)] Backup concluido: rpg_fichas_${TIMESTAMP}.sql.gz"
