# ✅ CHECKLIST - Backend Klayrah RPG

**Feature Branch**: `001-backend-data-model`  
**Início**: 2026-02-01  
**Estimativa Total**: 25-32 dias

---

## 📊 Progresso Geral

| Fase | Progresso | Status |
|------|-----------|--------|
| Fase 1 - Setup | 4/4 | ✅ Concluído |
| Fase 2 - Entidades Base | 3/3 | ✅ Concluído |
| Fase 3 - Configuração | 7/7 | ✅ Concluído |
| Fase 4 - Ficha | 0/9 | 🔴 Não iniciado |
| Fase 5 - Auxiliares | 0/1 | 🔴 Não iniciado |
| Fase 6 - Validações | 0/2 | 🔴 Não iniciado |
| Fase 7 - Auditoria | 0/2 | 🔴 Não iniciado |
| **TOTAL** | **14/28** | **50%** |

---

## ✅ Fase 1: Setup do Projeto e Infraestrutura (2-3 dias) - CONCLUÍDO

### Task 1.1: Configuração do Projeto Maven
**Estimativa**: 4h | **Status**: ✅ Concluído

- [x] Atualizar `pom.xml` com dependências:
  - [x] Spring Boot 4.0.2
  - [x] Spring Security OAuth2
  - [x] Spring Data JPA
  - [x] Hibernate Envers
  - [x] MapStruct 1.5.5
  - [x] Flyway 10.x
  - [x] PostgreSQL Driver
  - [x] H2 (test scope)
  - [x] SpringDoc OpenAPI 2.x
  - [x] Validation API
- [x] Configurar profiles:
  - [x] `application.properties` (base)
  - [x] `application-dev.properties`
  - [x] `application-test.properties` (H2)
  - [x] `application-prod.properties`
- [x] Configurar MapStruct no Maven Compiler Plugin
- [x] Verificar compilação sem erros

---

### Task 1.2: Configuração de Segurança OAuth2
**Estimativa**: 6h | **Status**: 🔴 Não iniciado

- [ ] Criar `SecurityConfig.java`:
  - [ ] Configurar OAuth2 Login (Google)
  - [ ] Configurar CORS
  - [ ] Configurar CSRF com cookie
  - [ ] Definir endpoints públicos vs protegidos
- [ ] Criar `CustomOAuth2UserService.java`:
  - [ ] Criar/atualizar usuário no primeiro login
  - [ ] Mapear dados do Google para Usuario
- [ ] Criar `AuthController.java`:
  - [ ] GET /api/v1/auth/me
  - [ ] POST /api/v1/auth/logout
- [ ] **TESTES (H2)**:
  - [ ] `AuthControllerIntegrationTest.testGetMeComUsuarioAutenticado()`
  - [ ] `AuthControllerIntegrationTest.testGetMeSemAutenticacaoRetorna401()`

---

### Task 1.3: Configuração de Exception Handling
**Estimativa**: 3h | **Status**: 🔴 Não iniciado

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

### Task 1.4: Configuração de Auditoria Base
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `AuditConfig.java`:
  - [ ] Habilitar Hibernate Envers
  - [ ] Configurar AuditorAware<Long>
- [ ] Criar `CustomRevisionEntity.java`:
  - [ ] Campos: usuarioId, ipOrigem
- [ ] Criar `CustomRevisionListener.java`
- [ ] Criar `AuditableEntity.java` (classe base):
  - [ ] Campos: criadoEm, atualizadoEm
  - [ ] @PrePersist, @PreUpdate
- [ ] Verificar que tabelas _AUD são geradas

---

## 🔴 Fase 2: Entidades Base e Migrações (3-4 dias)

### Task 2.1: Entidade Usuario e Migração
**Estimativa**: 3h | **Status**: 🔴 Não iniciado

- [ ] Criar `Usuario.java`:
  - [ ] Campos conforme data-model.md
  - [ ] Estender AuditableEntity
  - [ ] Índices: email (unique), provider_id (unique)
- [ ] Criar migração `V1__create_usuarios_jogos.sql` (parte usuario)
- [ ] Criar `UsuarioRepository.java`
- [ ] Criar `UsuarioMapper.java` (MapStruct)
- [ ] Criar DTOs:
  - [ ] `UsuarioResponse.java`
- [ ] **TESTES (H2)**:
  - [ ] `UsuarioRepositoryTest.testSalvarUsuario()`
  - [ ] `UsuarioRepositoryTest.testBuscarPorEmail()`
  - [ ] `UsuarioRepositoryTest.testEmailUnicoViolacao()`

---

### Task 2.2: Entidade Jogo e JogoParticipante
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `RoleJogo.java` (enum)
- [ ] Criar `Jogo.java`:
  - [ ] Campos conforme data-model.md
  - [ ] Relacionamento 1:N com JogoParticipante
