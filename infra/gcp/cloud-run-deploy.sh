#!/bin/bash
set -euo pipefail

# =====================================================================
# Deploy inicial do Cloud Run — executar UMA VEZ manualmente
# =====================================================================
# Pre-requisitos:
#   - gcloud CLI instalado e autenticado (gcloud auth login)
#   - Imagem Docker disponivel no GHCR (ghcr.io/carlosdemetrio/ficha-controlador:latest)
#   - Secrets criados no Secret Manager (execute setup-secrets.sh primeiro)
#   - VM e2-micro com PostgreSQL rodando (T4)
# =====================================================================
# Uso: ./cloud-run-deploy.sh [REGION] [ZONE]
# Exemplo: ./cloud-run-deploy.sh us-central1 us-central1-a
# =====================================================================

REGION=${1:-"us-central1"}
ZONE=${2:-"us-central1-a"}
SERVICE_NAME="rpg-api"
IMAGE="ghcr.io/carlosdemetrio/ficha-controlador:latest"

echo "=== Deploy Cloud Run: ${SERVICE_NAME} ==="
echo "Regiao: ${REGION}"
echo "Zona VM DB: ${ZONE}"

# Get INTERNAL IP of VM (NOT public IP!)
echo ""
echo "[1/3] Obtendo IP INTERNO da VM rpg-db..."
DB_INTERNAL_IP=$(gcloud compute instances describe rpg-db \
  --zone="${ZONE}" \
  --format='value(networkInterfaces[0].networkIP)')
echo "IP Interno da VM: ${DB_INTERNAL_IP}"
echo "DATASOURCE_URL: jdbc:postgresql://${DB_INTERNAL_IP}:5432/rpg_fichas"

echo ""
echo "[2/3] Fazendo deploy no Cloud Run com Direct VPC Egress..."
gcloud run deploy "${SERVICE_NAME}" \
  --image "${IMAGE}" \
  --region "${REGION}" \
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
  --set-secrets "BACKEND_URL=rpg-backend-url:latest" \
  --set-secrets "CORS_ALLOWED_ORIGINS=rpg-cors-allowed-origins:latest" \
  --set-secrets "CLOUDINARY_CLOUD_NAME=rpg-cloudinary-cloud-name:latest" \
  --set-secrets "CLOUDINARY_API_KEY=rpg-cloudinary-api-key:latest" \
  --set-secrets "CLOUDINARY_API_SECRET=rpg-cloudinary-api-secret:latest"

echo ""
echo "[3/3] Deploy concluido!"
echo "URL do servico:"
gcloud run services describe "${SERVICE_NAME}" --region="${REGION}" --format='value(status.url)'

echo ""
echo "=== Proximos passos ==="
echo "1. Configurar dominio: gcloud run domain-mappings create --service ${SERVICE_NAME} --domain api.seu-dominio.com --region ${REGION}"
echo "2. DNS: CNAME api -> ghs.googlehosted.com"
echo "3. OAuth2: atualizar redirect URIs no Google Console"
echo "4. Health check: curl https://api.seu-dominio.com/actuator/health"
