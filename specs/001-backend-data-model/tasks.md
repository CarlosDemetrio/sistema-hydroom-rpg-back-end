# Tasks: Backend Klayrah RPG

**Feature Branch**: `001-backend-data-model`  
**Date**: 2026-02-01  
**Metodologia de Testes**: Testes de integração com H2 (sem Mockito)

---

## Visão Geral das Fases

| Fase | Descrição | Estimativa |
|------|-----------|------------|
| 1 | Setup do Projeto e Infraestrutura | 2-3 dias |
| 2 | Entidades Base e Migrações | 3-4 dias |
| 3 | Configuração do Jogo (Mestre) + Vantagens | 6-7 dias |
| 4 | Ficha de Personagem + Compra de Vantagens | 7-8 dias |
| 5 | Funcionalidades Auxiliares | 2-3 dias |
| 6 | Validações e Cálculos (incluindo Vantagens) | 3-4 dias |
| 7 | Auditoria e Histórico | 2-3 dias |
| **Total** | | **25-32 dias** |

---

## Fase 1: Setup do Projeto e Infraestrutura

### Task 1.1: Configuração do Projeto Maven
**Prioridade**: P0 (Bloqueante)  
**Estimativa**: 4h

**Subtarefas**:
1. Atualizar `pom.xml` com dependências:
   - Spring Boot 4.0.2
   - Spring Security OAuth2
   - Spring Data JPA
   - Hibernate Envers
   - MapStruct 1.5.5
   - Flyway 10.x
   - PostgreSQL Driver
   - H2 (test scope)
   - SpringDoc OpenAPI 2.x
   - Bucket4j
   - Validation API

2. Configurar profiles:
   - `application.properties` (base)
   - `application-dev.properties` (desenvolvimento)
   - `application-test.properties` (testes com H2)
   - `application-prod.properties` (produção)

3. Configurar MapStruct no Maven Compiler Plugin

**Critérios de Aceite**:
- [ ] Projeto compila sem erros
- [ ] Profile de teste usa H2 in-memory
- [ ] MapStruct gera implementações

---

### Task 1.2: Configuração de Segurança OAuth2
**Prioridade**: P0  
**Estimativa**: 6h

**Subtarefas**:
1. Criar `SecurityConfig.java`:
   - Configurar OAuth2 Login (Google)
   - Configurar CORS
   - Configurar CSRF com cookie
   - Definir endpoints públicos vs protegidos

2. Criar `OAuth2UserService` customizado:
   - Criar/atualizar usuário no primeiro login
   - Mapear dados do Google para `Usuario`

3. Criar `AuthController`:
   - `GET /api/v1/auth/me` - Dados do usuário atual
   - `POST /api/v1/auth/logout` - Logout

**Arquivos**:
```
src/main/java/.../config/SecurityConfig.java
src/main/java/.../service/CustomOAuth2UserService.java
src/main/java/.../controller/AuthController.java
```

**Testes** (H2):
```
src/test/java/.../controller/AuthControllerIntegrationTest.java
- testGetMeComUsuarioAutenticado()
- testGetMeSemAutenticacaoRetorna401()
```

**Critérios de Aceite**:
- [ ] Login com Google funciona
- [ ] Usuário é criado no banco no primeiro login
- [ ] Endpoint `/auth/me` retorna dados do usuário
- [ ] Testes passam com H2

---

### Task 1.3: Configuração de Exception Handling
**Prioridade**: P1  
**Estimativa**: 3h

**Subtarefas**:
1. Criar exceções customizadas:
   - `ResourceNotFoundException`
   - `BusinessException`
   - `ForbiddenException`
   - `ValidationException`

2. Criar `GlobalExceptionHandler` com `@RestControllerAdvice`:
   - Mapear exceções para `ProblemDetail` (RFC 7807)
   - Tratar `MethodArgumentNotValidException`
   - Tratar `DataIntegrityViolationException`

**Arquivos**:
```
src/main/java/.../exception/ResourceNotFoundException.java
src/main/java/.../exception/BusinessException.java
src/main/java/.../exception/ForbiddenException.java
src/main/java/.../exception/GlobalExceptionHandler.java
```

**Testes** (H2):
```
src/test/java/.../exception/GlobalExceptionHandlerTest.java
- testResourceNotFoundRetorna404()
- testValidationErrorRetorna400ComDetalhes()
- testForbiddenRetorna403()
```

**Critérios de Aceite**:
- [ ] Exceções retornam `ProblemDetail` no formato correto
- [ ] Erros de validação listam campos inválidos
- [ ] Testes passam

---

### Task 1.4: Configuração de Auditoria Base
**Prioridade**: P1  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `AuditConfig.java`:
   - Habilitar Hibernate Envers
   - Configurar `AuditorAware<Long>` para capturar usuário

2. Criar `CustomRevisionEntity`:
   - Campos: `usuarioId`, `ipOrigem`
   - `RevisionListener` para preencher dados

3. Criar classe base `AuditableEntity`:
   - Campos: `criadoEm`, `atualizadoEm`
   - `@PrePersist`, `@PreUpdate`

**Arquivos**:
```
src/main/java/.../config/AuditConfig.java
src/main/java/.../model/audit/CustomRevisionEntity.java
src/main/java/.../model/audit/CustomRevisionListener.java
src/main/java/.../model/base/AuditableEntity.java
```

**Critérios de Aceite**:
- [ ] Entidades anotadas com `@Audited` geram tabelas `_AUD`
- [ ] `CustomRevisionEntity` captura usuário e IP
- [ ] Timestamps são preenchidos automaticamente

---

## Fase 2: Entidades Base e Migrações

### Task 2.1: Entidade Usuario e Migração
**Prioridade**: P0  
**Estimativa**: 3h

**Subtarefas**:
1. Criar `Usuario.java`:
   - Campos conforme data-model.md
   - Estender `AuditableEntity`
   - Índices: `email` (unique), `provider_id` (unique)

2. Criar migração `V1__create_usuarios_jogos.sql`:
   - Tabela `usuario`