- [ ] Criar `JogoParticipante.java`
- [ ] Atualizar migração V1:
  - [ ] Tabela jogo
  - [ ] Tabela jogo_participante
- [ ] Criar repositories
- [ ] Criar `JogoMapper.java`
- [ ] Criar DTOs:
  - [ ] `CriarJogoRequest.java`
  - [ ] `AtualizarJogoRequest.java`
  - [ ] `JogoResponse.java`
  - [ ] `JogoResumoResponse.java`
- [ ] **TESTES (H2)**:
  - [ ] `JogoRepositoryTest.testCriarJogo()`
  - [ ] `JogoRepositoryTest.testBuscarJogosPorUsuario()`
  - [ ] `JogoParticipanteRepositoryTest.testAdicionarParticipante()`
  - [ ] `JogoParticipanteRepositoryTest.testBuscarRoleNoJogo()`

---

### Task 2.3: JogoService e JogoController
**Estimativa**: 6h | **Status**: 🔴 Não iniciado

- [ ] Criar `JogoService.java`:
  - [ ] `listarMeusJogos(Usuario)`
  - [ ] `criar(CriarJogoRequest, Usuario)` - usuário vira Mestre
  - [ ] `obter(Long jogoId, Usuario)` - validar acesso
  - [ ] `atualizar(Long, AtualizarJogoRequest, Usuario)` - apenas Mestre
  - [ ] `arquivar(Long, Usuario)` - soft delete
  - [ ] `validarMestre(Long jogoId, Usuario)`
- [ ] Criar `JogoController.java`:
  - [ ] GET /api/v1/jogos
  - [ ] POST /api/v1/jogos
  - [ ] GET /api/v1/jogos/{id}
  - [ ] PUT /api/v1/jogos/{id}
  - [ ] DELETE /api/v1/jogos/{id}
- [ ] Criar `ParticipanteService.java`:
  - [ ] `listar(Long jogoId, Usuario)`
  - [ ] `convidar(Long jogoId, ConvidarJogadorRequest, Usuario)`
  - [ ] `remover(Long jogoId, Long participanteId, Usuario)`
- [ ] **TESTES (H2)**:
  - [ ] `JogoServiceTest.testCriarJogoUsuarioViraMestre()`
  - [ ] `JogoServiceTest.testListarJogosQueParticipo()`
  - [ ] `JogoServiceTest.testAtualizarJogoApenasMestre()`
  - [ ] `JogoServiceTest.testAtualizarJogoJogadorRetorna403()`
  - [ ] `JogoControllerIntegrationTest.testCriarJogoComSucesso()`
  - [ ] `JogoControllerIntegrationTest.testListarJogos()`
  - [ ] `JogoControllerIntegrationTest.testAcessarJogoSemPermissaoRetorna403()`

---

## 🔴 Fase 3: Configuração do Jogo - Mestre (6-7 dias)

### Task 3.1: Entidades de Configuração - Atributos e Níveis
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `AtributoConfig.java`
- [ ] Criar `NivelConfig.java`
- [ ] Criar migração `V2__create_config_tables.sql` (parte 1)
- [ ] Criar repositories
- [ ] Criar mappers
- [ ] Criar DTOs
- [ ] **TESTES (H2)**:
  - [ ] `AtributoConfigRepositoryTest.testCriarAtributo()`
  - [ ] `AtributoConfigRepositoryTest.testNomeUnicoPorJogo()`
  - [ ] `AtributoConfigRepositoryTest.testListarAtivosOrdenados()`
  - [ ] `NivelConfigRepositoryTest.testCriarNivel()`
  - [ ] `NivelConfigRepositoryTest.testNumeroNivelUnicoPorJogo()`
  - [ ] `NivelConfigRepositoryTest.testBuscarNivelPorXp()`

---

### Task 3.2: Entidades de Configuração - Aptidões e Bônus
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `TipoAptidao.java`
- [ ] Criar `AptidaoConfig.java`
- [ ] Criar `BonusConfig.java`
- [ ] Atualizar migração V2
- [ ] Criar repositories, mappers e DTOs
- [ ] **TESTES (H2)**:
  - [ ] `AptidaoConfigRepositoryTest.testCriarAptidaoComTipo()`
  - [ ] `AptidaoConfigRepositoryTest.testListarPorTipo()`

---

### Task 3.3: Entidades de Configuração - Classes e Raças
**Estimativa**: 5h | **Status**: 🔴 Não iniciado

