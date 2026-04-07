# Deploy Backend — OCI Free Tier

> Guia específico do deploy do **backend** (Spring Boot + Docker) no OCI.
> Para infraestrutura compartilhada (VM, Caddy, segurança), veja [`DEPLOY-OCI.md`](./DEPLOY-OCI.md).

---

## Arquitetura do Deploy

```
GitHub Actions (manual)
   │
   ├─ 1. Roda testes (./mvnw test)
   ├─ 2. Build Docker image linux/arm64 (multi-stage)
   ├─ 3. Push para GitHub Container Registry (ghcr.io)
   └─ 4. SSH na VM: docker compose pull + up -d
         │
         v
   ┌─────────────────────────────────────┐
   │  VM OCI                             │
   │                                     │
   │  Caddy (api.seu-dominio.com)        │
   │    → reverse proxy localhost:8081   │
   │                                     │
   │  Docker Compose                     │
   │    ├─ Spring Boot (:8081)           │
   │    │   384MB heap, read-only FS     │
   │    └─ PostgreSQL 16 (rede interna)  │
   │        256MB limit, read-only FS    │
   └─────────────────────────────────────┘

Tempo: ~8-15 min (build ARM64 via QEMU)
Downtime: ~10-20 seg (restart do container)
```

---

## Arquivos Relevantes

```
ficha-controlador/
  .github/workflows/
    deploy.yml                  ← Workflow de deploy (manual)
  infra/
    setup-oci.sh                ← Setup VM hardened (14 etapas)
    rollback.sh                 ← Script de rollback
    .env.example                ← Template de variáveis
  docker-compose.prod.yml       ← Compose produção (hardened)
  Dockerfile                    ← Multi-stage (health check, non-root)
  src/main/resources/
    application-prod.properties ← forward-headers, same-site=lax
```

---

## Dockerfile (Multi-stage)

O Dockerfile usa 3 stages:

| Stage | Base | Propósito |
|-------|------|-----------|
| `development` | `maven:3.9-eclipse-temurin-25` | Dev com hot-reload |
| `build` | `maven:3.9-eclipse-temurin-25` | Compilação + testes |
| `production` | `eclipse-temurin:25-jre` | Runtime mínimo |

**Segurança no stage de produção:**
- Usuário não-root (`appuser`)
- `curl` para health checks
- Health check integrado (`/actuator/health/liveness`)
- JVM flags via `JAVA_TOOL_OPTIONS` (definidos no docker-compose)

---

## Docker Compose (Produção)

**`docker-compose.prod.yml`** define dois serviços:

### PostgreSQL
- Imagem: `postgres:16-alpine`
- Rede interna only (não exposta)
- `read_only: true` + `no-new-privileges`
- Limite: 256MB RAM
- Health check: `pg_isready`

### Backend (Spring Boot)
- Imagem: `ghcr.io/<owner>/ficha-controlador:<tag>`
- Porta: `127.0.0.1:8081` (apenas localhost — Caddy faz proxy)
- `read_only: true` + `no-new-privileges`
- Limite: 512MB RAM
- JVM: `-Xms256m -Xmx384m -XX:+UseZGC -XX:+ZGenerational`
- Health check: `/actuator/health/liveness`

---

## Variáveis de Ambiente

Arquivo `/opt/app/.env` na VM (template em `infra/.env.example`):

```env
DB_USERNAME=rpg_prod_user
DB_PASSWORD=<SENHA_FORTE>
POSTGRES_DB=rpg_fichas
GHCR_OWNER=<seu-usuario-github>
FRONTEND_URL=https://seu-dominio.com
BACKEND_URL=https://api.seu-dominio.com
GOOGLE_CLIENT_ID=<client-id>
GOOGLE_CLIENT_SECRET=<client-secret>
IMAGE_TAG=latest
```

---

## Workflow de Deploy

**`.github/workflows/deploy.yml`** — trigger manual com opções:

