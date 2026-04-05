---
name: MVP Reprioritization 2026-04-03
description: Full reprioritization of all specs by PO with phase-based roadmap and real task counts
type: project
---

PO reprioritized all specs on 2026-04-03 with clear phase ordering.

**Priority sequence:** 007 (P0-ABSOLUTA) -> 006+005 (P0) -> 008+009-ext+010+012 (P1) -> 011 (P2)

**Key decisions:**
- Spec 007 (VantagemEfeito) is absolute priority -- motor must be correct before any ficha creation
- Spec 010 (Roles ADMIN) must be implemented LAST due to transversal impact (~50+ @PreAuthorize)
- GAP-02 (XP vulnerability) marked URGENTE -- jogador can alter own XP via PUT /fichas/{id}
- Decision P-03 PENDING: does ADMIN bypass canAccessJogo()? Blocks Spec 010 T3/T4.

**Real task counts from INDEX.md files:**
- 007: 12 tasks (8B + 4F)
- 006: 13 tasks (5B + 8F)
- 005: 6 tasks (3B + 3F)
- 008: TBD (spec.md only, frontend-only spec)
- 009-ext: 10 tasks (6B + 4F)
- 010: 9 tasks (5B + 3F + 1T)
- 011: 8 tasks (4B + 4F)
- 012: TBD (directory only)
- Total confirmed: 59+ tasks, estimated 70-80 with 008/012
- 016: ~21 tasks (~15B + ~4F + ~2D) — adicionada 2026-04-04, P1, MVP total agora ~105 tasks

**Why:** PO wants configs 100% correct before ficha creation. Motor with wrong calculations means every ficha created would have incorrect values.

**How to apply:** Always check this priority order when allocating agents. Never start Spec 010 before all others are done. Bug GAP-02 should be fixed before any deploy.