- [ ] Criar `ClassePersonagem.java`
- [ ] Criar `ClasseBonus.java`
- [ ] Criar `ClasseAptidaoBonus.java`
- [ ] Criar `Raca.java`
- [ ] Criar `RacaAtributoBonus.java`
- [ ] Criar `RacaClassePermitida.java`
- [ ] Atualizar migração V2
- [ ] Criar repositories, mappers e DTOs
- [ ] **TESTES (H2)**:
  - [ ] `ClassePersonagemRepositoryTest.testCriarClasseComBonus()`
  - [ ] `ClassePersonagemRepositoryTest.testClasseComBonusPorNivel()`
  - [ ] `RacaRepositoryTest.testCriarRacaComBonusAtributo()`
  - [ ] `RacaRepositoryTest.testRacaComClassesPermitidas()`

---

### Task 3.4: Entidades de Configuração - Vida, Essência, Ameaça, Prospecção
**Estimativa**: 3h | **Status**: 🔴 Não iniciado

- [ ] Criar `MembroCorpoConfig.java`
- [ ] Criar `VidaConfig.java`
- [ ] Criar `EssenciaConfig.java`
- [ ] Criar `AmeacaConfig.java`
- [ ] Criar `DadoProspeccaoConfig.java`
- [ ] Finalizar migração V2
- [ ] Criar repositories, mappers e DTOs
- [ ] **TESTES (H2)**:
  - [ ] `MembroCorpoConfigRepositoryTest.testCriarMembro()`
  - [ ] `MembroCorpoConfigRepositoryTest.testPorcentagemValida()`
  - [ ] `DadoProspeccaoConfigRepositoryTest.testCriarDado()`
  - [ ] `DadoProspeccaoConfigRepositoryTest.testValorMaximoCorreto()`

---

### Task 3.4.5: Entidades de Configuração - Sistema de Vantagens
**Estimativa**: 6h | **Status**: 🔴 Não iniciado

- [ ] Criar enums:
  - [ ] `TipoPreRequisito.java`
  - [ ] `TipoEfeito.java`
- [ ] Criar `CategoriaVantagem.java`
- [ ] Criar `VantagemConfig.java`
- [ ] Criar `VantagemPreRequisito.java`
- [ ] Criar `VantagemEfeito.java`
- [ ] Criar `PontosVantagemConfig.java`
- [ ] Criar migração `V2.5__create_vantagem_tables.sql`
- [ ] Criar repositories, mappers e DTOs
- [ ] **TESTES (H2)**:
  - [ ] `VantagemConfigRepositoryTest.testCriarVantagemComCategoria()`
  - [ ] `VantagemConfigRepositoryTest.testCriarVantagemComPreRequisitos()`
  - [ ] `VantagemConfigRepositoryTest.testCriarVantagemComEfeitos()`
  - [ ] `VantagemConfigRepositoryTest.testBuscarVantagensPorCategoria()`
  - [ ] `CategoriaVantagemRepositoryTest.testCriarCategoria()`
  - [ ] `CategoriaVantagemRepositoryTest.testNomeUnicoPorJogo()`

---

### Task 3.5: ConfiguracaoService e ConfiguracaoController
**Estimativa**: 8h | **Status**: 🔴 Não iniciado

- [ ] Criar `ConfiguracaoService.java`:
  - [ ] CRUD para cada tipo de configuração
  - [ ] Validação de permissão (apenas Mestre)
  - [ ] `verificarConfiguracaoMinima(Long jogoId)`
- [ ] Criar `ConfiguracaoController.java`:
  - [ ] Endpoints /config/atributos
  - [ ] Endpoints /config/niveis
  - [ ] Endpoints /config/aptidoes
  - [ ] Endpoints /config/bonus
  - [ ] Endpoints /config/membros
  - [ ] Endpoints /config/classes
  - [ ] Endpoints /config/racas
  - [ ] Endpoints /config/prospeccao
  - [ ] Endpoints /config/categorias-vantagem
  - [ ] Endpoints /config/vantagens
  - [ ] Endpoints /config/pontos-vantagem
- [ ] **TESTES (H2)**:
  - [ ] `ConfiguracaoServiceTest.testCriarAtributoComSucesso()`
  - [ ] `ConfiguracaoServiceTest.testCriarAtributoSemSerMestreRetorna403()`
  - [ ] `ConfiguracaoServiceTest.testAtualizarAtributo()`
  - [ ] `ConfiguracaoServiceTest.testDesativarAtributo()`
  - [ ] `ConfiguracaoServiceTest.testVerificarConfiguracaoMinima()`
  - [ ] `ConfiguracaoServiceTest.testCriarVantagemComPreRequisitos()`
  - [ ] `ConfiguracaoServiceTest.testCriarVantagemComEfeitos()`
  - [ ] `ConfiguracaoControllerIntegrationTest.testListarAtributos()`
  - [ ] `ConfiguracaoControllerIntegrationTest.testCriarAtributoComoMestre()`
  - [ ] `ConfiguracaoControllerIntegrationTest.testCriarAtributoComoJogadorRetorna403()`
  - [ ] `ConfiguracaoControllerIntegrationTest.testListarVantagens()`
  - [ ] `ConfiguracaoControllerIntegrationTest.testCriarVantagemComoMestre()`