3. Criar `UsuarioRepository`

4. Criar `UsuarioMapper` (MapStruct)

5. Criar DTOs:
   - `UsuarioResponse`
   - `AtualizarUsuarioRequest`

**Arquivos**:
```
src/main/java/.../model/Usuario.java
src/main/java/.../repository/UsuarioRepository.java
src/main/java/.../mapper/UsuarioMapper.java
src/main/java/.../dto/response/UsuarioResponse.java
src/main/resources/db/migration/V1__create_usuarios_jogos.sql
```

**Testes** (H2):
```
src/test/java/.../repository/UsuarioRepositoryTest.java
- testSalvarUsuario()
- testBuscarPorEmail()
- testEmailUnicoViolacao()
```

**Critérios de Aceite**:
- [ ] Migração executa sem erros
- [ ] Constraint de email único funciona
- [ ] Testes passam com H2

---

### Task 2.2: Entidade Jogo e JogoParticipante
**Prioridade**: P0  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `RoleJogo.java` (enum)

2. Criar `Jogo.java`:
   - Campos conforme data-model.md
   - Relacionamento 1:N com `JogoParticipante`

3. Criar `JogoParticipante.java`:
   - Campos conforme data-model.md
   - Relacionamento N:1 com `Jogo` e `Usuario`

4. Atualizar migração V1:
   - Tabela `jogo`
   - Tabela `jogo_participante`

5. Criar repositories e mappers

6. Criar DTOs

**Arquivos**:
```
src/main/java/.../model/enums/RoleJogo.java
src/main/java/.../model/Jogo.java
src/main/java/.../model/JogoParticipante.java
src/main/java/.../repository/JogoRepository.java
src/main/java/.../repository/JogoParticipanteRepository.java
src/main/java/.../mapper/JogoMapper.java
src/main/java/.../dto/request/CriarJogoRequest.java
src/main/java/.../dto/response/JogoResponse.java
src/main/java/.../dto/response/JogoResumoResponse.java
```

**Testes** (H2):
```
src/test/java/.../repository/JogoRepositoryTest.java
- testCriarJogo()
- testBuscarJogosPorUsuario()

src/test/java/.../repository/JogoParticipanteRepositoryTest.java
- testAdicionarParticipante()
- testBuscarRoleNoJogo()
- testUsuarioJaParticipante()
```

**Critérios de Aceite**:
- [ ] Relacionamentos funcionam corretamente
- [ ] Enum `RoleJogo` persiste como VARCHAR
- [ ] Testes passam

---

### Task 2.3: JogoService e JogoController
**Prioridade**: P0  
**Estimativa**: 6h

**Subtarefas**:
1. Criar `JogoService`:
   - `listarMeusJogos(Usuario)` - Jogos que participo
   - `criar(CriarJogoRequest, Usuario)` - Criar e virar Mestre
   - `obter(Long jogoId, Usuario)` - Validar acesso
   - `atualizar(Long, AtualizarJogoRequest, Usuario)` - Apenas Mestre
   - `arquivar(Long, Usuario)` - Soft delete, apenas Mestre
   - `validarMestre(Long jogoId, Usuario)` - Helper

2. Criar `JogoController`:
   - `GET /api/v1/jogos`
   - `POST /api/v1/jogos`
   - `GET /api/v1/jogos/{id}`
   - `PUT /api/v1/jogos/{id}`
   - `DELETE /api/v1/jogos/{id}`

3. Criar `ParticipanteService`:
   - `listar(Long jogoId, Usuario)`
   - `convidar(Long jogoId, ConvidarJogadorRequest, Usuario)`
   - `remover(Long jogoId, Long participanteId, Usuario)`

**Arquivos**:
```
src/main/java/.../service/JogoService.java
src/main/java/.../service/ParticipanteService.java
src/main/java/.../controller/JogoController.java
```

**Testes** (H2):
```
src/test/java/.../service/JogoServiceTest.java
- testCriarJogoUsuarioViraMestre()
- testListarJogosQueParticipo()
- testAtualizarJogoApenasMestre()
- testAtualizarJogoJogadorRetorna403()

src/test/java/.../controller/JogoControllerIntegrationTest.java
- testCriarJogoComSucesso()
- testListarJogos()
- testAcessarJogoSemPermissaoRetorna403()
```

**Critérios de Aceite**:
- [ ] Criar jogo torna o usuário Mestre automaticamente
- [ ] Apenas participantes veem o jogo
- [ ] Apenas Mestre pode atualizar/arquivar
- [ ] Testes passam

---

## Fase 3: Configuração do Jogo (Mestre)

### Task 3.1: Entidades de Configuração - Atributos e Níveis
**Prioridade**: P0  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `AtributoConfig.java`
2. Criar `NivelConfig.java`
3. Criar migração `V2__create_config_tables.sql` (parte 1)
4. Criar repositories
5. Criar mappers
6. Criar DTOs

**Arquivos**:
```
src/main/java/.../model/config/AtributoConfig.java
src/main/java/.../model/config/NivelConfig.java
src/main/java/.../repository/config/AtributoConfigRepository.java
src/main/java/.../repository/config/NivelConfigRepository.java
src/main/java/.../mapper/config/AtributoConfigMapper.java
src/main/java/.../mapper/config/NivelConfigMapper.java
src/main/resources/db/migration/V2__create_config_tables.sql
```

**Testes** (H2):
```
src/test/java/.../repository/config/AtributoConfigRepositoryTest.java
- testCriarAtributo()
- testNomeUnicoPorJogo()
- testListarAtivosOrdenados()

src/test/java/.../repository/config/NivelConfigRepositoryTest.java
- testCriarNivel()
- testNumeroNivelUnicoPorJogo()
- testBuscarNivelPorXp()
```

**Critérios de Aceite**:
- [ ] Constraint de nome único por jogo funciona
- [ ] Ordem de exibição é respeitada
- [ ] Testes passam

---

