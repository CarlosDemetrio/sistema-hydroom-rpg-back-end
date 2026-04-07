# T5 — Cloud Run: Servico + Dominio + Secrets + Cloud Scheduler

> Fase: Infra/GCP | Prioridade: P0
> Dependencias: T2 (Dockerfile.native — imagem disponivel)
> Bloqueia: T6 (GitHub Actions), T7 (validacao)
> Estimativa: 1 dia

---

## Objetivo

Configurar o servico Cloud Run no GCP: criar o servico, mapear dominio customizado (`api.seu-dominio.com`), configurar secrets no Secret Manager, e (Plano B) criar job no Cloud Scheduler para keepalive.

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `infra/gcp/cloud-run-deploy.sh` | Script de deploy inicial (primeira vez) |
| `infra/gcp/setup-secrets.sh` | Criar secrets no GCP Secret Manager |

---

## Passo 1 — Secrets no GCP Secret Manager

```bash
# Criar cada secret
echo -n "rpg_prod_user" | gcloud secrets create rpg-db-username --data-file=-
echo -n "SENHA_FORTE" | gcloud secrets create rpg-db-password --data-file=-
echo -n "client-id-google" | gcloud secrets create rpg-google-client-id --data-file=-
echo -n "client-secret-google" | gcloud secrets create rpg-google-client-secret --data-file=-
echo -n "https://seu-dominio.com" | gcloud secrets create rpg-frontend-url --data-file=-
echo -n "https://api.seu-dominio.com" | gcloud secrets create rpg-backend-url --data-file=-

# Dar permissao ao Service Account do Cloud Run
PROJECT_NUMBER=$(gcloud projects describe $(gcloud config get-value project) --format='value(projectNumber)')
SA_EMAIL="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

for SECRET in rpg-db-username rpg-db-password rpg-google-client-id rpg-google-client-secret rpg-frontend-url rpg-backend-url; do
  gcloud secrets add-iam-policy-binding $SECRET \
    --member="serviceAccount:${SA_EMAIL}" \
    --role="roles/secretmanager.secretAccessor"
done
```

---

## Passo 2 — Deploy Inicial do Cloud Run

```bash
# Obter IP INTERNO da VM (NÃO o IP publico!)
DB_INTERNAL_IP=$(gcloud compute instances describe rpg-db \
  --zone=us-central1-a \
  --format='value(networkInterfaces[0].networkIP)')
echo "IP Interno da VM: $DB_INTERNAL_IP"

# Primeiro deploy (cria o servico com Direct VPC Egress)
gcloud run deploy rpg-api \
  --image ghcr.io/carlosdemetrio/ficha-controlador:latest \
  --region us-central1 \
  --platform managed \
  --port 8081 \
  --memory 256Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 3 \
  --timeout 300 \
  --allow-unauthenticated \
  --network default \
  --subnet default \
  --vpc-egress private-ranges-only \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_INTERNAL_IP}:5432/rpg_fichas" \
  --set-secrets "DB_USERNAME=rpg-db-username:latest" \
  --set-secrets "DB_PASSWORD=rpg-db-password:latest" \
  --set-secrets "GOOGLE_CLIENT_ID=rpg-google-client-id:latest" \
  --set-secrets "GOOGLE_CLIENT_SECRET=rpg-google-client-secret:latest" \
  --set-secrets "FRONTEND_URL=rpg-frontend-url:latest" \
  --set-secrets "BACKEND_URL=rpg-backend-url:latest"
```

> **CRITICO — Direct VPC Egress:**
> Os flags `--network default`, `--subnet default` e `--vpc-egress private-ranges-only` habilitam
> o Cloud Run a acessar a rede VPC interna. Com isso:
> - O Spring Boot conecta ao PostgreSQL via IP interno (ex: `10.128.0.2`)
> - A porta 5432 da VM fica INVISIVEL para a internet
> - Trafego para a internet (Google OAuth, etc.) continua normal
>
> **DATASOURCE_URL usa o IP INTERNO** (ex: `10.128.0.2`), **NAO** o IP publico!

---

## Passo 3 — Mapear Dominio Customizado

```bash
# Verificar dominio (primeira vez — Google pede verificacao DNS)
gcloud run domain-mappings create \
  --service rpg-api \
  --domain api.seu-dominio.com \
  --region us-central1

# O comando acima informa os registros DNS necessarios.
# Tipicamente: CNAME api → ghs.googlehosted.com
```

### DNS a configurar (no registrador de dominio)

```
Tipo: CNAME   Nome: api   Valor: ghs.googlehosted.com   TTL: 300
```

