# GitHub Actions Workflows

## deploy.yml — Deploy Backend para OCI (Oracle Cloud Infrastructure)

**Trigger:** Manual via `workflow_dispatch`.

**Jobs:**
1. 🧪 **test** — `./mvnw test` com upload de resultados
2. 🐳 **build-and-push** — Build Docker (`linux/arm64`) + push para GHCR
3. 🚀 **deploy** — Deploy via SSH na VM OCI + health check em loop

**Secrets necessários:** `OCI_VM_HOST`, `OCI_VM_USER`, `OCI_SSH_PRIVATE_KEY`

---

## deploy-gcp.yml — Deploy Backend para Cloud Run

**Trigger:** Push na branch `main` (mudanças em `src/**`, `pom.xml`, `Dockerfile.*`) ou manual via `workflow_dispatch`.

**Jobs:**
1. 🧪 **test** — `./mvnw test` (H2 in-memory, sem PostgreSQL)
2. 🐳 **build** — Build Docker (native ou JVM) + push para GHCR
3. 🚀 **deploy** — `gcloud run deploy` + health check automático

**Secrets necessários no GitHub:**
| Secret | Descrição |
|--------|-----------|
| `GCP_SA_KEY` | JSON da Service Account GCP (roles: `run.admin` + `iam.serviceAccountUser`) |
| `GCP_PROJECT_ID` | ID do projeto GCP (ex: `meu-projeto-123456`) |

**Como criar a Service Account:**
```bash
gcloud iam service-accounts create github-deployer --display-name="GitHub Actions Deployer"
SA_EMAIL="github-deployer@<PROJECT_ID>.iam.gserviceaccount.com"
gcloud projects add-iam-policy-binding <PROJECT_ID> --member="serviceAccount:${SA_EMAIL}" --role="roles/run.admin"
gcloud projects add-iam-policy-binding <PROJECT_ID> --member="serviceAccount:${SA_EMAIL}" --role="roles/iam.serviceAccountUser"
gcloud iam service-accounts keys create gcp-sa-key.json --iam-account=${SA_EMAIL}
# Conteúdo de gcp-sa-key.json → GitHub Secret GCP_SA_KEY
```

**Rollback manual:**
```bash
gcloud run revisions list --service=rpg-api --region=us-central1
gcloud run services update-traffic rpg-api --region=us-central1 --to-revisions=REVISION_NAME=100
```
