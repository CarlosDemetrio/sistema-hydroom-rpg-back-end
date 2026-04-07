# T6 — GitHub Actions: Build Native + Deploy Cloud Run

> Fase: CI/CD | Prioridade: P0
> Dependencias: T2 (Dockerfile.native), T5 (Cloud Run configurado)
> Bloqueia: T7 (validacao)
> Estimativa: 1 dia

---

## Objetivo

Criar workflow do GitHub Actions que automatiza: testes → build Docker native → push para GHCR → deploy no Cloud Run. Trigger: push na branch `main` ou manual via `workflow_dispatch`.

---

## Arquivo a Criar

| Arquivo | Descricao |
|---------|-----------|
| `.github/workflows/deploy-gcp.yml` | Workflow completo de CI/CD |

---

## Workflow: deploy-gcp.yml

```yaml
name: Deploy Backend to Cloud Run

on:
  push:
    branches: [main]
    paths:
      - 'src/**'
      - 'pom.xml'
      - 'Dockerfile.native'
      - 'Dockerfile.jvm-cloudrun'
  workflow_dispatch:
    inputs:
      skip_tests:
        description: 'Skip tests?'
        type: boolean
        default: false
      build_mode:
        description: 'Build mode (native ou jvm)'
        type: choice
        options:
          - native
          - jvm
        default: native

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository_owner }}/ficha-controlador
  CLOUD_RUN_SERVICE: rpg-api
  CLOUD_RUN_REGION: us-central1

jobs:
  # -----------------------------------------------
  # Job 1: Testes
  # -----------------------------------------------
  test:
    name: 🧪 Testes
    runs-on: ubuntu-latest
    if: ${{ github.event_name == 'push' || !inputs.skip_tests }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 25
          cache: maven
      - name: Run tests
        run: ./mvnw test -B

  # -----------------------------------------------
  # Job 2: Build Docker + Push GHCR
  # -----------------------------------------------
  build:
    name: 🐳 Build & Push
    needs: [test]
    if: always() && (needs.test.result == 'success' || needs.test.result == 'skipped')
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    outputs:
      image_tag: ${{ steps.meta.outputs.tags }}
    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Determine Dockerfile
        id: dockerfile
        run: |
          MODE="${{ inputs.build_mode || 'native' }}"
          if [ "$MODE" = "jvm" ]; then
            echo "file=Dockerfile.jvm-cloudrun" >> $GITHUB_OUTPUT
          else
            echo "file=Dockerfile.native" >> $GITHUB_OUTPUT
          fi

      - name: Docker meta
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=sha
            type=raw,value=latest

      - name: Build & Push
        uses: docker/build-push-action@v5
        with:
          context: .
          file: ./${{ steps.dockerfile.outputs.file }}
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
          platforms: linux/amd64

  # -----------------------------------------------
  # Job 3: Deploy Cloud Run
  # -----------------------------------------------
  deploy:
    name: 🚀 Deploy Cloud Run
    needs: [build]
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Auth GCP
        uses: google-github-actions/auth@v2
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}

      - name: Setup gcloud
        uses: google-github-actions/setup-gcloud@v2
        with:
          project_id: ${{ secrets.GCP_PROJECT_ID }}

      - name: Deploy to Cloud Run
        run: |
          gcloud run deploy ${{ env.CLOUD_RUN_SERVICE }} \
            --image ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${GITHUB_SHA::7} \
            --region ${{ env.CLOUD_RUN_REGION }} \
            --platform managed \
            --quiet

      - name: Verify deployment
        run: |
          URL=$(gcloud run services describe ${{ env.CLOUD_RUN_SERVICE }} \
            --region ${{ env.CLOUD_RUN_REGION }} \
            --format='value(status.url)')
          echo "Service URL: $URL"
          sleep 10
          STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${URL}/actuator/health")
          if [ "$STATUS" != "200" ]; then
            echo "Health check failed! Status: $STATUS"
            exit 1
          fi
          echo "Health check passed!"
```

---

## Secrets Necessarios no GitHub

| Secret | Descricao | Como obter |
|--------|-----------|------------|
| `GCP_SA_KEY` | JSON da Service Account com `roles/run.admin` + `roles/iam.serviceAccountUser` | GCP Console → IAM → Service Accounts → Create Key |
| `GCP_PROJECT_ID` | ID do projeto GCP | GCP Console → Dashboard |
| `GITHUB_TOKEN` | Automatico (para push ao GHCR) | Automatico |

### Criar Service Account no GCP

```bash
# Criar SA
gcloud iam service-accounts create github-deployer \
  --display-name="GitHub Actions Deployer"

# Dar permissoes
SA_EMAIL="github-deployer@<PROJECT_ID>.iam.gserviceaccount.com"
gcloud projects add-iam-policy-binding <PROJECT_ID> \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/run.admin"
gcloud projects add-iam-policy-binding <PROJECT_ID> \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/iam.serviceAccountUser"

# Gerar chave JSON
gcloud iam service-accounts keys create gcp-sa-key.json \
  --iam-account=${SA_EMAIL}
# Conteudo de gcp-sa-key.json → GitHub Secret GCP_SA_KEY
```

---

## GitHub Environment

Criar environment `production` no repositorio:
- **Settings** → **Environments** → **New environment** → `production`
- (Opcional) Adicionar reviewers para aprovacao manual antes de deploy

---

## Rollback

```bash
# Listar revisoes
gcloud run revisions list --service=rpg-api --region=us-central1

# Redirecionar trafego para revisao anterior
gcloud run services update-traffic rpg-api \
  --region=us-central1 \
  --to-revisions=rpg-api-XXXXX=100
```

Ou via GitHub Actions: re-run do workflow com tag anterior (manual).

---

## Criterios de Aceitacao

- [ ] Workflow `.github/workflows/deploy-gcp.yml` criado e valido (YAML lint)
- [ ] Push na `main` dispara build + deploy automaticamente
- [ ] `workflow_dispatch` permite escolher `native` ou `jvm`
- [ ] Build Docker completa no CI (native: ~10-15min, jvm: ~3-5min)
- [ ] Deploy no Cloud Run completa sem erros
- [ ] Health check pos-deploy passa
- [ ] Rollback funcional via re-deploy de revisao anterior

---

*Produzido por: Tech Lead / DevOps | 2026-04-07*
