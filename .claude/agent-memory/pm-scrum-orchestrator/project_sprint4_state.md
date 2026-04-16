---
name: Sprint 4 State
description: Sprint 4 — Wave P0 + P1w2 + P1w3 + R17 + R18 + R19 CONCLUIDAS; 17/17 P1 + 4/5 P2 + Spec 026 T05/T08 + T-025-* FE entregues; Sprint ENCERRADO
type: project
---

Sprint 4 iniciado 2026-04-13 (sessao 20), ENCERRADO 2026-04-16 (sessao 22, rodadas 18 e 19).

**Why:** Sessao 20 trouxe Spec 023, NPC gaps, GAPs auditoria BE→FE, Spec 024. Rodadas 17-18 fecharam Spec 024 + P2 principais; R18 entregou pacote UX final + ajustes BE (FichaStatus MORTA/ABANDONADA; PUT /status; DELETE NPC — resolve INCONS-02 BE). R19 completou INCONS-02 no FE (badges + canEdit + remove Excluir Jogador), aba Sessao, Spec 026 T05/T08 e T-025-13.

**How to apply:**
- CONCLUIDAS Wave P0 (5): S007-T10 FE (FormulaEditor, 54 testes), S015-T4 BE (origem no response), S023-BE (+18), UX-JOGO-SELECT+COR-PREVIEW (+22), NPC-FORM-CAMPOS (+2)
- CONCLUIDAS Wave P1 w2 (4): S023-FE (+56), UX mass fix 13-14 telas, NPC-DIFICULDADE-BE (+18 → 832), BaseConfig migration habilidades + tipos-item
- CONCLUIDAS Wave P1 w3 (4, 2026-04-15 manha): GAP-NPC-FE-01 (+31), GAP-XP-01 (+14), GAP-INS-01 (UI ja existia + testes), GAP-PROS-01 (+28)
- CONCLUIDAS Rodada 17 (4, 2026-04-15 tarde): Spec 024 T1 (+14), UX-BASE-COMP raridades (+27) + itens (+35), GAP-DASH-01 (+16), UX-PREREQ-EMPTY (ja no 023 FE)
- CONCLUIDAS Rodada 18 (2026-04-16): consolidacao DTO UX-TIPO-VANTAGEM, Dashboard Mestre com JogoDashboard, re-migracao BaseConfig itens/raridades, pacote UX (Quick wins; Back buttons 12 telas; NPC delete/back; edicao nome Profile; corrige rota ficha-detail; remove console.log; type fixes); BE: FichaStatus ATIVA/MORTA/ABANDONADA (COMPLETA @Deprecated) + PUT /fichas/{id}/status + AtualizarStatusFichaRequest + DELETE /fichas/{id} restrito a NPC
- CONCLUIDAS Rodada 19 (2026-04-16 tarde, 8 tasks): T-025-12 (labels wizard Vantagens+Revisao), T-025-08 (wizard path param :id), T-025-01-FE+09+10 (badges status fichas-list + canEdit MORTA/ABANDONADA + remove Excluir Jogador; +33 testes; INCONS-02 FE), T-025-03+04+07+11 (aba Sessao com vida/essencia/membros + polling 30s + badge header; +25 testes), S026-T08 FE (coluna Data Criacao lista jogos), S026-T05 (badge participantes pendentes sidebar Mestre), T-025-13 (remove Criar Ficha sidebar Jogador), S026-T08 BE (criadoEm no JogoResumoResponse + mapper; 21 testes afetados)
- **Spec 024 CONCLUIDA (2/2)**: T1 R17 consolidada R18, T2 via Spec 023 FE
- **Spec 026 CONCLUIDA (T05+T08)**: badge sidebar + Data Criacao
- **GAP-EXPRT-01 CANCELADO (R18)** — botoes removidos (BE nunca teve endpoint)
- **INCONS-02 RESOLVIDO END-TO-END** — BE R18 + FE R19
- Total Sprint 4: **17/17 P1** + **4/5 P2** + Spec 026 T05/T08 + T-025-* FE entregues
- Testes: 832 BE pre-R18 (auditar) + 21 afetados em R19, ~1525+ FE + 58 confirmados em R19 (reconciliar)
- **Gap novo R19 (P1, R20):** GAP-ESTADO-COMBATE — endpoint `GET /fichas/{id}/estado-combate` para expor `danoRecebido` por membro (FichaResumoResponse nao expoe; aba Sessao FE inicia dano=0). Decisao PO: endpoint dedicado, nao engrossar FichaResumoResponse.
- Residuais: AUDIT-BE-FE (P2 baixa prio) + GAP-ESTADO-COMBATE (P1, R20)
- Proxima rodada (R20): PRIORIDADE = GAP-ESTADO-COMBATE (BE + FE), depois auditoria testes BE pos-R18+R19, depois fechamento v0.0.1-RC OU novo ciclo
