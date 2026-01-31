#!/bin/bash

# Script para rodar o projeto localmente SEM OAuth2 configurado
# Para desenvolvimento rápido quando não precisa testar autenticação

echo "🚀 Iniciando Ficha Controlador - Modo Local (Sem OAuth2)"
echo ""

# Verificar se PostgreSQL está rodando
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "⚠️  PostgreSQL não está rodando!"
    echo ""
    echo "Opções:"
    echo "1. Iniciar com Docker: docker compose up -d"
    echo "2. Instalar PostgreSQL localmente"
    echo ""
    read -p "Deseja iniciar PostgreSQL via Docker? (s/n): " choice

    if [[ "$choice" == "s" || "$choice" == "S" ]]; then
        echo "🐳 Iniciando PostgreSQL com Docker..."
        docker compose up -d postgres
        echo "⏳ Aguardando PostgreSQL iniciar..."
        sleep 5
    else
        echo "❌ PostgreSQL é necessário. Abortando."
        exit 1
    fi
fi

echo "✅ PostgreSQL está rodando"
echo ""

# Verificar se existe .env com as chaves
if [ -f ".env" ]; then
    echo "📄 Carregando variáveis do arquivo .env"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "⚠️  Arquivo .env não encontrado"
    echo ""
    echo "Rodando com valores dummy (OAuth2 não funcionará)"
    echo "Para configurar OAuth2:"
    echo "1. Crie arquivo .env na raiz do projeto"
    echo "2. Adicione:"
    echo "   GOOGLE_CLIENT_ID=seu-client-id"
    echo "   GOOGLE_CLIENT_SECRET=seu-secret"
    echo ""
fi

# Compilar e rodar
echo "🔨 Compilando projeto..."
./mvnw clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Compilação bem-sucedida!"
    echo ""
    echo "🚀 Iniciando aplicação..."
    echo ""
    echo "📍 URLs disponíveis:"
    echo "   API: http://localhost:8080/api/public/health"
    echo "   Swagger: http://localhost:8080/swagger-ui.html"
    echo "   Actuator: http://localhost:8080/actuator/health"
    echo ""

    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-local
else
    echo ""
    echo "❌ Erro na compilação. Verifique os erros acima."
    exit 1
fi
