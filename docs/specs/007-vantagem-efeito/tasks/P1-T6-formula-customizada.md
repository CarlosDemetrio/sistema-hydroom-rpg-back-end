# T6 — FichaCalculationService: FORMULA_CUSTOMIZADA

> Fase: Backend | Dependencias: T1, T2, T4 (mapas de lookup ja criados) | Bloqueia: T8
> Estimativa: 3–4 horas

---

## Objetivo

Implementar no `FichaCalculationService` o processamento do efeito `FORMULA_CUSTOMIZADA`, avaliando a formula exp4j com o conjunto estendido de variaveis (nivel_vantagem, nivel_personagem, siglas de atributos, valor_fixo, valor_por_nivel) e aplicando o resultado ao alvo configurado.

---

## Contexto

`FORMULA_CUSTOMIZADA` e o tipo mais flexivel — cobre casos que os 7 outros tipos nao atendem. Usa o `FormulaEvaluatorService` (exp4j) ja existente, mas com variaveis adicionais especificas de vantagem.

**Estado atual de `FormulaEvaluatorService.calcularDerivado()`:**
```java
// Aceita apenas siglas de atributos:
public double calcularDerivado(String formula, Map<String, Integer> variaveis)
```

Para FORMULA_CUSTOMIZADA, precisamos adicionar `nivel_vantagem`, `nivel_personagem`, `valor_fixo`, `valor_por_nivel` ao mapa. O problema: o metodo atual aceita `Map<String, Integer>` mas as novas variaveis podem ter nomes com underscore e valores decimais.

**Verificar:** O exp4j aceita nomes de variaveis com underscore? Sim — exp4j suporta identificadores com underscores.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/FormulaEvaluatorService.java` | Novo overload `calcularDerivado(formula, Map<String, Double>)` |
| `service/FichaCalculationService.java` | Novo metodo `buildVariaveisFormula()` + case FORMULA_CUSTOMIZADA |
| `service/configuracao/VantagemEfeitoService.java` | Validacao de formula com variaveis do jogo ao criar efeito |

---

## Passos de Implementacao

### Passo 1 — Verificar ou adicionar overload em FormulaEvaluatorService

Se `calcularDerivado()` aceita apenas `Map<String, Integer>`, adicionar overload com `Map<String, Double>`:

```java
/**
 * Avalia formula exp4j com variaveis genericas (Double).
 * Suporta nomes de variaveis com underscore (nivel_vantagem, etc.).
 */
public double calcularDerivado(String formula, Map<String, Double> variaveis) {
    ExpressionBuilder builder = new ExpressionBuilder(formula);
    variaveis.forEach((nome, valor) -> builder.variable(nome));
    Expression expression = builder.build();
    variaveis.forEach(expression::setVariable);
    return expression.evaluate();
}
```

### Passo 2 — Metodo `buildVariaveisFormula()`

```java
/**
 * Constroi o mapa de variaveis para FORMULA_CUSTOMIZADA.
 * Inclui: nivel_vantagem, nivel_personagem, valor_fixo, valor_por_nivel,
 * e todas as siglas de atributos do jogo (via atributosTotais).
 */
private Map<String, Double> buildVariaveisFormula(
        VantagemEfeito efeito,
        int nivelVantagem,
        Ficha ficha,
        Map<String, Integer> atributosTotais) {

    Map<String, Double> variaveis = new HashMap<>();

    // Variaveis de vantagem
    variaveis.put("nivel_vantagem", (double) nivelVantagem);
    variaveis.put("nivel_personagem", ficha.getNivel() != null ? ficha.getNivel().doubleValue() : 1.0);
    variaveis.put("valor_fixo",
        efeito.getValorFixo() != null ? efeito.getValorFixo().doubleValue() : 0.0);
    variaveis.put("valor_por_nivel",
        efeito.getValorPorNivel() != null ? efeito.getValorPorNivel().doubleValue() : 0.0);

    // Siglas de atributos do jogo
    atributosTotais.forEach((sigla, total) ->
        variaveis.put(sigla.toLowerCase(), total.doubleValue())
    );
    // Tambem adicionar em uppercase para compatibilidade com formulas existentes
    atributosTotais.forEach((sigla, total) ->
        variaveis.put(sigla.toUpperCase(), total.doubleValue())
    );

    return variaveis;
}
```

**Nota sobre case-sensitivity:** exp4j diferencia maiusculas de minusculas. Adicionar tanto `FOR` quanto `for` evita erros de formula escritas em lowercase. O Mestre pode escrever `floor(FOR / 2)` ou `floor(for / 2)`.

### Passo 3 — Construir mapa de totais de atributos no metodo aplicarEfeitosVantagens

```java
// Construido uma vez fora do loop de vantagens:
Map<String, Integer> atributosTotais = buildVariaveisAtributos(atributos);
// Nota: buildVariaveisAtributos() ja existe no FichaCalculationService
```

### Passo 4 — Adicionar case FORMULA_CUSTOMIZADA no switch

```java
case FORMULA_CUSTOMIZADA -> {
    if (efeito.getFormula() == null || efeito.getFormula().isBlank()) {
        log.warn("FORMULA_CUSTOMIZADA sem formula — efeito ID {}", efeito.getId());
        break;
    }

    Map<String, Double> vars = buildVariaveisFormula(efeito, nivel, ficha, atributosTotais);

    try {
        double resultado = formulaEvaluatorService.calcularDerivado(efeito.getFormula(), vars);
        int bonus = (int) Math.round(resultado);
        aplicarResultadoFormula(efeito, bonus, atributosMap, aptidoesMap, bonusMap, membrosMap);
    } catch (Exception e) {
        log.warn("Erro ao avaliar FORMULA_CUSTOMIZADA — efeito ID {}: {}", efeito.getId(), e.getMessage());
    }
}
```

### Passo 5 — Metodo `aplicarResultadoFormula()`

```java
/**
 * Aplica o resultado de FORMULA_CUSTOMIZADA ao alvo configurado.
 * Se nenhum alvo definido, registra aviso e descarta (PA-004 pendente).
 */
