---
name: Docs Reorganization 2026-04-07
description: Docs restructured — tracking/rodadas/, historico/{arquivado,backlogs-iniciais}, deploy/, README.md; stale files archived
type: project
---

## 2026-04-07 — Deep reorganization (this is the current structure)

**What changed:**
- Created `docs/README.md` as the navigation entry point (replaces old INDEX.md)
- Created `docs/tracking/rodadas/` — moved all `RODADA-N-TRACKING.md` → `RODADA-N.md`
  - RODADA-10.md, RODADA-11.md, RODADA-12.md, RODADA-13.md (encerradas)
  - RODADA-14.md (EM ANDAMENTO — sessão auditoria + Spec 017 planejamento)
- Created `docs/historico/` for archived content:
  - `historico/CRONOLOGIA.md` (moved from docs/)
  - `historico/arquivado/` — INDEX.md, TEAM-PLAN.md, PROXIMA-SESSAO.md, RODADA-14-TRACKING-abandonada-2026-04-06.md
  - `historico/backlogs-iniciais/` — EPICS-BACKLOG.md, PRODUCT-BACKLOG.md, UX-BACKLOG.md (Specs tomaram o lugar)
- Created `docs/deploy/` — DEPLOY-BACKEND.md, DEPLOY-OCI.md
- Root of docs/ now contains only ACTIVE tracking: README.md, HANDOFF-SESSAO.md, MASTER.md, SPRINT-ATUAL.md, PM.md, GLOSSARIO.md, API-CONTRACT.md, AI_GUIDELINES_BACKEND.md
- Reinforced headers of PM.md, MASTER.md, SPRINT-ATUAL.md with clear role statement

**Why:** 5 RODADA-N-TRACKING files accumulated in docs/ root; INDEX/TEAM-PLAN/PROXIMA-SESSAO were stale since 2026-04-05 (absorbed by MASTER but still cluttering root); initial backlogs (EPICS/PRODUCT/UX) were frozen on 2026-04-05 since Specs replaced them as source of truth; PO asked for a cleanup.

**How to apply:**
- When creating a new rodada: `docs/tracking/rodadas/RODADA-N.md` (NOT `docs/RODADA-N-TRACKING.md`)
- Updated `feedback_incremental_tracking.md` with new path convention
- Updated MEMORY.md `Memoria do Projeto` root with new paths
- Tracking files are NO LONGER deleted after rodada closes — they become historical record
- Never create new root-level files except the 8 active ones listed above
- When referencing CRONOLOGIA, use `docs/historico/CRONOLOGIA.md`

## 2026-04-02 — First reorganization (historical context)

- `docs/MASTER.md` created as the single source of truth
- `docs/CRONOLOGIA.md` created (now at `historico/CRONOLOGIA.md`)
- `INDEX.md`, `TEAM-PLAN.md`, `PROXIMA-SESSAO.md` marked ABSORBED (now at `historico/arquivado/`)
- BA analysis files annotated with validity status
