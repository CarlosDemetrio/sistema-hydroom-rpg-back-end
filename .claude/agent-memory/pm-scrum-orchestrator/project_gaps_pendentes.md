---
name: GAPs MVP Pendentes
description: GAP-MVP-02 RESOLVIDO end-to-end (R18 BE + R19 FE). GAP-MVP-04 polling RESOLVIDO R19. Gap novo R19: GAP-ESTADO-COMBATE (P1, R20).
type: project
---

GAPs do MVP identificados na auditoria de 2026-04-04. 3 resolvidos (MVP-05 R5, MVP-02 R18+R19, MVP-04 R19). Restam MVP-08 e novo GAP-ESTADO-COMBATE (R19).

**Why:** Estes gaps representam funcionalidades que o PO confirmou como necessarias para o MVP, mas que nenhuma spec cobria com task dedicada e criterios de aceitacao.

**How to apply:**
- ~~**GAP-MVP-02 (FichaStatus MORTA/ABANDONADA):**~~ **RESOLVIDO END-TO-END**. BE R18: FichaStatus ganhou ATIVA/MORTA/ABANDONADA (COMPLETA @Deprecated); endpoint PUT /fichas/{id}/status; DELETE restrito a `isNpc=true`. FE R19: badges de status na fichas-list (T-025-01-FE); canEdit() bloqueia MORTA/ABANDONADA (T-025-09); remove botao Excluir do Jogador (T-025-10); badge status no FichaHeaderComponent (T-025-11). INCONS-02 do PO totalmente implementado.
- ~~**GAP-MVP-04 (Polling 30s):**~~ **RESOLVIDO R19** — aba Sessao em ficha-detail (T-025-03+04+07) implementa polling 30s sobre vida/essencia/membros do corpo. Alinha com decisao PO Q14 (Polling no MVP, SSE/WebSocket futuro).
- ~~**GAP-MVP-05 (FichaVantagem revogacao):**~~ **RESOLVIDO** na Rodada 5 (S007-T7). DELETE /fichas/{id}/vantagens/{vid} implementado (MESTRE-only, soft delete). Commit bd75582.
- **GAP-MVP-08 (NPC criacao frontend):** POST /api/v1/jogos/{jogoId}/npcs existe. NPC form FE recebeu raça/classe (Wave P0) + selector de dificuldade (Wave P1w3) + delete/back (R18). Avaliar se ainda ha fluxo simplificado pendente ou considerar RESOLVIDO.
- **GAP-ESTADO-COMBATE (NOVO R19, P1, R20):** Identificado durante implementacao da aba Sessao (T-025-03+04+07). `FichaResumoResponse` nao expoe `danoRecebido` dos membros do corpo, fazendo a aba Sessao iniciar com dano=0 mesmo quando ha estado persistido. **Decisao PO:** criar endpoint novo `GET /fichas/{id}/estado-combate` (ou similar) em R20, em vez de engrossar o `FichaResumoResponse`. Endpoint deve retornar `vidaAtual`, `essenciaAtual` e `danoRecebido` por membro. PRIORIDADE IMEDIATA em R20.
