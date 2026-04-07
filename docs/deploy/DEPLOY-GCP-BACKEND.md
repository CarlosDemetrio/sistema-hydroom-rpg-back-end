# Deploy Backend — GCP Free Tier (Cloud Run + VM e2-micro)

> **Stack:** Spring Boot 4 / Java 25 | Native Image (GraalVM) | Cloud Run | PostgreSQL 16

---

## Arquitetura

```
Internet → Cloud Run (rpg-api) ──[Direct VPC Egress]──→ VM e2-micro (PostgreSQL)
                ↑
         HTTPS (domínio customizado)
         SSL gerenciado pelo Google
```

**Componentes:**
| Componente | Recurso GCP | Custo Free Tier |
|---|---|---|
| Backend API | Cloud Run (us-central1) | 2M req/mês grátis |
| Banco de dados | VM e2-micro + PostgreSQL 16 | 1 VM e2-micro/mês grátis |
| Imagens Docker | GHCR (GitHub Container Registry) | Grátis |
| Secrets | Secret Manager | 6 secrets × 10k acessos/mês |
| CI/CD | GitHub Actions | 2000 min/mês grátis |

> **CRITICAL:** Cloud Run acessa PostgreSQL via **IP INTERNO da VPC** (Direct VPC Egress).
> PostgreSQL **NUNCA** fica exposto à internet pública.

---

## Pré-requisitos

- [ ] Conta GCP com projeto criado e faturamento habilitado (necessário para Cloud Run)
- [ ] `gcloud` CLI instalado e autenticado (`gcloud auth login`)
- [ ] Docker instalado localmente (para testes opcionais)
- [ ] Domínio próprio registrado (para mapeamento customizado)
- [ ] Conta Google OAuth2 configurada no Google Cloud Console
- [ ] GitHub repository com secrets `GCP_SA_KEY` e `GCP_PROJECT_ID` configurados

---

## Passo a Passo

### 1. VM e2-micro — PostgreSQL

#### 1.1 Criar VM

```bash
gcloud compute instances create rpg-db \
  --machine-type=e2-micro \
  --zone=us-central1-a \
  --image-family=ubuntu-2404-lts-amd64 \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --boot-disk-type=pd-standard \
  --tags=postgres-server
```

#### 1.2 IP Estático

```bash
gcloud compute addresses create rpg-db-ip --region=us-central1
IP=$(gcloud compute addresses describe rpg-db-ip --region=us-central1 --format='value(address)')
echo "IP Estático: $IP"
```

#### 1.3 Regras de Firewall

```bash
# PostgreSQL — APENAS rede interna VPC (Cloud Run via Direct VPC Egress)
gcloud compute firewall-rules create allow-postgres-vpc \
  --direction=INGRESS --action=ALLOW --rules=tcp:5432 \
  --target-tags=postgres-server \
  --source-ranges=10.128.0.0/20

# SSH — APENAS seu IP (obter em https://ifconfig.me)
gcloud compute firewall-rules create allow-ssh-admin \
  --direction=INGRESS --action=ALLOW --rules=tcp:22 \
  --target-tags=postgres-server \
  --source-ranges=SEU_IP/32
```

#### 1.4 Setup da VM

```bash
# Copiar scripts para a VM
gcloud compute scp infra/gcp/setup-db-vm.sh rpg-db:~ --zone=us-central1-a
gcloud compute scp infra/gcp/backup-postgres.sh rpg-db:~ --zone=us-central1-a

# SSH na VM e executar setup
gcloud compute ssh rpg-db --zone=us-central1-a
sudo bash setup-db-vm.sh
```

O script configura: swap 2GB, Docker CE, PostgreSQL 16, backup diário, fail2ban, UFW.

#### 1.5 Anotar IP Interno

```bash
# IP INTERNO (para Cloud Run via VPC) — usar este!
gcloud compute instances describe rpg-db \
  --zone=us-central1-a \
  --format='value(networkInterfaces[0].networkIP)'
# Ex: 10.128.0.2
```

---

### 2. GCP Secrets

```bash
cd infra/gcp
./setup-secrets.sh
```

Cria 6 secrets no Secret Manager: `rpg-db-username`, `rpg-db-password`, `rpg-google-client-id`, `rpg-google-client-secret`, `rpg-frontend-url`, `rpg-backend-url`.

---

### 3. Cloud Run — Primeiro Deploy

```bash
cd infra/gcp
./cloud-run-deploy.sh
```

O script obtém automaticamente o IP interno da VM e faz o deploy com Direct VPC Egress.

---

### 4. Domínio e SSL

```bash
# Mapear domínio customizado
gcloud run domain-mappings create \
  --service rpg-api \
  --domain api.seu-dominio.com \
  --region us-central1
```

**DNS a configurar no registrador:**
```
Tipo: CNAME   Nome: api   Valor: ghs.googlehosted.com   TTL: 300
```

