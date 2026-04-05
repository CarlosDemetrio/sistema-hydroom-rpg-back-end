---
name: Docs Reorganization 2026-04-02
description: Docs consolidated into MASTER.md + CRONOLOGIA.md; INDEX/TEAM-PLAN/PROXIMA-SESSAO marked absorbed; BA analyses annotated with validity status
type: project
---

On 2026-04-02, docs/ was reorganized to reduce duplication and establish clear entry points:

- `docs/MASTER.md` created as the single source of truth (navigation, state, backlog, references)
- `docs/CRONOLOGIA.md` created with reverse-chronological project history from git log
- `docs/INDEX.md`, `docs/TEAM-PLAN.md`, `docs/PROXIMA-SESSAO.md` marked as ABSORBED (point to MASTER.md)
- `docs/PM.md` and `docs/SPRINT-ATUAL.md` kept ACTIVE but with header pointing to MASTER.md
- All 6 BA analysis files in `docs/analises/` annotated with validity status, last review date, and what changed since writing
- `docs/EPICS-BACKLOG.md`, `docs/PRODUCT-BACKLOG.md`, `docs/UX-BACKLOG.md` kept as reference (detailed content not duplicated in MASTER)

**Why:** User frustrated with duplicated/overlapping information across 9+ root-level docs files. Multiple files tracked the same state (PM.md, SPRINT-ATUAL.md, PROXIMA-SESSAO.md, INDEX.md all had sprint status).

**How to apply:** When creating new docs, add them to MASTER.md references section. When updating state, update PM.md/SPRINT-ATUAL.md for details and MASTER.md for summary. Never create new root-level tracking files -- consolidate into existing ones.
