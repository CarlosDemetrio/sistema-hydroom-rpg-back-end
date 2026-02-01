#!/bin/bash

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🐳 Docker Compose - Hot Reload Helper"
echo ""

# Função de ajuda
show_help() {
    echo "Uso: ./dev.sh [comando]"
    echo ""
    echo "Comandos disponíveis:"
    echo "  start       - Iniciar todos os serviços (primeira vez usa --build)"
    echo "  stop        - Parar todos os serviços"
    echo "  restart     - Reiniciar todos os serviços"
    echo "  logs        - Ver logs de todos os serviços"
    echo "  logs-be     - Ver logs apenas do backend"
    echo "  logs-fe     - Ver logs apenas do frontend"
    echo "  rebuild     - Reconstruir e reiniciar tudo"
    echo "  rebuild-be  - Reconstruir apenas o backend"
    echo "  rebuild-fe  - Reconstruir apenas o frontend"
    echo "  clean       - Parar e remover containers e volumes"
    echo "  shell-be    - Entrar no container do backend"
    echo "  shell-fe    - Entrar no container do frontend"
    echo "  status      - Ver status dos containers"
    echo ""
}

# Verificar Docker
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker não está rodando!${NC}"
        echo "Por favor, inicie o Docker Desktop e tente novamente."
        exit 1
    fi
}

# Verificar .env
check_env() {
    if [ ! -f .env ]; then
        echo -e "${YELLOW}⚠️  Arquivo .env não encontrado${NC}"
        echo "Criando .env de exemplo..."
        cat > .env << EOF
GOOGLE_CLIENT_ID=your-client-id-here
GOOGLE_CLIENT_SECRET=your-client-secret-here
EOF
        echo -e "${GREEN}✅ .env criado. Configure as credenciais do Google OAuth2.${NC}"
    fi
}

# Comandos
case "$1" in
    start)
        check_docker
        check_env
        echo "🚀 Iniciando serviços..."

        # Verifica se é primeira vez (sem imagens)
        if ! docker images | grep -q "ficha-controlador"; then
            echo "📦 Primeira execução - construindo imagens..."
            docker compose up --build -d
        else
            docker compose up -d
        fi

        echo ""
        echo -e "${GREEN}✅ Serviços iniciados!${NC}"
        echo ""
        echo "📍 URLs disponíveis:"
        echo "   Frontend: http://localhost:4200"
        echo "   Backend:  http://localhost:8080"
        echo "   Swagger:  http://localhost:8080/swagger-ui.html"
        echo ""
        echo "📝 Para ver logs: ./dev.sh logs"
        ;;

    stop)
        echo "🛑 Parando serviços..."
        docker compose stop
        echo -e "${GREEN}✅ Serviços parados${NC}"
        ;;

    restart)
        echo "🔄 Reiniciando serviços..."
        docker compose restart
        echo -e "${GREEN}✅ Serviços reiniciados${NC}"
        ;;

    logs)
        echo "📋 Logs de todos os serviços (Ctrl+C para sair)"
        docker compose logs -f
        ;;

    logs-be)
        echo "📋 Logs do backend (Ctrl+C para sair)"
        docker compose logs -f backend
        ;;

    logs-fe)
        echo "📋 Logs do frontend (Ctrl+C para sair)"
        docker compose logs -f frontend
        ;;

    rebuild)
        echo "🔨 Reconstruindo todos os serviços..."
        docker compose up -d --build
        echo -e "${GREEN}✅ Serviços reconstruídos${NC}"
        ;;

    rebuild-be)
        echo "🔨 Reconstruindo backend..."
        docker compose up -d --build backend
        echo -e "${GREEN}✅ Backend reconstruído${NC}"
        ;;

    rebuild-fe)
        echo "🔨 Reconstruindo frontend..."
        docker compose up -d --build frontend
        echo -e "${GREEN}✅ Frontend reconstruído${NC}"
        ;;

    clean)
        echo "🧹 Limpando tudo..."
        docker compose down -v
        echo -e "${GREEN}✅ Containers e volumes removidos${NC}"
        ;;

    shell-be)
        echo "🐚 Entrando no container do backend..."
        docker exec -it rpg-backend bash
        ;;

    shell-fe)
        echo "🐚 Entrando no container do frontend..."
        docker exec -it rpg-frontend sh
        ;;

    status)
        echo "📊 Status dos containers:"
        docker compose ps
        echo ""
        echo "📊 Uso de recursos:"
        docker stats --no-stream
        ;;

    help|--help|-h|"")
        show_help
        ;;

    *)
        echo -e "${RED}❌ Comando desconhecido: $1${NC}"
        echo ""
        show_help
        exit 1
        ;;
esac
