# Infraestrutura — Ficha Controlador

Scripts e configuracoes de deploy organizados por provedor de cloud.

---

## Estrutura

```
infra/
  gcp/                    ← ATIVO — Deploy no GCP Free Tier
    README.md             — Guia completo GCP (VM, Cloud Run, firewall)
    setup-db-vm.sh        — Setup da VM e2-micro (PostgreSQL + hardening)
    docker-compose-db.yml — Compose apenas PostgreSQL
    .env.example          — Template de variaveis da VM
    backup-postgres.sh    — Backup diario com retencao 7 dias
    cloud-run-deploy.sh   — Deploy automatizado no Cloud Run
    setup-secrets.sh      — Criacao de segredos no Secret Manager
  oci/                    ← DESCONTINUADO — Mantido como referencia
    README.md             — Documentacao (descontinuado)
    setup-oci.sh          — Setup da VM OCI (14 etapas)
    rollback.sh           — Script de rollback Docker
    .env.example          — Template de variaveis OCI
```

---

## Arquitetura Atual (GCP + Firebase)

```
Internet
   │
   ├─── Firebase Hosting (CDN Global) ─── Frontend Angular
   │
   └─── Cloud Run (serverless)        ─── Backend Spring Boot (Native Image)
              │ Direct VPC Egress (rede interna)
              ▼
        VM e2-micro (1 vCPU, 1GB RAM) ─── PostgreSQL 16 via Docker
              porta 5432: INVISIVEL para internet
```

---

## Contas e Pre-requisitos

### 1. Google Cloud Platform (GCP)

| Recurso | Tier | Custo |
|---------|------|-------|
| Cloud Run | Free Tier (2M req/mês) | $0 |
| VM e2-micro | Free Tier (1 VM por região) | $0 |
| Egress interno VPC | Gratuito | $0 |
| Secret Manager | Free Tier (6 segredos ativos) | $0 |
| Artifact Registry / GHCR | Usamos GHCR (gratuito) | $0 |

**Como criar conta GCP:**
1. Acessar https://cloud.google.com → **Comece gratuitamente** ($300 crédito)
2. Criar projeto: `gcloud projects create ficha-controlador-rpg --name "Ficha Controlador RPG"`
3. Habilitar APIs necessárias:
   ```bash
   gcloud services enable run.googleapis.com \
     compute.googleapis.com \
     secretmanager.googleapis.com \
     vpcaccess.googleapis.com
   ```
4. Criar Service Account para CI/CD:
   ```bash
   gcloud iam service-accounts create github-actions-sa \
     --display-name "GitHub Actions SA"

   # Permissões mínimas necessárias
   gcloud projects add-iam-policy-binding <PROJECT_ID> \
     --member="serviceAccount:github-actions-sa@<PROJECT_ID>.iam.gserviceaccount.com" \
     --role="roles/run.admin"
   gcloud projects add-iam-policy-binding <PROJECT_ID> \
     --member="serviceAccount:github-actions-sa@<PROJECT_ID>.iam.gserviceaccount.com" \
     --role="roles/secretmanager.secretAccessor"
   gcloud projects add-iam-policy-binding <PROJECT_ID> \
     --member="serviceAccount:github-actions-sa@<PROJECT_ID>.iam.gserviceaccount.com" \
     --role="roles/storage.admin"

   # Exportar chave para usar como secret no GitHub
   gcloud iam service-accounts keys create gcp-sa-key.json \
     --iam-account=github-actions-sa@<PROJECT_ID>.iam.gserviceaccount.com
   ```
5. Adicionar `gcp-sa-key.json` como secret `GCP_SA_KEY` no repositório GitHub (backend)

> Guia completo: `infra/gcp/README.md` | `docs/deploy/DEPLOY-GCP-BACKEND.md`

---

### 2. Firebase (Frontend Hosting)

| Recurso | Tier | Custo |
|---------|------|-------|
| Firebase Hosting | Spark (gratuito) | $0 |
| CDN + SSL automático | Incluído | $0 |
| 10 GB armazenamento | Spark gratuito | $0 |
| 360 MB/dia de transferência | Spark gratuito | $0 |

**Como criar projeto Firebase:**
1. Acessar https://console.firebase.google.com → **Adicionar projeto**
2. Pode **reutilizar o projeto GCP** da etapa anterior (conectar projeto existente)
3. Habilitar Firebase Hosting no projeto
4. Instalar firebase-tools: `npm install -g firebase-tools`
5. Autenticar: `firebase login`
6. Gerar Service Account para GitHub Actions:
   - Firebase Console → **Project Settings** (engrenagem) → aba **Service Accounts**
   - **Generate new private key** → baixar JSON
   - Adicionar no GitHub Actions (repositório frontend) como secret `FIREBASE_SERVICE_ACCOUNT`

> Guia DNS/domínio customizado: `ficha-controlador-front-end/docs/DEPLOY-FIREBASE-DNS.md`

---

### 3. GitHub Secrets necessários

#### Repositório `ficha-controlador` (backend)

| Secret | Valor | Como obter |
|--------|-------|-----------|
| `GCP_SA_KEY` | JSON da Service Account GCP | `gcloud iam service-accounts keys create` (item 1.5 acima) |
| `GCP_PROJECT_ID` | ID do projeto GCP | `gcloud config get-value project` |

#### Repositório `ficha-controlador-front-end` (frontend)

| Secret | Valor | Como obter |
|--------|-------|-----------|
| `FIREBASE_SERVICE_ACCOUNT` | JSON da Service Account Firebase | Firebase Console → Project Settings → Service Accounts |

**Como adicionar um secret no GitHub:**
- Repositório → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

---

### 4. GitHub Environments

Criar environment `production` em **ambos** os repositórios:
- Repositório → **Settings** → **Environments** → **New environment** → `production`
- (Opcional) Adicionar proteção com revisores manuais antes do deploy

---

## Ordem de Provisionamento

```
1. Criar conta GCP + projeto
2. Habilitar APIs (Cloud Run, Compute, Secret Manager)
3. Criar VM e2-micro → executar infra/gcp/setup-db-vm.sh
4. Configurar segredos → executar infra/gcp/setup-secrets.sh
5. Criar Service Account GCP → adicionar como GCP_SA_KEY no GitHub (backend)
6. Criar/associar projeto Firebase
7. Gerar Service Account Firebase → adicionar como FIREBASE_SERVICE_ACCOUNT no GitHub (frontend)
8. Executar infra/gcp/cloud-run-deploy.sh (primeiro deploy manual do backend)
9. Push na branch main → GitHub Actions automatiza deploys seguintes
```

---

## Guias Detalhados

| Guia | Localização |
|------|------------|
| Backend GCP (Cloud Run + VM PostgreSQL) | `docs/deploy/DEPLOY-GCP-BACKEND.md` |
| Infra GCP (scripts, firewall, segurança) | `infra/gcp/README.md` |
| Frontend Firebase (Hosting + CI/CD) | `ficha-controlador-front-end/docs/DEPLOY-FIREBASE-DNS.md` |
| GitHub Actions (workflows CI/CD) | `.github/workflows/README.md` |

---

## Specs Relacionadas

- **Spec 018** — Deploy Backend GCP: `docs/specs/018-deploy-backend-gcp/`
- **Spec 019** — Deploy Frontend Firebase: `docs/specs/019-deploy-frontend-firebase/`
