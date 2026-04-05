# Backlog de Epics — Ficha Controlador (Backend)

> Atualizado em: 2026-04-03
> Base: Glossario completo (01 a 05) x Estado atual do codigo x Specs planejadas
> **Tudo referente a Ficha sera refeito do ZERO.**

---

## Specs e Planejamento

| # | Spec | Epico(s) | Status |
|---|------|---------|--------|
| 001 | `001-backend-data-model` | Data model inicial, 13 CRUDs base | ✅ Concluido |
| 003 | `003-backend-refactor-best-practices` | DTOs records, validacoes, exceptions, mappers, testes, AWS | ✅ Concluido |
| 004 | [`004-configuracoes-siglas-formulas`](../specs/004-configuracoes-siglas-formulas/spec.md) | EPIC 1 + EPIC 2 (siglas, formulas, relacionamentos) | ✅ Concluido (17 tasks) |
| 005 | [`005-participantes`](../specs/005-participantes/spec.md) | EPIC 3 (participantes, aprovacao, permissoes) | 🟡 Spec + plan + tasks criados (6 tasks), implementacao pendente |
| 006 | [`006-ficha-wizard`](../specs/006-ficha-wizard/spec.md) | EPIC 4 (Wizard de criacao de ficha — 5-6 passos) | 🟡 Spec + plan + tasks criados (13 tasks), implementacao pendente |
| 007 | [`007-vantagem-efeito`](../specs/007-vantagem-efeito/spec.md) | EPIC 1.2 + EPIC 5 (VantagemEfeito + Motor de Calculos) | 🟡 Spec criado, plan+tasks pendentes |
| 008 | `008-utilidade-fluidez` | EPIC 7 (dashboards, filtros, reordenacao, export/import) | ✅ Backend ~100% |
| 009 | `009-npc-fichas-mestre` | EPIC 8 (NPC, duplicacao de ficha) | ✅ Backend ~100% (457 testes) |
| **008** | [`008-sub-recursos-classes-racas`](../specs/008-sub-recursos-classes-racas/) | Sub-recursos de Classes e Racas no frontend (ClasseBonus, AptidaoBonus, RacaBonus, ClassePermitida) | 🟡 spec.md PRONTO, tasks em criacao |
| **009-ext** | [`009-npc-visibility`](../specs/009-npc-visibility/) | NPC Visibility granular + Prospeccao sessao + Essencia (10 tasks: 6B + 4F) | 🟡 Spec + plan + tasks criados, implementacao pendente |
| **010** | [`010-roles-refactor`](../specs/010-roles-refactor/) | Roles ADMIN/MESTRE/JOGADOR por jogo, onboarding (9 tasks: 5B + 3F + 1T) | 🟡 Spec + plan + tasks criados, implementacao pendente |
| **011** | [`011-galeria-anotacoes`](../specs/011-galeria-anotacoes/) | EPIC 9 (galeria de imagens, anotacoes do jogador/mestre) (8 tasks: 4B + 4F) | 🟡 Spec + plan + tasks criados, implementacao pendente |
| **012** | [`012-niveis-progressao-frontend`](../specs/012-niveis-progressao-frontend/) | Niveis, Level Up e PontosVantagem no frontend | 📝 Diretorio criado, spec em criacao |

> Spec 002 nao utilizado.
> Specs 008, 009-ext, 010, 011 e 012 sao novas (adicionadas em 2026-04-03).

### 📋 Tasks do Spec 004

> Índice completo: [`specs/004-configuracoes-siglas-formulas/tasks/INDEX.md`](../specs/004-configuracoes-siglas-formulas/tasks/INDEX.md)