---

### Task 3.6: TemplateService - Aplicar Template Klayrah
**Estimativa**: 6h | **Status**: 🔴 Não iniciado

- [ ] Criar `TemplateService.java`:
  - [ ] `aplicarTemplateKlayrah(Long jogoId, Usuario)`:
    - [ ] Criar 7 atributos
    - [ ] Criar 36 níveis
    - [ ] Criar 2 tipos de aptidão
    - [ ] Criar 24 aptidões
    - [ ] Criar 6 bônus
    - [ ] Criar 7 membros do corpo
    - [ ] Criar 12 classes
    - [ ] Criar 6 dados de prospecção
    - [ ] Criar VidaConfig, EssenciaConfig, AmeacaConfig
    - [ ] Criar 8 categorias de vantagem
    - [ ] Criar ~50 vantagens com pré-requisitos e efeitos
    - [ ] Criar PontosVantagemConfig (3 pontos/nível)
- [ ] Adicionar endpoint: POST /config/template/klayrah
- [ ] **TESTES (H2)**:
  - [ ] `TemplateServiceTest.testAplicarTemplateKlayrahComSucesso()`
  - [ ] `TemplateServiceTest.testAplicarTemplateJogoJaTemConfiguracao()`
  - [ ] `TemplateServiceTest.testAplicarTemplateCria7Atributos()`
  - [ ] `TemplateServiceTest.testAplicarTemplateCria36Niveis()`
  - [ ] `TemplateServiceTest.testAplicarTemplateCria24Aptidoes()`
  - [ ] `TemplateServiceTest.testAplicarTemplateCria8CategoriasVantagem()`
  - [ ] `TemplateServiceTest.testAplicarTemplateCriaVantagensComPreRequisitos()`

---

## 🔴 Fase 4: Ficha de Personagem (7-8 dias)

### Task 4.1: Entidade Ficha e Migração
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `Ficha.java`:
  - [ ] Campos conforme data-model.md
  - [ ] @Audited para auditoria
  - [ ] Relacionamentos com Jogo, Usuario, Classe, Raca
  - [ ] Campo pontos_vantagem_gastos
- [ ] Criar migração `V3__create_ficha_tables.sql` (parte 1)
- [ ] Criar `FichaRepository.java`
- [ ] Criar `FichaMapper.java`
- [ ] Criar DTOs:
  - [ ] `CriarFichaRequest.java`
  - [ ] `AtualizarFichaRequest.java`
  - [ ] `FichaResponse.java`
  - [ ] `FichaResumoResponse.java`
- [ ] **TESTES (H2)**:
  - [ ] `FichaRepositoryTest.testCriarFicha()`
  - [ ] `FichaRepositoryTest.testBuscarFichasPorJogo()`
  - [ ] `FichaRepositoryTest.testBuscarFichasPorUsuario()`
  - [ ] `FichaRepositoryTest.testSoftDeleteFicha()`

---

### Task 4.2: Entidades Ficha_Atributo, Ficha_Aptidao, Ficha_Bonus
**Estimativa**: 5h | **Status**: 🔴 Não iniciado

- [ ] Criar `FichaAtributo.java` (@Audited)
- [ ] Criar `FichaAptidao.java` (@Audited)
- [ ] Criar `FichaBonus.java` (@Audited)
- [ ] Atualizar migração V3
- [ ] Criar repositories, mappers e DTOs
- [ ] **TESTES (H2)**:
  - [ ] `FichaAtributoRepositoryTest.testCriarAtributoParaFicha()`
  - [ ] `FichaAtributoRepositoryTest.testUniqueConstraintFichaAtributo()`
  - [ ] `FichaAptidaoRepositoryTest.testCriarAptidaoParaFicha()`

---

### Task 4.3: Entidades Ficha_Vida, Ficha_Essencia, Ficha_Ameaca
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `FichaVida.java` (1:1 com Ficha, @Audited)
- [ ] Criar `FichaVidaMembro.java` (N:1 com Ficha, @Audited)
- [ ] Criar `FichaEssencia.java` (1:1 com Ficha, @Audited)
- [ ] Criar `FichaAmeaca.java` (1:1 com Ficha, @Audited)
- [ ] Atualizar migração V3
- [ ] Criar repositories, mappers e DTOs
- [ ] **TESTES (H2)**:
  - [ ] `FichaVidaRepositoryTest.testCriarVidaParaFicha()`
  - [ ] `FichaVidaRepositoryTest.testRelacionamento1para1()`

---

### Task 4.4: Entidades Ficha_Prospeccao e Anotacao
**Estimativa**: 3h | **Status**: 🔴 Não iniciado

