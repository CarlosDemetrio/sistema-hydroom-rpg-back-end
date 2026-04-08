# 🎲 Ficha Controlador - Backend API

Sistema de gerenciamento de fichas de personagens para RPG, com suporte a configurações totalmente customizáveis pelo Mestre.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-25-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-green)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Execução](#execução)
- [Deploy (Produção)](#deploy-produção)
- [Estrutura do Banco de Dados](#estrutura-do-banco-de-dados)
- [Documentação](#documentação)
- [Testes](#testes)

---

## 🎯 Sobre o Projeto

O **Ficha Controlador** é uma API RESTful para gerenciamento completo de fichas de personagens de RPG, com foco em flexibilidade e customização. O Mestre pode configurar TODAS as regras do jogo:

✅ **Atributos personalizados** (Força, Agilidade, etc) com fórmulas configuráveis
✅ **Aptidões** físicas e mentais
✅ **Classes e Raças** com bônus específicos
✅ **Sistema de Níveis e XP** totalmente configurável
✅ **Vantagens compráveis** com sistema de evolução
✅ **Limitadores de atributos** por nível
✅ **Sistema de Vida e Essência** com integridade de membros
✅ **Soft Delete** em todas tabelas principais

**Princípio Fundamental**: **TUDO CONFIGURÁVEL - NADA HARDCODED**

---

## 🛠️ Tecnologias

### Core
- **Java 25** - Linguagem principal
- **Spring Boot 4.0.2** - Framework web
- **Spring Data JPA** - ORM e persistência
- **PostgreSQL 16+** - Banco de dados relacional
- **Flyway** - Versionamento de migrations

### Segurança
- **Spring Security** - Autenticação e autorização
- **OAuth2 Client** - Login social (Google, Facebook)
- **OAuth2 Resource Server** - Proteção de APIs
- **Bucket4j** - Rate limiting

### Utilitários
- **MapStruct 1.5.5** - Mapeamento de DTOs
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI** - Documentação Swagger
- **H2 Database** - Testes in-memory

---

## ✅ Pré-requisitos

Antes de começar, você precisará ter instalado:

- **Java 25** ou superior
- **Maven 3.8+**
- **PostgreSQL 16+** (ou Docker)
- **Docker** (opcional, para ambiente containerizado)

---

## 🚀 Instalação

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/ficha-controlador.git
cd ficha-controlador
```

### 2. Configure o banco de dados

#### Opção A: PostgreSQL Local
```bash
# Crie o banco de dados
psql -U postgres
CREATE DATABASE ficha_controlador;
\q
```

#### Opção B: Docker Compose (Recomendado)
```bash
docker-compose up -d
```

### 3. Execute as migrations
```bash
./mvnw flyway:migrate
```

---

## ▶️ Execução

### Desenvolvimento Local
```bash
./mvnw spring-boot:run
```

### Com Docker
```bash
docker-compose up
```

**Aplicação rodará em**: http://localhost:8080

**Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 🗄️ Estrutura do Banco de Dados

### Resumo
- **29 tabelas** distribuídas em 3 categorias
- **200+ registros** de configuração padrão (seeds)
- **50+ índices** para performance
- **Soft delete** em todas tabelas principais

### Categorias

#### 1. Tabelas Base (4)
- `usuarios`, `jogos`, `jogo_participantes`, `fichas`

#### 2. Tabelas de Configuração (13)
- Atributos, Aptidões, Níveis, Limitadores, Classes, Raças, Vantagens, etc

#### 3. Tabelas de Ficha (9)
- Atributos, Aptidões, Vantagens, Vida, Essência, Prospecção, etc

📊 **Diagrama completo**: [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md)

---

## 📚 Documentação

### Guias Técnicos
- [Flyway Guidelines](docs/FLYWAY_GUIDELINES.md) - Padrões de migrations
- [Database Schema](docs/DATABASE_SCHEMA.md) - Estrutura completa do banco
- [Implementação Concluída](.planning/BACKEND_IMPLEMENTATION_COMPLETE.md)

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 🚀 Deploy (Produção)

O backend é deployado no **Google Cloud Run** via GitHub Actions.

| Recurso | Detalhes |
|---------|----------|
| Plataforma | GCP Cloud Run |
| URL | `https://api.hydroon.com.br` |
| Docker Registry | GCP Artifact Registry |
| CI/CD | `.github/workflows/deploy-gcp.yml` |
| Trigger | Manual via `workflow_dispatch` (GitHub Actions) |
| Memória | 2Gi (JVM: `-Xmx1536m`, ZGC) |
| Dockerfile | `Dockerfile.jvm-cloudrun` |

### Secrets (Secret Manager)
Todos os segredos são injetados via GCP Secret Manager como variáveis de ambiente no Cloud Run. As variáveis necessárias cobrem: conexão com banco de dados, credenciais OAuth2 Google e URLs públicas do frontend/backend.

### Deploy manual
Consulte `.github/workflows/deploy-gcp.yml` para os comandos completos de build, push e deploy.

### Rollback
```bash
gcloud run services update-traffic <SERVICE> \
  --to-revisions PREVIOUS_REVISION=100 \
  --region <REGION> \
  --project <PROJECT>
```

---

## 🧪 Testes

### Executar todos os testes
```bash
./mvnw test
```

**Último resultado**: ✅ 35 tests, 0 failures, 0 errors

---

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/com/hydroom/rpg/fichacontrolador/
│   │   ├── model/           # Entities JPA (29 models)
│   │   ├── repository/      # Spring Data repositories
│   │   ├── service/         # Business logic
│   │   ├── controller/      # REST Controllers
│   │   └── config/          # Configurações Spring
│   └── resources/
│       ├── db/migration/    # Flyway migrations (7 arquivos)
│       └── application.properties
└── test/
    └── java/                # Testes (35 testes)
```

---
