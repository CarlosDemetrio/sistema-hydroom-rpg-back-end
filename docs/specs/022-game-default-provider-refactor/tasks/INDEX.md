# Spec 022 — Refatoracao do GameDefaultConfigProvider: INDEX de Tasks

> Spec: `022-game-default-provider-refactor`
> Status: PLANEJADO — implementacao PENDENTE
> Total: 13 tasks (todas backend)
> Estimativa: ~4-6 dias
> Depende de: nada

---

## Fase 1 — Scaffold e Migracao

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T1 | [F1-T1-providers-config.md](F1-T1-providers-config.md) | 10 providers de configs (exceto vantagens) | 1-2 dias | — | PENDENTE |
| T2 | [F1-T2-vantagens-scaffold.md](F1-T2-vantagens-scaffold.md) | DefaultVantagensProvider scaffold (categorias + 9 builds vazios + helper) | 0.5 dia | — | PENDENTE |
| T3 | [F1-T3-facade.md](F1-T3-facade.md) | Refatorar DefaultGameConfigProviderImpl como facade | 0.5 dia | T1, T2 | PENDENTE |

## Fase 2 — Vantagens (paralelas)

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T4 | [F2-T4-treinamento-fisico.md](F2-T4-treinamento-fisico.md) | buildTreinamentoFisico() — 3 vantagens | 15 min | T3 | PENDENTE |
| T5 | [F2-T5-treinamento-mental.md](F2-T5-treinamento-mental.md) | buildTreinamentoMental() — 4 vantagens | 15 min | T3 | PENDENTE |
| T6 | [F2-T6-acao.md](F2-T6-acao.md) | buildAcao() — 2 vantagens | 10 min | T3 | PENDENTE |
| T7 | [F2-T7-reacao.md](F2-T7-reacao.md) | buildReacao() — 7 vantagens | 20 min | T3 | PENDENTE |
| T8 | [F2-T8-atributo.md](F2-T8-atributo.md) | buildAtributo() — 8 vantagens | 20 min | T3 | PENDENTE |
| T9 | [F2-T9-geral.md](F2-T9-geral.md) | buildGeral() — 5 vantagens | 15 min | T3 | PENDENTE |
| T10 | [F2-T10-historica.md](F2-T10-historica.md) | buildHistorica() — 7 vantagens | 20 min | T3 | PENDENTE |
| T11 | [F2-T11-renascimento.md](F2-T11-renascimento.md) | buildRenascimento() — 11 vantagens | 25 min | T3 | PENDENTE |
| T12 | [F2-T12-raciais.md](F2-T12-raciais.md) | buildRaciais() — 17 vantagens INSOLITUS | 30 min | T3 | PENDENTE |

## Fase 3 — Testes

| Task | Arquivo | Descricao | Estimativa | Dependencias | Status |
|------|---------|-----------|-----------|-------------|--------|
| T13 | [F3-T13-testes.md](F3-T13-testes.md) | Corrigir T5-02/T5-09 + novos T5-11..T5-19 | 1 dia | T4-T12 | PENDENTE |

---

## Dependencias Visuais

```
[T1: 10 providers] ──┐
                      ├──> [T3: Facade] ──> [T4-T12: 9 builds em paralelo] ──> [T13: Testes]
[T2: Vantagens scaffold] ┘
```

---

*Produzido por: Tech Lead / Copilot | 2026-04-09*
