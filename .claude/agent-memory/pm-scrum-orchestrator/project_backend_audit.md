---
name: Backend Audit and Test Progress
description: 474 testes backend (rodada 2). S007-T0+T1 e S015-T5 concluidas. GAP-CALC-01..08 resolvidos. T2-T7 DESBLOQUEADOS.
type: project
---

Backend state after rodada 2 (2026-04-04) on branch `feature/009-npc-fichas-mestre`.

**Why:** S007-T0 corrigiu 6 bugs criticos. S007-T1 adaptou modelo para efeitos. S015-T5 corrigiu defaults. T2-T7 agora desbloqueados.

**How to apply:** 474 testes passando, 0 falhas. Sempre verificar com `./mvnw test` antes de planejar.

Rodada 1 deliverables (S007-T0 — 464 testes):
- GAP-CALC-01/02/03/06/07/08 corrigidos
- Endpoint PUT /api/v1/fichas/{id}/xp com recalculo nivel + flag levelUp
- 7 novos testes em FichaCalculationServiceBugsIntegrationTest

Rodada 2 deliverables (S007-T1 + S015-T5 — 474 testes):
- SCHEMA-01: FichaAptidao.outros adicionado
- SCHEMA-02: FichaVidaMembro.bonusVantagens adicionado
- FichaProspeccao.dadoDisponivel adicionado
- findByFichaIdWithEfeitos() query JOIN FETCH
- recalcular() aceita List<FichaVantagem> + stub aplicarEfeitosVantagens()
- DefaultProvider: 8 bugs corrigidos (DC-02..09 exceto DC-03), 22 vantagens canonicas, 10 testes unitarios
- URG-01: PUT /fichas/{id}/xp ja tinha @PreAuthorize (sem correcao necessaria)

Decisao arquitetural pendente:
- FichaAptidao.classe: sobrescrever vs somar (recomendacao PM: sobrescrever)

DDL pendente producao (SP1-T27):
- ALTER TABLE ficha_vida ADD COLUMN vida_atual
- ALTER TABLE ficha_essencia ADD COLUMN essencia_atual
- ALTER TABLE fichas ADD COLUMN descricao
