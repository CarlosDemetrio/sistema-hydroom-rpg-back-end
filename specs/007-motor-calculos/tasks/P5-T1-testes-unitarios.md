# P5-T1 — Testes Unitários: FichaCalculationService

**Fase:** 5 — Testes
**Complexidade:** 🟢 Baixa
**Depende de:** P1-T1, P1-T2, P1-T3

## Objetivo

Testes unitários do FichaCalculationService usando Mockito (sem banco de dados).

## Checklist

### FichaCalculationServiceTest (unitário, @ExtendWith(MockitoExtension.class))

- [ ] `devCalcularTotalAtributo()` — base=3, nivel=2, outros=1 → total=6
- [ ] `devCalcularImpetoComFormula()` — mock FormulaEvaluatorService, formula="total*2", total=6 → 12.0
- [ ] `devRetornarImpetoZeroComFormulaNula()`
- [ ] `devBuildVariaveisAtributos()` — 3 atributos → Map com 3 entradas corretas
- [ ] `devCalcularBaseBonusComFormula()` — mock FormulaEvaluatorService
- [ ] `devCalcularTotalBonus()` — base+vantagens+classe+itens+gloria+outros
- [ ] `devCalcularVidaTotal()` — vigor+nivel+vt+renascimentos+outros
- [ ] `devCalcularEssenciaTotal()` — FLOOR((vigor+sab)/2)+nivel+renascimentos+vantagens+outros
- [ ] `devCalcularAmeacaTotal()`

## Arquivos afetados
- `test/.../FichaCalculationServiceTest.java` (NOVO)

## Verificações de aceitação
- [ ] Todos os testes passam sem acesso ao banco
- [ ] `./mvnw test` passa
