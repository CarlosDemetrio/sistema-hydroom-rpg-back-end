package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para criar um novo nível.
 *
 * @param jogoId ID do jogo
 * @param nivel Número do nível
 * @param xpNecessaria XP necessária para atingir este nível
 * @param pontosAtributo Pontos de atributo ganhos
 * @param pontosAptidao Pontos de aptidão ganhos
 * @param limitadorAtributo Valor máximo de atributo neste nível
 */
public record CreateNivelRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotNull(message = "Nível é obrigatório")
    @Min(value = 0, message = "Nível não pode ser negativo")
    Integer nivel,

    @NotNull(message = "XP necessária é obrigatória")
    @Min(value = 0, message = "XP necessária não pode ser negativa")
    Long xpNecessaria,

    @NotNull(message = "Pontos de atributo são obrigatórios")
    @Min(value = 0, message = "Pontos de atributo não podem ser negativos")
    Integer pontosAtributo,

    @Min(value = 0, message = "Pontos de aptidão não podem ser negativos")
    Integer pontosAptidao,

    @NotNull(message = "Limitador de atributo é obrigatório")
    @Min(value = 1, message = "Limitador de atributo deve ser maior que zero")
    Integer limitadorAtributo
) {}
