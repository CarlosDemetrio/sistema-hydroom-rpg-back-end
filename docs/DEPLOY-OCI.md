# Deploy OCI Free Tier — Infraestrutura Compartilhada

> Setup e configuração da VM OCI compartilhada por backend e frontend.
> Para deploy específico de cada aplicação, veja:
> - **Backend**: [`DEPLOY-BACKEND.md`](./DEPLOY-BACKEND.md)
> - **Frontend**: Repositório `ficha-controlador-front-end` → `docs/DEPLOY-FRONTEND.md`

---

## Arquitetura

```
Internet
   |
   v
Cloudflare (WAF + DDoS + CDN) --- Proxy Mode (laranja)
   |
   v
DNS:  seu-dominio.com     --> IP Publico Reservado (OCI)
      api.seu-dominio.com --> IP Publico Reservado (OCI)
   |
   v
+----------------------------------------------------------+
|  OCI Security List (L3/L4 firewall)                      |
|  + nftables (rate-limit SSH 3/min, HTTP 50/s per IP)     |
|  + Fail2Ban (SSH brute-force = 2h ban)                   |
+----------------------------------------------------------+
   |
   v
+----------------------------------------------------------+
|  VM OCI ARM Ampere A1 (2 OCPU / 12GB RAM / 2GB Swap)    |
|                                                           |
|  +-----------------------------------------------------+ |
|  | Caddy (porta 80/443) - HTTPS automatico             | |
|  |                                                      | |
|  |  seu-dominio.com:                                    | |
|  |    -> /opt/frontend/ (Angular SPA, arquivos static)  | |
|  |    -> gzip/zstd, cache imutavel para assets c/ hash  | |
|  |    -> try_files fallback para SPA routing            | |
|  |                                                      | |
|  |  api.seu-dominio.com:                                | |
|  |    -> reverse proxy localhost:8081 (Spring Boot)     | |
|  |    -> security headers, bloqueia actuator sensivel   | |
|  +------------------------+----------------------------+ |
|                           |                               |
|  +------------------------v----------------------------+ |
|  | Docker (User Namespace Remapping)                    | |
|  |                                                      | |
|  |  +----------------+    +------------------------+   | |
|  |  | Spring Boot    |----| PostgreSQL 16          |   | |
|  |  | (porta 8081)   |    | (rede interna only)    |   | |
|  |  | read-only FS   |    | read-only FS           |   | |
|  |  | 384MB heap     |    | 256MB limit            |   | |
|  |  +----------------+    +------------------------+   | |
|  +-----------------------------------------------------+ |
+----------------------------------------------------------+
```

### Deploy Independente

```
Repositorio Backend (ficha-controlador)
  GitHub Actions (manual) -> Build Docker ARM64 -> Push GHCR -> SSH deploy
  Resultado: Container Spring Boot atualizado

Repositorio Frontend (ficha-controlador-front-end)
  GitHub Actions (manual) -> npm ci -> ng build prod -> SCP dist -> /opt/frontend/
  Resultado: Arquivos estaticos atualizados (zero downtime, sem restart)
```

> Frontend **não** usa Docker. Caddy serve os arquivos estáticos diretamente.
> Isso economiza ~200MB de RAM que seriam gastos com um container nginx.

### Quando fazer deploy?

- **Backend mudou?** → Run workflow no repo backend
- **Frontend mudou?** → Run workflow no repo frontend
- **Ambos mudaram?** → Run workflow em cada repo (são independentes)

---

## Recursos OCI Free Tier Utilizados

| Recurso | Especificação | Custo |
|---------|--------------|-------|
| Compute VM | VM.Standard.A1.Flex — 2 OCPU, 12GB RAM, ARM64 | Free |
| Boot Volume | 50GB | Free (até 200GB total) |
| IP Público | Reservado (estático) | Free |
| VCN + Subnet | 1 VCN com subnet pública | Free |
| Egress | 10TB/mês | Free |

---

## Camadas de Segurança (Defense in Depth)

