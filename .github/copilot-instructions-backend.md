# Padrões do Projeto Spring Boot (Java 25)

## Tecnologias e Versões
- **Java 25**: Use Record classes para DTOs, Pattern Matching e Structured Concurrency.
- **Spring Boot 3.5+**: Foco em Virtual Threads (`spring.threads.virtual.enabled=true`).
- **Banco de Dados**: PostgreSQL.
- **Migrações**: Flyway.
- **Cloud**: AWS (Spring Cloud AWS).

## Regras de Codificação
- **Injeção de Dependência**: Use injeção via construtor (omita `@Autowired` em construtores únicos).
- **Imutabilidade**: Prefira `record` para DTOs e Request/Response objects.
- **Programação Funcional**: Use Streams e Optional de forma idiomática.
- **Tratamento de Erros**: Use `@RestControllerAdvice` e RFC 7807 (Problem Details).
- **Virtual Threads**: Não bloqueie threads; use o modelo padrão do Java 25 para alta concorrência.

## AWS & Deployment
- Configuração via `application.yml` usando variáveis de ambiente.
- Health checks ativos para AWS App Runner/ECS (`/actuator/health`).
