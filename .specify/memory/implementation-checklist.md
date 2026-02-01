# 📋 Checklist de Implementação - Backend Ficha Controlador

**Última Atualização**: 2026-02-01 11:45  
**Branch**: `001-backend-data-model`  
**Status Geral**: 50% Concluído (14/28 tasks)

---

## ✅ Fase 1: Setup do Projeto (4/4 - 100%)

- [x] **1.1** Setup inicial do projeto Spring Boot
  - [x] Configurar pom.xml com todas as dependências
  - [x] Configurar profiles (dev, test, prod)
  - [x] Configurar H2 para testes
  - [x] Configurar PostgreSQL para dev/prod

- [x] **1.2** Configurar segurança OAuth2
  - [x] Spring Security + OAuth2 Client
  - [x] Google OAuth2 Provider
  - [x] CustomOAuth2UserService
  - [x] SecurityConfig com CORS e CSRF
  - [x] Rate Limiting com Bucket4j

- [x] **1.3** Configurar exception handling global
  - [x] GlobalExceptionHandler
  - [x] ResourceNotFoundException
  - [x] BusinessException
  - [x] ForbiddenException
  - [x] ValidationException
  - [x] ProblemDetail (RFC 7807)

- [x] **1.4** Configurar auditoria (Hibernate Envers)
  - [x] AuditConfig
  - [x] AuditableEntity
  - [x] CustomRevisionEntity
  - [x] CustomRevisionListener
  - [x] Captura de usuário e IP

---

## ✅ Fase 2: Entidades Base (3/3 - 100%)

- [x] **2.1** Entidade Usuario
  - [x] Criar model Usuario
  - [x] Migrations (V1.01, V1.02)
  - [x] Repository com queries customizadas
  - [x] DTOs (UsuarioResponse, UsuarioUpdate)
  - [x] Mapper (MapStruct)
  - [x] ValidationMessages
  - [x] ✅ **12 testes passando**

- [x] **2.2** Entidade Jogo
  - [x] Criar model Jogo
  - [x] Migrations (V1.03, V1.04)
  - [x] Repository
  - [x] DTOs (JogoRequest, JogoResponse)
  - [x] Mapper
  - [x] Relacionamento com Usuario (mestre)
  - [x] ✅ **10 testes passando**

- [x] **2.3** Entidade JogoParticipante (N:M)
  - [x] Criar model JogoParticipante
  - [x] Migrations (V1.05, V1.06)
  - [x] Repository
  - [x] DTOs
  - [x] Mapper
  - [x] Constraint unique (jogoId, usuarioId)
  - [x] ✅ **12 testes passando**

---

## ✅ Fase 3: Entidades de Configuração (7/7 - 100%)

- [x] **3.1** Entidade Atributo
  - [x] Criar model Atributo
  - [x] Migration (V1.07 - config_tables)
  - [x] Migration auditoria (V1.08)
  - [x] Seed com 7 atributos (V1.09)
    - [x] Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia
  - [x] Repository
  - [x] DTOs e Mapper

- [x] **3.2** Entidade Aptidao
  - [x] Criar model Aptidao (categoria: FISICA, MENTAL)
  - [x] Migration (incluída em V1.07)
  - [x] Seed com 24 aptidões (12 físicas + 12 mentais)
  - [x] Repository
  - [x] DTOs e Mapper

- [x] **3.3** Entidade ClassePersonagem
  - [x] Criar model ClassePersonagem
  - [x] Migration (incluída em V1.07)
  - [x] Seed com 12 classes (V1.10)
    - [x] Guerreiro, Arqueiro, Monge, Berserker, Assassino, Fauno
    - [x] Mago, Feiticeiro, Necromante, Sacerdote, Ladrão, Negociante
  - [x] Repository
  - [x] DTOs e Mapper

- [x] **3.4** Entidade Raca
  - [x] Criar model Raca
  - [x] Migration (incluída em V1.07)
  - [x] Repository
  - [x] DTOs e Mapper

- [x] **3.5** Entidade TabelaExperiencia
  - [x] Criar model TabelaExperiencia
  - [x] Migration (incluída em V1.07)
  - [x] Seed com 35 níveis (0 a 35)
  - [x] Constraint unique (jogoId, nivel)
  - [x] Repository
  - [x] DTOs e Mapper

