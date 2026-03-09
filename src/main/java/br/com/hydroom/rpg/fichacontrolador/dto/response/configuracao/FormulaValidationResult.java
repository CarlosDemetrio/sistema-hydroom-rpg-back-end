package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.util.List;

/**
 * Resultado estruturado da validação de uma fórmula matemática.
 *
 * @param valid             true se a fórmula é válida
 * @param variaveisInvalidas variáveis usadas que não estão no conjunto permitido (vazia se válida)
 * @param erroSintaxe       mensagem de erro de sintaxe (null se sintaxe OK)
 */
public record FormulaValidationResult(
    boolean valid,
    List<String> variaveisInvalidas,
    String erroSintaxe
) {
    public static FormulaValidationResult ok() {
        return new FormulaValidationResult(true, List.of(), null);
    }

    public static FormulaValidationResult erroSintaxe(String erro) {
        return new FormulaValidationResult(false, List.of(), erro);
    }

    public static FormulaValidationResult variaveisInvalidas(List<String> invalidas) {
        return new FormulaValidationResult(false, invalidas, null);
    }
}
