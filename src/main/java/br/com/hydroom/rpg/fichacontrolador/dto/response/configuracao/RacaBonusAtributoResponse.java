package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

/**
 * Response DTO para bônus racial de atributo.
 */
public record RacaBonusAtributoResponse(
    Long id,
    Long atributoId,
    String atributoNome,
    Integer bonus
) {}
