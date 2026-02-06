# Project Context for AI Code Generation

> 🤖 **Guias para GitHub Copilot, Cursor, Claude, ChatGPT e outras IAs**

Este diretório contém contexto técnico e guias de melhores práticas para o projeto.

## 📖 Documentação Principal

### Backend (Spring Boot)
**CONSULTE SEMPRE**: [/docs/AI_GUIDELINES_BACKEND.md](../docs/AI_GUIDELINES_BACKEND.md)

Documentação modular completa em `/docs/backend/`:
- **01-architecture.md** - Estrutura de camadas
- **02-entities-dtos.md** - Modelagem de dados
- **03-exceptions.md** - Tratamento de erros
- **04-repositories.md** - Acesso a dados
- **05-services.md** - Lógica de negócio
- **06-mappers.md** - Conversões Entity ↔ DTO
- **07-controllers.md** - APIs REST + Swagger
- **08-security.md** - OAuth2 + Session
- **09-testing.md** - Testes (prioridade: integração)
- **10-database.md** - PostgreSQL + JPA
- **11-owasp-security.md** - Security checklist

### Frontend (Angular)
**CONSULTE**: [/ficha-controlador-front-end/.github/copilot-instructions.md](../../ficha-controlador-front-end/ficha-controlador-front-end/.github/copilot-instructions.md)

## 🎯 Objetivo

Garantir código de alta qualidade seguindo:
- ✅ Melhores práticas da indústria
- ✅ APIs mais recentes (não deprecated)
- ✅ Padrões de segurança (OWASP)
- ✅ Arquitetura limpa e testável

## 📚 Guias Técnicos Específicos

### 1. [Spring Boot 4](./spring-boot-4.md)
- Virtual Threads (Project Loom)
- Problem Details (RFC 9457)
- RestClient (novo)
- JdbcClient (novo)
- Observability
- **IMPORTANTE:** SEM JPA Converters, SEM JSON columns

### 2. [MapStruct](./mapstruct.md)
- Mapeamento compile-time
- Integração com Spring
- Patterns de Entity ↔ DTO
- **IMPORTANTE:** Use MapStruct SEMPRE ao invés de JPA Converters

### 4. [Bucket4j](./bucket4j.md)
- Rate limiting
- APIs mais recentes (8.10.1+)
- **IMPORTANTE:** Usar builder pattern, NÃO usar `Refill.of()` (deprecated)

## 🚫 O Que NÃO Fazer

### ❌ JPA Converters
```java
// ❌ ERRADO
@Converter
public class AtributosConverter implements AttributeConverter<Atributos, String> { }
```

### ❌ Colunas JSON
```sql
-- ❌ ERRADO
atributos_json TEXT
```

### ❌ APIs Deprecated
```java
// ❌ ERRADO - Bucket4j deprecated
Refill.of(100, Duration.ofHours(1))

// ❌ ERRADO - RestTemplate deprecated
RestTemplate restTemplate = new RestTemplate();
```

## ✅ O Que SEMPRE Fazer

### ✅ Tabelas Normalizadas
```sql
-- ✅ CORRETO
CREATE TABLE ficha_atributo (
    id BIGINT PRIMARY KEY,
    ficha_id BIGINT,
    atributo_config_id BIGINT,
    base INT,
    nivel INT,
    outros_bonus INT
);
```

### ✅ MapStruct para Mapeamento
```java
// ✅ CORRETO
@Mapper(componentModel = "spring")
public interface FichaMapper {
    FichaResponse toResponse(Ficha ficha);
}
```

### ✅ Builder Pattern (APIs modernas)
```java
// ✅ CORRETO - Bucket4j
Bandwidth limit = Bandwidth.builder()
    .capacity(100)
    .refillGreedy(100, Duration.ofHours(1))
    .build();
```

## 🏗️ Estrutura do Projeto

```
src/main/java
├── config/         # Spring configurations
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # JPA repositories
├── model/          # JPA entities
├── dto/            # Data Transfer Objects
├── mapper/         # MapStruct mappers (NOT converters!)
├── exception/      # Custom exceptions
└── constants/      # Constants and messages
```

## 📝 Princípios de Design

1. **Tudo Configurável** - Nenhum valor hardcoded, tudo em tabelas
2. **Banco Normalizado** - Sem colunas JSON
3. **MapStruct** - Mapeamento compile-time
4. **Histórico** - Hibernate Envers para auditoria
5. **Mínimo de Enums** - Apenas tipos fixos do sistema
6. **APIs Modernas** - Sempre usar versões não-deprecated

## 🔍 Como Usar

Quando um AI gerar código, ele deve:

1. **Ler estes guias** antes de gerar código
2. **Verificar** se está usando APIs deprecated
3. **Seguir** os padrões estabelecidos
4. **Evitar** anti-patterns listados

## 🚀 Tecnologias

| Tecnologia | Versão | Guia |
|------------|--------|------|
| Spring Boot | 4.0.2 | [spring-boot-4.md](./spring-boot-4.md) |
| Spring Framework | 7.0.3 | [spring-boot-4.md](./spring-boot-4.md) |
| MapStruct | 1.5.5 | [mapstruct.md](./mapstruct.md) |
| Flyway | Latest | [flyway.md](./flyway.md) |
| Bucket4j | 8.10.1+ | [bucket4j.md](./bucket4j.md) |
| Hibernate Envers | 7.2.1 | [spring-boot-4.md](./spring-boot-4.md) |

## 📖 Leitura Obrigatória

Antes de gerar código para este projeto, leia:

1. [Spring Boot 4 - Best Practices](./spring-boot-4.md)
2. [MapStruct - Mapeamento](./mapstruct.md)
3. [Flyway - Migrations](./flyway.md)
4. [Bucket4j - Rate Limiting](./bucket4j.md)

## 🎓 Referências Externas

- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Bucket4j Documentation](https://bucket4j.com/)

---

**Última atualização:** 2026-02-06  
**Java 25 + Spring Boot 4.0.2**
