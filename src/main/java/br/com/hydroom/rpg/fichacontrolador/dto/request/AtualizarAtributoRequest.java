package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para atualizar os valores de um atributo de uma ficha.
 *
 * <p>O campo {@code base} representa o investimento de pontos do jogador no atributo.
 * Não pode exceder o limitador do nível atual.</p>
 */
public record AtualizarAtributoRequest(
    @NotNull(message = "atributoConfigId é obrigatório")
    Long atributoConfigId,

    @Min(value = 0, message = "base não pode ser negativo")
    Integer base,

    @Min(value = 0, message = "nivel não pode ser negativo")
    Integer nivel,

    @Min(value = 0, message = "outros não pode ser negativo")
    Integer outros
) {}
