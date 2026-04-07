#!/bin/bash
# =====================================================================
# Setup da VM e2-micro GCP para PostgreSQL dedicado
# =====================================================================
#
# Executar como root na VM GCP (Ubuntu 24.04 LTS):
#   chmod +x setup-db-vm.sh && sudo ./setup-db-vm.sh
#
# O que este script faz:
#   1.  Atualizar sistema e instalar pacotes base
#   2.  Criar swap de 2GB (VM tem apenas 1GB — OBRIGATORIO)
#   3.  Instalar Docker CE
#   4.  Criar usuario 'deploy' restrito
#   5.  Estrutura de diretorios e permissoes
#   6.  Docker Compose (apenas PostgreSQL)
#   7.  Template .env
#   8.  Backup automatico (cron diario)
#   9.  Hardening SSH
#   10. Fail2Ban (brute force protection)
#   11. UFW firewall (22 + 5432)
#   12. Resumo final
#
set -euo pipefail

# =====================================================================
# Cores para output
# =====================================================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log()     { echo -e "${GREEN}[OK]${NC} $1"; }
warn()    { echo -e "${YELLOW}[!!]${NC} $1"; }
err()     { echo -e "${RED}[XX]${NC} $1"; exit 1; }
section() { echo -e "\n${CYAN}=== $1 ===${NC}"; }

# =====================================================================
# Pre-checks
# =====================================================================
if [ "$(id -u)" -ne 0 ]; then
    err "Execute como root: sudo ./setup-db-vm.sh"
fi

# =====================================================================
# 1. Atualizar sistema
# =====================================================================
section "1/12 - Atualizando sistema"
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get upgrade -y -qq
apt-get install -y -qq curl wget git unzip jq fail2ban apt-transport-https ca-certificates gnupg ufw
log "Sistema atualizado"

# =====================================================================
# 2. Criar Swap de 2GB (OBRIGATORIO — VM tem apenas 1GB RAM)
# =====================================================================
section "2/12 - Configurando Swap de 2GB"
SWAP_FILE="/swapfile"
SWAP_SIZE="2G"

if swapon --show | grep -q "$SWAP_FILE"; then
    log "Swap ja ativo: $(swapon --show)"
else
    log "Criando swap de $SWAP_SIZE..."
    fallocate -l $SWAP_SIZE $SWAP_FILE
    chmod 600 $SWAP_FILE
    mkswap $SWAP_FILE
    swapon $SWAP_FILE

    if ! grep -q "$SWAP_FILE" /etc/fstab; then
        echo "$SWAP_FILE none swap sw 0 0" >> /etc/fstab
    fi

    sysctl -w vm.swappiness=10
    echo "vm.swappiness=10" > /etc/sysctl.d/99-swap.conf

    log "Swap de $SWAP_SIZE ativo (swappiness=10)"
fi

free -h | grep -i swap
log "OOM Killer tera swap como buffer antes de matar processos"

# =====================================================================
# 3. Instalar Docker CE
# =====================================================================
section "3/12 - Instalando Docker CE"

if command -v docker &> /dev/null; then
    log "Docker ja instalado: $(docker --version)"
else
    log "Instalando Docker CE..."
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    apt-get update -qq
    apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-compose-plugin
    log "Docker instalado: $(docker --version)"
fi

mkdir -p /etc/docker
cat > /etc/docker/daemon.json << 'DAEMON_EOF'
{
    "log-driver": "json-file",
    "log-opts": {
        "max-size": "10m",
        "max-file": "3"
    },
    "live-restore": true,
    "storage-driver": "overlay2"
}
DAEMON_EOF

systemctl enable docker
systemctl restart docker
log "Docker configurado com log rotation"

# =====================================================================
# 4. Criar usuario 'deploy' restrito
# =====================================================================
section "4/12 - Criando usuario 'deploy'"
if id "deploy" &>/dev/null; then
    log "Usuario 'deploy' ja existe"
else
    useradd -m -s /bin/bash deploy
    log "Usuario 'deploy' criado"
fi

usermod -aG docker deploy

DEPLOY_SSH_DIR="/home/deploy/.ssh"
mkdir -p "$DEPLOY_SSH_DIR"
if [ ! -f "$DEPLOY_SSH_DIR/authorized_keys" ]; then
    touch "$DEPLOY_SSH_DIR/authorized_keys"
fi
chmod 700 "$DEPLOY_SSH_DIR"
chmod 600 "$DEPLOY_SSH_DIR/authorized_keys"
chown -R deploy:deploy "$DEPLOY_SSH_DIR"
log "Usuario 'deploy' pronto (grupo docker)"

# =====================================================================
# 5. Estrutura de diretorios
# =====================================================================
section "5/12 - Estrutura de diretorios"
mkdir -p /opt/db
mkdir -p /opt/backups
chown -R deploy:deploy /opt/db /opt/backups
log "Diretorios /opt/db e /opt/backups criados"

# =====================================================================
# 6. Docker Compose (apenas PostgreSQL)
# =====================================================================
section "6/12 - Docker Compose (PostgreSQL)"
cat > /opt/db/docker-compose.yml << 'COMPOSE_EOF'
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
COMPOSE_EOF

chown deploy:deploy /opt/db/docker-compose.yml
log "docker-compose.yml criado (apenas PostgreSQL)"

