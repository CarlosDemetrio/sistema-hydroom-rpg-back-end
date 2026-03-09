# Plano de Implementação — Spec 008 (Utilidade e Fluidez)

> Data: Março 2026
> Baseado em: spec.md, EPICS-BACKLOG.md, Specs 006 + 007

## Phase 0 — Descoberta

**Fontes consultadas:**
- `docs/EPICS-BACKLOG.md` — EPIC 7
- Controllers existentes de Jogo e Ficha
- Repositórios de configs (13 tipos)
- Spec 006 (Ficha entities) e Spec 007 (cálculos)

**Estado atual:**
- Sem dashboard, sem filtros, sem reordenação batch
- Sem duplicação de jogo
- Sem export/import
- Sem endpoint /minhas ou /meus

## Phase 1 — Dashboards + Minhas Fichas/Jogos

**Objetivo:** Endpoints de conveniência para navegação rápida.

**Tarefas:**
- P1-T1: DashboardService + GET /api/jogos/{id}/dashboard
- P1-T2: GET /api/jogos/{id}/fichas/minhas + GET /api/jogos/meus

## Phase 2 — Filtros e Busca

**Objetivo:** Parâmetros de busca em configs e fichas.

**Tarefas:**
- P2-T1: Filtro `?nome=` nos 13 endpoints de config
- P2-T2: Filtros nos endpoints de fichas

## Phase 3 — Reordenação em Batch

**Objetivo:** Atualização em batch do ordemExibicao.

**Tarefas:**
- P3-T1: ReordenacaoService + endpoints para os 13 tipos

## Phase 4 — Duplicação de Jogo

**Objetivo:** Copiar jogo com todas as configs.

**Tarefas:**
- P4-T1: JogoDuplicacaoService + POST /api/jogos/{id}/duplicar

## Phase 5 — Export/Import

**Objetivo:** Serialização/deserialização de configs em JSON.

**Tarefas:**
- P5-T1: ConfigExportImportService + endpoints export/import

## Phase 6 — Resumo de Ficha

**Objetivo:** Endpoint compacto de leitura.

**Tarefas:**
- P6-T1: GET /api/fichas/{id}/resumo

## Phase 7 — Testes

- P7-T1: Testes de duplicação de jogo
- P7-T2: Testes de export/import

## Ordem de execução

Phases 1-4 podem rodar em paralelo (independentes entre si).
Phase 5 (export/import) independente.
Phase 6 depende de Spec 007 (FichaResumoResponse usa dados calculados).

## Riscos

- Reordenação: validar que todos os IDs da lista pertencem ao jogo antes de atualizar
- Duplicação: N+1 ao carregar configs — usar JOIN FETCH ou batch queries
- Import: validar duplicatas antes de iniciar a inserção (fail fast)
- FR-020: documentar claramente que sub-entidades de relacionamento NÃO são copiadas
