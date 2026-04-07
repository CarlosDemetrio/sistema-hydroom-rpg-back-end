# Rodada 12 — Sprint 3 Kickoff

> Iniciado: 2026-04-06 [08:40]
> Branch: main
> Base: 581B + 624F testes, 0 falhas

---

## Objetivo

Iniciar Sprint 3: Spec 009-ext (NPC Visibility + Prospecção + Essência) backend, Spec 008 (sub-recursos Classes/Raças frontend), Spec 012 fase 1 (Níveis/Progressão config pages + PontosVantagem).

---

## Tracks Paralelos

| Track | Spec | Escopo | Status |
|-------|------|--------|--------|
| A — Backend | 009-ext | T1 (visivelGlobalmente) + T2 (FichaVisibilidade) + T3 (ProspeccaoUso) + T4 (resetar-estado) + T5 (essenciaAtual) + T6 (testes) | **AGENTE RODANDO EM BACKGROUND** |
| B — Frontend | 008 | T1 (modelos+API) + T2 (ClassesConfig) + T3 (RaçasConfig) + T4 (testes) | **AGENTE RODANDO EM BACKGROUND** (worktree isolado) |
| C — Frontend | 012 | T5 (verif backend pontos) + T1 (PontosVantagem model+API) + T2 (PontosVantagemConfigComponent) + T3 (CategoriaVantagemConfig) + T4 (NiveisConfig UX) + T14 (rotas sidebar) | **AGENTE RODANDO EM BACKGROUND** (worktree isolado) |

---

## Progresso

| Task | Tipo | Status | Horário | Testes |
|------|------|--------|---------|--------|
| S009-T1 | B | ✅ DONE | [~08:50] | commit c29dff2 |
| S009-T2 | B | ✅ DONE | [~08:55] | commit 87fc586 |
| S009-T3 | B | ✅ DONE | [~11:45] | commit f962293 |
| S009-T4 | B | ✅ DONE | [~08:55] | commit 87fc586 |
| S009-T5 | B | ✅ DONE | [~11:46] | commit 6f72754 |
| S009-T6 | B | ✅ DONE | [~11:47] | commit dfe051e — 32 testes novos |
| S008-T1 | F | ✅ DONE | [~11:30] | commit a5361a7 |
| S008-T2 | F | ✅ DONE | [~11:30] | commit a5361a7 |
| S008-T3 | F | ✅ DONE | [~11:30] | commit a5361a7 |
| S008-T4 | F | ✅ DONE | [~11:30] | 41 testes service, +specs componentes |
| S012-T5 | B | ✅ verificado | — | já feito em S006-T5 |
| S012-T1 | F | ✅ DONE | [~11:30] | commit dc907f9 |
| S012-T2 | F | ✅ DONE | [~11:30] | commit dc907f9 |
| S012-T3 | F | ✅ DONE | [~11:30] | commit dc907f9 |
| S012-T4 | F | ✅ DONE | [~11:30] | commit dc907f9 |
| S012-T14 | F | ✅ DONE | [~11:30] | commit dc907f9 |

**Frontend: 748 testes passando** (era 624 — +124)
**Backend: 613 testes passando, 0 falhas** (era 581 — +32)

## Rodada 12 — CONCLUÍDA ✅ [~11:50]

Todos os 16 tasks da rodada entregues. Commits: c29dff2, 87fc586, f962293, 6f72754, dfe051e (backend) + a5361a7, dc907f9 (frontend).