| Task | Fase | Descrição | Complexidade |
|------|------|-----------|-------------|
| [P1-T1](../specs/004-configuracoes-siglas-formulas/tasks/P1-T1-campos-sigla.md) | Siglas | Campos `sigla` em BonusConfig + VantagemConfig + DTOs/mappers | 🟢 |
| [P1-T2](../specs/004-configuracoes-siglas-formulas/tasks/P1-T2-repositorios.md) | Siglas | Métodos de repositório (queries de sigla e listagem) | 🟢 |
| [P1-T3](../specs/004-configuracoes-siglas-formulas/tasks/P1-T3-sigla-validation-service.md) | Siglas | `SiglaValidationService` cross-entity | 🔴 |
| [P1-T4](../specs/004-configuracoes-siglas-formulas/tasks/P1-T4-integracao-services.md) | Siglas | Integração nos services (Atributo, Bônus, Vantagem) | 🟡 |
| [P1-T5](../specs/004-configuracoes-siglas-formulas/tasks/P1-T5-sigla-controller.md) | Siglas | `SiglaController` — `GET /api/jogos/{id}/siglas` | 🟢 |
| [P2-T1](../specs/004-configuracoes-siglas-formulas/tasks/P2-T1-formula-evaluator.md) | Fórmulas | `FormulaEvaluatorService.validarFormula()` + `ValidationResult` | 🔴 |
| [P2-T2](../specs/004-configuracoes-siglas-formulas/tasks/P2-T2-formula-validation-services.md) | Fórmulas | Validação de fórmulas nos 3 services (dinâmico por jogo) | 🟡 |
| [P2-T3](../specs/004-configuracoes-siglas-formulas/tasks/P2-T3-formula-preview-endpoint.md) | Fórmulas | `FormulaPreviewService` + endpoints preview/variáveis | 🟡 |
| [P3-T1](../specs/004-configuracoes-siglas-formulas/tasks/P3-T1-categoria-vantagem-crud.md) | Configs | CategoriaVantagem: CRUD completo (fix Lombok + ConfiguracaoEntity) | 🟡 |
| [P3-T2](../specs/004-configuracoes-siglas-formulas/tasks/P3-T2-pontos-vantagem-crud.md) | Configs | PontosVantagemConfig: CRUD (service próprio, unique por nível) | 🟡 |
| [P3-T3](../specs/004-configuracoes-siglas-formulas/tasks/P3-T3-vantagem-categoria-fk.md) | Configs | VantagemConfig → CategoriaVantagem FK (nullable) | 🟢 |
| [P4-T1](../specs/004-configuracoes-siglas-formulas/tasks/P4-T1-prerequisito-entity.md) | Pré-requisitos | `VantagemPreRequisito` entity + repository + DTOs | 🟢 |
| [P4-T2](../specs/004-configuracoes-siglas-formulas/tasks/P4-T2-cycle-detection.md) | Pré-requisitos | Detecção de ciclos DFS (diretos + transitivos) | 🔴 |
| [P4-T3](../specs/004-configuracoes-siglas-formulas/tasks/P4-T3-vantagem-crud-update.md) | Pré-requisitos | VantagemConfig CRUD: endpoints dedicados + N+1 prevention | 🟡 |
| [P5-T1](../specs/004-configuracoes-siglas-formulas/tasks/P5-T1-classe-bonus-entities.md) | Classe | `ClasseBonus` + `ClasseAptidaoBonus` entities + repositories | 🟢 |
| [P5-T2](../specs/004-configuracoes-siglas-formulas/tasks/P5-T2-classe-personagem-update.md) | Classe | ClassePersonagem CRUD: endpoints dedicados + JOIN FETCH | 🟡 |
| [P6-T1](../specs/004-configuracoes-siglas-formulas/tasks/P6-T1-raca-classe-permitida.md) | Raça | `RacaClassePermitida` + atualização do CRUD de Raca | 🟡 |

---

## 📊 Resumo do Estado Atual

### ✅ O que EXISTE e funciona

- Infraestrutura: OAuth2, SecurityConfig, RateLimit, GlobalExceptionHandler, Swagger
- Jogo: CRUD + Template Klayrah Padrão (`GameConfigInitializerService`)
- JogoParticipante: Entity + Repository (associação Mestre/Jogador ao Jogo)
- **13 CRUDs de configuração** (controller + service + mapper + DTOs records + repository + testes de integração):
  AtributoConfig, NivelConfig, AptidaoConfig, TipoAptidao, BonusConfig,
  ClassePersonagem, Raca (c/ RacaBonusAtributo), MembroCorpoConfig,
  DadoProspeccaoConfig, GeneroConfig, IndoleConfig, PresencaConfig, VantagemConfig
- Todos os serviços de config validam: nome único por jogo, campos obrigatórios, nome duplicado em update
- `AbstractConfiguracaoService` com hooks `validarAntesCriar` / `validarAntesAtualizar`
- `BaseConfiguracaoServiceIntegrationTest` cobre ~10 cenários automaticamente
- `FormulaEvaluatorService`: motor básico com exp4j (`evaluate`, `isValid`, `calcularImpeto`, `calcularDerivado`, `calcularCustoVantagem`)
- CategoriaVantagem: Entity existe (Lombok incompleto, sem ConfiguracaoEntity, sem CRUD)
- PontosVantagemConfig: Entity existe (Lombok incompleto, sem CRUD)
- Entities de Ficha existem no model — **MAS serão descartadas/refeitas do zero**

