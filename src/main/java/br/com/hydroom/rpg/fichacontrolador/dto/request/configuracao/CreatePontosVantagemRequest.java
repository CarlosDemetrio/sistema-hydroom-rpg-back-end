package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para criar configuração de pontos de vantagem por nível.
 *
 * @param jogoId       ID do jogo
 * @param nivel        Nível ao qual os pontos se aplicam
 * @param pontosGanhos Quantidade de pontos de vantagem ganhos ao atingir este nível
 */
public record CreatePontosVantagemRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotNull(message = "Nível é obrigatório")
    @Min(value = 1, message = "Nível deve ser maior que zero")
    Integer nivel,

    @NotNull(message = "Pontos ganhos são obrigatórios")
    @Min(value = 0, message = "Pontos ganhos não podem ser negativos")
    Integer pontosGanhos
) {}
