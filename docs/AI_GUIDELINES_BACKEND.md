# Backend AI Guidelines - Índice Principal

> 📚 **Documentação modular e organizada para facilitar consumo pela IA**

## 🎯 Quick Start

Para implementar uma nova feature:
1. Consulte [01-architecture.md](./backend/01-architecture.md) para estrutura
2. Siga templates em cada arquivo específico
3. Use [09-testing.md](./backend/09-testing.md) para testes de integração

## 📋 Contexto do Projeto
- **Java 25** (com Virtual Threads, Records, Pattern Matching, Sealed Classes)
- **Spring Boot 4.0.2** (RestClient, JdbcClient, Problem Details RFC 9457)
- **PostgreSQL** database
- **OAuth2 Google** authentication with **session-based security** (NÃO JWT!)
- **RESTful API** architecture
- **MapStruct 1.5.5** para mapeamento Entity ↔ DTO

## 📚 Documentação Modular

### [📖 README Principal](./backend/README.md)
Visão geral completa, checklist rápido e links úteis.

### [🏗️ 01. Arquitetura e Estrutura](./backend/01-architecture.md)
- Layer Structure (Controller → Service → Repository)
- Dependency Injection patterns
- Responsabilidades por camada
- Fluxo completo de uma request

### [🏛️ 02. Entities e DTOs](./backend/02-entities-dtos.md)
- Entity patterns com Lombok
- DTO patterns (Request, Response, Update)
- ValidationMessages centralizadas
- Bean Validation annotations

### [⚠️ 03. Exceptions e Error Handling](./backend/03-exceptions.md)
- Hierarquia de exceptions customizadas
- GlobalExceptionHandler completo
- Error response patterns
- HTTP status codes

### [🗄️ 04. Repositories](./backend/04-repositories.md)
- JpaRepository patterns
- Query methods naming convention
- Uso correto de Optional
- Performance tips (N+1, @EntityGraph)

### [⚙️ 05. Services](./backend/05-services.md)
- Business logic patterns
- Transaction management
- Optional vs Exceptions
- Validações de negócio

### [🔄 06. Mappers](./backend/06-mappers.md)
- Entity ↔ DTO conversions
- Por que mapper na controller (não no service)
- Mapper patterns
- MapStruct alternativa

### [🌐 07. Controllers e Swagger](./backend/07-controllers.md)
- RESTful API patterns
- Swagger/OpenAPI documentation completa
- HTTP status codes
- Query parameters e paginação

### [🔐 08. Security](./backend/08-security.md)
- OAuth2 configuration (Session-based, NÃO JWT!)
- SecurityService patterns
- Method security (@PreAuthorize)
- CORS configuration

### [🧪 09. Testing - Prioridade: Integração](./backend/09-testing.md)
- **Testes de integração (80%)** - PRIORIDADE
- Testes unitários (20%) - quando necessário
- Padrão Arrange-Act-Assert
- AssertJ best practices

### [🗃️ 10. Database](./backend/10-database.md)
- Naming conventions (snake_case)
- JPA mapping patterns
- Relationships (ManyToOne, OneToMany)
- Audit fields e soft delete

### [🛡️ 11. OWASP Security](./backend/11-owasp-security.md)
- OWASP Top 10 - 2021 (todas as vulnerabilidades)
- Security best practices por categoria
- Vulnerability prevention
- Security checklist para produção
- Security tools (Dependency Check, SpotBugs)

## ⚡ Templates Rápidos

### Nova Feature - Checklist
```
1. ✅ Entity (02-entities-dtos.md)
2. ✅ Repository (04-repositories.md)
3. ✅ Service (05-services.md)
4. ✅ DTOs (02-entities-dtos.md)
5. ✅ Mapper (06-mappers.md)
6. ✅ Controller (07-controllers.md)
7. ✅ Exception handling (03-exceptions.md)
8. ✅ Testes de integração (09-testing.md)
9. ✅ Security (@PreAuthorize, validações)
```

### Fluxo de Request
```
HTTP POST → Controller
    ↓
    Mapper: DTO → Entity
    ↓
    Service: Business Logic + Validações
    ↓
    Repository: Persist
    ↓
    Service: Return Entity
    ↓
    Mapper: Entity → Response DTO
    ↓
    Controller: ResponseEntity
```

## 🚫 Top 12 Erros Comuns

1. ❌ Lógica de negócio na controller
2. ❌ Expor entities diretamente nas APIs
3. ❌ Mappers no service (devem estar na controller)
4. ❌ Não usar Optional corretamente
5. ❌ Usar exceptions genéricas
6. ❌ Não usar ValidationMessages
7. ❌ Não documentar com JavaDoc e Swagger
8. ❌ Esquecer @Transactional em escritas
9. ❌ Não preferir testes de integração
10. ❌ Usar JWT quando temos session-based auth
11. ❌ Não validar permissões (@PreAuthorize)
12. ❌ Expor stack traces em produção

## 🎓 Princípios Fundamentais

### Separação de Responsabilidades
- **Controller**: Coordena HTTP, valida DTOs, converte com Mapper
- **Service**: TODA lógica de negócio, trabalha com Entities
- **Repository**: Apenas acesso a dados
- **Mapper**: Conversões Entity ↔ DTO (NA CONTROLLER!)

### Tratamento de Erros
- Use exceptions específicas (ResourceNotFoundException, BusinessRuleException, etc.)
- GlobalExceptionHandler centralizado
- Mensagens claras e descritivas
- HTTP status codes apropriados

### Testes
- **PRIORIDADE: Testes de Integração** (80%)
- Testes unitários apenas para lógica complexa isolada (20%)
- Padrão Arrange-Act-Assert
- Verificar persistência no banco

### Segurança
- OAuth2 com **session-based** auth (NÃO JWT!)
- HttpOnly cookies
- @PreAuthorize para permissões
- CORS configurado corretamente

---

## 📖 Navegação Rápida

- **Dúvida sobre estrutura?** → [01-architecture.md](./backend/01-architecture.md)
- **Como criar DTOs?** → [02-entities-dtos.md](./backend/02-entities-dtos.md)
- **Qual exception usar?** → [03-exceptions.md](./backend/03-exceptions.md)
- **Como fazer queries?** → [04-repositories.md](./backend/04-repositories.md)
- **Onde vai lógica de negócio?** → [05-services.md](./backend/05-services.md)
- **Mapper na controller ou service?** → [06-mappers.md](./backend/06-mappers.md)
- **Como documentar API?** → [07-controllers.md](./backend/07-controllers.md)
- **OAuth2 ou JWT?** → [08-security.md](./backend/08-security.md)
- **Como testar?** → [09-testing.md](./backend/09-testing.md)
- **Naming conventions?** → [10-database.md](./backend/10-database.md)
- **Como prevenir vulnerabilidades?** → [11-owasp-security.md](./backend/11-owasp-security.md)

---

**Versão anterior (arquivo único):** Este arquivo foi refatorado em múltiplos arquivos modulares para facilitar o consumo pela IA e manutenção.

## Project Context


## User Roles & API Endpoints

### Roles
- **MESTRE**: Full access, creates games, manages players
- **JOGADOR**: Limited access, only own games and sheets

### API Endpoints Pattern
```
/api/public/*     - Public endpoints (no auth)
/api/user         - Current user info
/api/jogos        - Games management
/api/fichas       - Character sheets
/api/admin/*      - Admin only (MESTRE role)
```

## Dependencies

```xml
<!-- Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```
