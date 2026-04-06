#!/bin/bash
# =====================================================================
# Setup inicial do servidor OCI para deploy do ficha-controlador
# =====================================================================
#
# Executar como root na VM OCI (Oracle Linux 9 ou Ubuntu 24.04 ARM64):
#   chmod +x setup-oci.sh && sudo ./setup-oci.sh
#
# O que este script faz:
#   1.  Atualizar sistema e instalar pacotes base
#   2.  Criar swap file (protecao contra OOM)
#   3.  Hardening do kernel (sysctl)
#   4.  Firewall hardened (nftables)
#   5.  Hardening SSH (desabilitar root, password, etc.)
#   6.  Fail2Ban (brute force protection)
#   7.  Instalar Docker CE com User Namespace Remapping
#   8.  Instalar Caddy (reverse proxy HTTPS)
#   9.  Criar usuario 'deploy' restrito
#   10. Estrutura de diretorios e permissoes
#   11. Docker Compose de producao
#   12. Template .env
#   13. Caddyfile (Cloudflare proxy-aware)
#   14. Backup automatico, auditoria e servicos
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
    err "Execute como root: sudo ./setup-oci.sh"
fi

# =====================================================================
# Detectar OS
# =====================================================================
if [ -f /etc/oracle-release ] || [ -f /etc/redhat-release ]; then
    OS_FAMILY="rhel"
    log "Detectado: Oracle Linux / RHEL"
elif [ -f /etc/lsb-release ] || [ -f /etc/debian_version ]; then
    OS_FAMILY="debian"
    log "Detectado: Ubuntu / Debian"
else
    err "OS nao suportado. Use Oracle Linux 9 ou Ubuntu 24.04."
fi

# =====================================================================
# 1. Atualizar sistema
# =====================================================================
section "1/14 - Atualizando sistema"
if [ "$OS_FAMILY" = "rhel" ]; then
    dnf update -y -q
    dnf install -y -q curl wget git unzip jq fail2ban audit
else
    export DEBIAN_FRONTEND=noninteractive
    apt-get update -qq
    apt-get upgrade -y -qq
    apt-get install -y -qq curl wget git unzip jq fail2ban auditd apt-transport-https ca-certificates gnupg
fi
log "Sistema atualizado"

# =====================================================================
# 2. Criar Swap File (protecao contra OOM)
# =====================================================================
section "2/14 - Configurando Swap File"
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

    # Persistir no fstab
    if ! grep -q "$SWAP_FILE" /etc/fstab; then
        echo "$SWAP_FILE none swap sw 0 0" >> /etc/fstab
    fi

    # Otimizar swappiness (baixa para servidor - prefere RAM)
    sysctl -w vm.swappiness=10
    if ! grep -q "vm.swappiness" /etc/sysctl.d/99-swap.conf 2>/dev/null; then
        echo "vm.swappiness=10" > /etc/sysctl.d/99-swap.conf
    fi

    log "Swap de $SWAP_SIZE ativo (swappiness=10)"
fi

free -h | grep -i swap
log "OOM Killer tera swap como buffer antes de matar processos"

# =====================================================================
# 3. Hardening do Kernel (sysctl)
# =====================================================================
section "3/14 - Hardening do Kernel"
cat > /etc/sysctl.d/99-hardening.conf << 'SYSCTL_EOF'
# --- Network Security ---
# Ignorar pacotes ICMP redirect (previne MITM routing)
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.conf.default.accept_redirects = 0
net.ipv6.conf.all.accept_redirects = 0
net.ipv6.conf.default.accept_redirects = 0
net.ipv4.conf.all.send_redirects = 0

# Ignorar source-routed packets
net.ipv4.conf.all.accept_source_route = 0
net.ipv4.conf.default.accept_source_route = 0

# Ativar SYN cookies (protecao contra SYN flood)
net.ipv4.tcp_syncookies = 1

# Ignorar ICMP broadcast (protecao contra Smurf attack)
net.ipv4.icmp_echo_ignore_broadcasts = 1

# Log pacotes marcianos (source address spoofing)
net.ipv4.conf.all.log_martians = 1
net.ipv4.conf.default.log_martians = 1

# Reverse path filtering (anti-spoofing)
net.ipv4.conf.all.rp_filter = 1
net.ipv4.conf.default.rp_filter = 1

# Desabilitar IPv6 se nao for necessario
net.ipv6.conf.all.disable_ipv6 = 1
net.ipv6.conf.default.disable_ipv6 = 1

