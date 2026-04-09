# Plan 011 — Refatoração do GameDefaultConfigProvider

> Spec: `specs/011-game-default-provider-refactor/spec.md`
> Criado: 2026-04-09

## Visão Geral

Transformar o monolito `DefaultGameConfigProviderImpl` (887 linhas) em uma composição de 11 providers focados, cada um responsável por um grupo de configuração.

## Fases de Implementação

### Fase 1 — Scaffold + Configs Simples (blocking)

Criar a estrutura de pacotes e migrar as configurações mais simples, validando que a facade funciona.

**Tasks:**
- T1: Criar pacote `config/defaults/` e `DefaultConfigSimpleProvider` (gêneros, índoles, presenças, membros corpo)
- T2: Criar `DefaultAtributosProvider` (7 atributos)
- T3: Criar `DefaultBonusProvider` (9 bônus calculados)
- T4: Criar `DefaultProspeccoesProvider` (6 dados)
- T5: Atualizar `DefaultGameConfigProviderImpl` para delegar T1-T4, remover métodos migrados

### Fase 2 — Configs de Dados de Jogo (paralelas após Fase 1)

- T6: Criar `DefaultAptidoesProvider` (24 aptidões + 2 tipos)
- T7: Criar `DefaultNiveisProvider` (36 níveis + 5 limitadores)
- T8: Criar `DefaultClassesProvider` (12 classes)
- T9: Criar `DefaultRacasProvider` (6 raças + bônus raciais)
- T10: Criar `DefaultPontosVantagemProvider` (35 pontos de vantagem)
- T11: Atualizar facade para delegar T6-T10

### Fase 3 — Vantagens (a task principal, corrige os 33 placeholders)

- T12: Criar `DefaultVantagensProvider` com:
  - `getCategorias()` (9 categorias)
  - `getVantagens()` delegando para 9 métodos `build{Categoria}()`
  - Helper method `vantagem(...)` para reduzir boilerplate
  - Preencher as 64 vantagens do CSV `17-vantagem-config.csv`
- T13: Atualizar facade para delegar vantagens

### Fase 4 — Itens + Limpeza

- T14: Criar `DefaultItensProvider` (7 raridades + 20 tipos + 40 itens)
- T15: Limpar `DefaultGameConfigProviderImpl` (remover imports/código morto)
- T16: Atualizar testes existentes e adicionar novos (T5-11 a T5-19)

## Sequenciamento

```
Fase 1: T1 → T2 → T3 → T4 → T5 (sequencial, ~2h)
Fase 2: T6, T7, T8, T9, T10 (paralelas, ~1h) → T11
Fase 3: T12 (maior task, ~2h) → T13
Fase 4: T14 → T15 → T16 (~1h)
```

**Total estimado: ~6h de trabalho**

## Validação

Após cada fase:
1. `./mvnw compile` — deve compilar
2. `./mvnw test -Dtest=DefaultGameConfigProviderImplTest` — testes existentes passando
3. `./mvnw test` — suite completa passando

## Riscos

| Risco | Mitigação |
|-------|-----------|
| Testes quebram durante migração | Migrar um provider por vez, rodar testes após cada |
| `GameConfigInitializerService` quebra | Não alterar interface `GameDefaultConfigProvider` |
| Conflito com R07 tasks | R07 pode adotar este padrão diretamente |
