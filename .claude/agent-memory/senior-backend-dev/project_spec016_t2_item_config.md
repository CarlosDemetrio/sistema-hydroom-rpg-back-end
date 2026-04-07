---
name: Spec 016 T2 — ItemConfig implementation
description: ItemConfig + ItemEfeito + ItemRequisito — entidades, repositórios, services, mapper, controller e testes
type: project
---

Task T2 do Spec 016 (sistema de itens) implementada em 2026-04-07.

**Arquivos criados:**
- `model/enums/TipoItemEfeito.java` (7 tipos)
- `model/enums/TipoRequisito.java` (7 tipos)
- `model/ItemConfig.java` — entidade central com 2 List: efeitos, requisitos
- `model/ItemEfeito.java` — sem BaseEntity (sem soft-delete)
- `model/ItemRequisito.java` — sem BaseEntity (sem soft-delete)
- `repository/ItemConfigRepository.java` — inclui `findByJogoIdWithFilters` (Page) e `deleteAllByJogoIdNative`
- `repository/ItemEfeitoRepository.java`
- `repository/ItemRequisitoRepository.java`
- `dto/request/configuracao/{ItemConfig,ItemConfigUpdate,ItemEfeito,ItemRequisito}Request.java`
- `dto/response/configuracao/{ItemConfig,ItemConfigResumo,ItemEfeito,ItemRequisito}Response.java`
- `mapper/configuracao/{ItemConfig,ItemEfeito,ItemRequisito}Mapper.java`
- `service/configuracao/{ItemConfig,ItemEfeito,ItemRequisito}Service.java`
- `controller/configuracao/ItemConfigController.java` — 13 endpoints (CRUD + 2 sub-recursos)
- `test/.../ItemConfigServiceIntegrationTest.java` — 16 testes

**Por:** Self (senior-backend-dev agent). Bloqueia T3, T4, T6, T9 (frontend).
**Why:** ItemConfig é a entidade central do catálogo de itens — necessária para todas as features de inventário.
**How to apply:** ItemConfig usa `findByIdWithEfeitos` + `Hibernate.initialize(requisitos)` para evitar MultipleBagFetchException no buscarPorId.
