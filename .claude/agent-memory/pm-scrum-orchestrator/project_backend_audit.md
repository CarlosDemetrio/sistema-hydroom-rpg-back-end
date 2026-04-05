---
name: Backend Audit and Test Progress
description: 523 testes backend (rodada 5). 7/8 TipoEfeito implementados. ConfigPontos integrado. Branch main.
type: project
---

Backend state after rodada 5 (2026-04-05) on branch `main`.

**Why:** Rodadas 3-5 implementaram motor de efeitos (7/8 tipos), ConfigPontos (4 entidades + 14 endpoints + integracao FichaResumo), FichaStatus, validacao RacaClassePermitida, pontosDisponiveis, e Insolitus com concessao/revogacao.

**How to apply:** 523 testes passando, 0 falhas. Sempre verificar com `./mvnw test` antes de planejar.

Rodada 5 deliverables (523 testes):
- S007-T7: TipoVantagem enum (VANTAGEM/INSOLITUS), campo concedidoPeloMestre, POST insolitus, DELETE revogacao. 6 testes. Commit bd75582.
- S006-T2: Validacao RacaClassePermitida no criar() de FichaService. 3 testes. Commit 1cb523a.
- S006-T5: pontosAtributoDisponiveis, pontosAptidaoDisponiveis, pontosVantagemDisponiveis no FichaResumoResponse. 3 testes. Commit 61b0bb4.
- S015-T3: ClassePontosConfig + RacaPontosConfig somados no calculo (aptidao independente). 2 testes. Commit 5dc8bf2.

Rodada 4 deliverables (509 testes):
- S007-T3+T4+T5: BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP. Commit 0621bc8.
- S006-T1: FichaStatus (RASCUNHO/COMPLETA) + PUT /fichas/{id}/completar. 9 testes. Commit d55e312.
- S015-T2: 4 services + 14 endpoints CRUD sub-recursos. 2 testes. Commit ba52d29.

Rodada 3 deliverables (474 testes):
- S007-T2: BONUS_ATRIBUTO + BONUS_APTIDAO + BONUS_VIDA + BONUS_ESSENCIA. Commit 52738da.
- S015-T1: 4 entidades ConfigPontos + repos + DTOs + mappers. Commit 9ac2465.

Motor de efeitos: 7/8 tipos implementados (BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_VIDA, BONUS_ESSENCIA, BONUS_DERIVADO, BONUS_VIDA_MEMBRO, DADO_UP). Falta FORMULA_CUSTOMIZADA (bloqueado PA-004).

DDL pendente producao (SP1-T27):
- ALTER TABLE ficha_vida ADD COLUMN vida_atual
- ALTER TABLE ficha_essencia ADD COLUMN essencia_atual
- ALTER TABLE fichas ADD COLUMN descricao
