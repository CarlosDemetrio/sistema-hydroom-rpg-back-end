---
name: MapStruct + BaseEntity — não ignorar campos da superclasse
description: Mappers MapStruct não devem ignorar explicitamente os campos de BaseEntity (createdAt, updatedAt, deletedAt, createdBy, updatedBy) no toEntity — isso causa falha de compilação.
type: feedback
---

Nunca adicionar `@Mapping(target = "createdAt", ignore = true)` nem outros campos de `BaseEntity` nos mappers `toEntity`.

**Why:** `BaseEntity` é `@MappedSuperclass` com apenas `@Getter @Setter` (sem `@Builder`). O `@Builder` nas subclasses (via Lombok) não expõe os campos herdados no builder interno. Se você tenta ignorá-los explicitamente, o MapStruct falha na compilação com "Unknown property X in result type YBuilder". Os mappers existentes (AtributoConfigMapper, NivelConfigMapper, etc.) não ignoram esses campos — o MapStruct usa os setters herdados automaticamente.

**How to apply:** No `toEntity`, ignorar apenas `id` e as FKs que serão resolvidas no controller/service (ex: `jogo`, `classePersonagem`, `raca`, `vantagemConfig`). Jamais ignorar campos de auditoria da BaseEntity.
