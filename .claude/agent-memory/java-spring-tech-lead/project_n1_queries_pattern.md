---
name: N+1 Query Pattern - Ficha Repositories
description: Convention for JOIN FETCH queries in Ficha* repositories to avoid N+1 when accessing config entities
type: project
---

All Ficha sub-entity repositories now have `findByFichaIdWithConfig()` methods that use JOIN FETCH to eagerly load the related config entity (@ManyToOne LAZY). This was implemented in SP1-T22 (2026-04-01).

**Why:** FichaAtributo, FichaBonus, FichaVidaMembro, FichaProspeccao, FichaVantagem all have @ManyToOne LAZY references to their config entities (AtributoConfig, BonusConfig, etc.). Accessing config fields (abreviacao, formulaBase, porcentagemVida, nome) in loops caused N+1 queries in FichaService.recalcular(), FichaResumoService.getResumo(), FichaPreviewService.simular(), and FichaMapper.toResponse().

**How to apply:**
- Use `findByFichaIdWithConfig()` whenever the config fields will be accessed (calculation, mapping, copying)
- Use plain `findByFichaId()` only when config data is NOT needed (e.g., just counting or checking existence)
- FichaRepository has `findByIdWithRelationships()` and `findByJogoId*WithRelationships()` for when FichaMapper.toResponse() will access raca.nome, classe.nome, genero.nome, indole.nome, presenca.nome
- When adding new Ficha sub-entity, always add both `findByFichaId()` and `findByFichaIdWithConfig()` methods
