package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FormulaEvaluatorService - validarFormula")
class FormulaEvaluatorServiceTest {

    private final FormulaEvaluatorService service = new FormulaEvaluatorService();

    @Test
    @DisplayName("Fórmula válida com variáveis corretas retorna ok")
    void formulaValida_retornaOk() {
        FormulaValidationResult result = service.validarFormula("FOR + AGI", Set.of("FOR", "AGI"));
        assertThat(result.valid()).isTrue();
        assertThat(result.variaveisInvalidas()).isEmpty();
        assertThat(result.erroSintaxe()).isNull();
    }

    @Test
    @DisplayName("Fórmula com variável não permitida retorna variaveisInvalidas")
    void formulaComVariavelInvalida_retornaVariaveisInvalidas() {
        FormulaValidationResult result = service.validarFormula("FOR + XYZ", Set.of("FOR", "AGI"));
        assertThat(result.valid()).isFalse();
        assertThat(result.variaveisInvalidas()).containsExactly("XYZ");
        assertThat(result.erroSintaxe()).isNull();
    }

    @Test
    @DisplayName("Fórmula com sintaxe inválida retorna erroSintaxe")
    void formulaComSintaxeInvalida_retornaErroSintaxe() {
        FormulaValidationResult result = service.validarFormula("FLOOR(FOR", Set.of("FOR"));
        assertThat(result.valid()).isFalse();
        assertThat(result.erroSintaxe()).isNotNull();
        assertThat(result.variaveisInvalidas()).isEmpty();
    }

    @Test
    @DisplayName("Fórmula vazia retorna erroSintaxe")
    void formulaVazia_retornaErroSintaxe() {
        FormulaValidationResult result = service.validarFormula("", Set.of("FOR"));
        assertThat(result.valid()).isFalse();
        assertThat(result.erroSintaxe()).isNotNull();
    }

    @Test
    @DisplayName("Fórmula nula retorna erroSintaxe")
    void formulaNula_retornaErroSintaxe() {
        FormulaValidationResult result = service.validarFormula(null, Set.of("FOR"));
        assertThat(result.valid()).isFalse();
        assertThat(result.erroSintaxe()).isNotNull();
    }

    @Test
    @DisplayName("Funções matemáticas não são tratadas como variáveis")
    void funcoesMatematicas_naoSaoVariaveis() {
        FormulaValidationResult result = service.validarFormula("FLOOR(FOR + AGI)", Set.of("FOR", "AGI"));
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("Fórmula com total (variável de impeto) é válida")
    void formulaComTotal_eValida() {
        FormulaValidationResult result = service.validarFormula("total * 2", Set.of("total"));
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("Validação é case-insensitive para variáveis permitidas")
    void validacaoCaseInsensitive() {
        FormulaValidationResult result = service.validarFormula("for + agi", Set.of("FOR", "AGI"));
        assertThat(result.valid()).isTrue();
    }
}