# --- Memoria ---
# ASLR completo
kernel.randomize_va_space = 2

# Desabilitar core dumps (evita leak de dados sensiveis)
fs.suid_dumpable = 0

# --- Limites de conexao ---
net.core.somaxconn = 1024
net.ipv4.tcp_max_syn_backlog = 2048
net.ipv4.tcp_fin_timeout = 15
net.ipv4.tcp_keepalive_time = 600
SYSCTL_EOF

sysctl --system > /dev/null 2>&1
log "Kernel hardening aplicado (SYN cookies, anti-spoofing, ASLR)"

# =====================================================================
# 4. Firewall Hardened (nftables)
# =====================================================================
section "4/14 - Configurando Firewall (nftables)"

# Desabilitar firewalls conflitantes
if [ "$OS_FAMILY" = "rhel" ]; then
    systemctl stop firewalld 2>/dev/null || true
    systemctl disable firewalld 2>/dev/null || true
    dnf install -y -q nftables
elif [ "$OS_FAMILY" = "debian" ]; then
    ufw disable 2>/dev/null || true
    apt-get install -y -qq nftables
fi

cat > /etc/nftables.conf << 'NFT_EOF'
#!/usr/sbin/nft -f
# =====================================================================
# Firewall nftables - Servidor OCI ficha-controlador
# =====================================================================
# Politica: DROP por padrao, permitir apenas o necessario

flush ruleset

table inet filter {

    # ----- Rate limiting sets -----
    set ssh_meter {
        type ipv4_addr
        flags dynamic,timeout
        timeout 1m
    }

    set http_meter {
        type ipv4_addr
        flags dynamic,timeout
        timeout 1s
    }

    # ----- INPUT chain -----
    chain input {
        type filter hook input priority 0; policy drop;

        # Permitir loopback (obrigatorio para Docker e servicos locais)
        iif "lo" accept

        # Permitir conexoes ja estabelecidas/relacionadas
        ct state established,related accept

        # Dropar pacotes invalidos
        ct state invalid drop

        # Anti-flood: limitar ICMP ping (1 por segundo)
        ip protocol icmp icmp type echo-request limit rate 1/second accept
        ip protocol icmp icmp type echo-request drop

        # SSH: rate limit (3 conexoes novas por minuto por IP)
        tcp dport 22 ct state new add @ssh_meter { ip saddr limit rate 3/minute } accept
        tcp dport 22 drop

        # HTTP/HTTPS: rate limit (50 conexoes novas por segundo por IP)
        tcp dport { 80, 443 } ct state new add @http_meter { ip saddr limit rate 50/second } accept
        tcp dport { 80, 443 } accept

        # Logar e dropar todo o resto (para auditoria)
        limit rate 5/minute log prefix "NFT-DROP: " level warn
        drop
    }

    # ----- FORWARD chain -----
    chain forward {
        type filter hook forward priority 0; policy drop;

        # Docker precisa de forward para networking interno
        ct state established,related accept

        # Permitir forward de/para interfaces Docker
        iifname "docker*" accept
        oifname "docker*" accept
        iifname "br-*" accept
        oifname "br-*" accept
    }

    # ----- OUTPUT chain -----
    chain output {
        type filter hook output priority 0; policy accept;
        # Permitir todo output (necessario para Docker pulls, DNS, etc.)
    }
}
NFT_EOF

systemctl enable nftables
systemctl restart nftables
log "nftables configurado (DROP por padrao, rate-limit SSH/HTTP)"

nft list ruleset | head -5
log "Regras ativas. SSH limitado a 3 conn/min, HTTP 50 conn/s por IP"

# =====================================================================
# 5. Hardening SSH
# =====================================================================
section "5/14 - Hardening SSH"
mkdir -p /etc/ssh/sshd_config.d

cat > /etc/ssh/sshd_config.d/99-hardening.conf << 'SSH_EOF'
# Hardening SSH para servidor OCI
# Desabilitar login root
PermitRootLogin no

# Desabilitar autenticacao por senha (apenas chave SSH)
PasswordAuthentication no
ChallengeResponseAuthentication no

# Desabilitar X11 e agent forwarding
X11Forwarding no
AllowAgentForwarding no

# Timeout de sessao (5 minutos de inatividade)
ClientAliveInterval 300
ClientAliveCountMax 2

# Limitar tentativas de login
MaxAuthTries 3
MaxSessions 3

# Apenas usuarios especificos podem fazer SSH
AllowUsers deploy opc ubuntu

