#!/bin/bash
# ═══════════════════════════════════════════════════
# 🔄 Rollback para versão anterior da imagem Docker
# ═══════════════════════════════════════════════════
#
# Uso: ./rollback.sh <image_tag>
# Exemplo: ./rollback.sh abc1234   (SHA curto do commit anterior)
#
set -euo pipefail

TAG=${1:?"❌ Uso: ./rollback.sh <image_tag>"}

echo "🔄 Iniciando rollback para tag: $TAG"

cd /opt/app

# Atualizar tag no .env
sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=$TAG/" .env
export IMAGE_TAG=$TAG

# Pull da imagem específica
echo "📦 Pulling imagem com tag $TAG..."
docker compose -f docker-compose.prod.yml pull backend

# Restart
echo "🔄 Restarting backend..."
docker compose -f docker-compose.prod.yml up -d --no-deps backend

# Health check
echo "⏳ Aguardando health check..."
sleep 20

for i in $(seq 1 6); do
    if curl -sf http://localhost:8081/actuator/health/readiness > /dev/null 2>&1; then
        echo "✅ Rollback para $TAG concluído com sucesso!"
        exit 0
    fi
    echo "⏳ Tentativa $i/6..."
    sleep 10
done

echo "❌ Health check falhou após rollback!"
echo "📋 Logs:"
docker compose -f docker-compose.prod.yml logs --tail 50 backend
exit 1
