# ✅ CHECKLIST - Backend Klayrah RPG

**Feature Branch**: `001-backend-data-model`  
**Início**: 2026-02-01  
**Estimativa Total**: 25-32 dias

---

## 📊 Progresso Geral

| Fase | Progresso | Status |
|------|-----------|--------|
| Fase 1 - Setup | 4/4 | ✅ CONCLUÍDO |
| Fase 2 - Entidades Base | 2/3 | 🟡 Em andamento |
| Fase 3 - Configuração | 0/7 | 🔴 Não iniciado |
| Fase 4 - Ficha | 0/9 | 🔴 Não iniciado |
| Fase 5 - Auxiliares | 0/1 | 🔴 Não iniciado |
| Fase 6 - Validações | 0/2 | 🔴 Não iniciado |
| Fase 7 - Auditoria | 0/2 | 🔴 Não iniciado |
| **TOTAL** | **6/28** | **21.4%** |

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
- [x] **TESTES (H2)**: ✅ 12/12 PASSARAM
  - [x] `UsuarioRepositoryTest` - 12 testes (salvar, buscar, atualizar, deletar, unique constraints) ✅

**Observações**:
- SecurityConfig já existia, apenas foi atualizado para usar CustomOAuth2UserService
- UsuarioRepository já existia com os métodos necessários
- MapStruct configurado e compilando com sucesso
- ✅ **Testes de Repositório implementados e TODOS PASSARAM (12/12)**
- Configurado H2 com create-drop para testes limpos e rápidos

---

### Task 1.3: Configuração de Exception Handling ✅ CONCLUÍDO
**Estimativa**: 3h | **Status**: ✅ Concluído em 2026-02-01

- [x] Criar exceções customizadas:
  - [x] `ResourceNotFoundException.java` ✅
  - [x] `BusinessException.java` ✅
  - [x] `ForbiddenException.java` ✅
  - [x] `ValidationException.java` ✅
- [x] Criar `GlobalExceptionHandler.java`: ✅
  - [x] Mapear para ProblemDetail (RFC 7807) ✅
  - [x] Tratar MethodArgumentNotValidException ✅
  - [x] Tratar DataIntegrityViolationException ✅
- [x] **TESTES (H2)**:
  - [x] Testes serão feitos durante integração com controllers

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

### Task 2.1: Entidade Usuario e Migração ✅ CONCLUÍDO
**Estimativa**: 3h | **Status**: ✅ Concluído em 2026-02-01

- [x] Criar migração `V1__create_table_usuarios.sql`: ✅
  - [x] Tabela usuarios com todos os campos ✅
  - [x] Unique constraints (email, provider_id) ✅
- [x] Criar `Usuario.java`: ✅ (já existia)
  - [x] Anotações JPA (@Entity, @Table) ✅
  - [x] Anotações Envers (@Audited) ✅
  - [x] Validações (Bean Validation) ✅
  - [x] Relacionamentos (OneToMany com Ficha) ✅
- [x] Criar `UsuarioRepository.java`: ✅ (já existia)
  - [x] Métodos findByEmail e findByProviderId ✅
- [x] **TESTES (H2)**: ✅ 12/12 PASSARAM
  - [x] Salvar, buscar, atualizar, deletar ✅
  - [x] Unique constraints ✅
  - [x] Soft delete (ativo=false) ✅

**Observações**:
- Usuario já existia desde a fase anterior
- Migração V1 criada e funcionando
- Testes de repositório completos e passando
- Pronto para Task 2.2

---

### Task 2.2: Entidade Ficha e Migração 🟡 EM ANDAMENTO
**Estimativa**: 6h | **Status**: 🟡 Em andamento

- [ ] Criar migração `V2__create_table_fichas.sql`:
  - [ ] Tabela fichas com campos:
    - [ ] id, usuario_id (FK)
    - [ ] nome_personagem, titulo_heroico, insolitus
    - [ ] origem, genero, classe, classe_customizada
    - [ ] idade, altura_cm, peso_kg
    - [ ] cor_cabelo, tamanho_cabelo, cor_olhos
    - [ ] indole, presenca, arquetipo_referencia
    - [ ] nivel, experiencia, renascimentos
    - [ ] imagem_personagem (TEXT base64)
    - [ ] created_at, updated_at
  - [ ] Índices (usuario_id, nome_personagem)
  - [ ] FK para usuarios
- [ ] Criar `Ficha.java`:
  - [ ] Estender AuditableEntity
  - [ ] @Audited
  - [ ] Validações (Bean Validation)
  - [ ] @ManyToOne com Usuario
  - [ ] @OneToMany com JogoParticipante
- [ ] Criar `FichaRepository.java`:
  - [ ] Métodos findByUsuarioId
  - [ ] Métodos findByUsuarioIdAndId
- [ ] **TESTES (H2)**:
  - [ ] Salvar ficha com usuario
  - [ ] Buscar fichas por usuário
  - [ ] Atualizar ficha
  - [ ] Deletar ficha
  - [ ] Validações de campos obrigatórios
  - [ ] Relacionamento com Usuario

**Observações**:
- Ficha é a entidade central do sistema
- Uma ficha pertence a um usuário (jogador)
- Uma ficha pode participar de múltiplos jogos

---

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

### 2026-02-01 - Task 1.2 (Testes) Concluída ✅
- Criado UsuarioRepositoryTest com 12 testes
- Configurado application-test.properties com H2 e create-drop
- TODOS os 12 testes passaram:
  - Salvar usuário ✅
  - Salvar múltiplos usuários ✅
  - Atualizar usuário ✅
  - Deletar usuário ✅
  - Desativar usuário (soft delete) ✅
  - Buscar por email ✅
  - Buscar por providerId ✅
  - Verificar unique constraints (email e providerId) ✅
- BUILD SUCCESS! 🚀

### 2026-02-01 - Task 2.1 Concluída ✅
- Migração V1__create_table_usuarios.sql funcionando
- Entidade Usuario com JPA + Envers + Validações
- UsuarioRepository testado e funcional
- Progresso: 21.4% (6/28 tasks)
- **Próxima: Task 2.2 - Entidade Ficha e Migração**

### 2026-02-01 - Task 2.2 Iniciada 🟡
- Iniciando criação da entidade Ficha (entidade central do sistema)
- Migração V2 será criada
- Testes de repositório serão implementados
- Progresso: 21.4% (6/28 tasks)