- [ ] Criar `FichaProspeccao.java` (@Audited)
- [ ] Criar `Anotacao.java` (sem auditoria)
- [ ] Finalizar migração V3
- [ ] Criar repositories, mappers e DTOs
- [ ] **TESTES (H2)**:
  - [ ] `FichaProspeccaoRepositoryTest.testCriarProspeccaoParaFicha()`
  - [ ] `FichaProspeccaoRepositoryTest.testValorAtualDentroDoLimite()`
  - [ ] `AnotacaoRepositoryTest.testCriarAnotacao()`
  - [ ] `AnotacaoRepositoryTest.testListarOrdenadoPorDataDesc()`

---

### Task 4.4.5: Entidade FichaVantagem e VantagemService
**Estimativa**: 6h | **Status**: 🔴 Não iniciado

- [ ] Criar `FichaVantagem.java`:
  - [ ] @Audited
  - [ ] Campos: nivel_atual, pontos_gastos_total, data_compra
  - [ ] **REGRA**: Não pode ser deletado
  - [ ] **REGRA**: nivel_atual só pode aumentar
- [ ] Criar `VantagemService.java`:
  - [ ] `listarVantagensDisponiveis(Long fichaId)`
  - [ ] `comprarVantagem(Long fichaId, Long vantagemConfigId, Usuario)`
  - [ ] `subirNivelVantagem(Long fichaId, Long fichaVantagemId, Usuario)`
  - [ ] `calcularPontosDisponiveis(Ficha)`
  - [ ] `validarPreRequisitos(Ficha, VantagemConfig)`
- [ ] Adicionar endpoints ao FichaController:
  - [ ] GET /fichas/{fichaId}/vantagens
  - [ ] GET /fichas/{fichaId}/vantagens/disponiveis
  - [ ] POST /fichas/{fichaId}/vantagens
  - [ ] PUT /fichas/{fichaId}/vantagens/{id}/subir-nivel
- [ ] Criar DTOs:
  - [ ] `ComprarVantagemRequest.java`
  - [ ] `FichaVantagemResponse.java`
  - [ ] `VantagemDisponivelResponse.java`
- [ ] **TESTES (H2)**:
  - [ ] `VantagemServiceTest.testComprarVantagemComSucesso()`
  - [ ] `VantagemServiceTest.testComprarVantagemSemPreRequisitoRetornaErro()`
  - [ ] `VantagemServiceTest.testComprarVantagemSemPontosRetornaErro()`
  - [ ] `VantagemServiceTest.testSubirNivelVantagemComSucesso()`
  - [ ] `VantagemServiceTest.testSubirNivelAlemDoMaximoRetornaErro()`
  - [ ] `VantagemServiceTest.testNaoPodeRemoverVantagemComprada()`
  - [ ] `VantagemServiceTest.testCalculoCustoPorNivel()`
  - [ ] `VantagemServiceTest.testCalculoPontosDisponiveis()`
  - [ ] `FichaControllerIntegrationTest.testListarVantagensDaFicha()`
  - [ ] `FichaControllerIntegrationTest.testComprarVantagem()`
  - [ ] `FichaControllerIntegrationTest.testSubirNivelVantagem()`

---

### Task 4.5: FichaService - Criar e Listar
**Estimativa**: 8h | **Status**: 🔴 Não iniciado

- [ ] Criar `FichaService.java`:
  - [ ] `listar(Long jogoId, Usuario, FiltroFicha)`
  - [ ] `criar(Long jogoId, CriarFichaRequest, Usuario)`:
    - [ ] Validar participante do jogo
    - [ ] Validar configuração mínima
    - [ ] Se isNpc=true, validar Mestre
    - [ ] Criar FichaAtributo para cada AtributoConfig
    - [ ] Criar FichaAptidao para cada AptidaoConfig
    - [ ] Criar FichaBonus para cada BonusConfig
    - [ ] Criar FichaVida
    - [ ] Criar FichaVidaMembro para cada MembroCorpoConfig
    - [ ] Criar FichaEssencia
    - [ ] Criar FichaAmeaca
- [ ] Criar `FichaFactory.java`:
  - [ ] `inicializarComponentesFicha(...)`
- [ ] **TESTES (H2)**:
  - [ ] `FichaServiceTest.testListarFichasDoJogo()`
  - [ ] `FichaServiceTest.testListarApenaMinhasFichas()`
  - [ ] `FichaServiceTest.testCriarFichaComSucesso()`
  - [ ] `FichaServiceTest.testCriarFichaCriaAtributosAutomaticamente()`
  - [ ] `FichaServiceTest.testCriarFichaCriaAptidoesAutomaticamente()`
  - [ ] `FichaServiceTest.testCriarFichaCriaVidaComMembros()`
  - [ ] `FichaServiceTest.testCriarNpcApenasMestre()`
  - [ ] `FichaServiceTest.testCriarFichaSemConfigRetornaErro()`

