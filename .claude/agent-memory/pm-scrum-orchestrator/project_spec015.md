---
name: Spec 015 Created
description: Spec 015 ConfigPontos Classe/Raca + DefaultProvider — 7 tasks, P0, bloqueia 006 wizard
type: project
---

Spec 015 criada em 2026-04-04 com 7 tasks (5B + 2F) para resolver GAP-PONTOS-CONFIG e BUG-DC-02..09. TODAS as tasks e o dataset estao PRONTOS.

**Why:** PO confirmou (Q14, 2026-04-03) que Classe e Raca devem liberar pontos por nivel alem de NivelConfig. Auditoria do DefaultProvider revelou 8+ bugs que criam jogos novos com dados incorretos.

**How to apply:**
- spec.md + plan.md + 7 tasks individuais + dataset TODOS COMPLETOS
- Dataset de defaults em `015-config-pontos-classe-raca/dataset/dataset-defaults-classe-raca.md` (43K)
- Spec 015 T5 (DefaultProvider) e INDEPENDENTE — paralelizavel com qualquer outra task/spec
- Spec 015 T3 (pontos disponiveis) deve ser concluida ANTES de Spec 006 T5
- Spec 015 T4 (auto-vantagens) deve ser concluida ANTES de Spec 006 wizard nivel 1
- 4 novas entidades: ClassePontosConfig, ClasseVantagemPreDefinida, RacaPontosConfig, RacaVantagemPreDefinida
- PA-015-01/02/03: PO delegou valores exatos aos BAs. Dataset pode conter respostas.
- PA-015-04: campo `origem` em FichaVantagem (JOGADOR/MESTRE/SISTEMA) — recomendado enum
