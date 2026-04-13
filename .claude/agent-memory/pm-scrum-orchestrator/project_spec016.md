---
name: Spec 016 Sistema de Itens
description: Spec 016 ~90% implementada. T1-T10 OK (T5 BE Wave 1, T8-T10 FE Wave 1). T11 FE em andamento (Wave 2).
type: project
---

Spec 016 (Sistema de Itens/Equipamentos) ~90% implementada em 2026-04-13 (Wave 1+2).

**Why:** Equipamentos sao parte essencial do MVP (decisao PO 2026-04-04). Wave 1 desbloqueou T5 BE (recalcularStats com itens) e T8-T10 FE (raridades, tipos, catalogo itens).

**How to apply:**
- T1-T4+T6+T7: CONCLUIDOS (Copilot R01+R02 anteriores)
- T5 BE: CONCLUIDO Wave 1 (FichaCalculationService Passo 6, commits `c2b4522`+`74c2d36`, +10 testes)
- T8+T9+T10 FE: CONCLUIDOS Wave 1 (raridades, tipos item, catalogo + classe equip, commits `944739e`/`95a00a9`/`f4f9736`, +89 testes)
- T11 FE: EM ANDAMENTO Wave 2 (inventario na FichaDetail)
- PA-R02-01 (4x TODO FichaItemService recalcularStats): **RESOLVIDO** Wave 1
- Restante: apenas T11 FE para 100%