---

### Task 4.6: FichaService - Obter, Atualizar, Arquivar
**Estimativa**: 6h | **Status**: 🔴 Não iniciado

- [ ] Expandir `FichaService.java`:
  - [ ] `obter(Long jogoId, Long fichaId, Usuario)`:
    - [ ] Validar acesso
    - [ ] Carregar todos os componentes
    - [ ] Calcular valores derivados
  - [ ] `atualizar(Long jogoId, Long fichaId, AtualizarFichaRequest, Usuario)`
  - [ ] `arquivar(Long jogoId, Long fichaId, Usuario)`
- [ ] Criar helpers de validação:
  - [ ] `validarAcessoFicha(Ficha, Usuario)`
  - [ ] `validarPermissaoEdicao(Ficha, Usuario)`
- [ ] **TESTES (H2)**:
  - [ ] `FichaServiceTest.testObterFichaComTodosComponentes()`
  - [ ] `FichaServiceTest.testObterFichaComValoresCalculados()`
  - [ ] `FichaServiceTest.testAtualizarMinhaFicha()`
  - [ ] `FichaServiceTest.testAtualizarFichaOutroJogadorRetorna403()`
  - [ ] `FichaServiceTest.testMestrePoderEditarQualquerFicha()`
  - [ ] `FichaServiceTest.testArquivarFicha()`

---

### Task 4.7: FichaController
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `FichaController.java`:
  - [ ] GET /jogos/{jogoId}/fichas
  - [ ] POST /jogos/{jogoId}/fichas
  - [ ] GET /jogos/{jogoId}/fichas/{fichaId}
  - [ ] PUT /jogos/{jogoId}/fichas/{fichaId}
  - [ ] DELETE /jogos/{jogoId}/fichas/{fichaId}
- [ ] Documentação OpenAPI (@Operation, @ApiResponse)
- [ ] **TESTES (H2)**:
  - [ ] `FichaControllerIntegrationTest.testListarFichasDoJogo()`
  - [ ] `FichaControllerIntegrationTest.testCriarFichaComSucesso()`
  - [ ] `FichaControllerIntegrationTest.testCriarFichaValidacaoDeRequest()`
  - [ ] `FichaControllerIntegrationTest.testObterFichaComDetalhes()`
  - [ ] `FichaControllerIntegrationTest.testAtualizarFicha()`
  - [ ] `FichaControllerIntegrationTest.testArquivarFicha()`
  - [ ] `FichaControllerIntegrationTest.testAcessoNegadoParaNaoParticipante()`

---

### Task 4.8: Endpoints de Componentes da Ficha
**Estimativa**: 6h | **Status**: 🔴 Não iniciado

- [ ] Adicionar ao FichaController:
  - [ ] PUT /fichas/{fichaId}/atributos
  - [ ] PUT /fichas/{fichaId}/aptidoes
  - [ ] PUT /fichas/{fichaId}/bonus
  - [ ] PUT /fichas/{fichaId}/vida
  - [ ] PUT /fichas/{fichaId}/essencia
  - [ ] PUT /fichas/{fichaId}/ameaca
  - [ ] PUT /fichas/{fichaId}/prospeccao
- [ ] Expandir FichaService com métodos de atualização parcial
- [ ] **TESTES (H2)**:
  - [ ] `FichaControllerIntegrationTest.testAtualizarAtributos()`
  - [ ] `FichaControllerIntegrationTest.testAtualizarAptidoes()`
  - [ ] `FichaControllerIntegrationTest.testAtualizarVidaEDanos()`
  - [ ] `FichaControllerIntegrationTest.testAtualizarProspeccaoComLimite()`

---

## 🔴 Fase 5: Funcionalidades Auxiliares (2-3 dias)

### Task 5.1: AnotacaoService e Endpoints
**Estimativa**: 3h | **Status**: 🔴 Não iniciado

- [ ] Criar `AnotacaoService.java`:
  - [ ] `listar(Long jogoId, Long fichaId, Usuario)`
  - [ ] `criar(Long jogoId, Long fichaId, CriarAnotacaoRequest, Usuario)`
  - [ ] `excluir(Long jogoId, Long fichaId, Long anotacaoId, Usuario)`
- [ ] Adicionar endpoints ao FichaController:
  - [ ] GET /fichas/{fichaId}/anotacoes
  - [ ] POST /fichas/{fichaId}/anotacoes
  - [ ] DELETE /fichas/{fichaId}/anotacoes/{id}
- [ ] **TESTES (H2)**:
  - [ ] `AnotacaoServiceTest.testCriarAnotacao()`
  - [ ] `AnotacaoServiceTest.testListarAnotacoesOrdenadas()`
  - [ ] `AnotacaoServiceTest.testExcluirAnotacao()`
  - [ ] `AnotacaoServiceTest.testExcluirAnotacaoDeOutroJogador()`

