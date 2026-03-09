package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para adicionar classe permitida a uma raça.
 */
public record RacaClassePermitidaRequest(
    @NotNull(message = "ID da classe é obrigatório")
    Long classeId
) {}
