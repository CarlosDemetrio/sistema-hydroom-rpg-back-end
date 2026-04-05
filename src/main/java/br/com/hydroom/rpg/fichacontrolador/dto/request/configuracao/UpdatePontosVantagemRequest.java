package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;

/**
 * Request DTO para atualizar configuração de pontos de vantagem por nível.
 *
 * @param nivel        Nível ao qual os pontos se aplicam
 * @param pontosGanhos Quantidade de pontos de vantagem ganhos ao atingir este nível
 */
public record UpdatePontosVantagemRequest(
    @Min(value = 1, message = "Nível deve ser maior que zero")
    Integer nivel,

    @Min(value = 0, message = "Pontos ganhos não podem ser negativos")
    Integer pontosGanhos
) {}
