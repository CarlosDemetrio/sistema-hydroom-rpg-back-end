# P2-T1 — FormulaEvaluatorService: validação com variáveis dinâmicas

## Objetivo
Adicionar `validarFormula()` ao `FormulaEvaluatorService` — retorna resultado estruturado (válida/inválida + variáveis não reconhecidas + erro de sintaxe) usando o conjunto de variáveis dinâmico do jogo.

## Depende de
Phase 1 concluída (siglas registradas nos repositórios — necessário para P2-T2)

## Contexto
O método `isValid(String formula, String... expectedVariables)` existente testa se a fórmula é **sintaticamente** válida com variáveis esperadas. O novo método precisa:
1. Testar sintaxe
2. Identificar **quais variáveis** a fórmula usa
3. Verificar se todas estão no conjunto `variaveisPermitidas`
4. Retornar lista das que não estão (em vez de boolean simples)

## Steps

### 1. Criar ValidationResult record
```java
// Pode ser inner record em FormulaEvaluatorService ou arquivo separado
public record ValidationResult(
    boolean valid,
    List<String> variaveisInvalidas,  // variáveis usadas que não estão no conjunto permitido
    String erroSintaxe                // null se sintaxe OK, mensagem se erro
) {
    public static ValidationResult ok() {
        return new ValidationResult(true, List.of(), null);
    }
    public static ValidationResult erroSintaxe(String erro) {
        return new ValidationResult(false, List.of(), erro);
    }
    public static ValidationResult variaveisInvalidas(List<String> invalidas) {
        return new ValidationResult(false, invalidas, null);
    }
}
```

### 2. Adicionar validarFormula() em FormulaEvaluatorService

**Estratégia de extração de variáveis**: exp4j não expõe facilmente as variáveis usadas numa expressão sem executar. Usar regex para extrair tokens que parecem variáveis:

```java
public ValidationResult validarFormula(String formula, Set<String> variaveisPermitidas) {
    if (formula == null || formula.isBlank()) {
        return ValidationResult.erroSintaxe("Fórmula não pode ser vazia.");
    }

    // Passo 1: testar sintaxe tentando construir a expressão com TODAS as variáveis permitidas
    try {
        ExpressionBuilder builder = new ExpressionBuilder(normalizeFormula(formula));
        variaveisPermitidas.forEach(builder::variable);
        builder.build().validate(); // lança InvalidExpressionException se inválida
    } catch (Exception e) {
        return ValidationResult.erroSintaxe("Fórmula inválida: " + e.getMessage());
    }

    // Passo 2: extrair tokens que parecem variáveis (letras/underscore/ponto, não funções conhecidas)
    Set<String> funcoesConhecidas = Set.of("floor", "ceil", "min", "max", "abs", "sqrt");
    Set<String> variaveisUsadas = extrairVariaveis(formula, funcoesConhecidas);

    // Passo 3: verificar se alguma não está no conjunto permitido
    List<String> invalidas = variaveisUsadas.stream()
        .filter(v -> !variaveisPermitidas.contains(v.toUpperCase())
                  && !variaveisPermitidas.contains(v))
        .sorted()
        .toList();

    if (!invalidas.isEmpty()) {
        return ValidationResult.variaveisInvalidas(invalidas);
    }

    return ValidationResult.ok();
}

/**
 * Extrai tokens que parecem variáveis (sequência de letras/dígitos/underscore/ponto
 * não precedidos por '(' ou seguidos de '(' — que seria uma função).
 */
private Set<String> extrairVariaveis(String formula, Set<String> funcoesConhecidas) {
    Set<String> variaveis = new LinkedHashSet<>();
    // Regex: sequência de chars de identificador não seguida de '(' (que seria função)
    Pattern pattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*(?!\\()");
    Matcher matcher = pattern.matcher(normalizeFormula(formula));
    while (matcher.find()) {
        String token = matcher.group();
        if (!funcoesConhecidas.contains(token.toLowerCase())) {
            variaveis.add(token);
        }
    }
    return variaveis;
}
```

### 3. Manter isValid() existente para compatibilidade
Não remover. O novo `validarFormula()` complementa, não substitui.

## Casos de teste

| Fórmula | Permitidas | Esperado |
|---|---|---|
| `"FLOOR(FOR + AGI)"` | `{FOR, AGI}` | ok() |
| `"FLOOR(FOR + XYZ)"` | `{FOR, AGI}` | variaveisInvalidas([XYZ]) |
| `"total * 2"` | `{total}` | ok() |
| `"FLOOR(FOR"` | `{FOR}` | erroSintaxe(...) |
| `"custoBase + nivelVantagem"` | `{custoBase, nivelVantagem}` | ok() |
| `""` | qualquer | erroSintaxe("Fórmula não pode ser vazia") |

## Acceptance Checks
- [ ] Fórmula com variável não permitida → `valid=false`, `variaveisInvalidas` não vazio
- [ ] Fórmula com sintaxe inválida → `valid=false`, `erroSintaxe` não nulo
- [ ] Fórmula válida com variáveis corretas → `valid=true`
- [ ] Funções matemáticas (floor, ceil, min, max) não são tratadas como variáveis
- [ ] Case-insensitive: `for` e `FOR` reconhecidos como mesma variável quando permitido

## File Checklist
- `service/FormulaEvaluatorService.java`
- `dto/FormulaValidationResult.java` (ou inner record) — se extraído para arquivo separado

## References
- `service/FormulaEvaluatorService.java` — implementação atual (método `isValid`, `normalizeFormula`)
- `docs/glossario/04-siglas-formulas.md` — variáveis e funções válidas
