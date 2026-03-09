# Plano de Implementação — Spec 009 (NPC e Fichas do Mestre)

> Data: Março 2026
> Baseado em: spec.md, EPICS-BACKLOG.md, Spec 006

## Phase 0 — Descoberta

**Fontes consultadas:**
- `docs/EPICS-BACKLOG.md` — EPIC 8
- `specs/006-ficha-personagem/spec.md` — Ficha entity com isNpc e jogadorId nullable
- `service/FichaService.java` — CRUD existente de Ficha

**Estado atual:**
- Ficha entity já tem isNpc e jogadorId nullable (definido em Spec 006)
- FichaRepository já tem findByJogoIdAndIsNpcFalse e findByJogoIdAndIsNpcTrue
- FichaCalculationService funciona para qualquer Ficha (NPC ou não)

**Bloqueante:** Spec 006 concluída

## Phase 1 — Flag isNpc (garantir que Spec 006 implementou)

**Tarefas:**
- P1-T1: Verificar que isNpc e jogadorId nullable estão na Ficha entity; se não, adicionar agora

## Phase 2 — NPC CRUD

**Tarefas:**
- P2-T1: NpcController + adaptações mínimas no FichaService para criar/listar NPCs

## Phase 3 — Duplicação de Ficha

**Tarefas:**
- P3-T1: FichaDuplicacaoService + endpoint POST /api/fichas/{id}/duplicar

## Phase 4 — Testes

- P4-T1: Testes NPC CRUD
- P4-T2: Testes duplicação de ficha

## Ordem de execução

Phase 1 → Phase 2 → Phase 3 → Phase 4

## Riscos

- Duplicação: carregar todas as sub-entities antes de copiar pode ser pesado para fichas grandes — usar batch queries com JOIN FETCH
- isNpc na Ficha entity pode conflitar com implementação de Spec 006 se não sincronizados — verificar antes de implementar