- [x] **3.6** Entidade Limitador
  - [x] Criar model Limitador
  - [x] Migration (incluída em V1.07)
  - [x] Seed com 6 faixas de limitadores
    - [x] 0-1: 10, 2-20: 50, 21-25: 75, 26-30: 100, 31-35: 120, 36+: Renascimento
  - [x] Repository
  - [x] DTOs e Mapper

- [x] **3.7** Entidade Vantagem
  - [x] Criar model Vantagem
  - [x] Migration (incluída em V1.07)
  - [x] Repository
  - [x] DTOs e Mapper

---

## 🔄 Fase 4: Ficha de Personagem (0/3 - 0%)

- [ ] **4.1** Entidade Ficha (Principal)
  - [ ] Criar model Ficha
  - [ ] Campos: jogador, personagem, titulo_heroico, insolitus, origem, genero, classe, raca
  - [ ] Campos físicos: idade, altura, peso, cor_cabelo, tamanho_cabelo, cor_olhos
  - [ ] Campos personalidade: indole, presenca, arquetipo
  - [ ] Campos de jogo: experiencia, nivel, renascimentos
  - [ ] Migrations (Ficha + Ficha_aud)
  - [ ] Repository com queries para buscar por jogo/usuario
  - [ ] DTOs (FichaRequest, FichaResponse, FichaUpdate)
  - [ ] Mapper
  - [ ] **Testes de integração com H2**

- [ ] **4.2** Tabelas de Valores (Ficha_Atributo, Ficha_Aptidao, Ficha_Bonus)
  - [ ] Criar model FichaAtributo (ficha, atributo, valor_base, valor_nivel, valor_outros)
  - [ ] Criar model FichaAptidao (ficha, aptidao, valor_base, valor_sorte, valor_classe)
  - [ ] Criar model FichaBonus (ficha, tipo_bonus, bonus_base, vantagens, classe, itens, gloria, outros)
  - [ ] Migrations (3 tabelas + 3 _aud)
  - [ ] Repositories
  - [ ] DTOs e Mappers
  - [ ] **Testes de integração com H2**

- [ ] **4.3** Tabelas de Vida, Essência e Ameaça
  - [ ] Criar model FichaVida (ficha, total, dano_cabeca, dano_tronco, dano_bracos, dano_pernas, dano_sangue)
  - [ ] Criar model FichaEssencia (ficha, total, gastos)
  - [ ] Criar model FichaAmeaca (ficha, total)
  - [ ] Migrations (3 tabelas + 3 _aud)
  - [ ] Repositories
  - [ ] DTOs e Mappers
  - [ ] **Testes de integração com H2**

---

## 🔄 Fase 5: Services da Ficha (0/3 - 0%)

- [ ] **5.1** FichaService (CRUD básico)
  - [ ] Criar FichaService
  - [ ] criar(FichaRequest) - Validar permissões
  - [ ] buscarPorId(id) - Com todos os relacionamentos
  - [ ] buscarPorJogo(jogoId) - Apenas fichas do jogo
  - [ ] buscarPorJogador(usuarioId, jogoId)
  - [ ] atualizar(id, FichaUpdate) - Validar permissões
  - [ ] deletar(id) - Soft delete
  - [ ] **Testes unitários com Mockito**

- [ ] **5.2** FichaAtributoService
  - [ ] Criar FichaAtributoService
  - [ ] atualizarAtributo(fichaId, atributoId, valores)
  - [ ] calcularImpeto(atributoId, total) - Usar fórmula da config
  - [ ] validarLimitador(nivel, valorAtributo)
  - [ ] validarPontosAtributo(nivel, pontosGastos)
  - [ ] **Testes unitários**

- [ ] **5.3** FichaCalculoService (Lógica de Negócio)
  - [ ] calcularNivelPorExperiencia(experiencia, jogoId)
  - [ ] calcularBonusBase(atributos) - BBA, BBM, Bloqueio, Reflexo, etc.
  - [ ] calcularVidaTotal(vigor, nivel, renascimentos, vantagens, outros)
  - [ ] calcularVidaMembro(vidaTotal, membro) - Porcentagens
  - [ ] calcularEssenciaTotal(vigor, sabedoria, nivel, renascimentos, vantagens)
  - [ ] calcularAmeacaTotal(nivel, itens, titulos, renascimentos)
  - [ ] **Testes unitários com casos de borda**