# =====================================================================
# 7. Template .env
# =====================================================================
section "7/12 - Template .env"
if [ ! -f /opt/db/.env ]; then
    cat > /opt/db/.env << 'ENV_EOF'
# =====================================================================
# Variaveis de ambiente - VM PostgreSQL (GCP e2-micro)
# =====================================================================
# EDITE TODOS OS VALORES ABAIXO!

# Database
DB_USERNAME=rpg_prod_user
DB_PASSWORD=TROCAR_POR_SENHA_FORTE_AQUI
POSTGRES_DB=rpg_fichas
ENV_EOF

    chmod 600 /opt/db/.env
    chown deploy:deploy /opt/db/.env
    warn "ACAO MANUAL: Edite /opt/db/.env com a senha real!"
else
    log ".env ja existe, nao sobrescrevendo"
fi

# =====================================================================
# 8. Backup automatico
# =====================================================================
section "8/12 - Backup automatico"

cat > /opt/backups/backup-postgres.sh << 'BACKUP_EOF'
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
BACKUP_EOF

chmod +x /opt/backups/backup-postgres.sh
chown deploy:deploy /opt/backups/backup-postgres.sh

(crontab -u deploy -l 2>/dev/null; echo "0 3 * * * /opt/backups/backup-postgres.sh >> /opt/backups/backup.log 2>&1") | sort -u | crontab -u deploy -
log "Cron de backup configurado (diario as 3h, retencao 7 dias)"

# =====================================================================
# 9. Hardening SSH
# =====================================================================
section "9/12 - Hardening SSH"
mkdir -p /etc/ssh/sshd_config.d

cat > /etc/ssh/sshd_config.d/99-hardening.conf << 'SSH_EOF'
PermitRootLogin no
PasswordAuthentication no
ChallengeResponseAuthentication no
X11Forwarding no
AllowAgentForwarding no
ClientAliveInterval 300
ClientAliveCountMax 2
MaxAuthTries 3
MaxSessions 3
PermitEmptyPasswords no
LogLevel VERBOSE
SSH_EOF

if sshd -t 2>/dev/null; then
    systemctl restart sshd
    log "SSH hardened (root disabled, password disabled, key-only)"
else
    warn "Erro na configuracao SSH - revertendo"
    rm -f /etc/ssh/sshd_config.d/99-hardening.conf
fi

# =====================================================================
# 10. Fail2Ban
# =====================================================================
section "10/12 - Fail2Ban"
cat > /etc/fail2ban/jail.local << 'F2B_EOF'
[DEFAULT]
bantime  = 3600
findtime = 600
maxretry = 3
backend  = systemd

[sshd]
enabled = true
port    = ssh
filter  = sshd
logpath = /var/log/auth.log
maxretry = 3
bantime  = 7200
F2B_EOF

systemctl enable fail2ban
systemctl restart fail2ban
log "Fail2Ban ativo (ban SSH: 3 tentativas = 2h bloqueio)"

# =====================================================================
# 11. UFW Firewall (Defense in Depth — segunda camada apos firewall GCP)
# =====================================================================
section "11/12 - UFW Firewall"
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp comment "SSH (GCP firewall ja restringe por IP)"
# PostgreSQL APENAS da rede interna VPC (Cloud Run via Direct VPC Egress)
ufw allow from 10.128.0.0/20 to any port 5432 comment "PostgreSQL - VPC interna APENAS"
ufw --force enable
log "UFW ativo: SSH (22) + PostgreSQL (5432 — APENAS rede VPC interna 10.128.0.0/20)"
warn "PostgreSQL NAO esta acessivel da internet publica (defense in depth)"

# =====================================================================
# 12. Resumo final
# =====================================================================
section "12/12 - Resumo"
echo ""
echo "====================================================="
echo -e "${GREEN} Setup concluido!${NC}"
echo "====================================================="
echo ""
echo " ESTRUTURA CRIADA:"
echo "  /opt/db/                  -> PostgreSQL (Docker Compose + .env)"
echo "  /opt/backups/             -> Backups diarios (retencao 7 dias)"
echo ""
echo " SEGURANCA ATIVA:"
echo "  [x] Swap 2GB (protecao OOM)"
echo "  [x] SSH hardened (root off, password off)"
echo "  [x] Fail2Ban (ban SSH: 3 falhas = 2h)"
echo "  [x] UFW (SSH: 22, PostgreSQL: 5432 APENAS VPC interna)"
echo "  [x] Docker com log rotation"
echo "  [x] PostgreSQL INVISIVEL para internet (acesso apenas via VPC)"
echo ""
echo " ACOES MANUAIS:"
echo ""
echo "  1. Editar /opt/db/.env com a senha real:"
echo "     sudo -u deploy nano /opt/db/.env"
echo ""
echo "  2. Subir PostgreSQL:"
echo "     cd /opt/db && sudo -u deploy docker compose up -d"
echo ""
echo "  3. Verificar:"
echo "     docker exec rpg-postgres pg_isready -U rpg_prod_user"
echo ""
echo "  4. Adicionar chave SSH (se necessario):"
echo "     echo 'CHAVE_PUBLICA' >> /home/deploy/.ssh/authorized_keys"
echo ""
echo "====================================================="
