# Spec 014 — Tasks Index

> Spec: `014-cobertura-testes`
> Total de tasks: 6
> Status geral: BACKLOG — executar apos specs funcionais implementadas

---

## Fase Infraestrutura (T1 + T5)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T1](P1-T1-jacoco-setup.md) | Configurar JaCoCo no pom.xml com threshold 75% branch | — | Pendente |
| [T5](P2-T5-vitest-coverage.md) | Configurar Vitest coverage com relatorio HTML | — | Pendente |

## Fase Backend — Testes (T2 + T3 + T4)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T2](P1-T2-testes-ficha-calculation.md) | Testes de integracao para FichaCalculationService | T1 | Pendente |
| [T3](P1-T3-testes-formula-gameconfig.md) | Testes para FormulaEvaluatorService e GameConfigInitializerService | T1 | Pendente |
| [T4](P1-T4-testes-default-provider.md) | Testes para DefaultGameConfigProviderImpl | T1 | Pendente |

## Fase Frontend — Testes (T6)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T6](P2-T6-testes-componentes-frontend.md) | Adicionar testes para componentes sem cobertura | T5 | Pendente |

---

## Grafo de dependencias

```
T1 (JaCoCo setup) ──> T2 (FichaCalculation tests)
                  ──> T3 (Formula + GameConfig tests)
                  ──> T4 (DefaultProvider tests)

T5 (Vitest coverage) ──> T6 (Frontend component tests)
```

**Paralelismo:** T1 e T5 sao independentes. T2, T3, T4 sao independentes entre si (mas dependem de T1).

---

## Metricas Target

| Area | Antes | Depois |
|------|-------|--------|
| Backend testes | 457 | >= 500 |
| Backend branch coverage | Desconhecido | >= 75% |
| Frontend testes | 271 | >= 300 |
| Frontend coverage | Desconhecido | Mensurado (relatorio gerado) |

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-04*