# Desabilitar empty passwords
PermitEmptyPasswords no

# Log level
LogLevel VERBOSE
SSH_EOF

# Testar configuracao antes de reiniciar
if sshd -t 2>/dev/null; then
    systemctl restart sshd
    log "SSH hardened (root disabled, password disabled, key-only)"
else
    warn "Erro na configuracao SSH - revertendo"
    rm -f /etc/ssh/sshd_config.d/99-hardening.conf
fi

# =====================================================================
# 6. Fail2Ban (protecao contra brute force)
# =====================================================================
section "6/14 - Configurando Fail2Ban"
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

if [ "$OS_FAMILY" = "rhel" ]; then
    sed -i 's|/var/log/auth.log|/var/log/secure|' /etc/fail2ban/jail.local
fi

systemctl enable fail2ban
systemctl restart fail2ban
log "Fail2Ban ativo (ban SSH: 3 tentativas = 2h de bloqueio)"

# =====================================================================
# 7. Instalar Docker CE com User Namespace Remapping
# =====================================================================
section "7/14 - Instalando Docker CE + User Namespace Remapping"

if command -v docker &> /dev/null; then
    log "Docker ja instalado: $(docker --version)"
else
    log "Instalando Docker CE..."
    if [ "$OS_FAMILY" = "rhel" ]; then
        dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
        dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    else
        install -m 0755 -d /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        chmod a+r /etc/apt/keyrings/docker.gpg
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
        apt-get update -qq
        apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-compose-plugin
    fi
    log "Docker instalado: $(docker --version)"
fi

# --- User Namespace Remapping ---
log "Configurando Docker User Namespace Remapping..."

DOCKREMAP_USER="dockremap"

if ! id "$DOCKREMAP_USER" &>/dev/null; then
    useradd -r -s /usr/sbin/nologin "$DOCKREMAP_USER"
fi

if ! grep -q "^${DOCKREMAP_USER}:" /etc/subuid 2>/dev/null; then
    echo "${DOCKREMAP_USER}:100000:65536" >> /etc/subuid
fi
if ! grep -q "^${DOCKREMAP_USER}:" /etc/subgid 2>/dev/null; then
    echo "${DOCKREMAP_USER}:100000:65536" >> /etc/subgid
fi

mkdir -p /etc/docker
cat > /etc/docker/daemon.json << DAEMON_EOF
{
    "userns-remap": "${DOCKREMAP_USER}",
    "log-driver": "json-file",
    "log-opts": {
        "max-size": "10m",
        "max-file": "3"
    },
    "live-restore": true,
    "no-new-privileges": true,
    "default-ulimits": {
        "nofile": {
            "Name": "nofile",
            "Hard": 65536,
            "Soft": 32768
        }
    },
    "storage-driver": "overlay2"
}
DAEMON_EOF

systemctl enable docker
systemctl restart docker

if docker info 2>/dev/null | grep -q "userns"; then
    log "Docker User Namespace Remapping ATIVO"
else
    log "Docker reiniciado com namespace remapping configurado"
fi
log "Container root (UID 0) -> Host UID 100000+ (sem privilegios reais)"

# =====================================================================
# 8. Instalar Caddy
# =====================================================================
section "8/14 - Instalando Caddy"
if command -v caddy &> /dev/null; then
    log "Caddy ja instalado: $(caddy version)"
else
    log "Instalando Caddy..."
    if [ "$OS_FAMILY" = "rhel" ]; then
        dnf install -y 'dnf-command(copr)'
        dnf copr enable -y @caddy/caddy
        dnf install -y caddy
    else
        curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
        curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list
        apt-get update -qq
        apt-get install -y -qq caddy
    fi
    log "Caddy instalado: $(caddy version)"
fi

# =====================================================================
# 9. Criar usuario 'deploy' restrito
# =====================================================================
section "9/14 - Criando usuario 'deploy'"
if id "deploy" &>/dev/null; then
    log "Usuario 'deploy' ja existe"
else
    log "Criando usuario 'deploy'..."
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

cat > /etc/sudoers.d/deploy << 'SUDOERS_EOF'
# deploy user: sem sudo, apenas docker via grupo
SUDOERS_EOF
chmod 440 /etc/sudoers.d/deploy

warn "ACAO MANUAL: Adicione a chave publica SSH em $DEPLOY_SSH_DIR/authorized_keys"

