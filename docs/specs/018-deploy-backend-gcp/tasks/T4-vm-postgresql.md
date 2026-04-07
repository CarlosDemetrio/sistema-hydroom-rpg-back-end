# T4 — VM e2-micro: Setup PostgreSQL + Swap + Backup + Hardening

> Fase: Infra/GCP | Prioridade: P0
> Dependencias: Nenhuma (paralelo com T1-T3)
> Bloqueia: T7 (validacao end-to-end)
> Estimativa: 1 dia

---

## Objetivo

Provisionar e configurar a VM e2-micro do GCP Free Tier para rodar **exclusivamente** PostgreSQL 16 via Docker. A VM tem apenas 1GB de RAM, entao swap de 2GB e obrigatorio. Inclui backup automatico diario, hardening basico e script de setup reproduzivel.

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `infra/gcp/setup-db-vm.sh` | Script de setup completo da VM (13 etapas) |
| `infra/gcp/docker-compose-db.yml` | Docker Compose apenas com PostgreSQL |
| `infra/gcp/.env.example` | Template de variaveis de ambiente |
| `infra/gcp/backup-postgres.sh` | Script de backup diario |

---

## Comandos GCP (execucao manual via Console ou gcloud CLI)

### 1. Criar VM

```bash
gcloud compute instances create rpg-db \
  --machine-type=e2-micro \
  --zone=us-central1-a \
  --image-family=ubuntu-2404-lts-amd64 \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --boot-disk-type=pd-standard \
  --tags=postgres-server \
  --metadata=startup-script='#!/bin/bash
    echo "VM criada. Execute setup-db-vm.sh manualmente."'
```

### 2. IP Estatico

```bash
gcloud compute addresses create rpg-db-ip --region=us-central1
IP=$(gcloud compute addresses describe rpg-db-ip --region=us-central1 --format='value(address)')
echo "IP Estatico: $IP"
```

### 3. Regra de Firewall

```bash
gcloud compute firewall-rules create allow-postgres \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:5432 \
  --target-tags=postgres-server \
  --source-ranges=0.0.0.0/0 \
  --description="PostgreSQL - acesso protegido por senha forte"
```

> **NOTA:** Idealmente restringir `source-ranges` aos IPs de saida do Cloud Run. Mas Cloud Run usa IPs dinamicos. Senha forte no PostgreSQL e o controle primario.

### 4. SSH na VM e executar setup

```bash
gcloud compute ssh rpg-db --zone=us-central1-a
# Na VM:
sudo bash setup-db-vm.sh
```

---

## setup-db-vm.sh — Etapas

1. **Atualizar sistema** (apt update + upgrade)
2. **Criar swap de 2GB** (obrigatorio — VM tem apenas 1GB)
3. **Instalar Docker CE**
4. **Criar usuario `deploy`** (sem sudo, apenas docker)
5. **Estrutura de diretorios** (`/opt/db`, `/opt/backups`)
6. **Docker Compose** (`docker-compose-db.yml` copiado para `/opt/db/`)
7. **Template .env** (se nao existir)
8. **Subir PostgreSQL**
9. **Backup automatico** (cron diario as 3h, retencao 7 dias)
10. **Hardening SSH** (root off, password off, key-only)
11. **Fail2Ban** (protecao brute-force SSH)
12. **UFW firewall** (permitir apenas 22 + 5432)
13. **Resumo final** com acoes manuais

---

## docker-compose-db.yml

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: rpg-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-rpg_fichas}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d ${POSTGRES_DB:-rpg_fichas}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    security_opt:
      - no-new-privileges:true
    deploy:
      resources:
        limits:
          memory: 384m

volumes:
  pgdata:
    driver: local
```

---

## .env.example

```env
# =====================================================================
# Template de variaveis - VM PostgreSQL (GCP e2-micro)
# =====================================================================
# Copie para /opt/db/.env e preencha os valores reais.

# Database
DB_USERNAME=rpg_prod_user
DB_PASSWORD=GERAR_SENHA_FORTE_AQUI
POSTGRES_DB=rpg_fichas
```

---

## backup-postgres.sh

```bash
#!/bin/bash
set -euo pipefail
BACKUP_DIR="/opt/backups"
RETENTION_DAYS=7
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
source /opt/db/.env
docker exec rpg-postgres pg_dump -U "$DB_USERNAME" "$POSTGRES_DB" \
  | gzip > "$BACKUP_DIR/rpg_fichas_${TIMESTAMP}.sql.gz"
find "$BACKUP_DIR" -name "rpg_fichas_*.sql.gz" -mtime +$RETENTION_DAYS -delete
echo "[$(date)] Backup: rpg_fichas_${TIMESTAMP}.sql.gz"
```

---

## Criterios de Aceitacao

- [ ] VM e2-micro criada em regiao dos EUA (us-central1, us-east1 ou us-west1)
- [ ] IP externo fixado como estatico
- [ ] Swap de 2GB ativo (`free -h` mostra swap)
- [ ] PostgreSQL rodando e acessivel remotamente: `psql -h <IP> -U rpg_prod_user -d rpg_fichas`
- [ ] Firewall permite porta 5432
- [ ] Backup diario configurado no cron
- [ ] SSH hardened (key-only, root off)
- [ ] `setup-db-vm.sh` e idempotente (rodar 2x nao quebra)

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
