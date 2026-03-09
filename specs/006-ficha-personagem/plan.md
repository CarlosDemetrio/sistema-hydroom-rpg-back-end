# Plano de Implementação — Spec 006 (Ficha de Personagem)

> Data: Março 2026
> Baseado em: spec.md, EPICS-BACKLOG.md, Specs 004 e 005

## Phase 0 — Descoberta

**Fontes consultadas:**
- `docs/EPICS-BACKLOG.md` — EPIC 4
- `docs/glossario/03-termos-dominio.md` — campos de Ficha, sub-componentes
- `specs/004-configuracoes-siglas-formulas/spec.md` — VantagemPreRequisito (bloqueante)
- `specs/005-gestao-participantes/spec.md` — validação de participação (bloqueante)
- `service/FormulaEvaluatorService.java` — API de cálculo de fórmulas
- Entities existentes: AtributoConfig, BonusConfig, VantagemConfig, NivelConfig, RacaBonusAtributo

**Estado atual:**
- Ficha: entities existem no model mas serão descartadas/refeitas do zero
- Nenhum service, controller, DTO ou mapper de Ficha existe
- FormulaEvaluatorService funciona com exp4j (calcularImpeto, calcularDerivado, calcularCustoVantagem)
- VantagemPreRequisito depende de Spec 004 Phase 4 (P4-T1, P4-T2)

**Bloqueantes:**
- Spec 004 Phase 4 concluída (VantagemPreRequisito entity + cycle detection)
- Spec 005 Phase 2 concluída (ParticipanteSecurityService disponível)

## Phase 1 — Ficha Entity + CRUD Base

**Objetivo:** Criar a entity Ficha com todos os campos de identidade/narrativos e o CRUD base.

**Tarefas:**
- P1-T1: Ficha entity com campos de identidade, narrativos, FKs para configs (Raca, ClassePersonagem, GeneroConfig, IndoleConfig, PresencaConfig), campos: nivel, xp, renascimentos
- P1-T2: FichaService (CRUD + createSubRecords) + FichaController (thin layer)

## Phase 2 — Sub-entities de Atributo e Aptidão

**Objetivo:** FichaAtributo e FichaAptidao — registros inicializados por config do jogo.

**Tarefas:**
- P2-T1: FichaAtributo entity + FichaAptidao entity + repositories
- P2-T2: Endpoints de update (PUT /api/fichas/{id}/atributos, PUT /api/fichas/{id}/aptidoes)

## Phase 3 — FichaBonus

**Objetivo:** FichaBonus com campos de bônus e endpoint de update.

**Tarefas:**
- P3-T1: FichaBonus entity + endpoint PUT /api/fichas/{id}/bonus

## Phase 4 — Vida, Essência, Ameaça, Prospecção

**Objetivo:** Quatro sub-entities auxiliares de estado do personagem.

**Tarefas:**
- P4-T1: FichaVida + FichaVidaMembro + FichaEssencia + FichaAmeaca + FichaProspeccao entities
- P4-T2: Endpoints PUT para cada uma

## Phase 5 — FichaVantagem

**Objetivo:** Compra de vantagens com validação de pré-requisitos.

**Tarefas:**
- P5-T1: FichaVantagem entity + lógica de compra + validação de pré-requisitos + cálculo de custo

## Phase 6 — FichaDescricaoFisica

**Objetivo:** Dados físicos do personagem.

**Tarefas:**
- P6-T1: FichaDescricaoFisica entity + endpoint PUT /api/fichas/{id}/descricao-fisica

## Phase 7 — Testes de Integração

**Tarefas:**
- P7-T1: Testes FichaService (criar, listar, atualizar, inicialização de sub-registros)
- P7-T2: Testes FichaVantagem (compra, pré-requisitos, custo)

## Ordem de execução

Phase 1 → Phase 2 → Phase 3 (paralelo com 4 e 5) → Phase 6 → Phase 7

Phases 3, 4 e 5 podem rodar em paralelo após Phase 2.

## Riscos

- Inicialização de sub-registros ao criar Ficha é complexa: uma transação cria Ficha + N registros de cada tipo (N = qtd de configs do jogo)
- FichaAtributo deve respeitar o limitador de nível — acoplamento com NivelConfig
- FichaVantagem com pré-requisitos depende de Spec 004 Phase 4 (VantagemPreRequisito)
- FormulaEvaluatorService usa variáveis por sigla — mapear FichaAtributo por sigla do AtributoConfig