### Task 3.2: Entidades de Configuração - Aptidões e Bônus
**Prioridade**: P0  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `TipoAptidao.java`
2. Criar `AptidaoConfig.java`
3. Criar `BonusConfig.java`
4. Atualizar migração V2
5. Criar repositories, mappers e DTOs

**Arquivos**:
```
src/main/java/.../model/config/TipoAptidao.java
src/main/java/.../model/config/AptidaoConfig.java
src/main/java/.../model/config/BonusConfig.java
src/main/java/.../repository/config/TipoAptidaoRepository.java
src/main/java/.../repository/config/AptidaoConfigRepository.java
src/main/java/.../repository/config/BonusConfigRepository.java
```

**Testes** (H2):
```
src/test/java/.../repository/config/AptidaoConfigRepositoryTest.java
- testCriarAptidaoComTipo()
- testListarPorTipo()
```

**Critérios de Aceite**:
- [ ] Aptidão referencia TipoAptidao corretamente
- [ ] Fórmulas são armazenadas como texto
- [ ] Testes passam

---

### Task 3.3: Entidades de Configuração - Classes e Raças
**Prioridade**: P0  
**Estimativa**: 5h

**Subtarefas**:
1. Criar `ClassePersonagem.java`
2. Criar `ClasseBonus.java`
3. Criar `ClasseAptidaoBonus.java`
4. Criar `Raca.java`
5. Criar `RacaAtributoBonus.java`
6. Criar `RacaClassePermitida.java`
7. Atualizar migração V2
8. Criar repositories, mappers e DTOs

**Arquivos**:
```
src/main/java/.../model/config/ClassePersonagem.java
src/main/java/.../model/config/ClasseBonus.java
src/main/java/.../model/config/ClasseAptidaoBonus.java
src/main/java/.../model/config/Raca.java
src/main/java/.../model/config/RacaAtributoBonus.java
src/main/java/.../model/config/RacaClassePermitida.java
```

**Testes** (H2):
```
src/test/java/.../repository/config/ClassePersonagemRepositoryTest.java
- testCriarClasseComBonus()
- testClasseComBonusPorNivel()

src/test/java/.../repository/config/RacaRepositoryTest.java
- testCriarRacaComBonusAtributo()
- testRacaComClassesPermitidas()
```

**Critérios de Aceite**:
- [ ] Classe pode ter múltiplos bônus
- [ ] Bônus de classe pode ser por nível
- [ ] Raça pode restringir classes
- [ ] Testes passam

---

### Task 3.4: Entidades de Configuração - Vida, Essência, Ameaça, Prospecção
**Prioridade**: P0  
**Estimativa**: 3h

**Subtarefas**:
1. Criar `MembroCorpoConfig.java`
2. Criar `VidaConfig.java`
3. Criar `EssenciaConfig.java`
4. Criar `AmeacaConfig.java`
5. Criar `DadoProspeccaoConfig.java`
6. Finalizar migração V2
7. Criar repositories, mappers e DTOs

**Arquivos**:
```
src/main/java/.../model/config/MembroCorpoConfig.java
src/main/java/.../model/config/VidaConfig.java
src/main/java/.../model/config/EssenciaConfig.java
src/main/java/.../model/config/AmeacaConfig.java
src/main/java/.../model/config/DadoProspeccaoConfig.java
```

**Testes** (H2):
```
src/test/java/.../repository/config/MembroCorpoConfigRepositoryTest.java
- testCriarMembro()
- testPorcentagemValida()

src/test/java/.../repository/config/DadoProspeccaoConfigRepositoryTest.java
- testCriarDado()
- testValorMaximoCorreto()
```

**Critérios de Aceite**:
- [ ] Configurações 1:1 com Jogo funcionam
- [ ] Porcentagem é decimal (0.25, 0.75, 1.0)
- [ ] Testes passam

---

### Task 3.4.5: Entidades de Configuração - Sistema de Vantagens
**Prioridade**: P0  
**Estimativa**: 6h

**Subtarefas**:
1. Criar enums:
   - `TipoPreRequisito.java` (ATRIBUTO, BONUS, NIVEL_PERSONAGEM, RENASCIMENTOS, VANTAGEM)
   - `TipoEfeito.java` (BONUS, ATRIBUTO, APTIDAO, VIDA, ESSENCIA, REDUCAO_DANO, ESPECIAL)

2. Criar `CategoriaVantagem.java`:
   - Campos: nome, descricao, ordem_exibicao, ativo

3. Criar `VantagemConfig.java`:
   - Campos: nome, sigla, descricao, custo_base, formula_custo, niveis_maximos, dados_progressao, notas
   - Relacionamento com CategoriaVantagem

4. Criar `VantagemPreRequisito.java`:
   - Campos: tipo_requisito, referencia_id, valor_minimo, descricao
   - Relacionamento com VantagemConfig

5. Criar `VantagemEfeito.java`:
   - Campos: tipo_efeito, referencia_id, valor_por_nivel, descricao, formula
   - Relacionamento com VantagemConfig

6. Criar `PontosVantagemConfig.java`:
   - Campos: pontos_por_nivel, pontos_iniciais
   - Relacionamento 1:1 com Jogo

7. Criar migração `V2.5__create_vantagem_tables.sql`

8. Criar repositories, mappers e DTOs

**Arquivos**:
```
src/main/java/.../model/enums/TipoPreRequisito.java
src/main/java/.../model/enums/TipoEfeito.java
src/main/java/.../model/config/CategoriaVantagem.java
src/main/java/.../model/config/VantagemConfig.java
src/main/java/.../model/config/VantagemPreRequisito.java
src/main/java/.../model/config/VantagemEfeito.java
src/main/java/.../model/config/PontosVantagemConfig.java
src/main/java/.../repository/config/CategoriaVantagemRepository.java
src/main/java/.../repository/config/VantagemConfigRepository.java
src/main/resources/db/migration/V2.5__create_vantagem_tables.sql
```

