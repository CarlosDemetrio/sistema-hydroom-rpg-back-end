# Plano de Implementação — Spec 007 (Motor de Cálculos da Ficha)

> Data: Março 2026
> Baseado em: spec.md, EPICS-BACKLOG.md, Specs 004, 006

## Phase 0 — Descoberta

**Fontes consultadas:**
- `docs/EPICS-BACKLOG.md` — EPIC 5
- `service/FormulaEvaluatorService.java` — API existente (calcularImpeto, calcularDerivado, calcularCustoVantagem, isValid)
- `specs/006-ficha-personagem/spec.md` — sub-entities de Ficha a calcular
- `docs/glossario/04-siglas-formulas.md` — regras de fórmulas por tipo

**Estado atual:**
- FormulaEvaluatorService: funcional com exp4j, variáveis: total, nivel, base + siglas de atributos
- Ficha sub-entities: serão criadas em Spec 006 (bloqueante)
- Sem FichaCalculationService ainda
- Fórmulas:
  - formulaImpeto (AtributoConfig): variável `total` (total do atributo)
  - formulaBase (BonusConfig): variáveis = siglas de atributos do jogo (ex: FOR, AGI, VIG)
  - formulaCusto (VantagemConfig): variáveis `custoBase`, `nivelVantagem`

**Bloqueante:** Spec 006 concluída (entities FichaAtributo, FichaBonus, etc. devem existir)

## Phase 1 — FichaCalculationService (core)

**Objetivo:** Serviço stateless que recalcula todos os valores derivados de uma Ficha.

**Tarefas:**
- P1-T1: Cálculo de atributos (total + ímpeto via formulaImpeto)
- P1-T2: Cálculo de bônus (base via formulaBase + total)
- P1-T3: Cálculo de vida/essência/ameaça

## Phase 2 — Integração no fluxo de save

**Objetivo:** FichaCalculationService chamado automaticamente em FichaService.

**Tarefas:**
- P2-T1: Injetar e chamar FichaCalculationService em FichaService.criar() e .atualizar()
- P2-T1: Nível automático por XP (lookup em NivelConfig)

## Phase 3 — FichaValidationService

**Objetivo:** Validações de negócio antes de persistir.

**Tarefas:**
- P3-T1: FichaValidationService com todos os checks de integridade

## Phase 4 — Preview Endpoint

**Objetivo:** Simular cálculos sem persistir.

**Tarefas:**
- P4-T1: FichaPreviewService + POST /api/fichas/{id}/preview

## Phase 5 — Testes

**Tarefas:**
- P5-T1: Testes unitários do FichaCalculationService
- P5-T2: Testes de integração do fluxo completo

## Ordem de execução

Phase 1 (T1+T2+T3 em paralelo) → Phase 2 → Phase 3 → Phase 4 → Phase 5

## Riscos

- Vigor e Sabedoria para fórmula de essência: identificar por sigla — convencionar `VIG` e `SAB` como siglas padrão (ou buscar por configuração do jogo)
- Preview deve ser transacional mas não persistir — usar lógica in-memory com entities detached
- FormulaEvaluatorService usa variáveis por sigla — garantir que FichaAtributo está mapeado por atributoConfig.abreviacao
