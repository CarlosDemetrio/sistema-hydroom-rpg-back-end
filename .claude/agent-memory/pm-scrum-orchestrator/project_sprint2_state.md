---
name: Sprint 2 State
description: Sprint 2 EM ANDAMENTO. 15/35 tasks concluidas (43%). 523B+359F testes. Rodada 5 completa. Rodada 6 planejada.
type: project
---

Sprint 2 progresso apos rodada 5 (2026-04-05): 15 de 35 tasks concluidas (43%).

**Why:** Rodadas 3-5 avancaram significativamente. Motor de efeitos tem 7/8 tipos implementados. ConfigPontos integrado no FichaResumo. Insolitus completo com concessao + revogacao. Validacao RacaClassePermitida ativa na criacao. pontosDisponiveis calculado corretamente.

**How to apply:**
- Tasks CONCLUIDAS (15): S007-T0, S007-T1, S007-T2, S007-T3+T4+T5, S007-T7, S006-T1, S006-T2, S006-T5, S015-T1, S015-T2, S015-T3, S015-T5, URG-01, URG-02, QW-Bug3
- Backend: 523 testes, 0 falhas (branch main)
- Frontend: 359 testes, 0 falhas
- Commits rodada 5: bd75582 (S007-T7), 1cb523a (S006-T2), 61b0bb4 (S006-T5), 5dc8bf2 (S015-T3)
- Commits rodada 4: 0621bc8 (S007-T3+T4+T5), d55e312 (S006-T1), ba52d29 (S015-T2)
- Commits rodada 3: 9ac2465 (S015-T1), 52738da (S007-T2)

DESBLOQUEADOS para Rodada 6:
- S007-T8 (testes integracao todos efeitos) — deps T2-T7 todas concluidas
- S005-P1T1 (re-solicitacao constraint) — sem deps
- S005-P1T2 (endpoints faltantes participantes) — sem deps
- S006-T4 (PUT /fichas/{id}/xp MESTRE-only) — sem deps
- S015-T4 (auto-concessao vantagens pre-definidas) — deps S015-T1 + S007-T7 concluidas

BLOQUEADO:
- S007-T5alt (FORMULA_CUSTOMIZADA) — PA-004 nao resolvido
- S007-T9..T12 (frontend efeitos) — depende T8
- S006-T6..T13 (wizard frontend) — depende T1+T5+Spec007

Handoff completo em docs/HANDOFF-SESSAO.md
