package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoFormula;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request para preview/teste de uma fórmula.
 *
 * @param formula Expressão matemática a testar
 * @param tipo    Tipo de fórmula (determina variáveis permitidas)
 * @param valores Valores de teste para as variáveis (null usa 0.0 como default)
 */
public record FormulaPreviewRequest(
    @NotBlank(message = "Fórmula é obrigatória")
    String formula,

    @NotNull(message = "Tipo de fórmula é obrigatório")
    TipoFormula tipo,

    Map<String, Double> valores
) {}
