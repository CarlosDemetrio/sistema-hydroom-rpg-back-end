#!/bin/bash

echo "🎮 Ficha Controlador RPG - Script de Setup"
echo "=========================================="
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar se o arquivo .env existe
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠️  Arquivo .env não encontrado!${NC}"
    echo "Criando .env a partir do .env.example..."
    cp .env.example .env
    echo -e "${GREEN}✅ Arquivo .env criado!${NC}"
    echo -e "${RED}⚠️  IMPORTANTE: Edite o arquivo .env com suas credenciais do Google OAuth2!${NC}"
    echo ""
    echo "Para obter as credenciais:"
    echo "1. Acesse https://console.cloud.google.com/"
    echo "2. Crie um projeto ou selecione um existente"
    echo "3. Vá para 'APIs & Services' > 'Credentials'"
    echo "4. Crie 'OAuth 2.0 Client ID'"
    echo "5. Configure as URLs de redirecionamento:"
    echo "   - http://localhost:8080/login/oauth2/code/google"
    echo "   - http://localhost:8080/oauth2/authorization/google"
    echo ""
    read -p "Pressione Enter após configurar o .env para continuar..."
fi

# Carregar variáveis de ambiente
export $(cat .env | grep -v '^#' | xargs)

# Verificar se as credenciais foram configuradas
if [ "$GOOGLE_CLIENT_ID" = "your-google-client-id-here" ]; then
    echo -e "${RED}❌ Você precisa configurar as credenciais do Google no arquivo .env!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Credenciais do Google configuradas!${NC}"
echo ""

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker não está rodando! Por favor, inicie o Docker Desktop.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker está rodando!${NC}"
echo ""

# Opções de execução
echo "Escolha uma opção:"
echo "1) Iniciar apenas PostgreSQL (para desenvolvimento local)"
echo "2) Iniciar todos os serviços (PostgreSQL + Backend + Frontend)"
echo "3) Parar todos os serviços"
echo "4) Ver logs dos serviços"
echo "5) Rebuild e reiniciar serviços"
read -p "Opção [1-5]: " option

case $option in
    1)
        echo -e "${YELLOW}Iniciando PostgreSQL...${NC}"
        docker-compose up -d postgres
        echo -e "${GREEN}✅ PostgreSQL iniciado!${NC}"
        echo ""
        echo "Conecte-se ao banco:"
        echo "  Host: localhost"
        echo "  Port: 5432"
        echo "  Database: rpg_fichas"
        echo "  Username: myuser"
        echo "  Password: secret"
        echo ""
        echo "Para rodar o backend localmente:"
        echo "  ./mvnw spring-boot:run"
        echo ""
        echo "Para rodar o frontend localmente:"
        echo "  cd ../ficha-controlador-front-end/ficha-controlador-front-end"
        echo "  npm install"
        echo "  npm start"
        ;;
    2)
        echo -e "${YELLOW}Iniciando todos os serviços...${NC}"
        docker-compose up -d
        echo -e "${GREEN}✅ Todos os serviços iniciados!${NC}"
        echo ""
        echo "🌐 Acesse:"
        echo "  Frontend: http://localhost"
        echo "  Backend API: http://localhost:8080"
        echo "  Health Check: http://localhost:8080/api/public/health"
        echo ""
        echo "📋 Para ver logs:"
        echo "  docker-compose logs -f"
        ;;
    3)
        echo -e "${YELLOW}Parando todos os serviços...${NC}"
        docker-compose down
        echo -e "${GREEN}✅ Serviços parados!${NC}"
        ;;
    4)
        echo -e "${YELLOW}Mostrando logs (Ctrl+C para sair)...${NC}"
        docker-compose logs -f
        ;;
    5)
        echo -e "${YELLOW}Rebuilding e reiniciando serviços...${NC}"
        docker-compose down
        docker-compose build --no-cache
        docker-compose up -d
        echo -e "${GREEN}✅ Serviços reconstruídos e iniciados!${NC}"
        echo ""
        echo "🌐 Acesse:"
        echo "  Frontend: http://localhost"
        echo "  Backend API: http://localhost:8080"
        ;;
    *)
        echo -e "${RED}Opção inválida!${NC}"
        exit 1
        ;;
esac
