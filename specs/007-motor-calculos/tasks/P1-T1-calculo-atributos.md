# P1-T1 — FichaCalculationService: Cálculo de Atributos

**Fase:** 1 — Cálculos Core
**Complexidade:** 🟡 Média
**Depende de:** Spec 006 concluído
**Bloqueia:** P2-T1

## Objetivo

Implementar os métodos de cálculo de atributos no FichaCalculationService (total e ímpeto).

## Checklist

### 1. FichaCalculationService — métodos de atributo

- [ ] Criar `@Service FichaCalculationService` (stateless, sem estado de instância)
- [ ] `calcularTotalAtributo(FichaAtributo a)` → int:
  - Retorna `a.base + a.nivel + a.outros`
  - Atualiza `a.total`
- [ ] `calcularImpeto(FichaAtributo a, AtributoConfig config)` → double:
  - Se `config.formulaImpeto` é null ou blank → retorna 0.0
  - Usa `FormulaEvaluatorService.calcularImpeto(config.formulaImpeto, a.total)`
  - Atualiza `a.impeto`
- [ ] `recalcularAtributos(Ficha ficha, List<FichaAtributo> atributos)`:
  - Para cada atributo: calcularTotal → calcularImpeto

### 2. Injeções necessárias
- [ ] `@Autowired FormulaEvaluatorService formulaEvaluatorService`

## Arquivos afetados
- `service/FichaCalculationService.java` (NOVO)

## Verificações de aceitação
- [ ] calcularTotalAtributo(base=3, nivel=2, outros=1) → 6
- [ ] calcularImpeto com formulaImpeto="total*2" e total=6 → 12.0
- [ ] calcularImpeto com formulaImpeto=null → 0.0
- [ ] `./mvnw test` passa