### ❌ O que NÃO EXISTE (gap total)

- **Sub-entidades de relacionamento**: VantagemPreRequisito, VantagemEfeito ⚠️, ClasseBonus, ClasseAptidaoBonus, RacaClassePermitida
- **Validação cross-entity de siglas** (unicidade por jogo entre atributos, bônus e vantagens)
- **Campo sigla** em BonusConfig e VantagemConfig
- **Validação de fórmulas** ao criar/atualizar configs (formulaImpeto, formulaBase, formulaCusto)
- **CategoriaVantagem CRUD** e **PontosVantagemConfig CRUD**
- Todo o CRUD de Ficha (do zero)
- Motor de cálculos integrado à Ficha (recalcular ao salvar)
- Gestão de Participantes completa (convite, aprovação, remoção)
- Auditoria com Envers (configurado no properties mas sem `@Audited` nas entities)
- Endpoints de utilidade/fluidez (preview de fórmulas, reordenação batch, duplicação de jogo, export/import)

> ⚠️ **VantagemEfeito** foi explicitamente adiado para spec posterior — não entra no spec 004.
> ⚠️ **Detecção de ciclos em fórmulas de bônus** (ex: BonusA usa sigla de BonusB que usa sigla de BonusA) também adiada.

---

## 🏔️ EPIC 1 — Aprofundamento das Configurações Existentes

> **Objetivo:** As 13 configs têm CRUD básico, mas faltam os **relacionamentos**, **campos** e **validações** que o glossário exige.
> **Spec**: `specs/004-configuracoes-siglas-formulas/` cobre os itens 1.1 a 1.6.

### 1.1 — Sistema de Siglas/Abreviações `[spec 004, Phase 1]`

Tasks: [P1-T1](../specs/004-configuracoes-siglas-formulas/tasks/P1-T1-campos-sigla.md) → [P1-T2](../specs/004-configuracoes-siglas-formulas/tasks/P1-T2-repositorios.md) → [P1-T3](../specs/004-configuracoes-siglas-formulas/tasks/P1-T3-sigla-validation-service.md) → [P1-T4](../specs/004-configuracoes-siglas-formulas/tasks/P1-T4-integracao-services.md) → [P1-T5](../specs/004-configuracoes-siglas-formulas/tasks/P1-T5-sigla-controller.md)

- ✅ AtributoConfig já tem campo `abreviacao` com unique constraint DB `(jogo_id, abreviacao)`
- 📝 Adicionar campo `sigla` (obrigatório) em `BonusConfig`
- 📝 Adicionar campo `sigla` (opcional) em `VantagemConfig`
- 📝 `SiglaValidationService` — validação cross-entity de unicidade por jogo (AtributoConfig + BonusConfig + VantagemConfig)
- 📝 Integrar validação nos services: `AtributoConfiguracaoService`, `BonusConfiguracaoService`, `VantagemConfiguracaoService`
- 📝 `GET /api/jogos/{id}/siglas` — lista todas as siglas em uso (tipo + entidade de origem)
- 📝 Aviso/rejeição ao editar/excluir sigla usada em fórmulas de outras configs (FR-006)

### 1.2 — Vantagem: Sub-entidades `[spec 004, Phase 4 + ⚠️ VantagemEfeito adiado]`

Tasks: [P4-T1](../specs/004-configuracoes-siglas-formulas/tasks/P4-T1-prerequisito-entity.md) → [P4-T2](../specs/004-configuracoes-siglas-formulas/tasks/P4-T2-cycle-detection.md) → [P4-T3](../specs/004-configuracoes-siglas-formulas/tasks/P4-T3-vantagem-crud-update.md)

- 📝 `VantagemPreRequisito` — entity + relacionamento com VantagemConfig (nivelMinimo)
- 📝 Detecção de ciclos de pré-requisito (A → B → A bloqueado) — algoritmo DFS
- 📝 Atualizar VantagemConfig CRUD: endpoints dedicados para pré-requisitos + N+1 prevention
- ⏳ **ADIADO**: `VantagemEfeito` — entity + enum TipoEfeito, alvo, valor por nível (virá em spec posterior, bloqueia EPIC 4/5)

### 1.3 — Classe: Sub-entidades de Bônus `[spec 004, Phase 5]`

