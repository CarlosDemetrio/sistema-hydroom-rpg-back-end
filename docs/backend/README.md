# Backend Guidelines - Índice

## 📋 Visão Geral

Documentação completa de padrões e boas práticas para o backend Spring Boot.

## 🏗️ Contexto do Projeto
- **Java 25** (Virtual Threads, Records, Pattern Matching, Sealed Classes)
- **Spring Boot 4.0.2** (RestClient, JdbcClient, Problem Details RFC 9457)
- **PostgreSQL** database
- **OAuth2 Google** authentication with session-based security
- **RESTful API** architecture
- **MapStruct 1.5.5** para mapeamento compile-time

## 📚 Documentos

### 1. [Arquitetura e Estrutura](./01-architecture.md)
- Layer Structure (Controller → Service → Repository)
- Dependency Injection patterns
- Project structure

### 2. [Entities e DTOs](./02-entities-dtos.md)
- Entity patterns com Lombok
- DTO patterns e validações
- ValidationMessages centralizadas
- Boas práticas de modelagem

### 3. [Exceptions e Error Handling](./03-exceptions.md)
- Hierarquia de exceptions customizadas
- GlobalExceptionHandler
- Error response patterns
- HTTP status codes

### 4. [Repositories](./04-repositories.md)
- JpaRepository patterns
- Query methods
- Uso correto de Optional
- Best practices

### 5. [Services](./05-services.md)
- Business logic patterns
- Transaction management
- Optional vs Exceptions
- Validações de negócio

### 6. [Mappers](./06-mappers.md)
- Entity ↔ DTO conversions
- Mapper patterns
- Onde usar mappers (controller vs service)

### 7. [Controllers](./07-controllers.md)
- RESTful API patterns
- Swagger/OpenAPI documentation
- Request/Response handling
- HTTP status codes

### 8. [Security](./08-security.md)
- OAuth2 configuration
- Session-based auth (NÃO JWT!)
- CORS configuration
- Security best practices

### 9. [Testing](./09-testing.md)
- **Prioridade: Testes de Integração**
- Integration tests patterns
- Unit tests (quando necessário)
- Test best practices

### 10. [Database](./10-database.md)
- Naming conventions
- Migration strategies
- JPA configurations

### 11. [OWASP Security](./11-owasp-security.md)
- OWASP Top 10 - 2021
- Security best practices
- Vulnerability prevention
- Security checklist

## ⚡ Quick Reference

### Fluxo de uma Request
```
HTTP Request → Controller → Mapper → Service → Repository → Database
                    ↓           ↓         ↓
                  @Valid     Entity   Business
                   DTO                 Logic
```

### Checklist Rápido
- ✅ Controller apenas coordena (thin)
- ✅ Service contém toda lógica de negócio (fat)
- ✅ Mapper SEMPRE na controller
- ✅ Service trabalha com Entities
- ✅ Controller trabalha com DTOs
- ✅ SEMPRE use Optional para retornos nullable
- ✅ SEMPRE use exceptions específicas
- ✅ SEMPRE documente com JavaDoc
- ✅ SEMPRE documente API com Swagger
- ✅ SEMPRE use ValidationMessages
- ✅ PREFIRA testes de integração
- ✅ SEMPRE valide permissões (@PreAuthorize)
- ✅ SEMPRE use HTTPS em produção

## 🚫 Common Mistakes

As 20 falhas mais comuns estão documentadas em cada seção específica.

**Top 5:**
1. ❌ Lógica de negócio na controller
2. ❌ Expor entities diretamente
3. ❌ Mappers no service
4. ❌ Não usar Optional corretamente
5. ❌ Não preferir testes de integração

## 📖 Como Usar Esta Documentação

1. **Para IA/Copilot**: Consulte a seção específica que precisa
2. **Para código novo**: Siga os templates de cada seção
3. **Para revisão**: Use os checklists de cada documento
4. **Para dúvidas**: Consulte os exemplos práticos

## 🔗 Links Úteis

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [Bean Validation](https://beanvalidation.org/)