> O SSL e provisionado automaticamente pelo Google apos verificacao DNS (~5-15 minutos).

---

## Passo 4 — Cloud Scheduler (Plano B — keepalive)

Se estiver usando build JVM (Plano B), criar job para evitar cold start:

```bash
gcloud scheduler jobs create http rpg-api-keepalive \
  --schedule="*/10 * * * *" \
  --uri="https://api.seu-dominio.com/actuator/health" \
  --http-method=GET \
  --attempt-deadline=30s \
  --location=us-central1 \
  --description="Keepalive para evitar cold start do Cloud Run"
```

> Se estiver usando Native Image (cold start < 300ms), o Cloud Scheduler e opcional.

---

## Passo 5 — Configurar Google OAuth2

No [Google Cloud Console](https://console.cloud.google.com/apis/credentials):

1. **Authorized redirect URIs:** `https://api.seu-dominio.com/login/oauth2/code/google`
2. **Authorized JavaScript origins:** `https://api.seu-dominio.com`

> **IMPORTANTE:** Remover as URIs antigas (OCI) se existirem.

---

## Passo 6 — Verificacao

```bash
# Health check
curl https://api.seu-dominio.com/actuator/health
# Esperado: {"status":"UP"}

# Verificar SSL
curl -I https://api.seu-dominio.com
# Esperado: HTTP/2 200, com header strict-transport-security

# Verificar logs
gcloud run logs read --service=rpg-api --region=us-central1 --limit=50
```

---

## cloud-run-deploy.sh (script de referencia)

```bash
#!/bin/bash
set -euo pipefail

# =====================================================================
# Deploy inicial do Cloud Run — executar UMA VEZ
# =====================================================================
# Pre-requisitos:
#   - gcloud CLI instalado e autenticado
#   - Imagem Docker disponivel no GHCR
#   - Secrets criados no Secret Manager (setup-secrets.sh)
#   - VM e2-micro com PostgreSQL rodando (T4)
# =====================================================================

REGION=${1:-"us-central1"}
ZONE=${2:-"us-central1-a"}
SERVICE_NAME="rpg-api"
IMAGE="ghcr.io/carlosdemetrio/ficha-controlador:latest"

# Obter IP INTERNO da VM automaticamente (NAO o IP publico!)
DB_INTERNAL_IP=$(gcloud compute instances describe rpg-db \
  --zone=${ZONE} \
  --format='value(networkInterfaces[0].networkIP)')

echo "Deploying ${SERVICE_NAME} na regiao ${REGION}..."
echo "DB VM IP Interno: ${DB_INTERNAL_IP}"

gcloud run deploy ${SERVICE_NAME} \
  --image ${IMAGE} \
  --region ${REGION} \
  --platform managed \
  --port 8081 \
  --memory 256Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 3 \
  --timeout 300 \
  --allow-unauthenticated \
  --network default \
  --subnet default \
  --vpc-egress private-ranges-only \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_INTERNAL_IP}:5432/rpg_fichas" \
  --set-secrets "DB_USERNAME=rpg-db-username:latest" \
  --set-secrets "DB_PASSWORD=rpg-db-password:latest" \
  --set-secrets "GOOGLE_CLIENT_ID=rpg-google-client-id:latest" \
  --set-secrets "GOOGLE_CLIENT_SECRET=rpg-google-client-secret:latest" \
  --set-secrets "FRONTEND_URL=rpg-frontend-url:latest" \
  --set-secrets "BACKEND_URL=rpg-backend-url:latest"

echo ""
echo "Deploy concluido! URL do servico:"
gcloud run services describe ${SERVICE_NAME} --region=${REGION} --format='value(status.url)'
```

---

## Criterios de Aceitacao

- [ ] Secrets criados no GCP Secret Manager (6 secrets)
- [ ] Servico Cloud Run criado com Direct VPC Egress (`--network default --vpc-egress private-ranges-only`)
- [ ] Cloud Run conecta ao PostgreSQL via IP **interno** da VPC (nao IP publico)
- [ ] Dominio `api.seu-dominio.com` mapeado com SSL automatico
- [ ] `curl https://api.seu-dominio.com/actuator/health` → `{"status":"UP"}`
- [ ] CORS funcional: request de `https://seu-dominio.com` retorna `Access-Control-Allow-Origin` correto
- [ ] CORS bloqueado: request de `https://outro-site.com` NAO tem `Access-Control-Allow-Origin`
- [ ] OAuth2 redirect URI atualizado no Google Console
- [ ] (Plano B) Cloud Scheduler job criado e funcional
- [ ] Scripts `cloud-run-deploy.sh` e `setup-secrets.sh` versionados em `infra/gcp/`

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