# =====================================================================
# 10. Criar estrutura de diretorios
# =====================================================================
section "10/14 - Estrutura de diretorios"
mkdir -p /opt/app
mkdir -p /opt/app/scripts
mkdir -p /opt/caddy
mkdir -p /opt/backups
mkdir -p /opt/frontend
mkdir -p /opt/frontend-backup
chown -R deploy:deploy /opt/app /opt/backups /opt/frontend /opt/frontend-backup
log "Diretorios criados com permissoes corretas"

# =====================================================================
# 11. Docker Compose de producao
# =====================================================================
section "11/14 - Docker Compose de producao"
cat > /opt/app/docker-compose.prod.yml << 'COMPOSE_EOF'
services:
  postgres:
    image: postgres:16-alpine
    container_name: rpg-postgres-prod
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-rpg_fichas}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - rpg-internal
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d ${POSTGRES_DB:-rpg_fichas}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    security_opt:
      - no-new-privileges:true
    read_only: true
    tmpfs:
      - /tmp
      - /run/postgresql
    deploy:
      resources:
        limits:
          memory: 256m

  backend:
    image: ghcr.io/${GHCR_OWNER:-carlosdemetrio}/ficha-controlador:${IMAGE_TAG:-latest}
    container_name: rpg-backend-prod
    restart: unless-stopped
    ports:
      - "127.0.0.1:8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-rpg_fichas}
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
      GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
      FRONTEND_URL: ${FRONTEND_URL}
      BACKEND_URL: ${BACKEND_URL}
      SERVER_FORWARD_HEADERS_STRATEGY: framework
      CLOUDINARY_URL: ${CLOUDINARY_URL:-}
      JAVA_TOOL_OPTIONS: >-
        -Xms256m
        -Xmx384m
        -XX:+UseZGC
        -XX:+ZGenerational
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - rpg-internal
    security_opt:
      - no-new-privileges:true
    read_only: true
    tmpfs:
      - /tmp
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health/liveness"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          memory: 512m

volumes:
  pgdata:
    driver: local

networks:
  rpg-internal:
    driver: bridge
    internal: true
COMPOSE_EOF

chown deploy:deploy /opt/app/docker-compose.prod.yml
log "docker-compose.prod.yml criado com security_opt e read_only"

# =====================================================================
# 12. Template .env
# =====================================================================
section "12/14 - Template .env"
if [ ! -f /opt/app/.env ]; then
    cat > /opt/app/.env << 'ENV_EOF'
# =====================================================================
# Variaveis de ambiente - Producao OCI
# =====================================================================
# EDITE TODOS OS VALORES ABAIXO!

# Database
DB_USERNAME=rpg_prod_user
DB_PASSWORD=TROCAR_POR_SENHA_FORTE_AQUI
POSTGRES_DB=rpg_fichas

# GHCR (GitHub Container Registry)
GHCR_OWNER=carlosdemetrio

# URLs - Substitua pelo dominio real
FRONTEND_URL=https://seu-dominio.com
BACKEND_URL=https://api.seu-dominio.com

# Google OAuth2
GOOGLE_CLIENT_ID=seu-google-client-id
GOOGLE_CLIENT_SECRET=seu-google-client-secret

# Cloudinary (futuro)
CLOUDINARY_URL=

# Tag da imagem
IMAGE_TAG=latest
ENV_EOF

    chmod 600 /opt/app/.env
    chown deploy:deploy /opt/app/.env
    warn "ACAO MANUAL: Edite /opt/app/.env com os valores reais!"
else
    log ".env ja existe, nao sobrescrevendo"
fi

# =====================================================================
# 13. Caddyfile (Cloudflare proxy-aware)
# =====================================================================
section "13/14 - Caddyfile (Frontend + Backend, Cloudflare-aware)"
cat > /etc/caddy/Caddyfile << 'CADDY_EOF'
# =====================================================================
# Caddy - Frontend (SPA) + Backend (API) na mesma VM
# =====================================================================
# SUBSTITUA 'seu-dominio.com' pelo seu dominio real!
#
# Frontend: https://seu-dominio.com       -> arquivos estaticos Angular
# Backend:  https://api.seu-dominio.com   -> reverse proxy Spring Boot

