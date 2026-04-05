---
name: Sprint 1 State - Ficha Jogavel
description: Sprint 1 progress — 85% complete (23/27 tasks), P0 visual goals MET but audits revealed 23 gaps in real functionality, closing phase
type: project
---

Sprint 1 "Ficha Jogavel" launched 2026-04-01. Goal: end-to-end functional character sheet.

**Current state (updated 2026-04-02 session 4):**
- 23 of 27 tasks CONCLUIDO (85%, up from 70%)
- 5+ tasks EM ANDAMENTO (SP1-T18 pending commit, SP1-T22 N+1, SP1-T23 tests, SP1-T28/T29/T30 new endpoints, QW-1 reorder wiring)
- 4 tasks PENDENTE (SP1-T13 barras HP, SP1-T14 participantes UI, SP1-T17 skeletons, SP1-T27 DDL)
- 2 tasks ADIADO Sprint 2 (SP1-T15, T16 -> absorbed into US-FICHA-01)
- Test count: 422 (may increase with T28/T29/T30)
- Quick wins completed: QW-2 (badges sidebar), QW-3 (pre-req warning), QW-5 (tooltips), FIX-01 (totalFichas)

**Why:** Session 4 was transformative — the Tech Lead Frontend audit and BA audit revealed that Sprint 1 delivered visual structure but NOT real functionality in critical areas. 23 gaps catalogued (C1-C2 critical, M1-M7 major, G1-G10 ficha flow). 8 user stories mapped. The sprint "looked" 85% done but the ficha flow is fundamentally broken (form sends only {nome}, atributos mocked, aptidoes empty, no NPC screen, no XP progression).

**How to apply:** Sprint 1 closing is mechanical (commit T18, DDL T27, confirm tests). Sprint 2 must prioritize FICHA FUNCIONAL over Formula Editor. The strategic shift is: (1) make it possible to play RPG (US-FICHA-01/02/03/04/05), (2) complete config pages (Formula Editor, sub-resources), (3) polish (dashboard, profile, marketplace).
