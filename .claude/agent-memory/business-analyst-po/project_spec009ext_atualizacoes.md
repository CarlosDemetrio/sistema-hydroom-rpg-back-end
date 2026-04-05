---
name: Spec 009-ext — Atualizações 2026-04-03 (essência, bugs frontend, PA-UX-01)
description: Decisões de produto e correções de spec feitas com base na auditoria UX de fichas em 2026-04-03
type: project
---

## Decisão: Endpoints semanticos de essência (2026-04-03)

Inconsistência resolvida entre MODO-SESSAO.md e Spec 009-ext:
- **Antes:** spec dizia "usar PUT /fichas/{id}/vida passando essenciaAtual"
- **Depois:** dois endpoints semanticos dedicados:
  - `POST /fichas/{id}/essencia/gastar` — JOGADOR e MESTRE, valida que não vai negativo
  - `POST /fichas/{id}/essencia/curar` — MESTRE apenas, clampeia ao total sem erro
- Documentado em RF-ESS-005, RF-ESS-006, RF-ESS-007 do spec.md
- MODO-SESSAO.md sec.12 já usava esta convencao — spec estava desatualizado

**Why:** Endpoints semanticos são mais seguros (JOGADOR só pode gastar, não curar) e alinham com o design UX já documentado.
**How to apply:** Ao implementar backend, criar dois endpoints separados, não reaproveitar PUT /vida para essência em modo sessão.

## Decisão: PA-UX-01 — Polling no Modo Sessão (Q17, respondido)

PO decidiu: MVP usa Polling (Opção A), SSE/WebSocket para versão futura.
- Frequência recomendada: 30 segundos
- PainelSessaoComponent e FichaStatsCombateComponent devem implementar setInterval + GET /fichas/{id}/resumo
- Nenhuma task de infraestrutura adicional necessária

## Bugs frontend identificados e documentados em T-QW

Task criada: `docs/specs/009-npc-visibility/tasks/T-QW-bugs-frontend-criticos.md`
Prioridade: ALTA, independente das demais tasks da spec.

**Bug 1 (BLOQUEADOR):** FichaHeaderComponent — barras vida/essência hardcoded `[value]="100"`
- Arquivo: `src/app/features/jogador/pages/ficha-detail/components/ficha-header/ficha-header.component.ts`
- Causa: `FichaResumo` TypeScript não tem `vidaAtual` / `essenciaAtual` (só `vidaTotal` / `essenciaTotal`)
- Fix: adicionar campos ao backend FichaResumoResponse + modelo TypeScript + substituir bindings

**Bug 2 (ALTO):** FichaVantagensTabComponent — `pontosVantagemRestantes` defaulta para 0
- Arquivo: `src/app/features/jogador/pages/ficha-detail/components/ficha-vantagens-tab/ficha-vantagens-tab.component.ts`
- Input: `pontosVantagemRestantes = input<number>(0)` — mostra "0" para todos, não distingue "sem dados" de "zero pontos"
- Fix imediato: mudar para `input<number | null>(null)` e exibir `—` quando null; fix completo depende Spec 012 T5

**Bug 3 (MÉDIO):** NpcsComponent — `router.navigate(['/jogador/fichas', fichaId])` redireciona Mestre para rota errada
- Arquivo: `src/app/features/mestre/pages/npcs/npcs.component.ts`, linha 432
- Fix: verificar rotas em app.routes.ts e corrigir para rota correta do Mestre
