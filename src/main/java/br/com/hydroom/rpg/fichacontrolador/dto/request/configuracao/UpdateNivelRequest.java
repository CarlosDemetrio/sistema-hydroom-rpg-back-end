package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;

/**
 * Request DTO para atualizar um nível existente.
 *
 * @param xpNecessaria XP necessária para atingir este nível
 * @param pontosAtributo Pontos de atributo ganhos
 * @param pontosAptidao Pontos de aptidão ganhos
 * @param limitadorAtributo Valor máximo de atributo neste nível
 */
public record UpdateNivelRequest(
    @Min(value = 0, message = "XP necessária não pode ser negativa")
    Long xpNecessaria,

    @Min(value = 0, message = "Pontos de atributo não podem ser negativos")
    Integer pontosAtributo,

    @Min(value = 0, message = "Pontos de aptidão não podem ser negativos")
    Integer pontosAptidao,

    @Min(value = 1, message = "Limitador de atributo deve ser maior que zero")
    Integer limitadorAtributo
) {}