**Testes** (H2):
```
src/test/java/.../repository/config/VantagemConfigRepositoryTest.java
- testCriarVantagemComCategoria()
- testCriarVantagemComPreRequisitos()
- testCriarVantagemComEfeitos()
- testBuscarVantagensPorCategoria()

src/test/java/.../repository/config/CategoriaVantagemRepositoryTest.java
- testCriarCategoria()
- testNomeUnicoPorJogo()
```

**Critérios de Aceite**:
- [ ] Vantagem pode ter múltiplos pré-requisitos
- [ ] Vantagem pode ter múltiplos efeitos
- [ ] formula_custo padrão é 'CUSTO_BASE * NIVEL'
- [ ] Testes passam

---

### Task 3.5: ConfiguracaoService e ConfiguracaoController
**Prioridade**: P0  
**Estimativa**: 8h

**Subtarefas**:
1. Criar `ConfiguracaoService`:
   - CRUD para cada tipo de configuração
   - Validação de permissão (apenas Mestre)
   - `verificarConfiguracaoMinima(Long jogoId)`

2. Criar `ConfiguracaoController`:
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/atributos`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/niveis`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/aptidoes`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/bonus`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/membros`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/classes`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/racas`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/prospeccao`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/categorias-vantagem`
   - `GET/POST/PUT/DELETE /api/v1/jogos/{jogoId}/config/vantagens`
   - `GET/PUT /api/v1/jogos/{jogoId}/config/pontos-vantagem`

**Arquivos**:
```
src/main/java/.../service/ConfiguracaoService.java
src/main/java/.../controller/ConfiguracaoController.java
```

**Testes** (H2):
```
src/test/java/.../service/ConfiguracaoServiceTest.java
- testCriarAtributoComSucesso()
- testCriarAtributoSemSerMestreRetorna403()
- testAtualizarAtributo()
- testDesativarAtributo()
- testVerificarConfiguracaoMinima()
- testCriarVantagemComPreRequisitos()
- testCriarVantagemComEfeitos()

src/test/java/.../controller/ConfiguracaoControllerIntegrationTest.java
- testListarAtributos()
- testCriarAtributoComoMestre()
- testCriarAtributoComoJogadorRetorna403()
- testListarVantagens()
- testCriarVantagemComoMestre()
```

**Critérios de Aceite**:
- [ ] Apenas Mestre pode criar/editar configurações
- [ ] Jogador pode apenas listar configurações
- [ ] Validação de configuração mínima funciona
- [ ] Vantagens incluem pré-requisitos e efeitos
- [ ] Testes passam

---

### Task 3.6: TemplateService - Aplicar Template Klayrah
**Prioridade**: P1  
**Estimativa**: 6h

**Subtarefas**:
1. Criar `TemplateService`:
   - `aplicarTemplateKlayrah(Long jogoId, Usuario)`:
     - Criar 7 atributos
     - Criar 36 níveis
     - Criar 2 tipos de aptidão
     - Criar 24 aptidões
     - Criar 6 bônus
     - Criar 7 membros do corpo
     - Criar 12 classes
     - Criar 6 dados de prospecção
     - Criar VidaConfig, EssenciaConfig, AmeacaConfig
     - Criar 8 categorias de vantagem
     - Criar ~50 vantagens com pré-requisitos e efeitos
     - Criar PontosVantagemConfig (3 pontos/nível)
   - Usar dados do `seed-data.md`

2. Adicionar endpoint:
   - `POST /api/v1/jogos/{jogoId}/config/template/klayrah`

**Arquivos**:
```
src/main/java/.../service/TemplateService.java
```

**Testes** (H2):
```
src/test/java/.../service/TemplateServiceTest.java
- testAplicarTemplateKlayrahComSucesso()
- testAplicarTemplateJogoJaTemConfiguracao()
- testAplicarTemplateCria7Atributos()
- testAplicarTemplateCria36Niveis()
- testAplicarTemplateCria24Aptidoes()
- testAplicarTemplateCria8CategoriasVantagem()
- testAplicarTemplateCriaVantagensComPreRequisitos()
```

**Critérios de Aceite**:
- [ ] Template cria todas as configurações do seed-data.md
- [ ] Não permite aplicar se já existem configurações
- [ ] Vantagens são criadas com pré-requisitos e efeitos
- [ ] Testes passam

---

## Fase 4: Ficha de Personagem

### Task 4.1: Entidade Ficha e Migração
**Prioridade**: P0  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `Ficha.java`:
   - Campos conforme data-model.md
   - `@Audited` para auditoria
   - Relacionamentos com Jogo, Usuario, Classe, Raca

2. Criar migração `V3__create_ficha_tables.sql` (parte 1)

3. Criar `FichaRepository`

4. Criar `FichaMapper`

5. Criar DTOs:
   - `CriarFichaRequest`
   - `AtualizarFichaRequest`
   - `FichaResponse`
   - `FichaResumoResponse`

**Arquivos**:
```
src/main/java/.../model/ficha/Ficha.java
src/main/java/.../repository/ficha/FichaRepository.java
src/main/java/.../mapper/FichaMapper.java
src/main/java/.../dto/request/CriarFichaRequest.java
src/main/java/.../dto/request/AtualizarFichaRequest.java
src/main/java/.../dto/response/FichaResponse.java
src/main/java/.../dto/response/FichaResumoResponse.java
src/main/resources/db/migration/V3__create_ficha_tables.sql
```

**Testes** (H2):
```
src/test/java/.../repository/ficha/FichaRepositoryTest.java
- testCriarFicha()
- testBuscarFichasPorJogo()
- testBuscarFichasPorUsuario()
- testSoftDeleteFicha()
```

**Critérios de Aceite**:
- [ ] Ficha referencia Jogo, Usuario, Classe e Raca
- [ ] Soft delete funciona (campo `ativa`)
- [ ] Auditoria está ativa
- [ ] Testes passam

---

### Task 4.2: Entidades Ficha_Atributo, Ficha_Aptidao, Ficha_Bonus
**Prioridade**: P0  
**Estimativa**: 5h

**Subtarefas**:
1. Criar `FichaAtributo.java`:
   - Campos: base, nivel, outros_bonus
   - `@Audited`

2. Criar `FichaAptidao.java`:
   - Campos: base, sorte, classe_bonus
   - `@Audited`

3. Criar `FichaBonus.java`:
   - Campos: vantagens, classe_bonus, itens, gloria, outros_bonus
   - `@Audited`

4. Atualizar migração V3

5. Criar repositories, mappers e DTOs

**Arquivos**:
```
src/main/java/.../model/ficha/FichaAtributo.java
src/main/java/.../model/ficha/FichaAptidao.java
src/main/java/.../model/ficha/FichaBonus.java
src/main/java/.../repository/ficha/FichaAtributoRepository.java
src/main/java/.../repository/ficha/FichaAptidaoRepository.java
src/main/java/.../repository/ficha/FichaBonusRepository.java
```

**Testes** (H2):
```
src/test/java/.../repository/ficha/FichaAtributoRepositoryTest.java
- testCriarAtributoParaFicha()
- testUniqueConstraintFichaAtributo()