Tasks: [P5-T1](../specs/004-configuracoes-siglas-formulas/tasks/P5-T1-classe-bonus-entities.md) → [P5-T2](../specs/004-configuracoes-siglas-formulas/tasks/P5-T2-classe-personagem-update.md)

- 📝 `ClasseBonus` — entity (ClassePersonagem + BonusConfig + valorPorNivel BigDecimal)
- 📝 `ClasseAptidaoBonus` — entity (ClassePersonagem + AptidaoConfig + bonus fixo)
- 📝 Atualizar ClassePersonagem CRUD: endpoints dedicados + JOIN FETCH para N+1 prevention

### 1.4 — Raça: Classes Permitidas `[spec 004, Phase 6]`

Task: [P6-T1](../specs/004-configuracoes-siglas-formulas/tasks/P6-T1-raca-classe-permitida.md)

- ✅ `RacaBonusAtributo` já existe
- 📝 `RacaClassePermitida` — entity (Raca + ClassePersonagem) com unique `(raca_id, classe_id)`
- 📝 Atualizar Raca CRUD para incluir classes permitidas no response (JOIN FETCH duplo)

### 1.5 — CategoriaVantagem: CRUD completo `[spec 004, Phase 3]`

Tasks: [P3-T1](../specs/004-configuracoes-siglas-formulas/tasks/P3-T1-categoria-vantagem-crud.md) · [P3-T3](../specs/004-configuracoes-siglas-formulas/tasks/P3-T3-vantagem-categoria-fk.md)

- 📝 Corrigir entity: Lombok `@Data @Builder @EqualsAndHashCode(callSuper=true)`, implementar `ConfiguracaoEntity`, renomear `ordem` → `ordemExibicao`
- 📝 Controller, Service (extends AbstractConfiguracaoService), Mapper, DTOs records, Repository
- 📝 FK `VantagemConfig → CategoriaVantagem` (nullable) — validação de mesmo jogo no controller
- 📝 Testes de integração (extends BaseConfiguracaoServiceIntegrationTest)

### 1.6 — PontosVantagemConfig: CRUD completo `[spec 004, Phase 3]`

Task: [P3-T2](../specs/004-configuracoes-siglas-formulas/tasks/P3-T2-pontos-vantagem-crud.md)

- 📝 Corrigir entity: Lombok `@Data @Builder @EqualsAndHashCode(callSuper=true)`
- 📝 Service próprio (não extend AbstractConfiguracaoService — sem campo `nome`, unique por `nivel`)
- 📝 Controller, Mapper, DTOs records, Repository
- 📝 Testes de integração

---

## 🏔️ EPIC 2 — Motor de Fórmulas e Siglas

> **Objetivo:** Evoluir o `FormulaEvaluatorService` de básico para um motor robusto e integrado, com validação, preview e gestão centralizada.
> **Spec**: `specs/004-configuracoes-siglas-formulas/` cobre os itens 2.1 a 2.3.

### 2.1 — Validação de fórmulas contra siglas do jogo `[spec 004, Phase 2]`

Tasks: [P2-T1](../specs/004-configuracoes-siglas-formulas/tasks/P2-T1-formula-evaluator.md) → [P2-T2](../specs/004-configuracoes-siglas-formulas/tasks/P2-T2-formula-validation-services.md)

- 📝 Novo método `FormulaEvaluatorService.validarFormula(formula, Set<String> variaveis)` retornando `ValidationResult` (isValid, variáveis inválidas, erro de sintaxe) — extração de variáveis via regex
- 📝 `formulaImpeto` (AtributoConfig): variáveis aceitas = `{total}` + funções matemáticas
- 📝 `formulaBase` (BonusConfig): variáveis aceitas = siglas dos atributos do jogo (dinâmico, buscado no DB) + `{nivel, base}`
- 📝 `formulaCusto` (VantagemConfig): variáveis aceitas = `{custoBase, nivelVantagem}`
- 📝 Erro descritivo listando variáveis não reconhecidas

### 2.2 — Endpoint de preview/teste de fórmulas `[spec 004, Phase 2]`

Task: [P2-T3](../specs/004-configuracoes-siglas-formulas/tasks/P2-T3-formula-preview-endpoint.md)

- 📝 `POST /api/jogos/{id}/formulas/preview` — recebe `{formula, tipo, valores}`, retorna resultado sem persistir
- 📝 `FormulaPreviewService` (service de apoio, resolve variáveis por tipo de fórmula)