SSL é provisionado automaticamente pelo Google (~5-15 minutos após propagação DNS).

---

### 5. Google OAuth2

No [Google Cloud Console → APIs → Credentials](https://console.cloud.google.com/apis/credentials):

1. **Authorized redirect URIs:** `https://api.seu-dominio.com/login/oauth2/code/google`
2. **Authorized JavaScript origins:** `https://api.seu-dominio.com`

Remover URIs antigas (OCI) se existirem.

---

### 6. GitHub Actions (CI/CD automático)

#### 6.1 Criar Service Account para deploy

```bash
gcloud iam service-accounts create github-deployer \
  --display-name="GitHub Actions Deployer"

SA_EMAIL="github-deployer@$(gcloud config get-value project).iam.gserviceaccount.com"

gcloud projects add-iam-policy-binding $(gcloud config get-value project) \
  --member="serviceAccount:${SA_EMAIL}" --role="roles/run.admin"

gcloud projects add-iam-policy-binding $(gcloud config get-value project) \
  --member="serviceAccount:${SA_EMAIL}" --role="roles/iam.serviceAccountUser"

gcloud iam service-accounts keys create gcp-sa-key.json --iam-account=${SA_EMAIL}
```

#### 6.2 Configurar GitHub Secrets

Em **Settings → Secrets → Actions** no repositório:
- `GCP_SA_KEY` → conteúdo de `gcp-sa-key.json` (⚠️ deletar o arquivo após!)
- `GCP_PROJECT_ID` → ID do projeto GCP

#### 6.3 Criar Environment de produção

Em **Settings → Environments → New environment**: `production`
(Opcional: adicionar reviewers para aprovação manual de deploy)

#### 6.4 Primeiro deploy via CI

```bash
git push origin main  # ou disparar manualmente em Actions → Deploy Backend to Cloud Run
```

---

## Rollback

```bash
# Listar revisões
gcloud run revisions list --service=rpg-api --region=us-central1

# Redirecionar tráfego para revisão anterior
gcloud run services update-traffic rpg-api \
  --region=us-central1 \
  --to-revisions=rpg-api-XXXXXXXX=100
```

---

## Logs e Monitoramento

```bash
# Logs em tempo real
gcloud run logs tail --service=rpg-api --region=us-central1

# Últimos 50 logs
gcloud run logs read --service=rpg-api --region=us-central1 --limit=50

# Health check manual
curl https://api.seu-dominio.com/actuator/health
```

---

## Troubleshooting

| Problema | Causa provável | Solução |
|---|---|---|
| `ECONNREFUSED` na conexão com DB | IP errado no `DATASOURCE_URL` | Verificar se é o IP **interno** (ex: 10.128.x.x), não o público |
| Cold start lento (>10s) | Build JVM sem keepalive | Usar Dockerfile.native ou criar Cloud Scheduler keepalive |
| `reflection` error no native | Classe sem hint | Adicionar em `NativeHintsRegistrar.java` e rebuildar |
| OAuth2 redirect falha | URI não autorizada no Console | Atualizar `Authorized redirect URIs` no Google Console |
| `429 Too Many Requests` | Rate limiting ativo (Bucket4j) | Normal — 100 req/min por IP |
| Container não inicia | Porta errada | Confirmar `--port 8081` no Cloud Run e `server.port=${PORT:8081}` |

---

## Checklist Pré-Deploy

- [ ] VM e2-micro criada em região Free Tier (us-central1/us-east1/us-west1)
- [ ] PostgreSQL rodando: `docker exec rpg-postgres pg_isready`
- [ ] IP interno da VM anotado (ex: 10.128.0.2)
- [ ] 6 secrets criados no Secret Manager
- [ ] `GCP_SA_KEY` e `GCP_PROJECT_ID` configurados no GitHub
- [ ] Environment `production` criado no GitHub
- [ ] DNS CNAME configurado no registrador
- [ ] OAuth2 redirect URIs atualizadas no Google Console
- [ ] `curl https://api.seu-dominio.com/actuator/health` retorna `{"status":"UP"}`

---

## Custos Estimados (Free Tier)

| Recurso | Limite Free | Uso estimado | Status |
|---|---|---|---|
| Cloud Run | 2M req/mês, 360k GB-s | <50k req/mês | ✅ Gratuito |
| VM e2-micro | 1 instância/mês | 1 VM | ✅ Gratuito |
| Disco VM | 30GB HDD | 10-15GB utilizado | ✅ Gratuito |
| Secret Manager | 6 secrets, 10k acessos/mês | <1k acessos/mês | ✅ Gratuito |
| GHCR | 500MB grátis | ~150MB (native) | ✅ Gratuito |

> ⚠️ Cloud Run requer faturamento habilitado, mas não gera cobranças dentro dos limites do Free Tier.

---

*Última atualização: 2026-04-07 | Spec: 018-deploy-backend-gcp*
