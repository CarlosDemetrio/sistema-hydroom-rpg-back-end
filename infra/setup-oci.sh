#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# 🚀 Setup inicial do servidor OCI para deploy do ficha-controlador
# ═══════════════════════════════════════════════════════════════════
#
# Executar como root na VM OCI (Oracle Linux 9 ou Ubuntu 24.04 ARM64):
#   chmod +x setup-oci.sh && sudo ./setup-oci.sh
#
# O que este script faz:
#   1. Instala Docker CE + Docker Compose plugin
#   2. Instala Caddy (reverse proxy com HTTPS automático)
#   3. Cria usuário 'deploy' para GitHub Actions
#   4. Configura estrutura de diretórios
#   5. Configura firewall
#   6. Configura backup automático do PostgreSQL
#
set -euo pipefail

# ═══════════════════════════════════════════════════
# Cores para output
# ═══════════════════════════════════════════════════
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
err()  { echo -e "${RED}[✗]${NC} $1"; exit 1; }

# ═══════════════════════════════════════════════════
# Detectar OS
# ═══════════════════════════════════════════════════
if [ -f /etc/oracle-release ] || [ -f /etc/redhat-release ]; then
    OS_FAMILY="rhel"
    log "Detectado: Oracle Linux / RHEL"
elif [ -f /etc/lsb-release ] || [ -f /etc/debian_version ]; then
    OS_FAMILY="debian"
    log "Detectado: Ubuntu / Debian"
else
    err "OS não suportado. Use Oracle Linux 9 ou Ubuntu 24.04."
fi

# ═══════════════════════════════════════════════════
# 1. Atualizar sistema
# ═══════════════════════════════════════════════════
log "Atualizando sistema..."
if [ "$OS_FAMILY" = "rhel" ]; then
    dnf update -y -q
    dnf install -y -q curl wget git unzip
else
    apt-get update -qq
    apt-get upgrade -y -qq
    apt-get install -y -qq curl wget git unzip apt-transport-https ca-certificates gnupg
fi

# ═══════════════════════════════════════════════════
# 2. Instalar Docker CE
# ═══════════════════════════════════════════════════
if command -v docker &> /dev/null; then
    log "Docker já instalado: $(docker --version)"
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
    systemctl enable docker
    systemctl start docker
    log "Docker instalado: $(docker --version)"
fi

# ═══════════════════════════════════════════════════
# 3. Instalar Caddy
# ═══════════════════════════════════════════════════
if command -v caddy &> /dev/null; then
    log "Caddy já instalado: $(caddy version)"
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

# ═══════════════════════════════════════════════════
# 4. Criar usuário 'deploy' para GitHub Actions
# ═══════════════════════════════════════════════════
if id "deploy" &>/dev/null; then
    log "Usuário 'deploy' já existe"
else
    log "Criando usuário 'deploy'..."
    useradd -m -s /bin/bash deploy
    usermod -aG docker deploy
    log "Usuário 'deploy' criado e adicionado ao grupo docker"
fi

# Configurar SSH para o deploy user
DEPLOY_SSH_DIR="/home/deploy/.ssh"
mkdir -p "$DEPLOY_SSH_DIR"
if [ ! -f "$DEPLOY_SSH_DIR/authorized_keys" ]; then
    touch "$DEPLOY_SSH_DIR/authorized_keys"
fi
chmod 700 "$DEPLOY_SSH_DIR"
chmod 600 "$DEPLOY_SSH_DIR/authorized_keys"
chown -R deploy:deploy "$DEPLOY_SSH_DIR"

warn "⚠️  AÇÃO MANUAL: Adicione a chave pública SSH em $DEPLOY_SSH_DIR/authorized_keys"
warn "   Gere um par de chaves: ssh-keygen -t ed25519 -C 'github-actions-deploy' -f oci-deploy-key"
warn "   Cole o conteúdo de oci-deploy-key.pub em authorized_keys"
warn "   Adicione o conteúdo de oci-deploy-key como secret OCI_SSH_PRIVATE_KEY no GitHub"

# ═══════════════════════════════════════════════════
# 5. Criar estrutura de diretórios
# ═══════════════════════════════════════════════════
log "Criando estrutura de diretórios..."
mkdir -p /opt/app
mkdir -p /opt/caddy
mkdir -p /opt/backups
chown -R deploy:deploy /opt/app /opt/backups

