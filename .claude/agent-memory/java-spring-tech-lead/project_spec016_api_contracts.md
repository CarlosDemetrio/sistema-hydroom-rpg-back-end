---
name: Spec 016 API Contracts Delivered
description: API contracts for the item/equipment system (Spec 016) have been defined — 36 endpoints across 7 controllers with 28 business rules
type: project
---

Spec 016 (Sistema de Itens/Equipamentos) API contracts were defined on 2026-04-04 and saved to `docs/specs/016-sistema-itens/api-contracts/`.

**Why:** BA-016-01 was still producing tasks but the spec.md and plan.md were ready. Tech Lead defined contracts proactively so frontend can start working with typed interfaces and backend has a clear implementation target.

**How to apply:**
- When implementing T1-T7 (backend tasks), follow the contracts in CONTRATOS-API.md exactly for endpoint paths, request/response schemas, and HTTP status codes
- 7 controllers: RaridadeItemController, TipoItemController, ItemConfigController, ItemEfeitoController, ItemRequisitoController, ClasseController (existing, extended), FichaItemController
- 3 new enums: TipoItemEfeito (7 values), TipoRequisito (7 values), OrigemFichaItem (4 values)
- Key design decisions: D-016-01 through D-016-07 documented in the contracts file
- ClasseEquipamentoInicial endpoints go in existing ClasseController (D-016-04)
- FichaItem response uses wrapper FichaInventarioResponse with weight calculations (D-016-06)
