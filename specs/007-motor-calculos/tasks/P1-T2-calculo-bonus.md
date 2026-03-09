# P1-T2 — FichaCalculationService: Cálculo de Bônus

**Fase:** 1 — Cálculos Core
**Complexidade:** 🟡 Média
**Depende de:** P1-T1 (mesmo arquivo de service)
**Bloqueia:** P2-T1

## Objetivo

Implementar cálculo de bônus usando formulaBase com siglas dos atributos como variáveis.

## Checklist

### 1. Métodos no FichaCalculationService

- [ ] `buildVariaveisAtributos(List<FichaAtributo> atributos)` → Map<String, Double>:
  - Para cada FichaAtributo → chave = atributoConfig.abreviacao, valor = (double) fichaAtributo.total
  - Exemplo: {"FOR": 8.0, "AGI": 6.0, "VIG": 7.0}

- [ ] `calcularBaseBonus(FichaBonus b, BonusConfig config, Map<String, Double> variaveis)` → double:
  - Se `config.formulaBase` null/blank → retorna 0.0
  - Usa `FormulaEvaluatorService.calcularDerivado(config.formulaBase, variaveis)`
  - Atualiza `b.base`

- [ ] `calcularTotalBonus(FichaBonus b)` → double:
  - Retorna `b.base + b.vantagens + b.classe + b.itens + b.gloria + b.outros`
  - Atualiza `b.total`

- [ ] `recalcularBonus(Ficha ficha, List<FichaAtributo> atributos, List<FichaBonus> bonus)`:
  - Chamar buildVariaveisAtributos
  - Para cada bonus: calcularBase → calcularTotal

## Arquivos afetados
- `service/FichaCalculationService.java` (MODIFICAR — adicionar métodos)

## Verificações de aceitação
- [ ] buildVariaveisAtributos com 3 atributos retorna Map com 3 entradas
- [ ] calcularBaseBonus com formula="FOR+AGI" e FOR=8,AGI=6 → 14.0
- [ ] calcularTotalBonus(base=14, vantagens=2, outros=1) → 17.0
- [ ] `./mvnw test` passa
