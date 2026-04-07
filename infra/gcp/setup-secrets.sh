#!/bin/bash
set -euo pipefail

# =====================================================================
# Setup GCP Secret Manager — executar UMA VEZ antes do primeiro deploy
# =====================================================================
# Pre-requisitos:
#   - gcloud CLI instalado e autenticado
#   - Projeto GCP configurado (gcloud config set project PROJECT_ID)
#   - Secret Manager API habilitada
# =====================================================================
# Uso: ./setup-secrets.sh
# Os valores dos secrets serao solicitados interativamente
# =====================================================================

echo "=== Setup GCP Secret Manager ==="
echo "Projeto: $(gcloud config get-value project)"
echo ""

# Function to create or update a secret
create_secret() {
    local SECRET_NAME="$1"
    local SECRET_VALUE="$2"

    if gcloud secrets describe "${SECRET_NAME}" &>/dev/null; then
        echo "[UPDATE] ${SECRET_NAME}"
        echo -n "${SECRET_VALUE}" | gcloud secrets versions add "${SECRET_NAME}" --data-file=-
    else
        echo "[CREATE] ${SECRET_NAME}"
        echo -n "${SECRET_VALUE}" | gcloud secrets create "${SECRET_NAME}" --data-file=-
    fi
}

# Collect secret values interactively
echo "=== Informe os valores dos secrets ==="
echo "(Os valores NAO sao mostrados na tela)"
echo ""

read -rsp "DB_USERNAME (ex: rpg_prod_user): " DB_USERNAME; echo
read -rsp "DB_PASSWORD (senha forte): " DB_PASSWORD; echo
read -rsp "GOOGLE_CLIENT_ID: " GOOGLE_CLIENT_ID; echo
read -rsp "GOOGLE_CLIENT_SECRET: " GOOGLE_CLIENT_SECRET; echo
read -rsp "FRONTEND_URL (ex: https://seu-dominio.com): " FRONTEND_URL; echo
read -rsp "BACKEND_URL (ex: https://api.seu-dominio.com): " BACKEND_URL; echo

echo ""
echo "=== Criando secrets no Secret Manager ==="
create_secret "rpg-db-username" "${DB_USERNAME}"
create_secret "rpg-db-password" "${DB_PASSWORD}"
create_secret "rpg-google-client-id" "${GOOGLE_CLIENT_ID}"
create_secret "rpg-google-client-secret" "${GOOGLE_CLIENT_SECRET}"
create_secret "rpg-frontend-url" "${FRONTEND_URL}"
create_secret "rpg-backend-url" "${BACKEND_URL}"

echo ""
echo "=== Configurando permissoes para o Cloud Run Service Account ==="
PROJECT_NUMBER=$(gcloud projects describe "$(gcloud config get-value project)" --format='value(projectNumber)')
SA_EMAIL="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
echo "Service Account: ${SA_EMAIL}"

for SECRET in rpg-db-username rpg-db-password rpg-google-client-id rpg-google-client-secret rpg-frontend-url rpg-backend-url; do
    gcloud secrets add-iam-policy-binding "${SECRET}" \
        --member="serviceAccount:${SA_EMAIL}" \
        --role="roles/secretmanager.secretAccessor" \
        --quiet
    echo "  [OK] ${SECRET}"
done

echo ""
echo "=== Setup concluido! ==="
echo "Execute cloud-run-deploy.sh para fazer o primeiro deploy."
