---
name: Frontend Technical Debt and Feature Gaps
description: 359 testes passando (0 falhas). QW-Bug3 corrigido. 2 bugs pendentes (QW-Bug1 barras, QW-Bug2 pontos). 23 gaps originais parcialmente resolvidos.
type: project
---

Frontend state after rodada 2 (2026-04-04).

**RESOLVIDOS na rodada 2:**
- URG-02: 38 testes falhando corrigidos (base-config-table, atributos-config, niveis-config, npcs, formula-editor). 359/359 passando.
- QW-Bug3: Rota NPC corrigida (npcs.component.ts verFicha /jogador/ -> /mestre/)

**BUGS PENDENTES (proxima rodada):**
- QW-Bug1: ficha-header.component.ts L82/95 — barras vida/essencia [value]="100" hardcoded
- QW-Bug2: ficha-vantagens-tab.component.ts L107 — pontos vantagem hardcoded 0

**CRITICAL GAPS (session 4 discovery — parcialmente resolvidos):**
- C1: handleReorder() em 12/13 config components conectado (1 restante)
- C2: Mestre Dashboard "Jogadores Ativos" mostra game count
- G1: FichaFormComponent envia apenas {nome} — wizard pendente Spec 006
- G2/G3: Atributos/Aptidoes reais conectados (US-FICHA-02/03 entregue)
- G4: No UI para Mestre conceder XP — level progression impossivel
- G5: NPC screen implementada (US-FICHA-05)

**MAJOR GAPS restantes:**
- M1: PontosVantagemController sem frontend
- M2: FormulaController sem API service frontend
- M3: SiglaController sem API service frontend
- M7: PUT /usuarios/me sem UI

**TECHNICAL DEBT:**
- DT-FE-01: atualizarAnotacao() fantasma
- DT-FE-02: CategoriaVantagem URL sem /v1/
- DT-FE-03: ConfigStore type assertions any

**Why:** Rodada 2 estabilizou o CI frontend (0 testes falhando). QW-Bug1/Bug2 sao as ultimas correcoes rapidas antes de features novas.

**How to apply:** QW-Bug1+Bug2 podem ir no mesmo agente (1h45 total). Apos isso, frontend espera backend completar 007-T2..T8 para iniciar T9-T12 (efeitos UI).
