# Klayrah RPG - Constitution

## Core Principles

### I. Clean Architecture
Todo código segue arquitetura em camadas bem definidas:
- `api/controller`: Controllers REST e Exception Handlers
- `domain/service`: Lógica de negócio pura (sem dependências de framework)
- `domain/model`: Entidades JPA e Enums
- `infrastructure/repository`: Interfaces Spring Data JPA
- `dto`: Records para transferência de dados (Request/Response)
- `config`: Configurações de Bean, Security, etc.

### II. RESTful API Standards
- Todas as APIs seguem padrão RESTful
- Códigos HTTP apropriados (201 criação, 204 deleção, 400 validação, 401 não autenticado, 403 não autorizado)
- Versionamento de API via path (/api/v1/...)
- Documentação OpenAPI obrigatória para todos endpoints

### III. Security First
- OAuth2 para autenticação (Google)
- CSRF via HttpOnly Cookies
- Rate limiting em todos endpoints públicos
- Validação e sanitização de todas entradas
- Soft delete para dados sensíveis (fichas, usuários)
- Nunca expor stack traces em produção

### IV. Test-First Mindset
- Testes unitários para services (JUnit 5 + Mockito)
- Testes de integração para repositories
- Testes de contrato para APIs
- Cobertura mínima: 80% para services

### V. Domain-Driven Design
- Entidades ricas com lógica de negócio (cálculos de atributos, vida, etc.)
- Value Objects para conceitos imutáveis (Atributos, Aptidões)
- Aggregates com raiz clara (Ficha como aggregate root)
- Eventos de domínio para operações cross-cutting

### VI. Simplicity & YAGNI
- Começar simples, evoluir conforme necessário
- Evitar abstrações prematuras
- Preferir composição sobre herança
- Documentar decisões técnicas

## Technology Stack

- **Backend**: Spring Boot 4.0.2, Java 25
- **Database**: PostgreSQL
- **Security**: Spring Security + OAuth2
- **Documentation**: SpringDoc OpenAPI 2.3.0
- **Rate Limiting**: Bucket4j
- **Testing**: JUnit 5, Mockito
- **Build**: Maven

## Development Workflow

1. Feature branches a partir de main
2. Spec primeiro (spec.md)
3. Plan review (plan.md)
4. Implementation com testes
5. Code review obrigatório
6. Merge via Pull Request

## Governance

- Constitution supersede todas outras práticas
- Alterações requerem documentação e aprovação
- Complexidade deve ser justificada
- PRs devem verificar compliance com constitution

**Version**: 1.0.0 | **Ratified**: 2026-02-01 | **Last Amended**: 2026-02-01
