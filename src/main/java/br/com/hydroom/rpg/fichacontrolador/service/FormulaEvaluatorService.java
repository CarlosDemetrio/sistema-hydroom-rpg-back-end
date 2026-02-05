package br.com.hydroom.rpg.fichacontrolador.service;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service para avaliar fórmulas matemáticas em linguagem natural.
 * Usa exp4j para parsing seguro de expressões.
 *
 * Fórmulas suportadas:
 * - Operadores: +, -, *, /, ^, %
 * - Funções: min(a,b), max(a,b), abs(x), sqrt(x), ceil(x), floor(x)
 * - Variáveis: total, nivel, base, FOR, AGI, VIG, SAB, INT, INTU, AST
 *
 * Exemplos:
 * - "total * 3" (Força: três vezes o valor total)
 * - "total / 3" (Agilidade: um terço)
 * - "min(total / 20, 3)" (Intuição: mínimo entre divisão ou 3)
 * - "(FOR + AGI) / 2" (BBA: média de Força e Agilidade)
 *
 * @author Carlos Demétrio
 */
@Service
public class FormulaEvaluatorService {

    /**
     * Avalia uma fórmula com variáveis contextuais.
     *
     * @param formula Expressão matemática (ex: "total * 3", "min(total/20, 3)")
     * @param variables Mapa de variáveis (ex: {"total": 15.0, "nivel": 5.0})
     * @return Resultado calculado arredondado para 2 casas decimais
     * @throws IllegalArgumentException se fórmula inválida
     */
    public double evaluate(String formula, Map<String, Double> variables) {
        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException("Fórmula não pode ser vazia");
        }

        try {
            // Normalizar fórmula (lowercase para funções)
            String normalizedFormula = normalizeFormula(formula);

            // Construir expressão com exp4j
            ExpressionBuilder builder = new ExpressionBuilder(normalizedFormula);

            // Adicionar variáveis
            variables.keySet().forEach(builder::variable);

            Expression expression = builder.build();

            // Setar valores das variáveis
            variables.forEach(expression::setVariable);

            // Calcular
            double result = expression.evaluate();

            // Arredondar para 2 casas decimais
            return Math.round(result * 100.0) / 100.0;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Erro ao avaliar fórmula: '" + formula + "' | " + e.getMessage(), e
            );
        }
    }

    /**
     * Valida se uma fórmula é válida (sem executar).
     *
     * @param formula Fórmula a validar
     * @param expectedVariables Variáveis esperadas na fórmula
     * @return true se fórmula válida, false caso contrário
     */
    public boolean isValid(String formula, String... expectedVariables) {
        if (formula == null || formula.isBlank()) {
            return false;
        }

        try {
            String normalizedFormula = normalizeFormula(formula);
            ExpressionBuilder builder = new ExpressionBuilder(normalizedFormula);

            // Declarar variáveis esperadas
            for (String var : expectedVariables) {
                builder.variable(var);
            }

            // Tentar construir (valida sintaxe)
            Expression expression = builder.build();

            // Validar que todas as variáveis foram declaradas
            expression.validate();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Normaliza fórmula para formato exp4j.
     * Converte funções maiúsculas para minúsculas.
     *
     * @param formula Fórmula original
     * @return Fórmula normalizada
     */
    private String normalizeFormula(String formula) {
        return formula
                .replace("MIN", "min")
                .replace("MAX", "max")
                .replace("ABS", "abs")
                .replace("SQRT", "sqrt")
                .replace("CEIL", "ceil")
                .replace("FLOOR", "floor")
                .trim();
    }

    /**
     * Calcula ímpeto de um atributo.
     *
     * @param formula Fórmula do ímpeto (ex: "total * 3")
     * @param totalAtributo Valor total do atributo
     * @return Valor do ímpeto calculado
     */
    public double calcularImpeto(String formula, int totalAtributo) {
        Map<String, Double> vars = Map.of("total", (double) totalAtributo);
        return evaluate(formula, vars);
    }

    /**
     * Calcula stat derivado (BBA, BBM, Reflexo, etc.).
     *
     * @param formula Fórmula do stat (ex: "(FOR + AGI) / 2")
     * @param atributos Mapa de atributos com abreviações (FOR, AGI, etc.)
     * @return Valor do stat calculado
     */
    public double calcularDerivado(String formula, Map<String, Integer> atributos) {
        // Converter Integer -> Double
        Map<String, Double> vars = atributos.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().doubleValue()
                ));
        return evaluate(formula, vars);
    }

    /**
     * Calcula custo de vantagem baseado em fórmula.
     *
     * @param formula Fórmula do custo (ex: "custo_base * nivel_vantagem")
     * @param custoBase Custo base da vantagem
     * @param nivelVantagem Nível da vantagem
     * @return Custo calculado
     */
    public int calcularCustoVantagem(String formula, int custoBase, int nivelVantagem) {
        Map<String, Double> vars = Map.of(
                "custo_base", (double) custoBase,
                "nivel_vantagem", (double) nivelVantagem
        );
        return (int) Math.ceil(evaluate(formula, vars));
    }

    /**
     * Testa uma fórmula com valores de exemplo.
     * Útil para debug e validação de fórmulas customizadas.
     *
     * @param formula Fórmula a testar
     * @param testValues Valores de teste
     * @return Resultado do teste
     */
    public String testFormula(String formula, Map<String, Double> testValues) {
        try {
            double result = evaluate(formula, testValues);
            return String.format("Fórmula válida. Resultado: %.2f", result);
        } catch (Exception e) {
            return "Fórmula inválida: " + e.getMessage();
        }
    }
}
