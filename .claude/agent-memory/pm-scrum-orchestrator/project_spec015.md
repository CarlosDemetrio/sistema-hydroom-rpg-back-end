---
name: Spec 015 Progress
description: Spec 015 ConfigPontos Classe/Raca — 5/7 tasks concluidas (T1+T2+T3+T5 backend, T4 DESBLOQUEADO, 2F pendentes)
type: project
---

Spec 015 progresso apos rodada 5 (2026-04-05): 5 de 7 tasks concluidas.

**Why:** PO confirmou (Q14, 2026-04-03) que Classe e Raca devem liberar pontos por nivel alem de NivelConfig. Implementacao avancou significativamente nas rodadas 3-5.

**How to apply:**
- T1 CONCLUIDA (R3): 4 entidades + repos + DTOs + mappers. Commit 9ac2465.
- T2 CONCLUIDA (R4): 4 services + 14 endpoints CRUD sub-recursos. Commit ba52d29.
- T3 CONCLUIDA (R5): Integracao ClassePontosConfig+RacaPontosConfig no FichaResumoResponse. Commit 5dc8bf2.
- T5 CONCLUIDA (R2): DefaultProvider 8 bugs + defaults. Commit em feature/009 branch (merged).
- T4 DESBLOQUEADO: Auto-concessao vantagens pre-definidas (deps T1+S007-T7 concluidas).
- T6 PENDENTE (frontend): UI ClassePontosConfig/RacaPontosConfig.
- T7 PENDENTE (frontend): UI VantagemPreDefinida.
- PA-015-04: campo `origem` em FichaVantagem (JOGADOR/MESTRE/SISTEMA) — nao resolvido, bloqueia T4 parcialmente.
