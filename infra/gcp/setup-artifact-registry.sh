#!/bin/bash
set -euo pipefail

# =====================================================================
# Setup GCP Artifact Registry — executar UMA VEZ antes do primeiro deploy
# =====================================================================
# Pre-requisitos:
#   - gcloud CLI instalado e autenticado (gcloud auth login)
#   - Projeto GCP configurado (gcloud config set project hydroon-rpg)
#   - Artifact Registry API habilitada
# =====================================================================
# Uso: ./setup-artifact-registry.sh [SA_EMAIL]
#   SA_EMAIL = email da Service Account usada no GitHub Actions (GCP_SA_KEY)
#   Se não informado, tenta detectar automaticamente.
# =====================================================================

PROJECT_ID=$(gcloud config get-value project 2>/dev/null)
LOCATION="us-east1"
REPO_NAME="ficha-controlador"
SA_EMAIL="${1:-}"

echo "=== Setup Artifact Registry ==="
echo "Projeto: ${PROJECT_ID}"
echo "Localização: ${LOCATION}"
echo "Repositório: ${REPO_NAME}"
echo ""

# --- Step 1: Habilitar API do Artifact Registry ---
echo "[1/4] Habilitando Artifact Registry API..."
gcloud services enable artifactregistry.googleapis.com --quiet
echo "  ✅ API habilitada"

# --- Step 2: Criar repositório Docker ---
echo ""
echo "[2/4] Verificando/criando repositório Docker..."
if gcloud artifacts repositories describe "${REPO_NAME}" --location="${LOCATION}" &>/dev/null; then
    echo "  ✅ Repositório já existe"
else
    gcloud artifacts repositories create "${REPO_NAME}" \
        --repository-format=docker \
        --location="${LOCATION}" \
        --description="Docker images do backend ficha-controlador" \
        --quiet
    echo "  ✅ Repositório criado: ${LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}"
fi

# --- Step 3: Detectar Service Account se não informada ---
echo ""
echo "[3/4] Configurando permissões da Service Account..."
if [ -z "${SA_EMAIL}" ]; then
    echo "  SA_EMAIL não informado. Listando Service Accounts do projeto..."
    echo ""
    gcloud iam service-accounts list --format="table(email, displayName)"
    echo ""
    read -rp "Informe o email da SA usada no GitHub Actions (GCP_SA_KEY): " SA_EMAIL
fi

if [ -z "${SA_EMAIL}" ]; then
    echo "  ❌ SA_EMAIL não informado. Abortando."
    exit 1
fi

echo "  Service Account: ${SA_EMAIL}"

# --- Step 4: Conceder permissão de escrita no Artifact Registry ---
echo ""
echo "[4/4] Concedendo roles/artifactregistry.writer..."
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SA_EMAIL}" \
    --role="roles/artifactregistry.writer" \
    --quiet

echo "  ✅ Permissão concedida"

echo ""
echo "=== Setup concluído! ==="
echo ""
echo "Repositório: ${LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}"
echo "Service Account: ${SA_EMAIL} (com artifactregistry.writer)"
echo ""
echo "=== Verificação ==="
echo "Execute o deploy-gcp.yml no GitHub Actions para testar."
echo ""
echo "=== Troubleshooting ==="
echo "Se o erro persistir, verifique:"
echo "  1. O secret GCP_SA_KEY no GitHub contém a chave JSON da SA: ${SA_EMAIL}"
echo "  2. O secret GCP_PROJECT_ID no GitHub contém: ${PROJECT_ID}"
echo "  3. A SA tem permissão de 'Artifact Registry Writer' no projeto"
echo "     Verificar: gcloud artifacts repositories get-iam-policy ${REPO_NAME} --location=${LOCATION}"
