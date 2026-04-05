package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

/**
 * Response DTO para classe permitida por raça.
 */
public record RacaClassePermitidaResponse(
    Long id,
    Long classeId,
    String classeNome
) {}