# ═══════════════════════════════════════════════════
# 6. Copiar docker-compose.prod.yml
# ═══════════════════════════════════════════════════
log "Copiando docker-compose.prod.yml para /opt/app/..."
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
COMPOSE_EOF

chown deploy:deploy /opt/app/docker-compose.prod.yml

# ═══════════════════════════════════════════════════
# 7. Criar template .env
# ═══════════════════════════════════════════════════
if [ ! -f /opt/app/.env ]; then
    log "Criando template .env..."
    cat > /opt/app/.env << 'ENV_EOF'
# ═══════════════════════════════════════════════════
# Variáveis de ambiente — Produção
# ═══════════════════════════════════════════════════
# ⚠️  EDITE TODOS OS VALORES ABAIXO!

# Database
DB_USERNAME=rpg_prod_user
DB_PASSWORD=TROCAR_POR_SENHA_FORTE_AQUI
POSTGRES_DB=rpg_fichas

# GHCR (GitHub Container Registry)
GHCR_OWNER=carlosdemetrio

# URLs — Substitua pelo seu domínio real
FRONTEND_URL=https://seu-dominio.com
BACKEND_URL=https://api.seu-dominio.com

# Google OAuth2
GOOGLE_CLIENT_ID=seu-google-client-id
GOOGLE_CLIENT_SECRET=seu-google-client-secret

# Cloudinary (futuro — deixe vazio por enquanto)
CLOUDINARY_URL=

# Tag da imagem (managed by deploy script)
IMAGE_TAG=latest
ENV_EOF

    chmod 600 /opt/app/.env
    chown deploy:deploy /opt/app/.env
    warn "⚠️  AÇÃO MANUAL: Edite /opt/app/.env com os valores reais!"
else
    log ".env já existe, não sobrescrevendo"
fi

# ═══════════════════════════════════════════════════
# 8. Configurar Caddyfile
# ═══════════════════════════════════════════════════
log "Configurando Caddyfile..."
cat > /etc/caddy/Caddyfile << 'CADDY_EOF'
# ═══════════════════════════════════════════════════
# Caddy — Reverse Proxy com HTTPS automático (Let's Encrypt)
# ═══════════════════════════════════════════════════
# ⚠️  SUBSTITUA 'api.seu-dominio.com' pelo seu domínio real!

api.seu-dominio.com {
    # Reverse proxy para o backend Spring Boot
    reverse_proxy localhost:8081 {
        # Health check interno do Caddy
        health_uri /actuator/health/liveness
        health_interval 30s
        health_timeout 10s
    }

    # Headers de segurança
    header {
        # HSTS
        Strict-Transport-Security "max-age=31536000; includeSubDomains; preload"
        # Prevent clickjacking
        X-Frame-Options "DENY"
        # Prevent MIME sniffing
        X-Content-Type-Options "nosniff"
        # XSS Protection
        X-XSS-Protection "1; mode=block"
        # Referrer
        Referrer-Policy "strict-origin-when-cross-origin"
        # Remove server header
        -Server
    }

    # Logs
    log {
        output file /var/log/caddy/api-access.log {
            roll_size 10mb
            roll_keep 5
        }
    }

    # Request size limit (16MB for future Cloudinary uploads)
    request_body {
        max_size 16MB
    }
}
CADDY_EOF

mkdir -p /var/log/caddy
warn "⚠️  AÇÃO MANUAL: Edite /etc/caddy/Caddyfile — substitua 'api.seu-dominio.com' pelo seu domínio!"

# ═══════════════════════════════════════════════════
# 9. Configurar firewall
# ═══════════════════════════════════════════════════
log "Configurando firewall..."
if [ "$OS_FAMILY" = "rhel" ]; then
    # Oracle Linux usa firewalld
    if systemctl is-active --quiet firewalld; then
        firewall-cmd --permanent --add-service=http
        firewall-cmd --permanent --add-service=https
        firewall-cmd --permanent --add-service=ssh
        firewall-cmd --reload
        log "Firewalld configurado (HTTP, HTTPS, SSH)"
    else
        warn "firewalld não está ativo — iptables pode estar em uso"
    fi

    # Oracle Linux: abrir portas via iptables (necessário além do Security List do OCI)
    if command -v iptables &> /dev/null; then
        iptables -I INPUT -p tcp --dport 80 -j ACCEPT
        iptables -I INPUT -p tcp --dport 443 -j ACCEPT
        netfilter-persistent save 2>/dev/null || iptables-save > /etc/sysconfig/iptables 2>/dev/null || true
        log "iptables configurado"
    fi