| Input | Descrição | Default |
|-------|-----------|---------|
| `image_tag` | Tag da imagem Docker | `latest` |
| `skip_tests` | Pular testes? | `false` |
| `deploy_only` | Apenas deploy (sem build)? | `false` |

### Jobs

1. **🧪 Testes** — `./mvnw test` (pula se `skip_tests` ou `deploy_only`)
2. **🐳 Build & Push** — Multi-platform ARM64, push para GHCR
3. **🚀 Deploy OCI** — SSH na VM, `docker compose pull + up -d`, health check

### Secrets Necessários (GitHub)

| Secret | Valor |
|--------|-------|
| `OCI_VM_HOST` | IP ou hostname da VM |
| `OCI_VM_USER` | `deploy` |
| `OCI_SSH_PRIVATE_KEY` | Chave privada SSH (ed25519) |
| `GITHUB_TOKEN` | Automático (para GHCR) |

---

## Caddy — Configuração do Backend

Trecho do Caddyfile para o backend (configurado pelo `setup-oci.sh`):

```caddyfile
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

    # Bloquear endpoints sensíveis do Actuator
    @blocked path /actuator/env /actuator/configprops /actuator/beans /actuator/mappings /actuator/shutdown
    respond @blocked 404

    # Limite de tamanho do body
    request_body {
        max_size 16MB
    }

    log {
        output file /var/log/caddy/api-access.log {
            roll_size 10mb
            roll_keep 5
        }
        format json
    }
}
```

---

## Google OAuth2

No [Google Cloud Console](https://console.cloud.google.com/apis/credentials):

- **Authorized redirect URIs**: `https://api.seu-dominio.com/login/oauth2/code/google`
- **Authorized JavaScript origins**: `https://api.seu-dominio.com`

---

## Primeiro Deploy

1. Certifique-se que a VM está configurada (ver [`DEPLOY-OCI.md`](./DEPLOY-OCI.md))
2. PostgreSQL rodando: `cd /opt/app && docker compose -f docker-compose.prod.yml up -d postgres`
3. No GitHub: **Actions** → **Deploy Backend to OCI** → **Run workflow**

### Verificar

```bash
curl https://api.seu-dominio.com/actuator/health
# Resposta esperada: {"status":"UP"}
```

---

## Rollback

```bash
ssh deploy@api.seu-dominio.com
/opt/app/scripts/rollback.sh <tag_anterior>
```

O script `rollback.sh`:
1. Atualiza `IMAGE_TAG` no `.env`
2. Faz `docker compose pull backend`
3. Faz `docker compose up -d --no-deps backend`
4. Aguarda health check (6 tentativas, 10s cada)

---

## Logs

```bash
ssh deploy@api.seu-dominio.com

# Backend
cd /opt/app && docker compose -f docker-compose.prod.yml logs -f --tail 100 backend

# PostgreSQL
docker compose -f docker-compose.prod.yml logs -f --tail 50 postgres

# Caddy (requests ao backend)
sudo journalctl -u caddy -f
```

---

## Backups do Banco

Backup automático diário às 3h (configurado pelo `setup-oci.sh`):

```bash
# Listar backups
ls -la /opt/backups/rpg_fichas_*.sql.gz

# Restaurar
gunzip < /opt/backups/rpg_fichas_20260406.sql.gz | \
  docker exec -i rpg-postgres-prod psql -U rpg_prod_user rpg_fichas
```

Retenção: 7 dias.

---

## Checklist Pre-Deploy

- [ ] VM OCI configurada (ver [`DEPLOY-OCI.md`](./DEPLOY-OCI.md))
- [ ] `/opt/app/.env` preenchido com valores reais
- [ ] PostgreSQL rodando e saudável
- [ ] Caddyfile com domínio real configurado
- [ ] Google OAuth2 redirect URI configurado
- [ ] GitHub Secrets configurados (OCI_VM_HOST, OCI_VM_USER, OCI_SSH_PRIVATE_KEY)
- [ ] Environment "production" criado no GitHub
- [ ] Testes passando localmente (`./mvnw test`)