src/test/java/.../repository/ficha/FichaAptidaoRepositoryTest.java
- testCriarAptidaoParaFicha()
```

**Critérios de Aceite**:
- [ ] Constraint unique (ficha_id, config_id) funciona
- [ ] Auditoria gera tabelas _AUD
- [ ] Testes passam

---

### Task 4.3: Entidades Ficha_Vida, Ficha_Essencia, Ficha_Ameaca
**Prioridade**: P0  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `FichaVida.java` (1:1 com Ficha)
2. Criar `FichaVidaMembro.java` (N:1 com Ficha)
3. Criar `FichaEssencia.java` (1:1 com Ficha)
4. Criar `FichaAmeaca.java` (1:1 com Ficha)
5. Atualizar migração V3
6. Criar repositories, mappers e DTOs

**Arquivos**:
```
src/main/java/.../model/ficha/FichaVida.java
src/main/java/.../model/ficha/FichaVidaMembro.java
src/main/java/.../model/ficha/FichaEssencia.java
src/main/java/.../model/ficha/FichaAmeaca.java
```

**Testes** (H2):
```
src/test/java/.../repository/ficha/FichaVidaRepositoryTest.java
- testCriarVidaParaFicha()
- testRelacionamento1para1()
```

**Critérios de Aceite**:
- [ ] Relacionamentos 1:1 funcionam
- [ ] Auditoria está ativa
- [ ] Testes passam

---

### Task 4.4: Entidades Ficha_Prospeccao e Anotacao
**Prioridade**: P1  
**Estimativa**: 3h

**Subtarefas**:
1. Criar `FichaProspeccao.java`
2. Criar `Anotacao.java` (sem auditoria)
3. Finalizar migração V3
4. Criar repositories, mappers e DTOs

**Arquivos**:
```
src/main/java/.../model/ficha/FichaProspeccao.java
src/main/java/.../model/Anotacao.java
src/main/java/.../repository/ficha/FichaProspeccaoRepository.java
src/main/java/.../repository/AnotacaoRepository.java
```

**Testes** (H2):
```
src/test/java/.../repository/ficha/FichaProspeccaoRepositoryTest.java
- testCriarProspeccaoParaFicha()
- testValorAtualDentroDoLimite()

src/test/java/.../repository/AnotacaoRepositoryTest.java
- testCriarAnotacao()
- testListarOrdenadoPorDataDesc()
```

**Critérios de Aceite**:
- [ ] Prospecção valida valor_atual <= valor_maximo do dado
- [ ] Anotações ordenadas por data decrescente
- [ ] Testes passam

---

### Task 4.4.5: Entidade FichaVantagem e VantagemService
**Prioridade**: P1  
**Estimativa**: 6h

**Subtarefas**:
1. Criar `FichaVantagem.java`:
   - Campos: nivel_atual, pontos_gastos_total, data_compra
   - `@Audited` para auditoria
   - Relacionamento com Ficha e VantagemConfig
   - **Regra**: Não pode ser deletado (compra permanente)
   - **Regra**: nivel_atual só pode aumentar

2. Criar `VantagemService`:
   - `listarVantagensDisponiveis(Long fichaId)` - vantagens que a ficha pode comprar
   - `comprarVantagem(Long fichaId, Long vantagemConfigId, Usuario)`:
     - Validar pré-requisitos da vantagem
     - Validar pontos disponíveis
     - Calcular custo (formula_custo ou CUSTO_BASE * 1)
     - Criar FichaVantagem com nivel=1
     - Atualizar pontos_vantagem_gastos na Ficha
   - `subirNivelVantagem(Long fichaId, Long fichaVantagemId, Usuario)`:
     - Validar que não está no nível máximo
     - Calcular custo (formula_custo ou CUSTO_BASE * novo_nivel)
     - Validar pontos disponíveis
     - Incrementar nivel_atual
     - Atualizar pontos_gastos_total
     - Atualizar pontos_vantagem_gastos na Ficha
   - `calcularPontosDisponiveis(Ficha)`:
     - (nivel × pontos_por_nivel) + pontos_iniciais - pontos_vantagem_gastos
   - `validarPreRequisitos(Ficha, VantagemConfig)` - verificar todos os pré-requisitos

3. Adicionar endpoints ao `FichaController`:
   - `GET /api/v1/jogos/{jogoId}/fichas/{fichaId}/vantagens` - listar vantagens da ficha
   - `GET /api/v1/jogos/{jogoId}/fichas/{fichaId}/vantagens/disponiveis` - listar disponíveis
   - `POST /api/v1/jogos/{jogoId}/fichas/{fichaId}/vantagens` - comprar vantagem
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/vantagens/{id}/subir-nivel` - subir nível

**Arquivos**:
```
src/main/java/.../model/ficha/FichaVantagem.java
src/main/java/.../repository/ficha/FichaVantagemRepository.java
src/main/java/.../service/VantagemService.java
src/main/java/.../dto/request/ComprarVantagemRequest.java
src/main/java/.../dto/response/FichaVantagemResponse.java
src/main/java/.../dto/response/VantagemDisponivelResponse.java
```