private void aplicarResultadoFormula(
        VantagemEfeito efeito,
        int resultado,
        Map<Long, FichaAtributo> atributosMap,
        Map<Long, FichaAptidao> aptidoesMap,
        Map<Long, FichaBonus> bonusMap,
        Map<Long, FichaVidaMembro> membrosMap) {

    if (efeito.getAtributoAlvo() != null) {
        FichaAtributo alvo = atributosMap.get(efeito.getAtributoAlvo().getId());
        if (alvo != null) alvo.setOutros(alvo.getOutros() + resultado);
    } else if (efeito.getBonusAlvo() != null) {
        FichaBonus alvo = bonusMap.get(efeito.getBonusAlvo().getId());
        if (alvo != null) alvo.setVantagens(alvo.getVantagens() + resultado);
    } else if (efeito.getAptidaoAlvo() != null) {
        FichaAptidao alvo = aptidoesMap.get(efeito.getAptidaoAlvo().getId());
        if (alvo != null) alvo.setOutros(alvo.getOutros() + resultado);
    } else if (efeito.getMembroAlvo() != null) {
        FichaVidaMembro alvo = membrosMap.get(efeito.getMembroAlvo().getId());
        if (alvo != null) alvo.setBonusVantagens(alvo.getBonusVantagens() + resultado);
    } else {
        log.warn("FORMULA_CUSTOMIZADA sem alvo definido — resultado {} descartado. Efeito ID {}",
            resultado, efeito.getId());
        // PA-004: confirmar com PO o que fazer com resultado sem alvo
    }
}
```

### Passo 6 — Validacao de formula ao criar efeito (VantagemEfeitoService)

Atualizar `validarCamposObrigatorios()` para validar a formula com as variaveis do jogo:

```java
case FORMULA_CUSTOMIZADA -> {
    if (efeito.getFormula() == null || efeito.getFormula().isBlank()) {
        throw new ValidationException("formula e obrigatoria para FORMULA_CUSTOMIZADA");
    }
    // Validar formula com conjunto de variaveis conhecidas
    Set<String> variaveisPermitidas = buildVariaveisPermitidasParaFormula(jogoId);
    if (!formulaEvaluatorService.isValid(efeito.getFormula(), variaveisPermitidas)) {
        throw new ValidationException(
            "Formula invalida: verifique as variaveis e a sintaxe da expressao"
        );
    }
}

private Set<String> buildVariaveisPermitidasParaFormula(Long jogoId) {
    Set<String> vars = new HashSet<>();
    vars.addAll(List.of("nivel_vantagem", "nivel_personagem", "valor_fixo", "valor_por_nivel"));
    // Adicionar siglas de atributos do jogo
    atributoRepository.findByJogoId(jogoId)
        .forEach(a -> {
            if (a.getAbreviacao() != null) {
                vars.add(a.getAbreviacao().toLowerCase());
                vars.add(a.getAbreviacao().toUpperCase());
            }
        });
    return vars;
}
```

**Nota:** `VantagemEfeitoService` precisara de `ConfiguracaoAtributoRepository` (ja injetado) e de `FormulaEvaluatorService` (injetar se nao estiver). Tambem precisara receber `jogoId` nos metodos internos — verificar se ja e passado.

---

## Regras de Negocio

- **RN-003 (spec):** Toda formula customizada e validada ao criar/editar — HTTP 422 se invalida
- **Variaveis case-insensitive (implementacao):** Adicionar tanto uppercase quanto lowercase para tolerancia
- **Excecao em runtime:** Se a formula falha ao avaliar (variavel nao encontrada, divisao por zero, etc.), `log.warn` e pular o efeito — nao lancar excecao que quebre o calculo da ficha inteira
- **PA-004 (pendente):** Se nenhum alvo definido, resultado e descartado com log de aviso

---

## Exemplo de Calculo

Vantagem "Reflexos Aguados" com FORMULA_CUSTOMIZADA:
- Formula: `floor(AGI / 2) + nivel_vantagem * 2`
- atributoAlvo: B.B.M (FichaBonus)
- FichaVantagem nivelAtual=3, AGI total=20

```
nivel_vantagem = 3
AGI = 20
resultado = floor(20 / 2) + 3 * 2 = 10 + 6 = 16
FichaBonus.BBA.vantagens += 16
```

---

## Criterios de Aceitacao

- [ ] Formula `floor(FOR / 2) + nivel_vantagem * 2` avaliada corretamente
- [ ] Variaveis `nivel_vantagem`, `nivel_personagem`, `valor_fixo`, `valor_por_nivel` disponíveis
- [ ] Siglas de atributos do jogo disponíveis (case-insensitive)
- [ ] Resultado aplicado ao atributoAlvo (outros), bonusAlvo (vantagens), aptidaoAlvo (outros), ou membroAlvo (bonusVantagens) conforme configurado
- [ ] Formula invalida ao criar efeito: HTTP 422
- [ ] Formula que falha em runtime: log.warn, calculo continua sem excecao
- [ ] Formula sem alvo: log.warn, resultado descartado (PA-004)
- [ ] `./mvnw test` passa