### 2.3 — Endpoint de variáveis disponíveis `[spec 004, Phase 2]`

Task: [P2-T3](../specs/004-configuracoes-siglas-formulas/tasks/P2-T3-formula-preview-endpoint.md) (mesmo arquivo)

- 📝 `GET /api/jogos/{id}/formulas/variaveis` — lista variáveis agrupadas por tipo (atributos, bônus, vantagens, fixas)

### 2.4 — Detecção de dependências circulares em fórmulas `[⏳ adiado]`

- ⏳ **ADIADO para spec posterior**: BonusA usa sigla de BonusB, BonusB usa sigla de BonusA → detectar ciclo
- Complexidade elevada, pode ser adicionada após EPIC 1+2 estarem completos

### 2.5 — Impacto de renomear/excluir sigla `[spec 004, FR-006 — aviso parcial]`

- 📝 Spec 004 FR-006 cobre: aviso ou rejeição ao editar/excluir sigla usada em fórmulas de outras configs
- ⏳ **Análise detalhada de impacto** (lista de todas as fórmulas afetadas) pode vir em spec posterior

---

## 🏔️ EPIC 3 — Gestão de Participantes

> **Objetivo:** Fluxo completo de entrada, aprovação e gestão de jogadores em um Jogo.
> **Spec**: [`005-gestao-participantes`](../specs/005-gestao-participantes/spec.md)

### O que falta:

**3.1 — Fluxo de Convite/Solicitação**
- Jogador solicita entrada em um jogo
- Mestre aprova/rejeita solicitação
- Status do participante (PENDENTE, APROVADO, REJEITADO, BANIDO)

**3.2 — CRUD de Participantes**
- `GET /api/jogos/{id}/participantes` — listar participantes
- `POST /api/jogos/{id}/participantes/solicitar` — jogador solicita entrada
- `PUT /api/jogos/{id}/participantes/{pid}/aprovar` — mestre aprova
- `PUT /api/jogos/{id}/participantes/{pid}/rejeitar` — mestre rejeita
- `DELETE /api/jogos/{id}/participantes/{pid}` — mestre remove participante

**3.3 — Permissões baseadas em participação**
- SecurityService: `canAccessJogo(jogoId)`, `canEditFicha(fichaId)`, `isMestreDoJogo(jogoId)`
- Validar que jogador só acessa recursos de jogos onde é participante aprovado

---

## 🏔️ EPIC 4 — Ficha de Personagem (do ZERO)

> **Objetivo:** Implementar do zero todo o sistema de Ficha de Personagem — o core do sistema.
> **Spec**: [`006-ficha-personagem`](../specs/006-ficha-personagem/spec.md)
> **Bloqueado por**: EPIC 1 (sub-entidades), EPIC 3 (participantes)
> **Parcialmente bloqueado por**: VantagemEfeito (1.2 adiado)

### O que falta (TUDO):

**4.1 — Ficha: Entity principal + CRUD**
- Entity Ficha com campos de identidade: nome, jogador, jogo, raça, classe, gênero, índole, presença, nível, XP, renascimentos
- Campos narrativos: origem, arquétipo de referência, insólitus, título heróico
- `POST /api/jogos/{id}/fichas` — criar ficha (inicializa sub-componentes vazios baseados nas configs do jogo)
- `GET /PUT /DELETE` padrão
- `GET /api/jogos/{id}/fichas` — listar fichas do jogo (Mestre vê todas, Jogador vê as suas)

**4.2 — FichaAtributo**
- Um registro por atributo configurado no jogo
- Campos: base, nivel, outros → total (calculado)
- Ímpeto calculado via fórmula do AtributoConfig
- Respeitar Limitador do nível atual

**4.3 — FichaAptidao**
- Um registro por aptidão configurada no jogo
- Campos: base, sorte, classe → total (calculado)

**4.4 — FichaBonus**
- Um registro por bônus configurado no jogo
- Base calculada pela fórmula do BonusConfig (usa totais dos atributos)
- Campos adicionais: vantagens, classe, itens, gloria, outros → total (calculado)

**4.5 — FichaVida + FichaVidaMembro**
- FichaVida: vidaTotal = Vigor + Nível + VT + Renascimentos + Outros
- FichaVidaMembro: um registro por membro do corpo → vida = vidaTotal × porcentagem do membro
- Campo de dano recebido por membro