---

## 🔴 Fase 6: Validações e Cálculos (3-4 dias)

### Task 6.1: CalculoFichaService
**Estimativa**: 8h | **Status**: 🔴 Não iniciado

- [ ] Criar `CalculoFichaService.java`:
  - [ ] `calcularNivel(Integer experiencia, List<NivelConfig>)`
  - [ ] `calcularTotalAtributo(FichaAtributo, List<FichaVantagem>)`
  - [ ] `calcularImpeto(FichaAtributo, AtributoConfig)`
  - [ ] `calcularBonusBase(FichaBonus, BonusConfig, Map<String, Integer>)`
  - [ ] `calcularBonusTotal(FichaBonus, Integer base, List<FichaVantagem>)`
  - [ ] `calcularTotalAptidao(FichaAptidao, List<FichaVantagem>)`
  - [ ] `calcularVidaTotal(...)`
  - [ ] `calcularVidaMembro(Integer vidaTotal, MembroCorpoConfig)`
  - [ ] `calcularEssenciaTotal(...)`
  - [ ] `calcularAmeacaTotal(...)`
  - [ ] `calcularBonusVantagens(List<FichaVantagem>, TipoEfeito, Long referenciaId)`
  - [ ] `calcularPontosVantagemDisponiveis(Ficha, PontosVantagemConfig)`
- [ ] Criar `FormulaParser.java`:
  - [ ] Parse simples de fórmulas
  - [ ] Substituir variáveis
  - [ ] Suportar CUSTO_BASE * NIVEL
- [ ] **TESTES (H2)**:
  - [ ] `CalculoFichaServiceTest.testCalcularNivelPorXp()`
  - [ ] `CalculoFichaServiceTest.testCalcularTotalAtributo()`
  - [ ] `CalculoFichaServiceTest.testCalcularTotalAtributoComBonusVantagem()`
  - [ ] `CalculoFichaServiceTest.testCalcularImpetoForca()`
  - [ ] `CalculoFichaServiceTest.testCalcularImpetoAgilidade()`
  - [ ] `CalculoFichaServiceTest.testCalcularBonusBBA()`
  - [ ] `CalculoFichaServiceTest.testCalcularBonusBBAComTCO()`
  - [ ] `CalculoFichaServiceTest.testCalcularVidaTotal()`
  - [ ] `CalculoFichaServiceTest.testCalcularVidaTotalComSaudeDeFerro()`
  - [ ] `CalculoFichaServiceTest.testCalcularVidaPorMembro()`
  - [ ] `CalculoFichaServiceTest.testCalcularEssenciaTotal()`
  - [ ] `CalculoFichaServiceTest.testCalcularAmeacaTotal()`
  - [ ] `CalculoFichaServiceTest.testCalcularPontosVantagemDisponiveis()`
  - [ ] `FormulaParserTest.testParseFormulaSimples()`
  - [ ] `FormulaParserTest.testParseFormulaComFLOOR()`
  - [ ] `FormulaParserTest.testParseFormulaComMIN()`
  - [ ] `FormulaParserTest.testSubstituirVariaveis()`
  - [ ] `FormulaParserTest.testFormulaCustoVantagem()`

---

### Task 6.2: ValidacaoFichaService
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar `ValidacaoFichaService.java`:
  - [ ] `validarPontosAtributo(Ficha, List<FichaAtributo>, Integer nivel, List<NivelConfig>)`
  - [ ] `validarPontosVantagem(Ficha, PontosVantagemConfig)`
- [ ] Criar DTOs:
  - [ ] `ValidacaoResponse.java`
  - [ ] `ValidacaoVantagemResponse.java`
- [ ] Integrar validação no retorno de FichaService.obter()
- [ ] **TESTES (H2)**:
  - [ ] `ValidacaoFichaServiceTest.testValidacaoAtributosOk()`
  - [ ] `ValidacaoFichaServiceTest.testValidacaoAtributosPontosSobrando()`
  - [ ] `ValidacaoFichaServiceTest.testValidacaoAtributosPontosExcedidos()`
  - [ ] `ValidacaoFichaServiceTest.testCalculoPontosEsperadosPorNivel()`
  - [ ] `ValidacaoFichaServiceTest.testValidacaoVantagensComPontosDisponiveis()`
  - [ ] `ValidacaoFichaServiceTest.testValidacaoVantagensSemPontosDisponiveis()`

---

## 🔴 Fase 7: Auditoria e Histórico (2-3 dias)

### Task 7.1: Configuração de Auditoria Completa
**Estimativa**: 4h | **Status**: 🔴 Não iniciado

- [ ] Criar migração `V4__create_audit_tables.sql`:
  - [ ] Customizar tabela REVINFO
  - [ ] Adicionar colunas usuario_id, ip_origem