**Testes** (H2):
```
src/test/java/.../service/VantagemServiceTest.java
- testComprarVantagemComSucesso()
- testComprarVantagemSemPreRequisitoRetornaErro()
- testComprarVantagemSemPontosRetornaErro()
- testSubirNivelVantagemComSucesso()
- testSubirNivelAlemDoMaximoRetornaErro()
- testNaoPodeRemoverVantagemComprada()
- testCalculoCustoPorNivel()
- testCalculoPontosDisponiveis()

src/test/java/.../controller/FichaControllerIntegrationTest.java
- testListarVantagensDaFicha()
- testComprarVantagem()
- testSubirNivelVantagem()
```

**Critérios de Aceite**:
- [ ] Compra de vantagem é permanente (não pode deletar)
- [ ] Nível da vantagem só pode aumentar
- [ ] Pré-requisitos são validados
- [ ] Custo é calculado corretamente (CUSTO_BASE × NIVEL)
- [ ] Pontos disponíveis são verificados
- [ ] Efeitos da vantagem são aplicados nos cálculos
- [ ] Auditoria registra todas as compras/upgrades
- [ ] Testes passam

---

### Task 4.5: FichaService - Criar e Listar
**Prioridade**: P0  
**Estimativa**: 8h

**Subtarefas**:
1. Criar `FichaService`:
   - `listar(Long jogoId, Usuario, FiltroFicha)`:
     - Filtrar por minhas/todas
     - Incluir/excluir NPCs
   
   - `criar(Long jogoId, CriarFichaRequest, Usuario)`:
     - Validar participante do jogo
     - Validar configuração mínima do jogo
     - Se `isNpc = true`, validar que é Mestre
     - Criar ficha com valores padrão
     - Criar FichaAtributo para cada AtributoConfig
     - Criar FichaAptidao para cada AptidaoConfig
     - Criar FichaBonus para cada BonusConfig
     - Criar FichaVida
     - Criar FichaVidaMembro para cada MembroCorpoConfig
     - Criar FichaEssencia
     - Criar FichaAmeaca

2. Criar helper `FichaFactory`:
   - `inicializarComponentesFicha(Ficha, List<AtributoConfig>, ...)`

**Arquivos**:
```
src/main/java/.../service/FichaService.java
src/main/java/.../service/helper/FichaFactory.java
```

**Testes** (H2):
```
src/test/java/.../service/FichaServiceTest.java
- testListarFichasDoJogo()
- testListarApenaMinhasFichas()
- testCriarFichaComSucesso()
- testCriarFichaCriaAtributosAutomaticamente()
- testCriarFichaCriaAptidoesAutomaticamente()
- testCriarFichaCriaVidaComMembros()
- testCriarNpcApenasMestre()
- testCriarFichaSemConfigRetornaErro()
```

**Critérios de Aceite**:
- [ ] Criar ficha inicializa todos os componentes
- [ ] Apenas Mestre pode criar NPC
- [ ] Jogo precisa ter configuração mínima
- [ ] Testes passam

---

### Task 4.6: FichaService - Obter, Atualizar, Arquivar
**Prioridade**: P0  
**Estimativa**: 6h

**Subtarefas**:
1. Expandir `FichaService`:
   - `obter(Long jogoId, Long fichaId, Usuario)`:
     - Validar acesso
     - Carregar todos os componentes
     - Calcular valores derivados (nível, total, ímpeto, etc.)
   
   - `atualizar(Long jogoId, Long fichaId, AtualizarFichaRequest, Usuario)`:
     - Validar permissão (dono ou Mestre)
     - Atualizar campos básicos
   
   - `arquivar(Long jogoId, Long fichaId, Usuario)`:
     - Soft delete

2. Criar helpers de validação:
   - `validarAcessoFicha(Ficha, Usuario)`
   - `validarPermissaoEdicao(Ficha, Usuario)`

**Testes** (H2):
```
src/test/java/.../service/FichaServiceTest.java
- testObterFichaComTodosComponentes()
- testObterFichaComValoresCalculados()
- testAtualizarMinhaFicha()
- testAtualizarFichaOutroJogadorRetorna403()
- testMestrePoderEditarQualquerFicha()
- testArquivarFicha()
```

**Critérios de Aceite**:
- [ ] Dono pode editar sua ficha
- [ ] Mestre pode editar qualquer ficha
- [ ] Jogador não pode editar ficha de outro
- [ ] Valores são calculados ao obter
- [ ] Testes passam

---

