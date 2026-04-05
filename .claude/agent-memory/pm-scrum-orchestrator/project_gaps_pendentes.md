---
name: GAPs MVP Pendentes
description: 3 GAPs identificados que nao estao enderecados em nenhuma spec (FichaStatus MORTA, polling, NPC form). GAP-MVP-05 RESOLVIDO R5.
type: project
---

3 GAPs do MVP identificados na auditoria de 2026-04-04 que nao estao completamente enderecados nas specs existentes. (Originalmente 4, GAP-MVP-05 resolvido na Rodada 5.)

**Why:** Estes gaps representam funcionalidades que o PO confirmou como necessarias para o MVP, mas que nenhuma spec cobre com task dedicada e criterios de aceitacao.

**How to apply:**
- **GAP-MVP-02 (FichaStatus MORTA/ABANDONADA):** Spec 006 T1 cria enum FichaStatus com apenas RASCUNHO e COMPLETA. Faltam MORTA e ABANDONADA. DELETE /fichas/{id} deve retornar 405 (PO: fichas nunca deletadas). Adicionar ao escopo de Spec 006 T1 ou criar task T14.
- **GAP-MVP-04 (Polling 30s):** Spec 009-ext menciona polling na spec.md (L504) como "setInterval + chamada ao endpoint" mas nao existe task dedicada com AC. Pode ser absorvido nas tasks T8/T9 do frontend de 009-ext com AC explicito.
- ~~**GAP-MVP-05 (FichaVantagem revogacao):**~~ **RESOLVIDO** na Rodada 5 (S007-T7). DELETE /fichas/{id}/vantagens/{vid} implementado (MESTRE-only, soft delete). Commit bd75582.
- **GAP-MVP-08 (NPC criacao frontend):** POST /api/v1/jogos/{jogoId}/npcs existe no backend. Frontend tem rota `criar-npc` em Spec 006 T6, mas NPC precisa de fluxo simplificado (sem wizard completo?). Avaliar se Spec 009-ext ou 006 cobre.
