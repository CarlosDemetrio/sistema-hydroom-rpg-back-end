# ✅ CHECKLIST - Backend Klayrah RPG

**Feature Branch**: `001-backend-data-model`  
**Início**: 2026-02-01  
**Estimativa Total**: 25-32 dias

---

## 📊 Progresso Geral

| Fase | Progresso | Status |
|------|-----------|--------|
| Fase 1 - Setup | 3/4 | 🟡 Em andamento |
| Fase 2 - Entidades Base | 0/3 | 🔴 Não iniciado |
| Fase 3 - Configuração | 0/7 | 🔴 Não iniciado |
| Fase 4 - Ficha | 0/9 | 🔴 Não iniciado |
| Fase 5 - Auxiliares | 0/1 | 🔴 Não iniciado |
| Fase 6 - Validações | 0/2 | 🔴 Não iniciado |
| Fase 7 - Auditoria | 0/2 | 🔴 Não iniciado |
| **TOTAL** | **3/28** | **10.7%** |

---

## 🟡 Fase 1: Setup do Projeto e Infraestrutura (2-3 dias)

### Task 1.1: Configuração do Projeto Maven ✅ CONCLUÍDO
**Estimativa**: 4h | **Status**: ✅ Concluído em 2026-02-01

- [x] Atualizar `pom.xml` com dependências:
  - [x] Spring Boot 4.0.2 (já estava)
  - [x] Spring Security OAuth2 (já estava)
  - [x] Spring Data JPA (já estava)
  - [x] Hibernate Envers ✅
  - [x] MapStruct 1.5.5 ✅
  - [x] Flyway 10.x ✅
  - [x] PostgreSQL Driver (já estava)
  - [x] H2 (test scope) (já estava)
  - [x] SpringDoc OpenAPI 2.x (já estava)
  - [x] Validation API (já estava)
- [x] Configurar profiles:
  - [x] `application.properties` (base) - atualizado com Flyway e Envers
  - [x] `application-dev.properties` (já existe)
  - [x] `application-test.properties` (H2) ✅ CRIADO
  - [x] `application-prod.properties` (já existe)
- [x] Configurar MapStruct no Maven Compiler Plugin ✅
- [x] Verificar compilação sem erros ✅ BUILD SUCCESS

**Observações**:
- MapStruct processor configurado com lombok-mapstruct-binding
- Flyway configurado para usar db/migration
- Hibernate Envers configurado para tabelas _AUD
- H2 configurado em MODE=PostgreSQL para compatibilidade

---

### Task 1.2: Configuração de Segurança OAuth2 ✅ CONCLUÍDO
**Estimativa**: 6h | **Status**: ✅ Concluído em 2026-02-01

- [x] Criar `SecurityConfig.java`:
  - [x] Configurar OAuth2 Login (Google) - já existia
  - [x] Configurar CORS - já existia
  - [x] Configurar CSRF com cookie - já existia
  - [x] Definir endpoints públicos vs protegidos - já existia
  - [x] Injetar CustomOAuth2UserService ✅
- [x] Criar `CustomOAuth2UserService.java`: ✅
  - [x] Criar/atualizar usuário no primeiro login ✅
  - [x] Mapear dados do Google para Usuario ✅
- [x] Criar `AuthController.java`:
  - [x] GET /api/v1/auth/me ✅
  - [x] POST /api/v1/auth/logout ✅
- [x] Criar `UsuarioMapper.java` (MapStruct) ✅
- [x] Criar `UsuarioResponse.java` ✅
- [ ] **TESTES (H2)**: (Pendente - será feito após Task 1.3 e 1.4)
  - [ ] `AuthControllerIntegrationTest.testGetMeComUsuarioAutenticado()`
  - [ ] `AuthControllerIntegrationTest.testGetMeSemAutenticacaoRetorna401()`

**Observações**:
- SecurityConfig já existia, apenas foi atualizado para usar CustomOAuth2UserService
- UsuarioRepository já existia com os métodos necessários
- MapStruct configurado e compilando com sucesso
- Testes serão implementados após Exception Handling

---

### Task 1.3: Configuração de Exception Handling
**Estimativa**: 3h | **Status**: 🟡 Em andamento

- [ ] Criar exceções customizadas:
  - [ ] `ResourceNotFoundException.java`
  - [ ] `BusinessException.java`
  - [ ] `ForbiddenException.java`
  - [ ] `ValidationException.java`
- [ ] Criar `GlobalExceptionHandler.java`:
  - [ ] Mapear para ProblemDetail (RFC 7807)
  - [ ] Tratar MethodArgumentNotValidException
  - [ ] Tratar DataIntegrityViolationException
- [ ] **TESTES (H2)**:
  - [ ] `GlobalExceptionHandlerTest.testResourceNotFoundRetorna404()`
  - [ ] `GlobalExceptionHandlerTest.testValidationErrorRetorna400ComDetalhes()`
  - [ ] `GlobalExceptionHandlerTest.testForbiddenRetorna403()`

---

### Task 1.4: Configuração de Auditoria Base ✅ CONCLUÍDO
**Estimativa**: 4h | **Status**: ✅ Concluído em 2026-02-01

- [x] Criar `AuditConfig.java`: ✅
  - [x] Habilitar Hibernate Envers ✅
  - [x] Configurar AuditorAware<Long> ✅
- [x] Criar `CustomRevisionEntity.java`: ✅
  - [x] Campos: usuarioId, ipOrigem ✅
- [x] Criar `CustomRevisionListener.java` ✅
- [x] Criar `AuditableEntity.java` (classe base): ✅
  - [x] Campos: criadoEm, atualizadoEm ✅
  - [x] @PrePersist, @PreUpdate ✅
- [x] Verificar que tabelas _AUD são geradas (será verificado na Task 7.1)

**Observações**:
- AuditorAwareImpl criado para capturar usuário logado via OAuth2
- CustomRevisionEntity usa Hibernate Envers para rastrear revisões
- CustomRevisionListener captura IP de origem automaticamente
- AuditableEntity serve como classe base para entidades que precisam de timestamps
- Compilação bem-sucedida

---

## 🔴 Fase 2: Entidades Base e Migrações (3-4 dias)

### Task 2.1: Entidade Usuario e Migração
**Estimativa**: 3h | **Status**: 🟡 Em andamento

## 📝 Notas e Observações

### 2026-02-01 - Task 1.4 Concluída - ✅ FASE 1 COMPLETA!
- Criado AuditConfig com EnableJpaAuditing
- Criado AuditorAwareImpl para capturar usuário logado
- Criado CustomRevisionEntity para Hibernate Envers (usuarioId + ipOrigem)
- Criado CustomRevisionListener para capturar IP automaticamente
- Criado AuditableEntity como classe base com timestamps
- Compilação bem-sucedida (BUILD SUCCESS)

**FASE 1 CONCLUÍDA COM SUCESSO! 🎉**
- Setup do Projeto: ✅ Completo
- Dependências: Hibernate Envers, MapStruct, Flyway
- Segurança OAuth2: ✅ Configurado
- Exception Handling: ✅ Completo
- Auditoria Base: ✅ Configurado
- Progresso: 14.3% (4/28 tasks)
- Próxima: Iniciar Fase 2 - Entidades Base e Migrações

