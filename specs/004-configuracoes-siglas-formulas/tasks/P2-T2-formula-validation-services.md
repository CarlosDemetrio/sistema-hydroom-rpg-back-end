# P2-T2 — Validação de fórmulas nos services de configuração

## Objetivo
Integrar `FormulaEvaluatorService.validarFormula()` nos services de AtributoConfig, BonusConfig e VantagemConfig.

## Depende de
P2-T1 (método validarFormula com ValidationResult criado)

## Regras por tipo de fórmula

| Campo | Entidade | Variáveis permitidas |
|---|---|---|
| `formulaImpeto` | AtributoConfig | `{total}` — valor total do atributo |
| `formulaBase` | BonusConfig | siglas dos atributos do jogo (dinâmico) + `{nivel, base}` |
| `formulaCusto` | VantagemConfig | `{custoBase, nivelVantagem}` |

## Steps

### AtributoConfiguracaoService

Injetar `FormulaEvaluatorService formulaEvaluatorService`.

Criar método privado:
```java
private void validarFormulaImpeto(String formula) {
    if (formula == null || formula.isBlank()) return;
    ValidationResult result = formulaEvaluatorService.validarFormula(
        formula, Set.of("total")
    );
    if (!result.valid()) {
        String msg = result.erroSintaxe() != null
            ? ValidationMessages.AtributoConfig.FORMULA_IMPETO_SINTAXE_INVALIDA + ": " + result.erroSintaxe()
            : ValidationMessages.AtributoConfig.FORMULA_IMPETO_VARIAVEIS_INVALIDAS
                .formatted(String.join(", ", result.variaveisInvalidas()));
        throw new ValidationException(msg);
    }
}
```

Chamar em `validarAntesCriar` e `validarAntesAtualizar`:
```java
validarFormulaImpeto(configuracao.getFormulaImpeto());
```

---

### BonusConfiguracaoService

Injetar:
- `FormulaEvaluatorService formulaEvaluatorService`
- `ConfiguracaoAtributoRepository atributoRepository`

Criar método privado:
```java
private void validarFormulaBase(String formula, Long jogoId) {
    if (formula == null || formula.isBlank()) return;

    // Variáveis permitidas = siglas dos atributos do jogo + variáveis fixas
    List<String> siglasAtributos = atributoRepository.findAbreviacoesByJogoId(jogoId);
    Set<String> permitidas = new HashSet<>(siglasAtributos);
    permitidas.addAll(Set.of("nivel", "base"));

    ValidationResult result = formulaEvaluatorService.validarFormula(formula, permitidas);
    if (!result.valid()) {
        String msg = result.erroSintaxe() != null
            ? ValidationMessages.BonusConfig.FORMULA_BASE_SINTAXE_INVALIDA + ": " + result.erroSintaxe()
            : ValidationMessages.BonusConfig.FORMULA_BASE_VARIAVEIS_INVALIDAS
                .formatted(String.join(", ", result.variaveisInvalidas()));
        throw new ValidationException(msg);
    }
}
```

**Importante**: o `jogoId` precisa ser extraído da configuração. No `validarAntesCriar(BonusConfig c)`:
```java
validarFormulaBase(c.getFormulaBase(), c.getJogo().getId());
```

No `validarAntesAtualizar(BonusConfig existente, BonusConfig atualizado)`:
```java
// formulaBase usa jogoId do existente (que já está persistido com jogo)
validarFormulaBase(atualizado.getFormulaBase(), existente.getJogo().getId());
```

---

### VantagemConfiguracaoService

Variáveis fixas — não precisa consultar o banco:
```java
private void validarFormulaCusto(String formula) {
    if (formula == null || formula.isBlank()) return;
    ValidationResult result = formulaEvaluatorService.validarFormula(
        formula, Set.of("custoBase", "nivelVantagem")
    );
    if (!result.valid()) {
        String msg = result.erroSintaxe() != null
            ? ValidationMessages.VantagemConfig.FORMULA_CUSTO_SINTAXE_INVALIDA + ": " + result.erroSintaxe()
            : ValidationMessages.VantagemConfig.FORMULA_CUSTO_VARIAVEIS_INVALIDAS
                .formatted(String.join(", ", result.variaveisInvalidas()));
        throw new ValidationException(msg);
    }
}
```

---

### ValidationMessages — novas constantes

```java
// AtributoConfig
FORMULA_IMPETO_SINTAXE_INVALIDA = "Fórmula de ímpeto com sintaxe inválida"
FORMULA_IMPETO_VARIAVEIS_INVALIDAS = "Fórmula de ímpeto usa variáveis não permitidas: %s. Permitida: total"

// BonusConfig
FORMULA_BASE_SINTAXE_INVALIDA = "Fórmula base com sintaxe inválida"
FORMULA_BASE_VARIAVEIS_INVALIDAS = "Fórmula base usa variáveis não registradas no jogo: %s"

// VantagemConfig
FORMULA_CUSTO_SINTAXE_INVALIDA = "Fórmula de custo com sintaxe inválida"
FORMULA_CUSTO_VARIAVEIS_INVALIDAS = "Fórmula de custo usa variáveis inválidas: %s. Permitidas: custoBase, nivelVantagem"
```

## Acceptance Checks
- [ ] Criar AtributoConfig com formulaImpeto = `"total * 2"` → aceito
- [ ] Criar AtributoConfig com formulaImpeto = `"XYZ * 2"` → ValidationException com "XYZ"
- [ ] Criar BonusConfig com formulaBase usando siglas de atributos reais → aceito
- [ ] Criar BonusConfig com formulaBase usando sigla inexistente → ValidationException
- [ ] Criar VantagemConfig com formulaCusto = `"custoBase * nivelVantagem"` → aceito
- [ ] Criar VantagemConfig com formulaCusto = `"custoBase * NIVEL"` → ValidationException com "NIVEL"
- [ ] BonusConfig com formulaBase nula → aceito (campo optional)
- [ ] AtributoConfig com formulaImpeto nula → aceito (campo optional)

## File Checklist
- `service/configuracao/AtributoConfiguracaoService.java`
- `service/configuracao/BonusConfiguracaoService.java`
- `service/configuracao/VantagemConfiguracaoService.java`
- `exception/ValidationMessages.java`

## References
- `service/FormulaEvaluatorService.java`
- `repository/ConfiguracaoAtributoRepository.java` — `findAbreviacoesByJogoId`
- `docs/backend/05-services.md`