---

## 🔄 Fase 6: Controllers (0/3 - 0%)

- [ ] **6.1** FichaController
  - [ ] POST /api/v1/fichas - Criar ficha
  - [ ] GET /api/v1/fichas/{id} - Buscar por ID
  - [ ] GET /api/v1/fichas?jogoId=X - Listar por jogo
  - [ ] PUT /api/v1/fichas/{id} - Atualizar ficha
  - [ ] DELETE /api/v1/fichas/{id} - Deletar ficha
  - [ ] Validações de segurança (só mestre ou dono)
  - [ ] **Testes de integração com MockMvc**

- [ ] **6.2** FichaAtributoController
  - [ ] PUT /api/v1/fichas/{id}/atributos/{atributoId} - Atualizar atributo
  - [ ] GET /api/v1/fichas/{id}/atributos - Listar todos
  - [ ] **Testes de integração**

- [ ] **6.3** Controllers de Configuração (CRUD)
  - [ ] AtributoController
  - [ ] AptidaoController
  - [ ] ClassePersonagemController
  - [ ] RacaController
  - [ ] TabelaExperienciaController
  - [ ] LimitadorController
  - [ ] VantagemController
  - [ ] Todos apenas para Mestre do jogo
  - [ ] **Testes de integração**

---

## 🔄 Fase 7: Documentação e Deploy (0/5 - 0%)

- [ ] **7.1** Documentação OpenAPI
  - [ ] Configurar Springdoc
  - [ ] Adicionar @Operation em todos os endpoints
  - [ ] Adicionar @Schema nos DTOs
  - [ ] Testar swagger-ui

- [ ] **7.2** README.md completo
  - [ ] Como rodar localmente
  - [ ] Como rodar testes
  - [ ] Como configurar OAuth2
  - [ ] Arquitetura do projeto
  - [ ] Endpoints disponíveis

- [ ] **7.3** Docker Compose
  - [ ] Dockerfile do backend
  - [ ] docker-compose.yml (PostgreSQL + backend)
  - [ ] Scripts de inicialização

- [ ] **7.4** CI/CD
  - [ ] GitHub Actions para testes
  - [ ] GitHub Actions para deploy (se aplicável)

- [ ] **7.5** Validação Final
  - [ ] Todos os testes passando
  - [ ] Cobertura de testes > 80%
  - [ ] Zero warnings de compilação
  - [ ] Flyway funcionando em produção
  - [ ] OAuth2 funcionando em produção

---

## 📊 Estatísticas

### Por Fase
- ✅ Fase 1: 100% (4/4)
- ✅ Fase 2: 100% (3/3)
- ✅ Fase 3: 100% (7/7)
- 🔄 Fase 4: 0% (0/3)
- 🔄 Fase 5: 0% (0/3)
- 🔄 Fase 6: 0% (0/3)
- 🔄 Fase 7: 0% (0/5)

### Global
**Progresso Total**: 50% (14/28 tasks concluídas)

### Testes
- ✅ UsuarioRepositoryTest: 12/12 passando
- ✅ JogoRepositoryTest: 10/10 passando
- ✅ JogoParticipanteRepositoryTest: 12/12 passando
- ✅ FichaControladorApplicationTests: 1/1 passando
- **Total**: 35/35 testes passando (100%)

### Qualidade
- ✅ Zero erros de compilação
- ✅ Zero warnings críticos
- ✅ Flyway: 10 migrations executadas
- ✅ MapStruct: 10 mappers gerados
- ✅ Auditoria: 100% das entidades auditadas

---

## 🎯 Próxima Task

**Task 4.1**: Entidade Ficha (Principal)
- Criar a entidade central do sistema
- Migration com todos os campos
- Repository com queries otimizadas
- DTOs completos
- Mapper com relacionamentos
- **PRIORIDADE**: Testes de integração com H2

---

**Data de Criação**: 2026-02-01  
**Última Atualização**: 2026-02-01 11:45  
**Responsável**: Carlos Demétrio