**4.6 — FichaEssencia**
- EssênciaTotal = FLOOR((Vigor + Sabedoria) / 2) + Nível + Renascimentos + Vantagens + Outros
- Essência restante (total - gastos)

**4.7 — FichaAmeaca**
- Ameaça = Nível + Itens + Títulos + Renascimentos + Outros

**4.8 — FichaProspeccao**
- Um registro por tipo de dado configurado no jogo
- Contador de quantos dados o personagem possui atualmente

**4.9 — FichaVantagem**
- Vantagem comprada pelo personagem, com nível atual
- Validar pré-requisitos antes de permitir compra
- Uma vez comprada, NÃO pode ser removida (nível só sobe)
- Calcular custo via fórmula da VantagemConfig

**4.10 — Descrição Física**
- Altura, peso (auto-calculado por BMI se gênero definido), idade, olhos, cabelos, pele, etc.

---

## 🏔️ EPIC 5 — Motor de Cálculos da Ficha

> **Objetivo:** Integrar o FormulaEvaluatorService ao ciclo de vida da Ficha — recalcular TUDO ao salvar.
> **Spec**: [`007-motor-calculos`](../specs/007-motor-calculos/spec.md)
> **Bloqueado por**: EPIC 4

### O que falta:

**5.1 — FichaCalculationService (Backend)**
- Serviço que recebe uma Ficha e recalcula todos os valores derivados:
  - Totais de atributos (base + nível + outros)
  - Ímpetos (fórmula do atributo × total)
  - Bônus base (fórmula do bônus × atributos)
  - Totais de bônus (base + vantagens + classe + itens + glória + outros)
  - Vida total e vida por membro
  - Essência total
  - Ameaça
  - Nível atual (lookup na tabela de NivelConfig pela XP)
  - Pontos de atributo/vantagem disponíveis vs. gastos
  - Custo total de vantagens compradas

**5.2 — Integração no fluxo de save**
- Ao salvar ficha (`POST`/`PUT`), chamar `FichaCalculationService.recalcular(ficha)` ANTES de persistir
- Backend é a fonte oficial de todos os cálculos

**5.3 — Validações de negócio ao salvar**
- Total de pontos de atributo distribuídos ≤ pontos disponíveis pelo nível
- Nenhum atributo ultrapassa o limitador do nível atual
- Pontos de vantagem gastos ≤ pontos disponíveis
- Pré-requisitos de vantagens atendidos
- Classe permitida pela raça escolhida

**5.4 — Endpoint de preview de cálculos**
- `POST /api/fichas/{id}/preview` — recebe dados editados, retorna ficha com valores recalculados SEM persistir
- Para o frontend poder mostrar preview em tempo real sem salvar

---

## 🏔️ EPIC 6 — Auditoria e Histórico

> **Objetivo:** Rastreabilidade completa de alterações nas fichas.

### O que falta:

**6.1 — Hibernate Envers nas entities de Ficha**
- Adicionar `@Audited` em: Ficha, FichaAtributo, FichaAptidao, FichaBonus, FichaVida, FichaVidaMembro, FichaEssencia, FichaAmeaca, FichaProspeccao, FichaVantagem
- Configuração já existe no `application.properties` ✅

**6.2 — Endpoints de histórico**
- `GET /api/fichas/{id}/historico` — lista revisões da ficha com datas e autor
- `GET /api/fichas/{id}/historico/{rev}` — snapshot da ficha em uma revisão específica
- Diff entre revisões (opcional, pode ser 2ª fase)

---

## 🏔️ EPIC 7 — Endpoints de Utilidade e Fluidez

> **Objetivo:** Endpoints auxiliares que não são CRUD puro, mas melhoram a UX e permitem que o frontend seja fluido.
> **Spec**: [`008-utilidade-fluidez`](../specs/008-utilidade-fluidez/spec.md)

### O que falta:

**7.1 — Dashboard do Mestre**
- `GET /api/jogos/{id}/dashboard` — resumo: qtd fichas, participantes, fichas por nível, últimas alterações

**7.2 — Dashboard do Jogador**
- `GET /api/jogos/{id}/fichas/minhas` — fichas do jogador logado naquele jogo
- `GET /api/jogos/meus` — jogos onde o jogador participa (já existe parcial no JogoController)

**7.3 — Busca e Filtros**
- Filtros nas configurações: buscar por nome, ordenar por ordem_exibicao
- Filtros nas fichas: buscar por nome do personagem, classe, raça, nível