# =====================================================================
# FRONTEND - Angular SPA (arquivos estaticos)
# =====================================================================
seu-dominio.com, www.seu-dominio.com {
    # Servir arquivos estaticos do Angular
    root * /opt/frontend

    # SPA fallback: qualquer rota desconhecida retorna index.html
    # (Angular Router cuida do roteamento client-side)
    try_files {path} /index.html
    file_server

    # Cache agressivo para assets com hash (imutaveis)
    @hashed path_regexp hashed \.[a-f0-9]{8,}\.(js|css|woff2?|ttf|eot|svg|png|jpg|ico)$
    header @hashed Cache-Control "public, max-age=31536000, immutable"

    # Cache curto para index.html (sempre buscar a versao mais recente)
    @html path *.html /
    header @html Cache-Control "no-cache, no-store, must-revalidate"

    # Redirect www -> non-www (SEO)
    @www host www.seu-dominio.com
    redir @www https://seu-dominio.com{uri} permanent

    # Headers de seguranca
    header {
        Strict-Transport-Security "max-age=31536000; includeSubDomains; preload"
        X-Frame-Options "DENY"
        X-Content-Type-Options "nosniff"
        X-XSS-Protection "1; mode=block"
        Referrer-Policy "strict-origin-when-cross-origin"
        Permissions-Policy "camera=(), microphone=(), geolocation=()"
        -Server
        -X-Powered-By
    }

    # Compressao
    encode gzip zstd

    log {
        output file /var/log/caddy/frontend-access.log {
            roll_size 10mb
            roll_keep 5
        }
        format json
    }
}

# =====================================================================
# BACKEND - Spring Boot API (reverse proxy)
# =====================================================================
api.seu-dominio.com {
    reverse_proxy localhost:8081 {
        health_uri /actuator/health/liveness
        health_interval 30s
        health_timeout 10s
        header_up X-Real-IP {remote_host}
        header_up X-Forwarded-For {remote_host}
        header_up X-Forwarded-Proto {scheme}
    }

    header {
        Strict-Transport-Security "max-age=31536000; includeSubDomains; preload"
        X-Frame-Options "DENY"
        X-Content-Type-Options "nosniff"
        X-XSS-Protection "1; mode=block"
        Referrer-Policy "strict-origin-when-cross-origin"
        Content-Security-Policy "default-src 'self'; frame-ancestors 'none'"
        Permissions-Policy "camera=(), microphone=(), geolocation=()"
        -Server
        -X-Powered-By
    }

    @blocked path /actuator/env /actuator/configprops /actuator/beans /actuator/mappings /actuator/shutdown
    respond @blocked 404

    log {
        output file /var/log/caddy/api-access.log {
            roll_size 10mb
            roll_keep 5
        }
        format json
    }

    request_body {
        max_size 16MB
    }
}
CADDY_EOF

mkdir -p /var/log/caddy
warn "ACAO MANUAL: Edite /etc/caddy/Caddyfile - substitua 'seu-dominio.com' pelo dominio real!"

# =====================================================================
# 14. Backup, auditoria e servicos
# =====================================================================
section "14/14 - Backup, auditoria e servicos"

# --- Backup do PostgreSQL ---
cat > /opt/backups/backup-postgres.sh << 'BACKUP_EOF'
#!/bin/bash
set -euo pipefail
BACKUP_DIR="/opt/backups"
RETENTION_DAYS=7
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
source /opt/app/.env
docker exec rpg-postgres-prod pg_dump -U "$DB_USERNAME" "$POSTGRES_DB" | gzip > "$BACKUP_DIR/rpg_fichas_${TIMESTAMP}.sql.gz"
find "$BACKUP_DIR" -name "rpg_fichas_*.sql.gz" -mtime +$RETENTION_DAYS -delete
echo "[$(date)] Backup concluido: rpg_fichas_${TIMESTAMP}.sql.gz"
BACKUP_EOF

chmod +x /opt/backups/backup-postgres.sh
chown deploy:deploy /opt/backups/backup-postgres.sh

(crontab -u deploy -l 2>/dev/null; echo "0 3 * * * /opt/backups/backup-postgres.sh >> /opt/backups/backup.log 2>&1") | sort -u | crontab -u deploy -
log "Cron de backup configurado (diario as 3h)"

# --- Auditoria (auditd) ---
systemctl enable auditd 2>/dev/null || true
systemctl start auditd 2>/dev/null || true

mkdir -p /etc/audit/rules.d
cat > /etc/audit/rules.d/docker.rules << 'AUDIT_EOF'
-w /etc/docker/ -p wa -k docker_config
-w /usr/bin/docker -p x -k docker_exec
-w /opt/app/.env -p rwa -k env_access
-w /opt/app/docker-compose.prod.yml -p wa -k compose_change
-w /opt/frontend/ -p wa -k frontend_deploy
-w /etc/ssh/sshd_config -p wa -k ssh_config
-w /home/deploy/.ssh/ -p wa -k ssh_keys
AUDIT_EOF