### Task 4.7: FichaController
**Prioridade**: P0  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `FichaController`:
   - `GET /api/v1/jogos/{jogoId}/fichas`
   - `POST /api/v1/jogos/{jogoId}/fichas`
   - `GET /api/v1/jogos/{jogoId}/fichas/{fichaId}`
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}`
   - `DELETE /api/v1/jogos/{jogoId}/fichas/{fichaId}`

2. Documentação OpenAPI com `@Operation`, `@ApiResponse`

**Arquivos**:
```
src/main/java/.../controller/FichaController.java
```

**Testes** (H2):
```
src/test/java/.../controller/FichaControllerIntegrationTest.java
- testListarFichasDoJogo()
- testCriarFichaComSucesso()
- testCriarFichaValidacaoDeRequest()
- testObterFichaComDetalhes()
- testAtualizarFicha()
- testArquivarFicha()
- testAcessoNegadoParaNaoParticipante()
```

**Critérios de Aceite**:
- [ ] Todos os endpoints funcionam
- [ ] Validação de request funciona
- [ ] Documentação OpenAPI gerada
- [ ] Testes passam

---

### Task 4.8: Endpoints de Componentes da Ficha
**Prioridade**: P0  
**Estimativa**: 6h

**Subtarefas**:
1. Adicionar ao `FichaController`:
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/atributos`
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/aptidoes`
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/bonus`
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/vida`
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/essencia`
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/ameaca`
   - `PUT /api/v1/jogos/{jogoId}/fichas/{fichaId}/prospeccao`

2. Expandir `FichaService` com métodos de atualização parcial

**Testes** (H2):
```
src/test/java/.../controller/FichaControllerIntegrationTest.java
- testAtualizarAtributos()
- testAtualizarAptidoes()
- testAtualizarVidaEDanos()
- testAtualizarProspeccaoComLimite()
```

**Critérios de Aceite**:
- [ ] Atualização parcial funciona
- [ ] Validação de limites (prospecção)
- [ ] Auditoria registra alterações
- [ ] Testes passam

---

## Fase 5: Funcionalidades Auxiliares

### Task 5.1: AnotacaoService e Endpoints
**Prioridade**: P2  
**Estimativa**: 3h

**Subtarefas**:
1. Criar `AnotacaoService`:
   - `listar(Long jogoId, Long fichaId, Usuario)`
   - `criar(Long jogoId, Long fichaId, CriarAnotacaoRequest, Usuario)`
   - `excluir(Long jogoId, Long fichaId, Long anotacaoId, Usuario)`

2. Adicionar endpoints ao `FichaController`:
   - `GET /api/v1/jogos/{jogoId}/fichas/{fichaId}/anotacoes`
   - `POST /api/v1/jogos/{jogoId}/fichas/{fichaId}/anotacoes`
   - `DELETE /api/v1/jogos/{jogoId}/fichas/{fichaId}/anotacoes/{id}`

**Arquivos**:
```
src/main/java/.../service/AnotacaoService.java
```

**Testes** (H2):
```
src/test/java/.../service/AnotacaoServiceTest.java
- testCriarAnotacao()
- testListarAnotacoesOrdenadas()
- testExcluirAnotacao()
- testExcluirAnotacaoDeOutroJogador()
```

**Critérios de Aceite**:
- [ ] Anotações são ordenadas por data desc
- [ ] Apenas dono ou Mestre pode excluir
- [ ] Testes passam

---

## Fase 6: Validações e Cálculos

### Task 6.1: CalculoFichaService
**Prioridade**: P0  
**Estimativa**: 8h

**Subtarefas**:
1. Criar `CalculoFichaService`:
   - `calcularNivel(Integer experiencia, List<NivelConfig>)` → nivel e limitador
   - `calcularTotalAtributo(FichaAtributo, List<FichaVantagem>)` → base + nivel + outros + bônus_vantagens
   - `calcularImpeto(FichaAtributo, AtributoConfig)` → aplicar fórmula
   - `calcularBonusBase(FichaBonus, BonusConfig, Map<String, Integer> totaisAtributos)` → aplicar fórmula
   - `calcularBonusTotal(FichaBonus, Integer base, List<FichaVantagem>)` → base + vantagens + classe + itens + gloria + outros + bônus_vantagens
   - `calcularTotalAptidao(FichaAptidao, List<FichaVantagem>)` → base + sorte + classe + bônus_vantagens
   - `calcularVidaTotal(Ficha, FichaVida, VidaConfig, Map<String, Integer> totaisAtributos, List<FichaVantagem>)`
   - `calcularVidaMembro(Integer vidaTotal, MembroCorpoConfig)` → vida × porcentagem
   - `calcularEssenciaTotal(Ficha, FichaEssencia, EssenciaConfig, Map<String, Integer> totaisAtributos, List<FichaVantagem>)`
   - `calcularAmeacaTotal(Ficha, FichaAmeaca, AmeacaConfig)`
   - `calcularBonusVantagens(List<FichaVantagem>, TipoEfeito tipo, Long referenciaId)` → soma dos bônus de vantagens aplicáveis
   - `calcularPontosVantagemDisponiveis(Ficha, PontosVantagemConfig)` → pontos totais - gastos

2. Criar `FormulaParser`:
   - Parse simples de fórmulas (FLOOR, MIN, +, -, *, /)
   - Substituir variáveis (TOTAL, FORCA, AGILIDADE, NIVEL, etc.)
   - Suportar fórmula de custo de vantagem (CUSTO_BASE * NIVEL)

**Arquivos**:
```
src/main/java/.../service/CalculoFichaService.java
src/main/java/.../service/helper/FormulaParser.java
```

**Testes** (H2):
```
src/test/java/.../service/CalculoFichaServiceTest.java
- testCalcularNivelPorXp()
- testCalcularTotalAtributo()
- testCalcularTotalAtributoComBonusVantagem()
- testCalcularImpetoForca()
- testCalcularImpetoAgilidade()
- testCalcularBonusBBA()
- testCalcularBonusBBAComTCO()  // TCO adiciona +1 por nível
- testCalcularVidaTotal()
- testCalcularVidaTotalComSaudeDeFerro()  // +5 por nível
- testCalcularVidaPorMembro()
- testCalcularEssenciaTotal()
- testCalcularAmeacaTotal()
- testCalcularPontosVantagemDisponiveis()