```
Camada 1:  Cloudflare WAF       → DDoS, bot protection, rate limit L7
Camada 2:  OCI Security List    → Firewall L3/L4 na nuvem
Camada 3:  nftables             → Firewall L3/L4 no SO (DROP default)
Camada 4:  Fail2Ban             → Ban automático brute-force SSH
Camada 5:  SSH Hardened         → Key-only, root off, MaxAuthTries 3
Camada 6:  Caddy                → HTTPS, security headers, path blocking
Camada 7:  Docker Userns Remap  → Container root != host root
Camada 8:  Container Hardening  → read-only FS, no-new-privileges
Camada 9:  Spring Security      → OAuth2, CORS, CSRF, rate-limit app
Camada 10: Auditd               → Monitoramento de acessos críticos
Camada 11: Swap 2GB             → Buffer contra OOM Kill
```

---

## Passo a Passo — Setup Inicial

### Passo 1: Criar Conta OCI

1. Acesse [cloud.oracle.com](https://cloud.oracle.com) e crie uma conta (Always Free)
2. Selecione o **Home Region** mais próximo (ex: `sa-saopaulo-1` para Brasil)
3. Aguarde a ativação da conta

### Passo 2: Criar Infraestrutura no Console OCI

#### 2.1 — VCN (Virtual Cloud Network)
1. Console → **Networking** → **Virtual Cloud Networks**
2. **Start VCN Wizard** → "Create VCN with Internet Connectivity"
3. Nome: `rpg-vcn`

#### 2.2 — Security List (Firewall OCI — Camada 2)

| Source CIDR | Protocol | Port | Descrição |
|-------------|----------|------|-----------|
| `0.0.0.0/0` | TCP | 80 | HTTP (Caddy) |
| `0.0.0.0/0` | TCP | 443 | HTTPS (Caddy) |
| `SEU_IP/32` | TCP | 22 | SSH (apenas seu IP!) |

#### 2.3 — Compute Instance
- **Shape**: `VM.Standard.A1.Flex` (Always Free) — 2 OCPU, 12 GB
- **Image**: Oracle Linux 9 ou Ubuntu 24.04 (aarch64)
- **Boot Volume**: 50 GB

#### 2.4 — IP Público Reservado
Console → **Networking** → **Reserved Public IPs** → associar à instância.

### Passo 3: Configurar DNS (2 registros)

No painel do seu registrador de domínio (ou Cloudflare):

```
Tipo: A    Nome: @      Valor: <IP_PUBLICO>    TTL: 300
Tipo: A    Nome: api    Valor: <IP_PUBLICO>    TTL: 300
```

Verificar:
```bash
dig seu-dominio.com
dig api.seu-dominio.com
```

### Passo 4: Setup do Servidor (Hardened)

```bash
scp -i sua-chave.pem infra/setup-oci.sh opc@<IP>:~/
ssh -i sua-chave.pem opc@<IP>
chmod +x setup-oci.sh
sudo ./setup-oci.sh
```

O script `setup-oci.sh` (14 etapas) cria a estrutura completa:
```
/opt/app/                → Backend (Docker Compose, .env)
/opt/frontend/           → Frontend (Angular static files)
/opt/frontend-backup/    → Backups do frontend (últimas 3 versões)
/opt/backups/            → Backups do PostgreSQL (diários)
```

### Passo 5: Configurar Variáveis de Ambiente

```bash
sudo -u deploy nano /opt/app/.env
```

Ver template em `infra/.env.example`.

### Passo 6: Configurar Caddy (Domínio)

```bash
sudo nano /etc/caddy/Caddyfile
```

Substituir **TODAS** as ocorrências de `seu-dominio.com` pelo domínio real. Depois:
```bash
sudo systemctl restart caddy
```

### Passo 7: Configurar GitHub Actions (AMBOS repos)

#### 7.1 — Gerar par de chaves SSH
```bash
ssh-keygen -t ed25519 -C "github-actions-deploy" -f oci-deploy-key -N ""
```

#### 7.2 — Adicionar chave na VM
```bash
cat oci-deploy-key.pub | ssh -i sua-chave.pem opc@<IP> \
  'sudo -u deploy bash -c "cat >> /home/deploy/.ssh/authorized_keys"'
```

#### 7.3 — Secrets em AMBOS os repos

| Secret | Valor | Repos |
|--------|-------|-------|
| `OCI_VM_HOST` | IP ou `api.seu-dominio.com` | Backend + Frontend |
| `OCI_VM_USER` | `deploy` | Backend + Frontend |
| `OCI_SSH_PRIVATE_KEY` | Conteúdo de `oci-deploy-key` | Backend + Frontend |

> A mesma chave SSH funciona para ambos os repos, pois o deploy user é o mesmo.

#### 7.4 — Criar Environment "production" em ambos repos
**Settings** → **Environments** → **New environment** → `production`

---

## Cloudflare como WAF (Recomendado)

### Configurar DNS no Cloudflare (2 registros com proxy)
```
Tipo: A    Nome: @      Valor: <IP_OCI>    Proxy: ON (laranja)
Tipo: A    Nome: api    Valor: <IP_OCI>    Proxy: ON (laranja)
```

### SSL/TLS
- Modo: **Full (Strict)**
- Always Use HTTPS: ON
- Minimum TLS Version: TLS 1.2

### WAF Custom Rule (1 gratuita)
Bloquear caminhos suspeitos no subdomínio API:
```
Quando: Hostname = api.seu-dominio.com
  AND URI Path not starts with "/api/"
  AND URI Path not starts with "/actuator/health"
  AND URI Path not starts with "/login/"
  AND URI Path not starts with "/oauth2/"
  AND URI Path not starts with "/swagger"
Então: Block
```

### Rate Limiting (1 regra gratuita)
```
Quando: URI Path starts with "/api/"
Rate: 100 requests / 1 minuto
Ação: Block por 60 segundos
```

### Bot Fight Mode: ON

---

## Opções Avançadas

### OCI Bastion Service
Permite SSH **sem expor porta 22 na internet**. Criar subnet privada + NAT Gateway + Bastion.
Para o estágio inicial, SSH direto com nftables + Fail2Ban + key-only é bastante seguro.

### OCI Vault para Secrets
Centraliza secrets com criptografia HSM. Recomendado quando houver múltiplos ambientes.
Para o estágio inicial, `.env` com `chmod 600` é suficiente.

---

## Monitoramento

### Health Checks
```bash
curl https://api.seu-dominio.com/actuator/health   # Backend
curl -I https://seu-dominio.com                      # Frontend
```

### Logs do Sistema
```bash
ssh deploy@api.seu-dominio.com

# Caddy (frontend + backend)
sudo journalctl -u caddy -f

# Firewall (pacotes bloqueados)
sudo journalctl -k | grep NFT-DROP

# Fail2Ban
sudo fail2ban-client status sshd

# Auditoria
sudo ausearch -k env_access
sudo ausearch -k frontend_deploy
```

### Uptime Monitoring (UptimeRobot — Gratuito)
- `https://api.seu-dominio.com/actuator/health` (a cada 5 min)
- `https://seu-dominio.com` (a cada 5 min)

---

## Cloudinary (Futuro)

1. Criar conta em [cloudinary.com](https://cloudinary.com)
2. Editar `/opt/app/.env`: `CLOUDINARY_URL=cloudinary://KEY:SECRET@NAME`
3. Adicionar dependência no `pom.xml`
4. Criar `CloudinaryService` no backend
5. Redeploy backend

---

## Checklist Pre-Deploy — Infraestrutura

### OCI
- [ ] Conta OCI ativada (Always Free)
- [ ] VM ARM criada (VM.Standard.A1.Flex, 2 OCPU, 12GB)
- [ ] IP público reservado e associado
- [ ] DNS: `seu-dominio.com` → IP
- [ ] DNS: `api.seu-dominio.com` → IP

### Servidor
- [ ] `setup-oci.sh` executado
- [ ] Swap ativo (`free -h`)
- [ ] nftables ativo (`sudo nft list ruleset`)
- [ ] Fail2Ban rodando (`sudo fail2ban-client status sshd`)
- [ ] Docker userns-remap ativo (`docker info | grep userns`)

### Configuração
- [ ] `.env` preenchido (`/opt/app/.env`)
- [ ] Caddyfile editado com domínio real
- [ ] Caddy rodando (`sudo systemctl status caddy`)

### GitHub (ambos repos)
- [ ] Secrets: OCI_VM_HOST, OCI_VM_USER, OCI_SSH_PRIVATE_KEY
- [ ] Environment "production" criado

### Serviços Externos
- [ ] Google OAuth2 redirect URI configurado
- [ ] (Recomendado) Cloudflare com Proxy Mode

### Verificação Final
- [ ] Backend: `curl https://api.seu-dominio.com/actuator/health` → OK
- [ ] Frontend: `curl https://seu-dominio.com` → index.html
- [ ] OAuth2 login funcional
