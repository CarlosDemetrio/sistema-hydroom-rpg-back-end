package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para adicionar bônus racial de atributo.
 */
public record RacaBonusAtributoRequest(
    @NotNull(message = "ID do atributo é obrigatório")
    Long atributoId,

    @NotNull(message = "Valor do bônus é obrigatório")
    Integer bonus
) {}
