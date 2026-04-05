---
name: Schema changes — Sprint 1
description: Campos adicionados às entidades em 2026-04-01 que requerem migração em produção
type: project
---

## Campos adicionados em 2026-04-01 (SP1-T19, T20)

### `ficha_vida`
- `vida_atual INTEGER NOT NULL DEFAULT 0` — vida atual restante do personagem (estado de combate)

### `ficha_essencia`
- `essencia_atual INTEGER NOT NULL DEFAULT 0` — essência atual restante (estado de combate)

### `fichas`
- `descricao TEXT` — descrição textual livre do personagem/NPC (nullable)

**Why:** H2 em testes usa `ddl-auto=create-drop` então não há migração necessária nos testes. Em produção com PostgreSQL, esses campos precisam de migration (Flyway ou script manual).

**How to apply:** Ao fazer deploy em produção, executar antes de iniciar a aplicação:
```sql
ALTER TABLE ficha_vida ADD COLUMN IF NOT EXISTS vida_atual INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ficha_essencia ADD COLUMN IF NOT EXISTS essencia_atual INTEGER NOT NULL DEFAULT 0;
ALTER TABLE fichas ADD COLUMN IF NOT EXISTS descricao TEXT;
```
