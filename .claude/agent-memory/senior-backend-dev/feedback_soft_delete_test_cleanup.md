---
name: Soft-delete test cleanup with FK dependencies
description: Como limpar corretamente ItemConfig (soft-delete) em testes quando existem dependências FK (raridade, tipo)
type: feedback
---

Ao testar entidades com @SQLRestriction (soft-delete), `repository.deleteAll()` só exclui registros ATIVOS. Registros soft-deleted permanecem no banco e podem causar FK violation ao deletar dependências (raridade, tipo).

**Why:** `@SQLRestriction("deleted_at IS NULL")` afeta o `findAll()` interno do `deleteAll()`, deixando soft-deleted no banco e causando `DataIntegrityViolation` ao tentar deletar raridade/tipo referenciados.

**How to apply:**
1. Adicionar `deleteAllByJogoIdNative` com `@Modifying @Query(nativeQuery=true, value="DELETE FROM table WHERE jogo_id = :jogoId")` no repository — bypassa SQLRestriction
2. No `@AfterEach` da test: `entityManager.flush(); entityManager.clear()` primeiro, depois deletar sub-entidades (item_efeitos, item_requisitos), depois usar o native delete (para incluir soft-deleted), depois deletar dependências (raridade, tipo)
3. JUnit 5 executa `@AfterEach` da subclasse ANTES da superclasse — use isso para garantir ordem correta de cleanup