**7.4 — Reordenação em batch**
- `PUT /api/jogos/{id}/config/atributos/reordenar` — recebe lista de IDs na nova ordem, atualiza `ordemExibicao` de todos em batch
- Aplicar para todas as 13 configs que têm `ordemExibicao`

**7.5 — Duplicação de Jogo**
- `POST /api/jogos/{id}/duplicar` — cria novo jogo com cópia de todas as configurações (sem fichas/participantes)
- Útil quando o Mestre quer criar variação de campanha

**7.6 — Export/Import de configurações**
- `GET /api/jogos/{id}/config/export` — exporta todas as configs em JSON
- `POST /api/jogos/{id}/config/import` — importa configs de JSON
- Permite compartilhar "presets" entre mestres

**7.7 — Resumo de Ficha (readonly)**
- `GET /api/fichas/{id}/resumo` — versão compacta com apenas dados calculados (para listagens, cards, etc.)

---

## 🏔️ EPIC 8 — NPC e Fichas do Mestre

> **Objetivo:** Permitir que o Mestre crie e gerencie fichas de NPCs.
> **Spec**: [`009-npc-fichas-mestre`](../specs/009-npc-fichas-mestre/spec.md)

### O que falta:

**8.1 — Criação de NPC**
- Mestre pode criar fichas sem jogador dono (NPC)
- Flag `isNpc` ou `jogadorId = null` na Ficha

**8.2 — Gestão de NPCs**
- `GET /api/jogos/{id}/npcs` — listar NPCs do jogo
- CRUD normal mas com permissão exclusiva do Mestre
- NPC pode ter todas as mesmas capacidades de uma ficha normal

**8.3 — Duplicação de ficha/NPC**
- `POST /api/fichas/{id}/duplicar` — cria cópia de ficha (útil para criar NPCs variantes)

---

## EPIC 9 — Galeria de Imagens e Anotacoes

> **Objetivo:** Features complementares para enriquecer a experiencia.
> **Spec**: [`011-galeria-anotacoes`](../specs/011-galeria-anotacoes/) (EM ESPECIFICACAO)

### O que falta:

**9.1 — Galeria de imagens da ficha**
- Upload de imagem do personagem (avatar)
- Galeria de referencias visuais
- Storage: S3 ou filesystem local

**9.2 — Anotacoes/Diario do personagem**
- CRUD de anotacoes livres vinculadas a ficha
- Campos: titulo, conteudo (texto), data, visibilidade (publica/privada)

**9.3 — Anotacoes do Mestre sobre participante/ficha**
- Notas privadas que so o Mestre ve sobre cada ficha/jogador

---

## EPIC 10 — NPC Visibility, Prospeccao em Sessao, Essencia (NOVO)

> **Objetivo:** Completar o fluxo de NPC (visibilidade granular por jogador) e integrar prospeccao/essencia ao ciclo de sessao.
> **Spec**: [`009-npc-visibility`](../specs/009-npc-visibility/) (EM ESPECIFICACAO)
> **Depende de**: Spec 006 (ficha funcional), parcialmente Spec 009 (NPC backend existente)

### O que falta:

**10.1 — Visibilidade granular de NPC**
- Mestre revela stats de NPC para jogadores especificos (nao todos de uma vez)
- Dois niveis de revelacao: existencia vs. stats completos
- Modelagem adicional no backend para controle por jogador
- Design existente: `docs/design/NPC-VISIBILITY.md`

**10.2 — Prospeccao em sessao**
- Jogador usa dado de prospeccao, Mestre confirma consumo
- Mestre pode reverter uso (erro, situacao especial)
- Design existente: `docs/design/PROSPECCAO-SESSAO.md`

**10.3 — Essencia atual (campo persistido + endpoint)**
- `essenciaAtual` como campo persistido na ficha (nao apenas calculado)
- Endpoint para gastar/restaurar essencia em sessao
- GAP-07 do dossie de gaps

---

## EPIC 11 — Roles ADMIN/MESTRE/JOGADOR Refactor (NOVO)

> **Objetivo:** Refatorar o sistema de roles para suportar ADMIN global, separar MESTRE/JOGADOR por jogo, e implementar onboarding.
> **Spec**: [`010-roles-refactor`](../specs/010-roles-refactor/) (EM ESPECIFICACAO)
> **Depende de**: Spec 005 (participantes com maquina de estados)

### O que falta:

**11.1 — Role ADMIN global**
- Novo papel com acesso a todos os jogos e operacoes administrativas
- Diferente de MESTRE (que e por jogo)

