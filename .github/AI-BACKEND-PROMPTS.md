# ☕ Prompts para Java 25 & Spring Boot

## 1. Gerar Novo Recurso CRUD
> "Crie um CRUD completo para a entidade [NOME].
> - Use Spring Data JPA com PostgreSQL.
> - Use Records para os DTOs de Request e Response.
> - Implemente uma Service que utilize Virtual Threads se houver processamento pesado.
> - Inclua validações Bean Validation (@NotNull, @Size, etc)."

## 2. Gerar Testes de Integração (Testcontainers)
> "Crie um teste de integração para o Controller [NOME].
> - Use Testcontainers com PostgreSQL.
> - Use MockMvc para performar as requisições.
> - Garanta que o contexto do Spring carregue corretamente."

## 3. Configuração de Integração AWS
> "Configure a integração com o AWS [S3/SQS/SecretsManager] usando Spring Cloud AWS.
> - Forneça a classe de configuração usando `@Configuration`.
> - Use variáveis de ambiente para as credenciais e região.
> - Crie um serviço de exemplo que utilize essa integração."

## 4. Migração Flyway
> "Crie um arquivo de migração V1__create_table_[NOME].sql compatível com PostgreSQL, incluindo índices para busca e chaves estrangeiras."