else
    # Ubuntu usa ufw
    if command -v ufw &> /dev/null; then
        ufw allow OpenSSH
        ufw allow 80/tcp
        ufw allow 443/tcp
        ufw --force enable
        log "UFW configurado (HTTP, HTTPS, SSH)"
    fi
fi

# ═══════════════════════════════════════════════════
# 10. Configurar backup automático do PostgreSQL
# ═══════════════════════════════════════════════════
log "Configurando backup diário do PostgreSQL..."
cat > /opt/backups/backup-postgres.sh << 'BACKUP_EOF'
#!/bin/bash
# Backup diário do PostgreSQL — executado via cron
set -euo pipefail

BACKUP_DIR="/opt/backups"
RETENTION_DAYS=7
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

# Carregar variáveis
source /opt/app/.env

# Executar pg_dump
docker exec rpg-postgres-prod pg_dump -U "$DB_USERNAME" "$POSTGRES_DB" | gzip > "$BACKUP_DIR/rpg_fichas_${TIMESTAMP}.sql.gz"

# Remover backups antigos
find "$BACKUP_DIR" -name "rpg_fichas_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "[$(date)] Backup concluído: rpg_fichas_${TIMESTAMP}.sql.gz"
BACKUP_EOF

chmod +x /opt/backups/backup-postgres.sh
chown deploy:deploy /opt/backups/backup-postgres.sh

# Adicionar cron para backup diário às 3h
(crontab -u deploy -l 2>/dev/null; echo "0 3 * * * /opt/backups/backup-postgres.sh >> /opt/backups/backup.log 2>&1") | sort -u | crontab -u deploy -
log "Cron de backup configurado (diário às 3h)"

# ═══════════════════════════════════════════════════
# 11. Habilitar e iniciar serviços
# ═══════════════════════════════════════════════════
log "Habilitando serviços..."
systemctl enable docker
systemctl enable caddy
systemctl start docker
# Caddy será iniciado após editar o Caddyfile com o domínio real

# ═══════════════════════════════════════════════════
# Resumo final
# ═══════════════════════════════════════════════════
echo ""
echo "═══════════════════════════════════════════════════"
echo -e "${GREEN}✅ Setup concluído!${NC}"
echo "═══════════════════════════════════════════════════"
echo ""
echo "📋 AÇÕES MANUAIS RESTANTES:"
echo ""
echo "  1. Editar /opt/app/.env com os valores reais:"
echo "     sudo -u deploy nano /opt/app/.env"
echo ""
echo "  2. Editar /etc/caddy/Caddyfile — substituir 'api.seu-dominio.com':"
echo "     sudo nano /etc/caddy/Caddyfile"
echo ""
echo "  3. Adicionar chave SSH pública do GitHub Actions:"
echo "     echo 'CHAVE_PUBLICA' >> /home/deploy/.ssh/authorized_keys"
echo ""
echo "  4. Iniciar Caddy após configurar o domínio:"
echo "     sudo systemctl start caddy"
echo ""
echo "  5. Iniciar o banco de dados:"
echo "     cd /opt/app && sudo -u deploy docker compose -f docker-compose.prod.yml up -d postgres"
echo ""
echo "  6. Configurar GitHub Secrets:"
echo "     - OCI_VM_HOST     = IP público ou api.seu-dominio.com"
echo "     - OCI_VM_USER     = deploy"
echo "     - OCI_SSH_PRIVATE_KEY = conteúdo da chave privada"
echo ""
echo "  7. No Google Cloud Console, adicionar redirect URI:"
echo "     https://api.seu-dominio.com/login/oauth2/code/google"
echo ""
echo "  8. Configurar DNS: A record api.seu-dominio.com → IP público da VM"
echo ""
echo "═══════════════════════════════════════════════════"