- [ ] Verificar que todas as entidades de ficha têm @Audited:
  - [ ] Ficha
  - [ ] FichaAtributo
  - [ ] FichaAptidao
  - [ ] FichaBonus
  - [ ] FichaVida
  - [ ] FichaVidaMembro
  - [ ] FichaEssencia
  - [ ] FichaAmeaca
  - [ ] FichaProspeccao
  - [ ] FichaVantagem
- [ ] Configurar exclusão de campos sensíveis se necessário
- [ ] **TESTES (H2)**:
  - [ ] `AuditIntegrationTest.testAlteracaoFichaCriaRegistroAuditoria()`
  - [ ] `AuditIntegrationTest.testHistoricoRegistraUsuario()`

---

### Task 7.2: HistoricoService e Endpoint
**Estimativa**: 5h | **Status**: 🔴 Não iniciado

- [ ] Criar `HistoricoService.java`:
  - [ ] `listarHistorico(Long jogoId, Long fichaId, Usuario, Pageable)`:
    - [ ] Validar que é Mestre
    - [ ] Buscar revisões usando AuditReader
    - [ ] Mapear para HistoricoResponse
- [ ] Criar DTOs:
  - [ ] `HistoricoResponse.java`
  - [ ] `HistoricoPageResponse.java`
- [ ] Adicionar endpoint:
  - [ ] GET /fichas/{fichaId}/historico
- [ ] **TESTES (H2)**:
  - [ ] `HistoricoServiceTest.testListarHistoricoApenasMestre()`
  - [ ] `HistoricoServiceTest.testListarHistoricoJogadorRetorna403()`
  - [ ] `HistoricoServiceTest.testHistoricoMostraCamposAlterados()`
  - [ ] `FichaControllerIntegrationTest.testObterHistoricoFicha()`

---

## 📋 Resumo de Testes

### Total de Testes por Fase

| Fase | Testes |
|------|--------|
| Fase 1 | ~8 |
| Fase 2 | ~14 |
| Fase 3 | ~30 |
| Fase 4 | ~45 |
| Fase 5 | ~4 |
| Fase 6 | ~22 |
| Fase 7 | ~6 |
| **Total** | **~129** |

### Convenção de Testes (H2)

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExemploIntegrationTest {
    // ... Arrange-Act-Assert
}
```

---

## 📦 Arquivos a Criar

### Entidades (model/)
- [ ] Usuario.java
- [ ] Jogo.java
- [ ] JogoParticipante.java
- [ ] AtributoConfig.java
- [ ] NivelConfig.java
- [ ] TipoAptidao.java
- [ ] AptidaoConfig.java
- [ ] BonusConfig.java
- [ ] MembroCorpoConfig.java
- [ ] ClassePersonagem.java
- [ ] ClasseBonus.java
- [ ] ClasseAptidaoBonus.java
- [ ] Raca.java
- [ ] RacaAtributoBonus.java
- [ ] RacaClassePermitida.java
- [ ] VidaConfig.java
- [ ] EssenciaConfig.java
- [ ] AmeacaConfig.java
- [ ] DadoProspeccaoConfig.java
- [ ] CategoriaVantagem.java
- [ ] VantagemConfig.java
- [ ] VantagemPreRequisito.java
- [ ] VantagemEfeito.java
- [ ] PontosVantagemConfig.java
- [ ] Ficha.java
- [ ] FichaAtributo.java
- [ ] FichaAptidao.java
- [ ] FichaBonus.java
- [ ] FichaVida.java
- [ ] FichaVidaMembro.java
- [ ] FichaEssencia.java
- [ ] FichaAmeaca.java
- [ ] FichaProspeccao.java
- [ ] FichaVantagem.java
- [ ] Anotacao.java

### Enums (model/enums/)
- [ ] RoleJogo.java
- [ ] TipoPreRequisito.java
- [ ] TipoEfeito.java
- [ ] TipoAlteracao.java

### Migrações (db/migration/)
- [ ] V1__create_usuarios_jogos.sql
- [ ] V2__create_config_tables.sql
- [ ] V2.5__create_vantagem_tables.sql
- [ ] V3__create_ficha_tables.sql
- [ ] V4__create_audit_tables.sql

---

## 🚀 Checklist Final

- [ ] Todas as tasks completadas
- [ ] Todos os testes passando (~129 testes)
- [ ] Cobertura de testes > 80%
- [ ] Documentação OpenAPI atualizada
- [ ] Sem warnings de compilação
- [ ] Code review realizado
- [ ] Branch pronta para merge

---

## 📝 Notas e Observações

_Use este espaço para anotações durante o desenvolvimento:_

```
Data: ____/____/____
Observação: 
```

```
Data: ____/____/____
Observação: 
```

```
Data: ____/____/____
Observação: 
```
