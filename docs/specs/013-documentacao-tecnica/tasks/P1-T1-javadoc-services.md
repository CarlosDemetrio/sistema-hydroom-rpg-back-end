# T1 — Javadoc em Services Criticos do Backend

> Fase: Backend | Dependencias: Nenhuma | Bloqueia: Nenhuma
> Estimativa: 3–4 horas

---

## Objetivo

Adicionar Javadoc completo em todos os metodos publicos dos services criticos do backend. Foco no "por que" e nas dependencias implicitas, nao na descricao obvia do "o que".

---

## Arquivos Afetados

| Arquivo | Prioridade | Metodos publicos estimados |
|---------|-----------|---------------------------|
| `service/FichaCalculationService.java` | **P0** | ~10 (recalcular, aplicarEfeitosVantagens, calcularXxx) |
| `service/FichaService.java` | **P0** | ~15 (CRUD, wizard flow, duplicar, concederXP) |
| `service/FormulaEvaluatorService.java` | **P0** | ~5 (calcularImpeto, calcularDerivado, isValid) |
| `service/configuracao/AbstractConfiguracaoService.java` | **P1** | ~8 (criar, atualizar, deletar, restaurar, listar) |
| `service/SiglaValidationService.java` | **P1** | ~3 (validarSigla, validarAbreviacao) |
| `service/GameConfigInitializerService.java` | **P1** | ~2 (initializeDefaults) |

---

## Padrao de Javadoc

### Obrigatorio para todo metodo publico:
```java
/**
 * [Descricao em 1 linha — o que o metodo faz]
 *
 * [Paragrafo opcional — contexto de negocio, "por que" isso existe,
 *  dependencias de ordem, regras nao-obvias]
 *
 * @param nomePar descricao do parametro
 * @return descricao do retorno
 * @throws TipoException quando X acontece
 */
```

### Exemplo — FichaCalculationService.recalcular():
```java
/**
 * Recalcula todos os valores derivados de uma ficha de personagem.
 *
 * A ordem de calculo e critica e nao pode ser alterada:
 * 1. Zera contribuicoes de vantagens (previne dupla contagem)
 * 2. Aplica efeitos de vantagens (8 tipos de VantagemEfeito)
 * 3. Recalcula atributos (base + nivel + outros)
 * 4. Recalcula impetos via FormulaEvaluatorService
 * 5. Recalcula bonus derivados (formulas exp4j com atributos como variaveis)
 * 6. Recalcula estado (vida, essencia, ameaca)
 *
 * O metodo e idempotente: chamar N vezes com os mesmos dados
 * produz o mesmo resultado.
 *
 * @param ficha entidade principal da ficha
 * @param atributos FichaAtributos para recalculo — modificados in-place
 * @param vantagens FichaVantagens com efeitos carregados via JOIN FETCH
 */
```

### O que NAO documentar:
- Getters/setters gerados por Lombok
- Metodos triviais com nome autoexplicativo (ex: `findById`)
- Construtores padrao

---

## Criterios de Aceitacao

- [ ] `FichaCalculationService`: todos os metodos publicos com Javadoc incluindo ordem de calculo
- [ ] `FichaService`: todos os metodos publicos com Javadoc incluindo regras de permissao
- [ ] `FormulaEvaluatorService`: Javadoc com variaveis disponiveis e exemplos de formulas
- [ ] `AbstractConfiguracaoService`: Javadoc explicando o template method pattern
- [ ] `SiglaValidationService`: Javadoc explicando regra cross-entity de unicidade
- [ ] `GameConfigInitializerService`: Javadoc explicando quais configs sao criadas por padrao
- [ ] Codigo compila sem warnings de Javadoc (`./mvnw compile`)