**11.2 — Roles por jogo**
- MESTRE e JOGADOR sao papeis dentro de um jogo, nao globais
- Refatorar SecurityConfig e @PreAuthorize para suportar contexto de jogo

**11.3 — Onboarding**
- Fluxo de primeiro acesso para novos usuarios
- Perfil inicial, selecao de jogo

---

## Prioridade Sugerida (atualizada 2026-04-03)

| Ordem | Epic | Spec | Status | Justificativa |
|-------|------|------|--------|---------------|
| 1 | **EPIC 1+2** — Configs + Formulas | 004 | ✅ Concluido | Base para tudo — sub-entidades, siglas, formulas |
| 2 | **EPIC 1.2+5** — VantagemEfeito + Motor | 007 | 🟡 Spec criado | Pre-requisito obrigatorio para ficha funcional |
| 3 | **EPIC 4** — Wizard de Criacao de Ficha | 006 | 🟡 Spec+plan+tasks | O core — ficha funcional de verdade |
| 4 | **EPIC 3** — Gestao de Participantes | 005 | 🟡 Spec+plan+tasks | Desbloqueia fluxo completo de acesso |
| 5 | **EPIC 7** — Utilidade e Fluidez | 008 | ✅ Backend pronto | Melhora UX, endpoints de conveniencia |
| 6 | **EPIC 8** — NPC Backend | 009 | ✅ Backend pronto | Extensao natural da ficha |
| 7 | **EPIC 10** — NPC Visibility + Prospeccao | 009-ext | 📝 Em especificacao | Visibilidade granular, sessao, essencia |
| 8 | **EPIC 11** — Roles Refactor | 010 | 📝 Em especificacao | ADMIN global, roles por jogo, onboarding |
| 9 | **EPIC 9** — Galeria/Anotacoes | 011 | 📝 Em especificacao | Enriquecimento da ficha |
| 10 | **EPIC 6** — Auditoria (Envers) | — | Adiado | Importante mas nao MVP |

---

## Estimativa de Escopo

| Epic | Spec | Entities novas | Endpoints novos (aprox.) | Complexidade | Status |
|------|------|---------------|-------------------------|-------------|--------|
| EPIC 1+2 | 004 | ~6 (ClasseBonus, ClasseAptidaoBonus, RacaClassePermitida, VantagemPreRequisito, + 2 CRUDs) + services | ~16 | Media | ✅ Concluido |
| EPIC 1.2+5 | 007 | ~1 (InsolitusCo) + refactor motor | ~4 | Alta (logica) | 🟡 Spec criado |
| EPIC 4 | 006 | ~0 (refactor existentes) | ~5 backend + wizard frontend | Alta | 🟡 Spec+plan+tasks |
| EPIC 3 | 005 | ~0 (correcao existentes) | ~4 endpoints faltantes | Media | 🟡 Spec+plan+tasks |
| EPIC 7 | 008 | 0 | ~8 | Media | ✅ Backend pronto |
| EPIC 8 | 009 | 0 (reuso Ficha) | ~4 | Baixa | ✅ Backend pronto |
| EPIC 10 | 009-ext | ~1-2 (NpcVisibilidade, EssenciaLog) | ~6 | Media | 📝 Em especificacao |
| EPIC 11 | 010 | ~1 (AdminRole, refactor SecurityConfig) | ~5 | Alta (seguranca) | 📝 Em especificacao |
| EPIC 9 | 011 | ~2-3 (Galeria, Anotacao) | ~6 | Media | 📝 Em especificacao |
| EPIC 6 | — | 0 (@Audited) | ~3 | Baixa | Adiado |

---

## Itens Explicitamente Adiados

| Item | Motivo | Quando retomar |
|---|---|---|
| `VantagemEfeito` integrado ao motor (1.2) | Complexidade elevada | **Spec 007** — PRIORIDADE ABSOLUTA agora |
| Deteccao de ciclos em formulas (2.4) | Complexidade elevada, nao bloqueia EPIC 1-3 | Spec posterior, junto ou apos EPIC 5 |
| Analise detalhada de impacto de sigla (2.5) | Refinamento do aviso basico ja planejado | Idem EPIC 2.4 |
| Auditoria Envers (EPIC 6) | Nao MVP | Apos todas as specs principais |

---

*Documento vivo — atualizado conforme specs sao criadas e tasks concluidas. Ultima atualizacao: 2026-04-03.*