src/test/java/.../service/helper/FormulaParserTest.java
- testParseFormulaSimples()
- testParseFormulaComFLOOR()
- testParseFormulaComMIN()
- testSubstituirVariaveis()
- testFormulaCustoVantagem()
```

**Critérios de Aceite**:
- [ ] Todos os cálculos do seed-data.md funcionam
- [ ] Bônus de vantagens são aplicados corretamente
- [ ] Fórmulas configuráveis são avaliadas corretamente
- [ ] Testes passam

---

### Task 6.2: ValidacaoFichaService
**Prioridade**: P1  
**Estimativa**: 4h

**Subtarefas**:
1. Criar `ValidacaoFichaService`:
   - `validarPontosAtributo(Ficha, List<FichaAtributo>, Integer nivel, List<NivelConfig>)`:
     - Calcular pontos esperados (nivel × 3 ou soma de pontos_atributo até o nivel)
     - Calcular pontos distribuídos (soma dos campos `nivel` de cada atributo)
     - Retornar `ValidacaoResponse` com status e mensagem

2. Criar método `validarPontosVantagem(Ficha, PontosVantagemConfig)`:
   - Calcular pontos disponíveis (nivel × pontos_por_nivel + pontos_iniciais)
   - Calcular pontos gastos (soma de todos pontos_gastos_total das FichaVantagem)
   - Retornar `ValidacaoVantagemResponse`

3. Criar `ValidacaoResponse`:
   - status: OK, PONTOS_SOBRANDO, PONTOS_EXCEDIDOS
   - mensagem
   - pontosDistribuidos
   - pontosEsperados
   - diferenca

4. Criar `ValidacaoVantagemResponse`:
   - status: OK, PONTOS_DISPONIVEIS
   - pontosDisponiveisTotal
   - pontosGastos
   - pontosRestantes

5. Integrar validação no retorno de `FichaService.obter()`

**Arquivos**:
```
src/main/java/.../service/ValidacaoFichaService.java
src/main/java/.../dto/response/ValidacaoResponse.java
src/main/java/.../dto/response/ValidacaoVantagemResponse.java
```

**Testes** (H2):
```
src/test/java/.../service/ValidacaoFichaServiceTest.java
- testValidacaoAtributosOk()
- testValidacaoAtributosPontosSobrando()
- testValidacaoAtributosPontosExcedidos()
- testCalculoPontosEsperadosPorNivel()
- testValidacaoVantagensComPontosDisponiveis()
- testValidacaoVantagensSemPontosDisponiveis()
```

**Critérios de Aceite**:
- [ ] Validação de atributos retorna status correto
- [ ] Validação de vantagens retorna pontos disponíveis
- [ ] Mensagem é descritiva
- [ ] Integrado ao GET de ficha
- [ ] Testes passam

---

## Fase 7: Auditoria e Histórico

### Task 7.1: Configuração de Auditoria Completa
**Prioridade**: P1  
**Estimativa**: 4h

**Subtarefas**:
1. Criar migração `V4__create_audit_tables.sql`:
   - Customizar tabela `REVINFO`
   - Adicionar colunas `usuario_id`, `ip_origem`

2. Verificar que todas as entidades de ficha têm `@Audited`

3. Configurar exclusão de campos sensíveis se necessário

**Arquivos**:
```
src/main/resources/db/migration/V4__create_audit_tables.sql
```

**Testes** (H2):
```
src/test/java/.../audit/AuditIntegrationTest.java
- testAlteracaoFichaCriaRegistroAuditoria()
- testHistoricoRegistraUsuario()
```

**Critérios de Aceite**:
- [ ] Tabelas _AUD são criadas
- [ ] Alterações são registradas
- [ ] Usuário é capturado
- [ ] Testes passam

---

### Task 7.2: HistoricoService e Endpoint
**Prioridade**: P1  
**Estimativa**: 5h

**Subtarefas**:
1. Criar `HistoricoService`:
   - `listarHistorico(Long jogoId, Long fichaId, Usuario, Pageable)`:
     - Validar que é Mestre
     - Buscar revisões usando `AuditReader`
     - Mapear para `HistoricoResponse`

2. Criar `HistoricoResponse`:
   - revisao, dataAlteracao, tipoAlteracao
   - usuarioNome, usuarioEmail
   - entidade, camposAlterados

3. Adicionar endpoint:
   - `GET /api/v1/jogos/{jogoId}/fichas/{fichaId}/historico`

**Arquivos**:
```
src/main/java/.../service/HistoricoService.java
src/main/java/.../dto/response/HistoricoResponse.java
src/main/java/.../dto/response/HistoricoPageResponse.java
```

**Testes** (H2):
```
src/test/java/.../service/HistoricoServiceTest.java
- testListarHistoricoApenasMestre()
- testListarHistoricoJogadorRetorna403()
- testHistoricoMostraCamposAlterados()

src/test/java/.../controller/FichaControllerIntegrationTest.java
- testObterHistoricoFicha()
```

**Critérios de Aceite**:
- [ ] Apenas Mestre pode ver histórico
- [ ] Histórico mostra quem alterou e quando
- [ ] Mostra campos que mudaram
- [ ] Paginação funciona
- [ ] Testes passam

---

## Resumo de Testes

### Convenção de Testes (H2)

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FichaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Fixtures
    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        // Criar dados de teste no H2
        mestre = usuarioRepository.save(criarUsuario("mestre@test.com"));
        jogador = usuarioRepository.save(criarUsuario("jogador@test.com"));
        jogo = jogoRepository.save(criarJogo("Campanha Teste"));
        // ...
    }

    @Test
    @WithMockUser(username = "mestre@test.com")
    void testCriarFichaComSucesso() {
        // Arrange
        var request = new CriarFichaRequest("Aragorn", classeId, racaId, false);

        // Act
        var result = mockMvc.perform(post("/api/v1/jogos/{jogoId}/fichas", jogo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        // Assert
        var response = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            FichaResponse.class
        );
        
        assertThat(response.getNomePersonagem()).isEqualTo("Aragorn");
        assertThat(response.getAtributos()).hasSize(7);
    }
}
```

### application-test.properties

```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Disable OAuth2 for tests
spring.security.oauth2.client.registration.google.client-id=test
spring.security.oauth2.client.registration.google.client-secret=test
```

---

## Ordem de Execução Recomendada

```
1.1 → 1.2 → 1.3 → 1.4
      ↓
2.1 → 2.2 → 2.3
      ↓
3.1 → 3.2 → 3.3 → 3.4 → 3.5 → 3.6
      ↓
4.1 → 4.2 → 4.3 → 4.4 → 4.5 → 4.6 → 4.7 → 4.8
      ↓
5.1 → 6.1 → 6.2 → 7.1 → 7.2
```

---

## Checklist Final

- [ ] Todas as tasks completadas
- [ ] Todos os testes passando
- [ ] Cobertura de testes > 80%
- [ ] Documentação OpenAPI atualizada
- [ ] Sem warnings de compilação
- [ ] Code review realizado
- [ ] Branch pronta para merge