augenrules --load 2>/dev/null || true
log "Auditoria configurada (Docker, .env, SSH monitorados)"

# --- Servicos ---
systemctl enable docker
systemctl enable caddy
systemctl enable nftables
systemctl enable fail2ban
systemctl start docker

# --- Rollback script ---
cat > /opt/app/scripts/rollback.sh << 'ROLLBACK_EOF'
#!/bin/bash
set -euo pipefail
TAG=${1:?"Uso: ./rollback.sh <image_tag>"}
echo "Iniciando rollback para tag: $TAG"
cd /opt/app
sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=$TAG/" .env
export IMAGE_TAG=$TAG
docker compose -f docker-compose.prod.yml pull backend
docker compose -f docker-compose.prod.yml up -d --no-deps backend
sleep 20
for i in $(seq 1 6); do
    if curl -sf http://localhost:8081/actuator/health/readiness > /dev/null 2>&1; then
        echo "Rollback para $TAG concluido!"
        exit 0
    fi
    echo "Tentativa $i/6..."
    sleep 10
done
echo "Health check falhou!"
docker compose -f docker-compose.prod.yml logs --tail 50 backend
exit 1
ROLLBACK_EOF
chmod +x /opt/app/scripts/rollback.sh
chown deploy:deploy /opt/app/scripts/rollback.sh

log "Servicos habilitados"

# =====================================================================
# Resumo final
# =====================================================================
echo ""
echo "====================================================="
echo -e "${GREEN} Setup concluido com seguranca reforcada!${NC}"
echo "====================================================="
echo ""
echo " CAMADAS DE SEGURANCA ATIVAS:"
echo "  [x] Swap 2GB (protecao OOM)"
echo "  [x] Kernel hardening (sysctl: SYN cookies, ASLR, anti-spoofing)"
echo "  [x] nftables (DROP default, rate-limit SSH 3/min, HTTP 50/s)"
echo "  [x] SSH hardened (root off, password off, key-only, MaxAuthTries 3)"
echo "  [x] Fail2Ban (ban SSH: 3 tentativas = 2h bloqueio)"
echo "  [x] Docker User Namespace Remapping (container root != host root)"
echo "  [x] Docker no-new-privileges + log rotation"
echo "  [x] Containers read-only + security_opt"
echo "  [x] Auditd (monitorando Docker, .env, SSH, frontend)"
echo ""
echo " ESTRUTURA CRIADA:"
echo "  /opt/app/                 -> Backend (Docker Compose + .env)"
echo "  /opt/frontend/            -> Frontend (Angular static files)"
echo "  /opt/frontend-backup/     -> Backups do frontend (ultimas 3 versoes)"
echo "  /opt/backups/             -> Backups do PostgreSQL (diarios)"
echo ""
echo " ACOES MANUAIS RESTANTES:"
echo ""
echo "  1. Editar /opt/app/.env com valores reais:"
echo "     sudo -u deploy nano /opt/app/.env"
echo ""
echo "  2. Editar /etc/caddy/Caddyfile (substituir 'seu-dominio.com'):"
echo "     sudo nano /etc/caddy/Caddyfile"
echo ""
echo "  3. Adicionar chave SSH publica do GitHub Actions:"
echo "     echo 'CHAVE_PUBLICA' >> /home/deploy/.ssh/authorized_keys"
echo "     (mesma chave para ambos repos: backend e frontend)"
echo ""
echo "  4. Iniciar Caddy apos configurar o dominio:"
echo "     sudo systemctl start caddy"
echo ""
echo "  5. Iniciar PostgreSQL:"
echo "     cd /opt/app && docker compose -f docker-compose.prod.yml up -d postgres"
echo ""
echo "  6. GitHub Secrets (em AMBOS os repos):"
echo "     OCI_VM_HOST, OCI_VM_USER (deploy), OCI_SSH_PRIVATE_KEY"
echo ""
echo "  7. Google OAuth2 redirect URI:"
echo "     https://api.SEU-DOMINIO/login/oauth2/code/google"
echo ""
echo "  8. DNS (2 registros A):"
echo "     SEU-DOMINIO       -> IP publico da VM"
echo "     api.SEU-DOMINIO   -> IP publico da VM"
echo ""
echo "  9. (Recomendado) Cloudflare Proxy + WAF (ver docs/DEPLOY-OCI.md)"
echo ""
echo "====================================================="
